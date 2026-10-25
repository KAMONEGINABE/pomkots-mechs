package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HelicopterHoverMoveGoal extends Goal {
   private final BaseBossEntity mob;
   private final double baseSpeed;
   private final double minHoverHeight;
   private final double maxHoverHeight;
   private final int patternSwitchInterval;
   private LivingEntity target;
   private int tickCounter = 0;
   private int patternSwitchTimer = 0;
   private double currentHoverHeight;
   private HelicopterHoverMoveGoal.MovementPattern currentPattern = HelicopterHoverMoveGoal.MovementPattern.HOVER;
   private int circleDirection = 1;
   private int strafeDirection = 1;
   private int strafeTimer = 0;

   public HelicopterHoverMoveGoal(BaseBossEntity mob, double baseSpeed, double minHoverHeight, double maxHoverHeight, int patternSwitchInterval) {
      this.mob = mob;
      this.baseSpeed = baseSpeed;
      this.minHoverHeight = minHoverHeight;
      this.maxHoverHeight = maxHoverHeight;
      this.patternSwitchInterval = patternSwitchInterval;
      this.setFlags(EnumSet.of(Flag.JUMP));
      this.randomizeHoverHeight();
   }

   public boolean canUse() {
      this.target = this.mob.getTarget();
      return this.target != null && this.target.isAlive();
   }

   public boolean canContinueToUse() {
      return this.target != null && this.target.isAlive();
   }

   public void start() {
      this.tickCounter = 0;
      this.patternSwitchTimer = this.patternSwitchInterval;
      this.mob.setNoGravity(true);
      this.randomizePattern();
   }

   public void stop() {
      this.target = null;
      this.currentPattern = HelicopterHoverMoveGoal.MovementPattern.HOVER;
      Vec3 vel = this.mob.getDeltaMovement();
      this.mob.setDeltaMovement(0.0, vel.y * 0.5, 0.0);
   }

   public void tick() {
      this.tickCounter++;
      if (this.target != null) {
         if (this.mob.isStunning()) {
            this.mob.setDeltaMovement(Vec3.ZERO);
         } else {
            if (this.currentPattern == HelicopterHoverMoveGoal.MovementPattern.APPROACH
               || this.currentPattern == HelicopterHoverMoveGoal.MovementPattern.RAPID_APPROACH) {
               Vec3 currentPos = this.mob.position();
               Vec3 toTarget = this.target.position().subtract(currentPos);
               Vec3 horizontal = new Vec3(toTarget.x, 0.0, toTarget.z);
               double distance = horizontal.length();
               if (distance < 20.0) {
                  this.patternSwitchTimer = 0;
               }
            }

            this.mob.rotateToTarget(this.target);
            this.patternSwitchTimer--;
            if (this.patternSwitchTimer <= 0) {
               this.randomizePattern();
               this.patternSwitchTimer = this.patternSwitchInterval + this.mob.getRandom().nextInt(40) - 20;
            }

            if (this.mob.getRandom().nextFloat() < 0.1F) {
               this.randomizeHoverHeight();
            }

            this.performMovement();
         }
      }
   }

   private void performMovement() {
      Vec3 currentPos = this.mob.position();
      Vec3 toTarget = this.target.position().subtract(currentPos);
      Vec3 horizontalMove = this.calculateHorizontalMovement(toTarget);
      double verticalMove = this.calculateVerticalMovement(currentPos);
      this.mob.setDeltaMovement(horizontalMove.x, verticalMove, horizontalMove.z);
   }

   private Vec3 calculateHorizontalMovement(Vec3 toTarget) {
      Vec3 horizontal = new Vec3(toTarget.x, 0.0, toTarget.z);
      double distance = horizontal.length();

      return switch (this.currentPattern) {
         case APPROACH -> distance < 0.1 ? Vec3.ZERO : horizontal.normalize().scale(this.baseSpeed);
         case RAPID_APPROACH -> distance < 0.1 ? Vec3.ZERO : horizontal.normalize().scale(this.baseSpeed * 2.0);
         case CIRCLE -> {
            double angleToTarget = Math.atan2(horizontal.z, horizontal.x);
            double circleAngle = angleToTarget + this.circleDirection * Math.PI / 2.0;
            yield new Vec3(Math.cos(circleAngle) * this.baseSpeed * 1.2, 0.0, Math.sin(circleAngle) * this.baseSpeed * 1.2);
         }
         case STRAFE -> {
            this.strafeTimer--;
            if (this.strafeTimer <= 0) {
               this.strafeDirection *= -1;
               this.strafeTimer = 20 + this.mob.getRandom().nextInt(20);
            }

            double angleToTarget = Math.atan2(horizontal.z, horizontal.x);
            double strafeAngle = angleToTarget + this.strafeDirection * Math.PI / 2.0;
            yield new Vec3(Math.cos(strafeAngle) * this.baseSpeed * 1.5, 0.0, Math.sin(strafeAngle) * this.baseSpeed * 1.5);
         }
         case RETREAT -> distance < 0.1 ? Vec3.ZERO : horizontal.normalize().scale(-this.baseSpeed * 0.8);
         case HOVER -> {
            Vec3 drift = new Vec3(
               (this.mob.getRandom().nextDouble() - 0.5) * this.baseSpeed * 0.2, 0.0, (this.mob.getRandom().nextDouble() - 0.5) * this.baseSpeed * 0.2
            );
            yield drift;
         }
      };
   }

   private double calculateVerticalMovement(Vec3 currentPos) {
      double groundY = this.findGroundOrWaterLevel(currentPos.x, currentPos.y, currentPos.z);
      if (groundY == Double.MIN_VALUE) {
         return this.limitVerticalSpeed(0.0);
      }

      double targetY = groundY + this.currentHoverHeight;
      double yDiff = targetY - currentPos.y;
      double verticalSpeed = Mth.clamp(yDiff * 0.2, -0.5, 0.5);
      return this.limitVerticalSpeed(verticalSpeed);
   }

   private double limitVerticalSpeed(double speed) {
      return Mth.clamp(speed, -0.8, 0.8);
   }

   private double findGroundOrWaterLevel(double x, double y, double z) {
      Level level = this.mob.level();
      int startY = Mth.floor(y);
      int searchRange = 200;

      for (int checkY = startY; checkY >= startY - searchRange; checkY--) {
         BlockPos pos = new BlockPos(Mth.floor(x), checkY, Mth.floor(z));
         BlockState state = level.getBlockState(pos);
         if (!state.getFluidState().isEmpty()) {
            return checkY + 1.0;
         }

         if (state.isSolid() && !state.isAir()) {
            try {
               VoxelShape shape = state.getCollisionShape(level, pos);
               if (!shape.isEmpty()) {
                  return pos.getY() + shape.max(Axis.Y);
               }
            } catch (Exception var14) {
            }

            return pos.getY() + 1.0;
         }
      }

      return Double.MIN_VALUE;
   }

   private void randomizePattern() {
      HelicopterHoverMoveGoal.MovementPattern[] patterns = HelicopterHoverMoveGoal.MovementPattern.values();
      HelicopterHoverMoveGoal.MovementPattern newPattern = patterns[this.mob.getRandom().nextInt(patterns.length)];
      if (newPattern == this.currentPattern) {
         newPattern = patterns[this.mob.getRandom().nextInt(patterns.length)];
      }

      this.currentPattern = newPattern;
      switch (this.currentPattern) {
         case CIRCLE:
            this.circleDirection = this.mob.getRandom().nextBoolean() ? 1 : -1;
            break;
         case STRAFE:
            this.strafeDirection = this.mob.getRandom().nextBoolean() ? 1 : -1;
            this.strafeTimer = 20 + this.mob.getRandom().nextInt(20);
      }
   }

   private void randomizeHoverHeight() {
      this.currentHoverHeight = this.minHoverHeight + this.mob.getRandom().nextDouble() * (this.maxHoverHeight - this.minHoverHeight);
   }

   public HelicopterHoverMoveGoal.MovementPattern getCurrentPattern() {
      return this.currentPattern;
   }

   public double getCurrentHoverHeight() {
      return this.currentHoverHeight;
   }

   private enum MovementPattern {
      APPROACH,
      RAPID_APPROACH,
      CIRCLE,
      STRAFE,
      RETREAT,
      HOVER;
   }
}
