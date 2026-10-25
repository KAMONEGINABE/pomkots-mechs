package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.phys.Vec3;

public class FinFunnelGoal extends SmallMobGoalBase {
   private LivingEntity target;
   private final double baseSpeed;
   private final float longRange;
   private final float midRange;
   private final float closeRange;
   private final int attackCooldown;
   private FinFunnelGoal.CombatRange currentRange;
   private int rangeTimer;
   private int attackTimer;
   private double sphereRadius;
   private double sphereTheta;
   private double spherePhi;
   private double sphereThetaSpeed;
   private double spherePhiSpeed;
   private Vec3 sphereCenter;
   private int directionChangeTimer;
   private int directionChangeCooldown;
   private Vec3 targetPosition;
   private Vec3 currentVelocity;
   private boolean isHovering;
   private int hoverTimer;
   private int hoverDuration;
   private int closeRangeTimer;
   private static final int CLOSE_RANGE_MAX_TIME = 200;
   private Vec3 targetVelocity;
   private static final double VELOCITY_SMOOTHING = 0.15;
   private static final double POSITION_SMOOTHING = 0.08;

   public FinFunnelGoal(BaseSmallMonsterEntity mob, double baseSpeed, float longRange, float midRange, float closeRange, int attackCooldown) {
      super(mob, (float)baseSpeed);
      this.mob = mob;
      this.baseSpeed = baseSpeed;
      this.longRange = longRange;
      this.midRange = midRange;
      this.closeRange = closeRange;
      this.attackCooldown = attackCooldown;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      this.currentRange = FinFunnelGoal.CombatRange.LONG_RANGE;
      this.targetVelocity = Vec3.ZERO;
      this.currentVelocity = Vec3.ZERO;
      this.sphereRadius = midRange * 0.7;
      this.directionChangeCooldown = 60;
      this.hoverDuration = 20;
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
      return this.target != null && this.target.isAlive() && this.mob.distanceToSqr(this.target) < this.longRange * this.longRange * 4.0F;
   }

   public void start() {
      this.determineRange();
      this.initializeMovementPattern();
   }

   public void tick() {
      if (this.target != null) {
         this.mob.rotateToTarget(this.target);
         this.rangeTimer++;
         this.attackTimer++;
         this.directionChangeTimer++;
         if (this.rangeTimer % 20 == 0) {
            this.updateRange();
         }

         switch (this.currentRange) {
            case LONG_RANGE:
               this.handleLongRangeMovement();
               break;
            case MID_RANGE:
               this.handleMidRangeMovement();
               break;
            case CLOSE_RANGE:
               this.handleCloseRangeMovement();
         }

         this.handleAttacking();
         this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
      }
   }

   private void determineRange() {
      double distance = this.mob.distanceTo(this.target);
      if (distance > this.longRange) {
         this.currentRange = FinFunnelGoal.CombatRange.LONG_RANGE;
      } else if (distance > this.midRange) {
         this.currentRange = FinFunnelGoal.CombatRange.MID_RANGE;
      } else {
         this.currentRange = FinFunnelGoal.CombatRange.CLOSE_RANGE;
      }

      this.initializeMovementPattern();
   }

   private void updateRange() {
      double distance = this.mob.distanceTo(this.target);
      FinFunnelGoal.CombatRange newRange = this.currentRange;
      switch (this.currentRange) {
         case LONG_RANGE:
            if (distance <= this.midRange) {
               newRange = FinFunnelGoal.CombatRange.MID_RANGE;
            }
            break;
         case MID_RANGE:
            if (distance <= this.closeRange || this.mob.getRandom().nextInt(100) < 5) {
               newRange = FinFunnelGoal.CombatRange.CLOSE_RANGE;
            } else if (distance > this.longRange) {
               newRange = FinFunnelGoal.CombatRange.LONG_RANGE;
            }
            break;
         case CLOSE_RANGE:
            this.closeRangeTimer++;
            if (this.closeRangeTimer >= 200 || distance > this.midRange * 1.5) {
               newRange = FinFunnelGoal.CombatRange.MID_RANGE;
               this.closeRangeTimer = 0;
            }
      }

      if (newRange != this.currentRange) {
         this.currentRange = newRange;
         this.initializeMovementPattern();
      }
   }

   private void initializeMovementPattern() {
      RandomSource random = this.mob.getRandom();
      switch (this.currentRange) {
         case LONG_RANGE:
         default:
            break;
         case MID_RANGE:
            this.sphereCenter = this.target.position();
            this.sphereRadius = this.midRange * (0.6 + random.nextDouble() * 0.3);
            Vec3 toMob = this.mob.position().subtract(this.sphereCenter).normalize();
            this.sphereTheta = Math.acos(toMob.y);
            this.spherePhi = Math.atan2(toMob.z, toMob.x);
            this.sphereThetaSpeed = (random.nextDouble() - 0.5) * 0.03;
            this.spherePhiSpeed = (random.nextDouble() * 0.04 + 0.02) * (random.nextBoolean() ? 1 : -1);
            this.directionChangeTimer = 0;
            break;
         case CLOSE_RANGE:
            this.generateRandomClosePosition();
            this.isHovering = false;
            this.hoverTimer = 0;
            this.closeRangeTimer = 0;
      }
   }

   private void handleLongRangeMovement() {
      Vec3 mobPos = this.mob.position();
      Vec3 targetPos = this.target.position();
      Vec3 direction = targetPos.subtract(mobPos).normalize();
      double desiredDistance = this.midRange * 0.8;
      Vec3 desiredPos = targetPos.subtract(direction.scale(desiredDistance));
      Vec3 movement = desiredPos.subtract(mobPos);
      if (movement.length() > 0.1) {
         this.targetVelocity = movement.normalize().scale(this.baseSpeed * 1.2);
         this.applySmoothedMovement();
      }
   }

