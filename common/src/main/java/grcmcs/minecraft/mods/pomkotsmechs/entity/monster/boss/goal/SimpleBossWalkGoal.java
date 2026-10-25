package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SimpleBossWalkGoal extends BaseBossGoal {
   private final double moveSpeed;
   private final double desiredDistance;
   private LivingEntity target;
   private SimpleBossWalkGoal.Mode mode = SimpleBossWalkGoal.Mode.APPROACH;
   private int circleDirection = 1;
   private int ticksRemaining = 0;
   private int modeSwitchCooldown = 0;
   private int airTicks = 0;
   private static final int MAX_AIR_TICKS = 10;
   private static final int MIN_DURATION = 20;
   private static final int MAX_DURATION = 30;
   private static final int MODE_SWITCH_INTERVAL = 15;
   private static final double CIRCLE_RADIUS = 2.0;

   public SimpleBossWalkGoal(BaseBossEntity mob, double speed, double desiredDistance) {
      super(mob);
      this.moveSpeed = speed;
      this.desiredDistance = desiredDistance;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   @Override
   public boolean canUseInternal() {
      return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
   }

   public boolean canContinueToUse() {
      return this.ticksRemaining > 0 && this.target != null && this.target.isAlive() && this.airTicks < 10;
   }

   public void start() {
      this.target = this.mob.getTarget();
      this.ticksRemaining = 20 + this.mob.getRandom().nextInt(10);
      this.modeSwitchCooldown = 15;
      this.mode = SimpleBossWalkGoal.Mode.APPROACH;
      this.circleDirection = this.mob.getRandom().nextBoolean() ? 1 : -1;
      this.airTicks = 0;
      this.mob.setNoGravity(false);
   }

   public void stop() {
      this.target = null;
      this.ticksRemaining = 0;
      Vec3 vel = this.mob.getDeltaMovement();
      this.mob.setDeltaMovement(0.0, vel.y, 0.0);
   }

   public void tick() {
      this.ticksRemaining--;
      if (this.target != null) {
         this.updateAirTicks();
         this.mob.rotateToTarget(this.target);
         this.updateMode();
         this.performMovement();
      }
   }

   private void updateAirTicks() {
      if (!this.mob.onGround() && !this.mob.isInWater() && !this.mob.isInLava()) {
         this.airTicks++;
      } else {
         this.airTicks = 0;
      }
   }

   private void updateMode() {
      this.modeSwitchCooldown--;
      if (this.modeSwitchCooldown <= 0) {
         double distance = this.mob.distanceTo(this.target);
         if (this.mode == SimpleBossWalkGoal.Mode.APPROACH && distance <= this.desiredDistance) {
            this.mode = SimpleBossWalkGoal.Mode.CIRCLE;
            this.circleDirection *= -1;
         } else if (this.mode == SimpleBossWalkGoal.Mode.CIRCLE && distance > this.desiredDistance + 2.0) {
            this.mode = SimpleBossWalkGoal.Mode.APPROACH;
         } else {
            this.circleDirection *= -1;
         }

         this.modeSwitchCooldown = 15 + this.mob.getRandom().nextInt(20);
      }
   }

   private void performMovement() {
      Vec3 currentPos = this.mob.position();
      Vec3 toTarget = this.target.position().subtract(currentPos);
      Vec3 horizontalDir = this.calculateHorizontalDirection(toTarget);
      double nextX = currentPos.x + horizontalDir.x;
      double nextZ = currentPos.z + horizontalDir.z;
      if (!this.canMoveToPosition(nextX, currentPos.y, nextZ)) {
         this.applyGravityOnly();
         this.ticksRemaining = 0;
      } else {
         double yVelocity = this.calculateVerticalMovement(nextX, currentPos.y, nextZ);
         this.mob.setDeltaMovement(horizontalDir.x, yVelocity, horizontalDir.z);
      }
   }

   private Vec3 calculateHorizontalDirection(Vec3 toTarget) {
      Vec3 horizontal = new Vec3(toTarget.x, 0.0, toTarget.z);

      return switch (this.mode) {
         case APPROACH -> horizontal.lengthSqr() < 0.01 ? Vec3.ZERO : horizontal.normalize().scale(this.moveSpeed);
         case CIRCLE -> {
            double angleToTarget = Math.atan2(horizontal.z, horizontal.x);
            double circleAngle = angleToTarget + this.circleDirection * Math.PI / 2.0;
            yield new Vec3(Math.cos(circleAngle) * this.moveSpeed, 0.0, Math.sin(circleAngle) * this.moveSpeed);
         }
      };
   }

   private boolean isStandingInFluid() {
      BlockPos startPos = this.mob.blockPosition().above();

      for (int y = startPos.getY() + 5; y >= startPos.getY() - 5; y--) {
         BlockPos pos = new BlockPos(startPos.getX(), y, startPos.getZ());
         BlockState state = this.mob.level().getBlockState(pos);
         if (state.isSolid() && !state.isAir()) {
            return false;
         }

         if (!state.getFluidState().isEmpty()) {
            return true;
         }
      }

      return false;
   }

   private boolean canMoveToPosition(double x, double y, double z) {
      Level level = this.mob.level();
      boolean currentlyInFluid = this.mob.isInWater() || this.mob.isInLava();
      double groundY = this.findGroundY(x, y, z);
      if (groundY == Double.MIN_VALUE) {
         return false;
      } else {
         BlockPos footPos = new BlockPos(Mth.floor(x), Mth.floor(groundY), Mth.floor(z));
         BlockState footState = level.getBlockState(footPos);
         BlockPos footPosB = new BlockPos(Mth.floor(x), Mth.floor(groundY) + 1, Mth.floor(z));
         BlockState footStateB = level.getBlockState(footPosB);
         BlockPos footPosC = new BlockPos(Mth.floor(x), Mth.floor(groundY) - 1, Mth.floor(z));
         BlockState footStateC = level.getBlockState(footPosC);
         if (footState.getFluidState().isEmpty() && footStateB.getFluidState().isEmpty() && footStateC.getFluidState().isEmpty()) {
            double heightDiff = groundY - y;
            double maxStep = this.mob.maxUpStep() + 1.0;
            return heightDiff > maxStep ? false : this.hasHeadroom(x, groundY, z);
         } else {
            return currentlyInFluid;
         }
      }
   }

   private double findGroundY(double x, double startY, double z) {
      Level level = this.mob.level();
      int searchUp = (int)Math.ceil(this.mob.maxUpStep()) + 2;
      int searchDown = 30;
      int startYInt = Mth.floor(startY);

      for (int y = startYInt + searchUp; y >= startYInt - searchDown; y--) {
         BlockPos pos = new BlockPos(Mth.floor(x), y, Mth.floor(z));
         BlockState state = level.getBlockState(pos);
         if (state.getFluidState().isEmpty() && state.isSolid() && !state.isAir()) {
            return this.getBlockTopY(level, pos, state);
         }
      }

      return Double.MIN_VALUE;
   }

   private double getBlockTopY(Level level, BlockPos pos, BlockState state) {
      try {
         VoxelShape shape = state.getCollisionShape(level, pos);
         if (!shape.isEmpty()) {
            return pos.getY() + shape.max(Axis.Y);
         }
      } catch (Exception var5) {
      }

      return pos.getY() + 1.0;
   }

   private boolean hasHeadroom(double x, double groundY, double z) {
      Level level = this.mob.level();
      double height = this.mob.getBbHeight();
      int checkHeight = Mth.ceil(height) + 1;

      for (int y = 0; y < checkHeight; y++) {
         BlockPos pos = new BlockPos(Mth.floor(x), Mth.floor(groundY) + y, Mth.floor(z));
         BlockState state = level.getBlockState(pos);
         if (state.isSolid() && !state.isAir()) {
            return false;
         }
      }

      return true;
   }

   private double calculateVerticalMovement(double nextX, double currentY, double nextZ) {
      double groundY = this.findGroundY(nextX, currentY, nextZ);
      if (groundY == Double.MIN_VALUE) {
         return this.applyGravity();
      } else {
         double heightDiff = groundY - currentY;
         if (Math.abs(heightDiff) < 0.1) {
            return 0.0;
         } else if (heightDiff > 0.0) {
            double maxStep = this.mob.maxUpStep();
            return heightDiff <= maxStep ? heightDiff : maxStep;
         } else {
            return this.applyGravity();
         }
      }
   }

   private double applyGravity() {
      double currentVY = this.mob.getDeltaMovement().y;
      double gravity = 0.08;
      return !this.mob.onGround() ? Math.max(currentVY - gravity, -3.0) : 0.0;
   }

   private void applyGravityOnly() {
      this.mob.setDeltaMovement(0.0, this.applyGravity(), 0.0);
   }

   private enum Mode {
      APPROACH,
      CIRCLE;
   }
}
