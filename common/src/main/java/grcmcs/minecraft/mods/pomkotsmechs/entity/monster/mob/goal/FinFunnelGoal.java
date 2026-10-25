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
      this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK));
      this.currentRange = FinFunnelGoal.CombatRange.LONG_RANGE;
      this.targetVelocity = Vec3.f_82478_;
      this.currentVelocity = Vec3.f_82478_;
      this.sphereRadius = midRange * 0.7;
      this.directionChangeCooldown = 60;
      this.hoverDuration = 20;
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
      return this.target != null && this.target.m_6084_() && this.mob.m_20280_(this.target) < this.longRange * this.longRange * 4.0F;
   }

   public void m_8056_() {
      this.determineRange();
      this.initializeMovementPattern();
   }

   public void m_8037_() {
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
         this.mob.m_21563_().m_24960_(this.target, 30.0F, 30.0F);
      }
   }

   private void determineRange() {
      double distance = this.mob.m_20270_(this.target);
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
      double distance = this.mob.m_20270_(this.target);
      FinFunnelGoal.CombatRange newRange = this.currentRange;
      switch (this.currentRange) {
         case LONG_RANGE:
            if (distance <= this.midRange) {
               newRange = FinFunnelGoal.CombatRange.MID_RANGE;
            }
            break;
         case MID_RANGE:
            if (distance <= this.closeRange || this.mob.m_217043_().m_188503_(100) < 5) {
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
      RandomSource random = this.mob.m_217043_();
      switch (this.currentRange) {
         case LONG_RANGE:
         default:
            break;
         case MID_RANGE:
            this.sphereCenter = this.target.m_20182_();
            this.sphereRadius = this.midRange * (0.6 + random.m_188500_() * 0.3);
            Vec3 toMob = this.mob.m_20182_().m_82546_(this.sphereCenter).m_82541_();
            this.sphereTheta = Math.acos(toMob.f_82480_);
            this.spherePhi = Math.atan2(toMob.f_82481_, toMob.f_82479_);
            this.sphereThetaSpeed = (random.m_188500_() - 0.5) * 0.03;
            this.spherePhiSpeed = (random.m_188500_() * 0.04 + 0.02) * (random.m_188499_() ? 1 : -1);
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
      Vec3 mobPos = this.mob.m_20182_();
      Vec3 targetPos = this.target.m_20182_();
      Vec3 direction = targetPos.m_82546_(mobPos).m_82541_();
      double desiredDistance = this.midRange * 0.8;
      Vec3 desiredPos = targetPos.m_82546_(direction.m_82490_(desiredDistance));
      Vec3 movement = desiredPos.m_82546_(mobPos);
      if (movement.m_82553_() > 0.1) {
         this.targetVelocity = movement.m_82541_().m_82490_(this.baseSpeed * 1.2);
         this.applySmoothedMovement();
      }
   }

   private void handleMidRangeMovement() {
      this.sphereCenter = this.sphereCenter.m_165921_(this.target.m_20182_(), 0.08);
      if (this.directionChangeTimer >= this.directionChangeCooldown && this.mob.m_217043_().m_188503_(100) < 25) {
         this.sphereThetaSpeed = (this.mob.m_217043_().m_188500_() - 0.5) * 0.04;
         this.spherePhiSpeed = (this.mob.m_217043_().m_188500_() * 0.05 + 0.02) * (this.mob.m_217043_().m_188499_() ? 1 : -1);
         this.directionChangeTimer = 0;
         this.directionChangeCooldown = 40 + this.mob.m_217043_().m_188503_(80);
      }

      this.sphereTheta = this.sphereTheta + this.sphereThetaSpeed;
      this.spherePhi = this.spherePhi + this.spherePhiSpeed;
      this.sphereTheta = Mth.m_14008_(this.sphereTheta, 0.3, 2.8415926535897933);
      double x = this.sphereCenter.f_82479_ + this.sphereRadius * Math.sin(this.sphereTheta) * Math.cos(this.spherePhi);
      double y = this.sphereCenter.f_82480_ + this.sphereRadius * Math.cos(this.sphereTheta);
      double z = this.sphereCenter.f_82481_ + this.sphereRadius * Math.sin(this.sphereTheta) * Math.sin(this.spherePhi);
      Vec3 targetPos = new Vec3(x, y, z);
      Vec3 movement = targetPos.m_82546_(this.mob.m_20182_());
      if (movement.m_82553_() > 0.1) {
         this.targetVelocity = movement.m_82541_().m_82490_(this.baseSpeed * 0.8);
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
            (this.mob.m_217043_().m_188500_() - 0.5) * 0.1, (this.mob.m_217043_().m_188500_() - 0.5) * 0.1, (this.mob.m_217043_().m_188500_() - 0.5) * 0.1
         );
         this.targetVelocity = this.currentVelocity.m_82490_(0.1).m_82549_(wobble);
         this.applySmoothedMovement();
      } else {
         Vec3 mobPos = this.mob.m_20182_();
         Vec3 movement = this.targetPosition.m_82546_(mobPos);
         if (movement.m_82553_() < 0.8) {
            this.isHovering = true;
            this.hoverTimer = 0;
            this.hoverDuration = 15 + this.mob.m_217043_().m_188503_(20);
            this.applyCloseRangeMovement(Vec3.f_82478_);
         } else {
            this.targetVelocity = movement.m_82541_().m_82490_(this.baseSpeed * 1.5);
            this.applySmoothedMovement();
         }
      }
   }

   private void applyCloseRangeMovement(Vec3 movement) {
      if (this.mob.m_20068_()) {
         this.mob.m_20256_(movement);
      } else {
         Vec3 currentMovement = this.mob.m_20184_();
         this.mob.m_20334_(movement.f_82479_, currentMovement.f_82480_, movement.f_82481_);
      }
   }

   private void generateRandomClosePosition() {
      RandomSource random = this.mob.m_217043_();
      Vec3 targetPos = this.target.m_20182_();
      double distance = this.closeRange * (0.3 + random.m_188500_() * 0.4);
      double theta = random.m_188500_() * Math.PI * 2.0;
      double phi = random.m_188500_() * Math.PI;
      double x = targetPos.f_82479_ + distance * Math.sin(phi) * Math.cos(theta);
      double y = targetPos.f_82480_ + distance * Math.cos(phi);
      double z = targetPos.f_82481_ + distance * Math.sin(phi) * Math.sin(theta);
      y = Math.max(targetPos.f_82480_ - 2.0, Math.min(targetPos.f_82480_ + 4.0, y));
      this.targetPosition = new Vec3(x, y, z);
   }

   private void applySmoothedMovement() {
      this.currentVelocity = this.currentVelocity.m_165921_(this.targetVelocity, 0.15);
      if (this.mob.m_20068_()) {
         this.mob.m_20256_(this.currentVelocity);
      } else {
         Vec3 currentMovement = this.mob.m_20184_();
         this.mob.m_20334_(this.currentVelocity.f_82479_, currentMovement.f_82480_, this.currentVelocity.f_82481_);
      }
   }

   private void handleAttacking() {
      if (this.attackTimer >= this.attackCooldown) {
         double distance = this.mob.m_20270_(this.target);
         boolean shouldAttack = false;

         if (switch (this.currentRange) {
            case MID_RANGE -> this.mob.m_217043_().m_188503_(100) < 20;
            case CLOSE_RANGE -> this.isHovering && this.hoverTimer > 5;
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

   public void m_8041_() {
      this.target = null;
      this.currentVelocity = Vec3.f_82478_;
      this.targetVelocity = Vec3.f_82478_;
      this.isHovering = false;
      this.closeRangeTimer = 0;
   }

   private enum CombatRange {
      LONG_RANGE,
      MID_RANGE,
      CLOSE_RANGE;
   }
}
