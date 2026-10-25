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
      this.m_7021_(EnumSet.of(Flag.MOVE, Flag.JUMP));
   }

   @Override
   public boolean canUseInternal() {
      if (this.currentPhase != BossAerialDiveGoal2.Phase.FINISHED) {
         return false;
      } else {
         this.target = this.mob.m_5448_();
         if (this.target == null || !this.target.m_6084_()) {
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
      BlockState feetState = this.mob.m_9236_().m_8055_(this.mob.m_20183_().m_7494_());
      return !feetState.m_60819_().m_76178_();
   }

   public boolean m_8045_() {
      return this.currentPhase != BossAerialDiveGoal2.Phase.FINISHED && this.tickCounter < this.maxDuration && this.destination != null;
   }

   public void m_8056_() {
      this.currentPhase = BossAerialDiveGoal2.Phase.CHARGE;
      this.tickCounter = 0;
      this.mob.m_20242_(true);
   }

   public void m_8041_() {
      this.currentPhase = BossAerialDiveGoal2.Phase.FINISHED;
      this.tickCounter = 0;
      this.destination = null;
      this.target = null;
      this.mob.m_20242_(false);
      if (this.mob.m_20096_()) {
         this.mob.getActionController().getAction("onground").tryAction();
      }
   }

   public void m_8037_() {
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
      double currentY = this.mob.m_20186_();
      double goalY = this.destination.f_82480_ + this.targetHeight;
      if (currentY >= goalY) {
         this.currentPhase = BossAerialDiveGoal2.Phase.FLY;
      } else {
         Vec3 toDestination = this.destination.m_82546_(this.mob.m_20182_());
         Vec3 horizontalDir = new Vec3(toDestination.f_82479_, 0.0, toDestination.f_82481_).m_82541_();
         Vec3 movement = horizontalDir.m_82490_(this.flySpeed * 0.3).m_82520_(0.0, this.riseSpeed, 0.0);
         this.mob.m_20256_(movement);
      }
   }

   private void handleFlyPhase() {
      Vec3 currentPos = this.mob.m_20182_();
      Vec3 toDestination = this.destination.m_82546_(currentPos);
      double horizontalDistance = Math.sqrt(toDestination.f_82479_ * toDestination.f_82479_ + toDestination.f_82481_ * toDestination.f_82481_);
      if (horizontalDistance < 5.0) {
         this.currentPhase = BossAerialDiveGoal2.Phase.DIVE;
         this.mob.m_20242_(false);
      } else {
         Vec3 horizontalDir = new Vec3(toDestination.f_82479_, 0.0, toDestination.f_82481_).m_82541_();
         double goalY = this.destination.f_82480_ + this.targetHeight;
         double yAdjustment = (goalY - currentPos.f_82480_) * 0.2;
         Vec3 movement = horizontalDir.m_82490_(this.flySpeed).m_82520_(0.0, yAdjustment, 0.0);
         this.mob.m_20256_(movement);
      }
   }

   private void handleDivePhase() {
      Vec3 currentVel = this.mob.m_20184_();
      Vec3 toDestination = this.destination.m_82546_(this.mob.m_20182_());
      Vec3 horizontalDir = new Vec3(toDestination.f_82479_, 0.0, toDestination.f_82481_).m_82541_();
      Vec3 movement = new Vec3(
         horizontalDir.f_82479_ * this.flySpeed * 0.3, Math.max(currentVel.f_82480_ - 0.1, -this.diveSpeed), horizontalDir.f_82481_ * this.flySpeed * 0.3
      );
      this.mob.m_20256_(movement);
      if (this.mob.m_20096_() || this.isNearGround()) {
         this.currentPhase = BossAerialDiveGoal2.Phase.FINISHED;
      }
   }

   private void updateRotation() {
      if (this.destination != null) {
         Vec3 toDestination = this.destination.m_82546_(this.mob.m_20182_());
         float targetYaw = (float)(Mth.m_14136_(toDestination.f_82481_, toDestination.f_82479_) * (180.0 / Math.PI)) - 90.0F;
         this.mob.m_146922_(this.lerpRotation(this.mob.m_146908_(), targetYaw, 5.0F));
         this.mob.f_20883_ = this.mob.m_146908_();
         if (this.currentPhase == BossAerialDiveGoal2.Phase.FLY || this.currentPhase == BossAerialDiveGoal2.Phase.RISE) {
            double horizontalDist = Math.sqrt(toDestination.f_82479_ * toDestination.f_82479_ + toDestination.f_82481_ * toDestination.f_82481_);
            float targetPitch = (float)(Math.atan2(-toDestination.f_82480_, horizontalDist) * (180.0 / Math.PI));
            this.mob.m_146926_(this.lerpRotation(this.mob.m_146909_(), targetPitch, 3.0F));
         }
      }
   }

   private float lerpRotation(float current, float target, float maxDelta) {
      float delta = Mth.m_14177_(target - current);
      delta = Mth.m_14036_(delta, -maxDelta, maxDelta);
      return current + delta;
   }

   private boolean isNearGround() {
      BlockPos belowPos = this.mob.m_20183_().m_6625_(2);
      return this.mob.m_9236_().m_8055_(belowPos).m_280296_();
   }

   private boolean checkHeightDifference() {
      if (this.target == null) {
         return false;
      }

      double heightDiff = Math.abs(this.target.m_20186_() - this.mob.m_20186_());
      if (heightDiff < this.heightDifferenceThreshold) {
         return false;
      }

      Entity vehicle = this.target.m_20202_();
      return vehicle != null ? vehicle.m_20096_() && !vehicle.m_20069_() : this.target.m_20096_() && !this.target.m_20069_();
   }

   private Vec3 calculateTargetDestination() {
      if (this.target == null) {
         return null;
      }

      Vec3 targetPos = this.target.m_20182_();
      double groundY = this.findGroundY(targetPos.f_82479_, targetPos.f_82480_, targetPos.f_82481_);
      return groundY != Double.MIN_VALUE ? new Vec3(targetPos.f_82479_, groundY, targetPos.f_82481_) : targetPos;
   }

   private Vec3 findNearestLand() {
      Level level = this.mob.m_9236_();
      Vec3 currentPos = this.mob.m_20182_();
      BlockPos startPos = this.mob.m_20183_();

      for (int radius = 1; radius <= 50; radius += 5) {
         for (int angle = 0; angle < 360; angle += 30) {
            double rad = Math.toRadians(angle);
            int x = (int)(currentPos.f_82479_ + Math.cos(rad) * radius);
            int z = (int)(currentPos.f_82481_ + Math.sin(rad) * radius);
            BlockPos pos = this.findSurfaceBlockPos(x, currentPos.f_82480_, z);
            if (pos != null) {
               BlockState state = level.m_8055_(pos);
               if (state.m_280296_() && state.m_60819_().m_76178_()) {
                  return new Vec3(pos.m_123341_() + 0.5, pos.m_123342_() + 1, pos.m_123343_() + 0.5);
               }
            }
         }
      }

      return null;
   }

   private double findGroundY(double x, double startY, double z) {
      Level level = this.mob.m_9236_();
      int searchUp = 10;
      int searchDown = 50;
      int startYInt = Mth.m_14107_(startY);

      for (int y = startYInt + searchUp; y >= startYInt - searchDown; y--) {
         BlockPos pos = new BlockPos(Mth.m_14107_(x), y, Mth.m_14107_(z));
         BlockState state = level.m_8055_(pos);
         if (state.m_280296_() && !state.m_60795_()) {
            try {
               VoxelShape shape = state.m_60812_(level, pos);
               if (!shape.m_83281_()) {
                  return pos.m_123342_() + shape.m_83297_(Axis.Y);
               }
            } catch (Exception var15) {
            }

            return pos.m_123342_() + 1.0;
         }
      }

      return Double.MIN_VALUE;
   }

   private BlockPos findSurfaceBlockPos(double x, double startY, double z) {
      Level level = this.mob.m_9236_();
      int searchUp = 20;
      int searchDown = 20;
      int startYInt = Mth.m_14107_(startY);

      for (int y = startYInt + searchUp; y >= startYInt - searchDown; y--) {
         BlockPos pos = new BlockPos(Mth.m_14107_(x), y, Mth.m_14107_(z));
         BlockState state = level.m_8055_(pos);
         if (!state.m_60819_().m_76178_()) {
            return null;
         }

         if (state.m_280296_() && !state.m_60795_()) {
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
