package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RollerDashGoal extends SmallMobGoalBase {
   private final BaseSmallMonsterEntity mob;
   private LivingEntity target;
   private final double speed;
   private final float attackRange;
   private final int attackCooldown;
   private final int dashRange;
   private final int closeRange;
   private final int dashDuration;
   private final int circleDuration;
   private RollerDashGoal.MovementPattern currentPattern;
   private int patternTimer;
   private int patternDuration;
   private int attackTimer;
   private double circleRadius;
   private double circleAngle;
   private double circleSpeed;
   private Vec3 circleCenter;
   private Vec3 dashTarget;
   private boolean isDashing;
   private int dashCooldown;

   public RollerDashGoal(
      BaseSmallMonsterEntity mob, double speed, float attackRange, int attackCooldown, int dashRange, int closeRange, int dashDuration, int circleDuration
   ) {
      super(mob, (float)speed);
      this.mob = mob;
      this.speed = speed;
      this.attackRange = attackRange;
      this.attackCooldown = attackCooldown;
      this.dashRange = dashRange;
      this.closeRange = closeRange;
      this.dashDuration = dashDuration;
      this.circleDuration = circleDuration;
      this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK));
      this.currentPattern = RollerDashGoal.MovementPattern.DASH_TO_TARGET;
      this.patternTimer = 0;
      this.patternDuration = 60;
      this.attackTimer = 0;
      this.dashCooldown = 0;
      this.circleRadius = 3.0;
      this.circleSpeed = 0.1;
   }

   @Override
   public boolean m_8036_() {
      if (!super.m_8036_()) {
         return false;
      }

      this.target = this.mob.m_5448_();
      return this.target != null && this.target.m_6084_();
   }

   public boolean m_8045_() {
      return this.target != null && this.target.m_6084_();
   }

   public void m_8056_() {
      this.switchPattern();
   }

   public void m_8037_() {
      if (this.target != null) {
         this.mob.rotateToTarget(this.target);
         this.patternTimer++;
         this.attackTimer++;
         if (this.dashCooldown > 0) {
            this.dashCooldown--;
         }

         if (this.patternTimer >= this.patternDuration) {
            this.switchPattern();
         }

         switch (this.currentPattern) {
            case DASH_TO_TARGET:
               this.handleDashMovement();
               break;
            case CIRCLE_MOVEMENT:
               this.handleCircleMovement();
               break;
            case PATTERN_SWITCH_DELAY:
               this.mob.m_20256_(this.mob.m_20184_().m_82542_(0.8, 1.0, 0.8));
         }

         this.handleAttack();
         this.mob.m_21563_().m_24960_(this.target, 30.0F, 30.0F);
         this.mob.f_19812_ = true;
      }
   }

   private void switchPattern() {
      RandomSource random = this.mob.m_217043_();

      RollerDashGoal.MovementPattern newPattern;
      do {
         newPattern = random.m_188499_() ? RollerDashGoal.MovementPattern.DASH_TO_TARGET : RollerDashGoal.MovementPattern.CIRCLE_MOVEMENT;
      } while (newPattern == this.currentPattern && random.m_188501_() < 0.7F);

      this.currentPattern = newPattern;
      this.patternTimer = 0;
      switch (this.currentPattern) {
         case DASH_TO_TARGET:
            this.patternDuration = this.dashDuration + random.m_188503_(this.dashDuration);
            this.setupDash();
            break;
         case CIRCLE_MOVEMENT:
            this.patternDuration = this.circleDuration + random.m_188503_(this.circleDuration);
            this.setupCircleMovement();
      }
   }

   private void setupDash() {
      if (this.target != null && this.dashCooldown <= 0) {
         Vec3 targetPos = this.target.m_20182_();
         Vec3 mobPos = this.mob.m_20182_();
         Vec3 direction = targetPos.m_82546_(mobPos).m_82541_();
         double distance = Math.max(2.0, mobPos.m_82554_(targetPos) - 1.5);
         this.dashTarget = mobPos.m_82549_(direction.m_82490_(distance));
         this.isDashing = true;
         this.dashCooldown = 20;
      }
   }

   private void handleDashMovement() {
      if (this.target != null) {
         Vec3 mobPos = this.mob.m_20182_();
         Vec3 targetPos = this.target.m_20182_();
         if (this.isDashing && this.dashTarget != null) {
            Vec3 direction = this.dashTarget.m_82546_(mobPos);
            if (direction.m_82553_() < this.dashRange) {
               this.isDashing = false;
               this.dashTarget = null;
               return;
            }

            direction = direction.m_82541_();
            Vec3 movement = direction.m_82490_(this.speed * 1.5);
            Vec3 newMovement = this.adjustMovementForTerrain(movement);
            this.mob.m_20256_(newMovement);
         } else {
            Vec3 dist = targetPos.m_82546_(mobPos);
            if (dist.m_82553_() < this.closeRange) {
               this.switchPattern();
               return;
            }

            Vec3 direction = targetPos.m_82546_(mobPos).m_82541_();
            Vec3 movement = direction.m_82490_(this.speed);
            Vec3 newMovement = this.adjustMovementForTerrain(movement);
            this.mob.m_20256_(newMovement);
         }
      }
   }

   private void setupCircleMovement() {
      if (this.target != null) {
         this.circleCenter = this.target.m_20182_();
         this.circleRadius = 40.0 + this.mob.m_217043_().m_188500_() * 20.0;
         this.circleSpeed = 0.08 + this.mob.m_217043_().m_188500_() * 0.04;
         Vec3 mobPos = this.mob.m_20182_();
         Vec3 offset = mobPos.m_82546_(this.circleCenter);
         this.circleAngle = Math.atan2(offset.f_82481_, offset.f_82479_);
      }
   }

   private void handleCircleMovement() {
      if (this.target != null) {
         this.circleCenter = this.circleCenter.m_165921_(this.target.m_20182_(), 0.1);
         this.circleAngle = this.circleAngle + this.circleSpeed;
         double targetX = this.circleCenter.f_82479_ + Math.cos(this.circleAngle) * this.circleRadius;
         double targetZ = this.circleCenter.f_82481_ + Math.sin(this.circleAngle) * this.circleRadius;
         Vec3 mobPos = this.mob.m_20182_();
         Vec3 targetCirclePos = new Vec3(targetX, mobPos.f_82480_, targetZ);
         Vec3 direction = targetCirclePos.m_82546_(mobPos);
         if (direction.m_82553_() > 0.1) {
            direction = direction.m_82541_();
            Vec3 movement = direction.m_82490_(this.speed);
            Vec3 newMovement = this.adjustMovementForTerrain(movement);
            this.mob.m_20256_(newMovement);
         }
      }
   }

   private Vec3 adjustMovementForTerrain(Vec3 movement) {
      Vec3 mobPos = this.mob.m_20182_();
      Level level = this.mob.m_9236_();
      Vec3 newPos = mobPos.m_82549_(movement);
      BlockPos newBlockPos = new BlockPos((int)newPos.f_82479_, (int)newPos.f_82480_, (int)newPos.f_82481_);
      BlockPos groundPos = this.findGroundLevel(newBlockPos, level);
      if (groundPos != null) {
         double groundY = groundPos.m_123342_() + 1.0;
         double stepHeight = this.mob.m_274421_();
         if (groundY - mobPos.f_82480_ <= stepHeight && groundY - mobPos.f_82480_ >= -stepHeight) {
            return new Vec3(movement.f_82479_, Math.min(0.5, (groundY - mobPos.f_82480_) * 0.3), movement.f_82481_);
         }

         if (groundY > mobPos.f_82480_ + stepHeight) {
            return new Vec3(movement.f_82479_ * 0.5, 0.4, movement.f_82481_ * 0.5);
         }
      }

      return movement;
   }

   private BlockPos findGroundLevel(BlockPos pos, Level level) {
      for (int i = 0; i < 10; i++) {
         BlockPos checkPos = pos.m_6625_(i);
         BlockState state = level.m_8055_(checkPos);
         if (!state.m_60795_() && state.m_60804_(level, checkPos)) {
            return checkPos;
         }
      }

      for (int i = 1; i < 5; i++) {
         BlockPos checkPos = pos.m_6630_(i);
         BlockState state = level.m_8055_(checkPos.m_7495_());
         if (!state.m_60795_() && state.m_60804_(level, checkPos.m_7495_())) {
            return checkPos.m_7495_();
         }
      }

      return null;
   }

   private void handleAttack() {
      if (this.target != null && this.attackTimer >= this.attackCooldown) {
         double distance = this.mob.m_20270_(this.target);
         if (distance <= this.attackRange) {
            this.performAttack();
            this.attackTimer = 0;
         }
      }
   }

   private void performAttack() {
      this.mob.tryAttack();
   }

   public void m_8041_() {
      this.target = null;
      this.isDashing = false;
      this.dashTarget = null;
      this.currentPattern = RollerDashGoal.MovementPattern.DASH_TO_TARGET;
      this.patternTimer = 0;
   }

   private enum MovementPattern {
      DASH_TO_TARGET,
      CIRCLE_MOVEMENT,
      PATTERN_SWITCH_DELAY;
   }
}
