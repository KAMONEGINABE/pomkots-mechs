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
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      this.currentPattern = RollerDashGoal.MovementPattern.DASH_TO_TARGET;
      this.patternTimer = 0;
      this.patternDuration = 60;
      this.attackTimer = 0;
      this.dashCooldown = 0;
      this.circleRadius = 3.0;
      this.circleSpeed = 0.1;
   }

   @Override
   public boolean canUse() {
      if (!super.canUse()) {
         return false;
      }

      this.target = this.mob.getTarget();
      return this.target != null && this.target.isAlive();
   }

   public boolean canContinueToUse() {
      return this.target != null && this.target.isAlive();
   }

   public void start() {
      this.switchPattern();
   }

   public void tick() {
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
               this.mob.setDeltaMovement(this.mob.getDeltaMovement().multiply(0.8, 1.0, 0.8));
         }

         this.handleAttack();
         this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
         this.mob.hasImpulse = true;
      }
   }

   private void switchPattern() {
      RandomSource random = this.mob.getRandom();

      RollerDashGoal.MovementPattern newPattern;
      do {
         newPattern = random.nextBoolean() ? RollerDashGoal.MovementPattern.DASH_TO_TARGET : RollerDashGoal.MovementPattern.CIRCLE_MOVEMENT;
      } while (newPattern == this.currentPattern && random.nextFloat() < 0.7F);

      this.currentPattern = newPattern;
      this.patternTimer = 0;
      switch (this.currentPattern) {
         case DASH_TO_TARGET:
            this.patternDuration = this.dashDuration + random.nextInt(this.dashDuration);
            this.setupDash();
            break;
         case CIRCLE_MOVEMENT:
            this.patternDuration = this.circleDuration + random.nextInt(this.circleDuration);
            this.setupCircleMovement();
      }
   }

   private void setupDash() {
      if (this.target != null && this.dashCooldown <= 0) {
         Vec3 targetPos = this.target.position();
         Vec3 mobPos = this.mob.position();
         Vec3 direction = targetPos.subtract(mobPos).normalize();
         double distance = Math.max(2.0, mobPos.distanceTo(targetPos) - 1.5);
         this.dashTarget = mobPos.add(direction.scale(distance));
         this.isDashing = true;
         this.dashCooldown = 20;
      }
   }

   private void handleDashMovement() {
      if (this.target != null) {
         Vec3 mobPos = this.mob.position();
         Vec3 targetPos = this.target.position();
         if (this.isDashing && this.dashTarget != null) {
            Vec3 direction = this.dashTarget.subtract(mobPos);
            if (direction.length() < this.dashRange) {
               this.isDashing = false;
               this.dashTarget = null;
               return;
            }

            direction = direction.normalize();
            Vec3 movement = direction.scale(this.speed * 1.5);
            Vec3 newMovement = this.adjustMovementForTerrain(movement);
            this.mob.setDeltaMovement(newMovement);
         } else {
            Vec3 dist = targetPos.subtract(mobPos);
            if (dist.length() < this.closeRange) {
               this.switchPattern();
               return;
            }

            Vec3 direction = targetPos.subtract(mobPos).normalize();
            Vec3 movement = direction.scale(this.speed);
            Vec3 newMovement = this.adjustMovementForTerrain(movement);
            this.mob.setDeltaMovement(newMovement);
         }
      }
   }

   private void setupCircleMovement() {
      if (this.target != null) {
         this.circleCenter = this.target.position();
         this.circleRadius = 40.0 + this.mob.getRandom().nextDouble() * 20.0;
         this.circleSpeed = 0.08 + this.mob.getRandom().nextDouble() * 0.04;
         Vec3 mobPos = this.mob.position();
         Vec3 offset = mobPos.subtract(this.circleCenter);
         this.circleAngle = Math.atan2(offset.z, offset.x);
      }
   }

   private void handleCircleMovement() {
      if (this.target != null) {
         this.circleCenter = this.circleCenter.lerp(this.target.position(), 0.1);
         this.circleAngle = this.circleAngle + this.circleSpeed;
         double targetX = this.circleCenter.x + Math.cos(this.circleAngle) * this.circleRadius;
         double targetZ = this.circleCenter.z + Math.sin(this.circleAngle) * this.circleRadius;
         Vec3 mobPos = this.mob.position();
         Vec3 targetCirclePos = new Vec3(targetX, mobPos.y, targetZ);
         Vec3 direction = targetCirclePos.subtract(mobPos);
         if (direction.length() > 0.1) {
            direction = direction.normalize();
            Vec3 movement = direction.scale(this.speed);
            Vec3 newMovement = this.adjustMovementForTerrain(movement);
            this.mob.setDeltaMovement(newMovement);
         }
      }
   }

   private Vec3 adjustMovementForTerrain(Vec3 movement) {
      Vec3 mobPos = this.mob.position();
      Level level = this.mob.level();
      Vec3 newPos = mobPos.add(movement);
      BlockPos newBlockPos = new BlockPos((int)newPos.x, (int)newPos.y, (int)newPos.z);
      BlockPos groundPos = this.findGroundLevel(newBlockPos, level);
      if (groundPos != null) {
         double groundY = groundPos.getY() + 1.0;
         double stepHeight = this.mob.maxUpStep();
         if (groundY - mobPos.y <= stepHeight && groundY - mobPos.y >= -stepHeight) {
            return new Vec3(movement.x, Math.min(0.5, (groundY - mobPos.y) * 0.3), movement.z);
         }

         if (groundY > mobPos.y + stepHeight) {
            return new Vec3(movement.x * 0.5, 0.4, movement.z * 0.5);
         }
      }

      return movement;
   }

   private BlockPos findGroundLevel(BlockPos pos, Level level) {
      for (int i = 0; i < 10; i++) {
         BlockPos checkPos = pos.below(i);
         BlockState state = level.getBlockState(checkPos);
         if (!state.isAir() && state.isSolidRender(level, checkPos)) {
            return checkPos;
         }
      }

      for (int i = 1; i < 5; i++) {
         BlockPos checkPos = pos.above(i);
         BlockState state = level.getBlockState(checkPos.below());
         if (!state.isAir() && state.isSolidRender(level, checkPos.below())) {
            return checkPos.below();
         }
      }

      return null;
   }

   private void handleAttack() {
      if (this.target != null && this.attackTimer >= this.attackCooldown) {
         double distance = this.mob.distanceTo(this.target);
         if (distance <= this.attackRange) {
            this.performAttack();
            this.attackTimer = 0;
         }
      }
   }

   private void performAttack() {
      this.mob.tryAttack();
   }

   public void stop() {
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
