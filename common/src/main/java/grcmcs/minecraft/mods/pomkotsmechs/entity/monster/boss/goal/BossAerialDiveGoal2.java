package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BossAerialDiveGoal2 extends BaseBossGoal {
   private final double heightDifferenceThreshold;
   private final double riseSpeed;
   private final double flySpeed;
   private final double diveSpeed;
   private final double targetHeight;
   private final int maxDuration;
   private BossAerialDiveGoal2.Phase currentPhase = BossAerialDiveGoal2.Phase.FINISHED;
   private Vec3 destination;
   private int tickCounter;
   private LivingEntity target;
   private static final double DIVE_START_DISTANCE = 5.0;
   private static final int LAND_SEARCH_RANGE = 50;

   public BossAerialDiveGoal2(
      BaseBossEntity mob, double heightDifferenceThreshold, double riseSpeed, double flySpeed, double diveSpeed, double targetHeight, int maxDuration
   ) {
      super(mob);
      this.heightDifferenceThreshold = heightDifferenceThreshold;
      this.riseSpeed = riseSpeed;
      this.flySpeed = flySpeed;
      this.diveSpeed = diveSpeed;
      this.targetHeight = targetHeight;
      this.maxDuration = maxDuration;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
   }

   @Override
   public boolean canUseInternal() {
      if (this.currentPhase != BossAerialDiveGoal2.Phase.FINISHED) {
         return false;
      } else {
         this.target = this.mob.getTarget();
         if (this.target == null || !this.target.isAlive()) {
            return false;
         } else if (this.checkHeightDifference()) {
            this.destination = this.calculateTargetDestination();
            return true;
         } else if (this.isStandingInFluid()) {
            this.destination = this.findNearestLand();
            return this.destination != null;
         } else {
            return false;
         }
      }
   }

   private boolean isStandingInFluid() {
      BlockState feetState = this.mob.level().getBlockState(this.mob.blockPosition().above());
      return !feetState.getFluidState().isEmpty();
   }

   public boolean canContinueToUse() {
      return this.currentPhase != BossAerialDiveGoal2.Phase.FINISHED && this.tickCounter < this.maxDuration && this.destination != null;
   }

   public void start() {
      this.currentPhase = BossAerialDiveGoal2.Phase.CHARGE;
      this.tickCounter = 0;
      this.mob.setNoGravity(true);
   }

   public void stop() {
      this.currentPhase = BossAerialDiveGoal2.Phase.FINISHED;
      this.tickCounter = 0;
      this.destination = null;
      this.target = null;
      this.mob.setNoGravity(false);
      if (this.mob.onGround()) {
         this.mob.getActionController().getAction("onground").tryAction();
      }
   }

   public void tick() {
      this.tickCounter++;
      if (this.tickCounter >= this.maxDuration) {
         this.currentPhase = BossAerialDiveGoal2.Phase.FINISHED;
      } else {
         switch (this.currentPhase) {
            case CHARGE:
               this.handleChargePhase();
               break;
            case RISE:
               this.handleRisePhase();
               break;
            case FLY:
               this.handleFlyPhase();
               break;
            case DIVE:
               this.handleDivePhase();
         }

         this.updateRotation();
      }
   }

   private void handleChargePhase() {
      if (this.tickCounter == 1) {
         this.mob.triggerAnim("action_controller", "jump");
      } else if (this.tickCounter >= 10) {
         this.currentPhase = BossAerialDiveGoal2.Phase.RISE;
      }
   }

   private void handleRisePhase() {
      double currentY = this.mob.getY();
      double goalY = this.destination.y + this.targetHeight;
      if (currentY >= goalY) {
         this.currentPhase = BossAerialDiveGoal2.Phase.FLY;
      } else {
         Vec3 toDestination = this.destination.subtract(this.mob.position());
         Vec3 horizontalDir = new Vec3(toDestination.x, 0.0, toDestination.z).normalize();
         Vec3 movement = horizontalDir.scale(this.flySpeed * 0.3).add(0.0, this.riseSpeed, 0.0);
         this.mob.setDeltaMovement(movement);
      }
   }

   private void handleFlyPhase() {
      Vec3 currentPos = this.mob.position();
      Vec3 toDestination = this.destination.subtract(currentPos);
      double horizontalDistance = Math.sqrt(toDestination.x * toDestination.x + toDestination.z * toDestination.z);
      if (horizontalDistance < 5.0) {
         this.currentPhase = BossAerialDiveGoal2.Phase.DIVE;
         this.mob.setNoGravity(false);
      } else {
         Vec3 horizontalDir = new Vec3(toDestination.x, 0.0, toDestination.z).normalize();
         double goalY = this.destination.y + this.targetHeight;
         double yAdjustment = (goalY - currentPos.y) * 0.2;
         Vec3 movement = horizontalDir.scale(this.flySpeed).add(0.0, yAdjustment, 0.0);
         this.mob.setDeltaMovement(movement);
      }
   }

   private void handleDivePhase() {
      Vec3 currentVel = this.mob.getDeltaMovement();
      Vec3 toDestination = this.destination.subtract(this.mob.position());
      Vec3 horizontalDir = new Vec3(toDestination.x, 0.0, toDestination.z).normalize();
      Vec3 movement = new Vec3(
         horizontalDir.x * this.flySpeed * 0.3, Math.max(currentVel.y - 0.1, -this.diveSpeed), horizontalDir.z * this.flySpeed * 0.3
      );
      this.mob.setDeltaMovement(movement);
      if (this.mob.onGround() || this.isNearGround()) {
         this.currentPhase = BossAerialDiveGoal2.Phase.FINISHED;
      }
   }

   private void updateRotation() {
      if (this.destination != null) {
         Vec3 toDestination = this.destination.subtract(this.mob.position());
         float targetYaw = (float)(Mth.atan2(toDestination.z, toDestination.x) * (180.0 / Math.PI)) - 90.0F;
         this.mob.setYRot(this.lerpRotation(this.mob.getYRot(), targetYaw, 5.0F));
         this.mob.yBodyRot = this.mob.getYRot();
         if (this.currentPhase == BossAerialDiveGoal2.Phase.FLY || this.currentPhase == BossAerialDiveGoal2.Phase.RISE) {
            double horizontalDist = Math.sqrt(toDestination.x * toDestination.x + toDestination.z * toDestination.z);
            float targetPitch = (float)(Math.atan2(-toDestination.y, horizontalDist) * (180.0 / Math.PI));
            this.mob.setXRot(this.lerpRotation(this.mob.getXRot(), targetPitch, 3.0F));
         }
      }
   }

   private float lerpRotation(float current, float target, float maxDelta) {
      float delta = Mth.wrapDegrees(target - current);
      delta = Mth.clamp(delta, -maxDelta, maxDelta);
      return current + delta;
   }

   private boolean isNearGround() {
      BlockPos belowPos = this.mob.blockPosition().below(2);
      return this.mob.level().getBlockState(belowPos).isSolid();
   }

   private boolean checkHeightDifference() {
      if (this.target == null) {
         return false;
      }

      double heightDiff = Math.abs(this.target.getY() - this.mob.getY());
      if (heightDiff < this.heightDifferenceThreshold) {
         return false;
      }

      Entity vehicle = this.target.getVehicle();
      return vehicle != null ? vehicle.onGround() && !vehicle.isInWater() : this.target.onGround() && !this.target.isInWater();
   }

   private Vec3 calculateTargetDestination() {
      if (this.target == null) {
         return null;
      }

      Vec3 targetPos = this.target.position();
      double groundY = this.findGroundY(targetPos.x, targetPos.y, targetPos.z);
      return groundY != Double.MIN_VALUE ? new Vec3(targetPos.x, groundY, targetPos.z) : targetPos;
   }

   private Vec3 findNearestLand() {
      Level level = this.mob.level();
      Vec3 currentPos = this.mob.position();
      BlockPos startPos = this.mob.blockPosition();

      for (int radius = 1; radius <= 50; radius += 5) {
         for (int angle = 0; angle < 360; angle += 30) {
            double rad = Math.toRadians(angle);
            int x = (int)(currentPos.x + Math.cos(rad) * radius);
            int z = (int)(currentPos.z + Math.sin(rad) * radius);
            BlockPos pos = this.findSurfaceBlockPos(x, currentPos.y, z);
            if (pos != null) {
               BlockState state = level.getBlockState(pos);
               if (state.isSolid() && state.getFluidState().isEmpty()) {
                  return new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
               }
            }
         }
      }

      return null;
   }

   private double findGroundY(double x, double startY, double z) {
      Level level = this.mob.level();
      int searchUp = 10;
      int searchDown = 50;
      int startYInt = Mth.floor(startY);

      for (int y = startYInt + searchUp; y >= startYInt - searchDown; y--) {
         BlockPos pos = new BlockPos(Mth.floor(x), y, Mth.floor(z));
         BlockState state = level.getBlockState(pos);
         if (state.isSolid() && !state.isAir()) {
            try {
               VoxelShape shape = state.getCollisionShape(level, pos);
               if (!shape.isEmpty()) {
                  return pos.getY() + shape.max(Axis.Y);
               }
            } catch (Exception var15) {
            }

            return pos.getY() + 1.0;
         }
      }

      return Double.MIN_VALUE;
   }

   private BlockPos findSurfaceBlockPos(double x, double startY, double z) {
      Level level = this.mob.level();
      int searchUp = 20;
      int searchDown = 20;
      int startYInt = Mth.floor(startY);

      for (int y = startYInt + searchUp; y >= startYInt - searchDown; y--) {
         BlockPos pos = new BlockPos(Mth.floor(x), y, Mth.floor(z));
         BlockState state = level.getBlockState(pos);
         if (!state.getFluidState().isEmpty()) {
            return null;
         }

         if (state.isSolid() && !state.isAir()) {
            return pos;
         }
      }

      return null;
   }

   public BossAerialDiveGoal2.Phase getCurrentPhase() {
      return this.currentPhase;
   }

   public Vec3 getDestination() {
      return this.destination;
   }

   public int getTickCounter() {
      return this.tickCounter;
   }

   private enum Phase {
      CHARGE,
      RISE,
      FLY,
      DIVE,
      FINISHED;
   }
}