   private void handleMidRangeMovement() {
      this.sphereCenter = this.sphereCenter.lerp(this.target.position(), 0.08);
      if (this.directionChangeTimer >= this.directionChangeCooldown && this.mob.getRandom().nextInt(100) < 25) {
         this.sphereThetaSpeed = (this.mob.getRandom().nextDouble() - 0.5) * 0.04;
         this.spherePhiSpeed = (this.mob.getRandom().nextDouble() * 0.05 + 0.02) * (this.mob.getRandom().nextBoolean() ? 1 : -1);
         this.directionChangeTimer = 0;
         this.directionChangeCooldown = 40 + this.mob.getRandom().nextInt(80);
      }

      this.sphereTheta = this.sphereTheta + this.sphereThetaSpeed;
      this.spherePhi = this.spherePhi + this.spherePhiSpeed;
      this.sphereTheta = Mth.clamp(this.sphereTheta, 0.3, 2.8415926535897933);
      double x = this.sphereCenter.x + this.sphereRadius * Math.sin(this.sphereTheta) * Math.cos(this.spherePhi);
      double y = this.sphereCenter.y + this.sphereRadius * Math.cos(this.sphereTheta);
      double z = this.sphereCenter.z + this.sphereRadius * Math.sin(this.sphereTheta) * Math.sin(this.spherePhi);
      Vec3 targetPos = new Vec3(x, y, z);
      Vec3 movement = targetPos.subtract(this.mob.position());
      if (movement.length() > 0.1) {
         this.targetVelocity = movement.normalize().scale(this.baseSpeed * 0.8);
         this.applySmoothedMovement();
      }
   }

   private void handleCloseRangeMovement() {
      if (this.isHovering) {
         this.hoverTimer++;
         if (this.hoverTimer >= this.hoverDuration) {
            this.isHovering = false;
            this.hoverTimer = 0;
            this.generateRandomClosePosition();
         }

         Vec3 wobble = new Vec3(
            (this.mob.getRandom().nextDouble() - 0.5) * 0.1, (this.mob.getRandom().nextDouble() - 0.5) * 0.1, (this.mob.getRandom().nextDouble() - 0.5) * 0.1
         );
         this.targetVelocity = this.currentVelocity.scale(0.1).add(wobble);
         this.applySmoothedMovement();
      } else {
         Vec3 mobPos = this.mob.position();
         Vec3 movement = this.targetPosition.subtract(mobPos);
         if (movement.length() < 0.8) {
            this.isHovering = true;
            this.hoverTimer = 0;
            this.hoverDuration = 15 + this.mob.getRandom().nextInt(20);
            this.applyCloseRangeMovement(Vec3.ZERO);
         } else {
            this.targetVelocity = movement.normalize().scale(this.baseSpeed * 1.5);
            this.applySmoothedMovement();
         }
      }
   }

   private void applyCloseRangeMovement(Vec3 movement) {
      if (this.mob.isNoGravity()) {
         this.mob.setDeltaMovement(movement);
      } else {
         Vec3 currentMovement = this.mob.getDeltaMovement();
         this.mob.setDeltaMovement(movement.x, currentMovement.y, movement.z);
      }
   }

   private void generateRandomClosePosition() {
      RandomSource random = this.mob.getRandom();
      Vec3 targetPos = this.target.position();
      double distance = this.closeRange * (0.3 + random.nextDouble() * 0.4);
      double theta = random.nextDouble() * Math.PI * 2.0;
      double phi = random.nextDouble() * Math.PI;
      double x = targetPos.x + distance * Math.sin(phi) * Math.cos(theta);
      double y = targetPos.y + distance * Math.cos(phi);
      double z = targetPos.z + distance * Math.sin(phi) * Math.sin(theta);
      y = Math.max(targetPos.y - 2.0, Math.min(targetPos.y + 4.0, y));
      this.targetPosition = new Vec3(x, y, z);
   }

   private void applySmoothedMovement() {
      this.currentVelocity = this.currentVelocity.lerp(this.targetVelocity, 0.15);
      if (this.mob.isNoGravity()) {
         this.mob.setDeltaMovement(this.currentVelocity);
      } else {
         Vec3 currentMovement = this.mob.getDeltaMovement();
         this.mob.setDeltaMovement(this.currentVelocity.x, currentMovement.y, this.currentVelocity.z);
      }
   }

   private void handleAttacking() {
      if (this.attackTimer >= this.attackCooldown) {
         double distance = this.mob.distanceTo(this.target);
         boolean shouldAttack = false;

         if (switch (this.currentRange) {
            case MID_RANGE -> this.mob.getRandom().nextInt(100) < 20;
            case CLOSE_RANGE -> this.isHovering && this.hoverTimer > 5;
            default -> false;
         }) {
            this.performAttack();
            this.attackTimer = 0;
         }
      }
   }

   private void performAttack() {
      switch (this.currentRange) {
         case MID_RANGE:
            this.performRangedAttack();
            break;
         case CLOSE_RANGE:
            this.performMeleeAttack();
      }
   }

   private void performRangedAttack() {
      this.mob.tryAttack();
   }

   private void performMeleeAttack() {
      this.mob.tryAttack();
   }

   public void stop() {
      this.target = null;
      this.currentVelocity = Vec3.ZERO;
      this.targetVelocity = Vec3.ZERO;
      this.isHovering = false;
      this.closeRangeTimer = 0;
   }

   private enum CombatRange {
      LONG_RANGE,
      MID_RANGE,
      CLOSE_RANGE;
   }
}
