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
      this.m_7021_(EnumSet.of(Flag.JUMP));
      this.randomizeHoverHeight();
   }

   public boolean m_8036_() {
      this.target = this.mob.m_5448_();
      return this.target != null && this.target.m_6084_();
   }

   public boolean m_8045_() {
      return this.target != null && this.target.m_6084_();
   }

   public void m_8056_() {
      this.tickCounter = 0;
      this.patternSwitchTimer = this.patternSwitchInterval;
      this.mob.m_20242_(true);
      this.randomizePattern();
   }

   public void m_8041_() {
      this.target = null;
      this.currentPattern = HelicopterHoverMoveGoal.MovementPattern.HOVER;
      Vec3 vel = this.mob.m_20184_();
      this.mob.m_20334_(0.0, vel.f_82480_ * 0.5, 0.0);
   }

   public void m_8037_() {
      this.tickCounter++;
      if (this.target != null) {
         if (this.mob.isStunning()) {
            this.mob.m_20256_(Vec3.f_82478_);
         } else {
            if (this.currentPattern == HelicopterHoverMoveGoal.MovementPattern.APPROACH
               || this.currentPattern == HelicopterHoverMoveGoal.MovementPattern.RAPID_APPROACH) {
               Vec3 currentPos = this.mob.m_20182_();
               Vec3 toTarget = this.target.m_20182_().m_82546_(currentPos);
               Vec3 horizontal = new Vec3(toTarget.f_82479_, 0.0, toTarget.f_82481_);
               double distance = horizontal.m_82553_();
               if (distance < 20.0) {
                  this.patternSwitchTimer = 0;
               }
            }

            this.mob.rotateToTarget(this.target);
            this.patternSwitchTimer--;
            if (this.patternSwitchTimer <= 0) {
               this.randomizePattern();
               this.patternSwitchTimer = this.patternSwitchInterval + this.mob.m_217043_().m_188503_(40) - 20;
            }

            if (this.mob.m_217043_().m_188501_() < 0.1F) {
               this.randomizeHoverHeight();
            }

            this.performMovement();
         }
      }
   }

   private void performMovement() {
      Vec3 currentPos = this.mob.m_20182_();
      Vec3 toTarget = this.target.m_20182_().m_82546_(currentPos);
      Vec3 horizontalMove = this.calculateHorizontalMovement(toTarget);
      double verticalMove = this.calculateVerticalMovement(currentPos);
      this.mob.m_20334_(horizontalMove.f_82479_, verticalMove, horizontalMove.f_82481_);
   }

   private Vec3 calculateHorizontalMovement(Vec3 toTarget) {
      Vec3 horizontal = new Vec3(toTarget.f_82479_, 0.0, toTarget.f_82481_);
      double distance = horizontal.m_82553_();

      return switch (this.currentPattern) {
         case APPROACH -> distance < 0.1 ? Vec3.f_82478_ : horizontal.m_82541_().m_82490_(this.baseSpeed);
         case RAPID_APPROACH -> distance < 0.1 ? Vec3.f_82478_ : horizontal.m_82541_().m_82490_(this.baseSpeed * 2.0);
         case CIRCLE -> {
            double angleToTarget = Math.atan2(horizontal.f_82481_, horizontal.f_82479_);
            double circleAngle = angleToTarget + this.circleDirection * Math.PI / 2.0;
            yield new Vec3(Math.cos(circleAngle) * this.baseSpeed * 1.2, 0.0, Math.sin(circleAngle) * this.baseSpeed * 1.2);
         }
         case STRAFE -> {
            this.strafeTimer--;
            if (this.strafeTimer <= 0) {
               this.strafeDirection *= -1;
               this.strafeTimer = 20 + this.mob.m_217043_().m_188503_(20);
            }

            double angleToTarget = Math.atan2(horizontal.f_82481_, horizontal.f_82479_);
            double strafeAngle = angleToTarget + this.strafeDirection * Math.PI / 2.0;
            yield new Vec3(Math.cos(strafeAngle) * this.baseSpeed * 1.5, 0.0, Math.sin(strafeAngle) * this.baseSpeed * 1.5);
         }
         case RETREAT -> distance < 0.1 ? Vec3.f_82478_ : horizontal.m_82541_().m_82490_(-this.baseSpeed * 0.8);
         case HOVER -> {
            Vec3 drift = new Vec3(
               (this.mob.m_217043_().m_188500_() - 0.5) * this.baseSpeed * 0.2, 0.0, (this.mob.m_217043_().m_188500_() - 0.5) * this.baseSpeed * 0.2
            );
            yield drift;
         }
      };
   }

   private double calculateVerticalMovement(Vec3 currentPos) {
      double groundY = this.findGroundOrWaterLevel(currentPos.f_82479_, currentPos.f_82480_, currentPos.f_82481_);
      if (groundY == Double.MIN_VALUE) {
         return this.limitVerticalSpeed(0.0);
      }

      double targetY = groundY + this.currentHoverHeight;
      double yDiff = targetY - currentPos.f_82480_;
      double verticalSpeed = Mth.m_14008_(yDiff * 0.2, -0.5, 0.5);
      return this.limitVerticalSpeed(verticalSpeed);
   }

   private double limitVerticalSpeed(double speed) {
      return Mth.m_14008_(speed, -0.8, 0.8);
   }

   private double findGroundOrWaterLevel(double x, double y, double z) {
      Level level = this.mob.m_9236_();
      int startY = Mth.m_14107_(y);
      int searchRange = 200;

      for (int checkY = startY; checkY >= startY - searchRange; checkY--) {
         BlockPos pos = new BlockPos(Mth.m_14107_(x), checkY, Mth.m_14107_(z));
         BlockState state = level.m_8055_(pos);
         if (!state.m_60819_().m_76178_()) {
            return checkY + 1.0;
         }

         if (state.m_280296_() && !state.m_60795_()) {
            try {
               VoxelShape shape = state.m_60812_(level, pos);
               if (!shape.m_83281_()) {
                  return pos.m_123342_() + shape.m_83297_(Axis.Y);
               }
            } catch (Exception var14) {
            }

            return pos.m_123342_() + 1.0;
         }
      }

      return Double.MIN_VALUE;
   }

   private void randomizePattern() {
      HelicopterHoverMoveGoal.MovementPattern[] patterns = HelicopterHoverMoveGoal.MovementPattern.values();
      HelicopterHoverMoveGoal.MovementPattern newPattern = patterns[this.mob.m_217043_().m_188503_(patterns.length)];
      if (newPattern == this.currentPattern) {
         newPattern = patterns[this.mob.m_217043_().m_188503_(patterns.length)];
      }

      this.currentPattern = newPattern;
      switch (this.currentPattern) {
         case CIRCLE:
            this.circleDirection = this.mob.m_217043_().m_188499_() ? 1 : -1;
            break;
         case STRAFE:
            this.strafeDirection = this.mob.m_217043_().m_188499_() ? 1 : -1;
            this.strafeTimer = 20 + this.mob.m_217043_().m_188503_(20);
      }
   }

   private void randomizeHoverHeight() {
      this.currentHoverHeight = this.minHoverHeight + this.mob.m_217043_().m_188500_() * (this.maxHoverHeight - this.minHoverHeight);
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
