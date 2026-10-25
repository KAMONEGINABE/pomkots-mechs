package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.phys.Vec3;

public class BossAerialDiveGoal extends BaseBossGoal {
   private final double requiredHeightDifference;
   private final double jumpStrength;
   private final double boosterSpeed;
   private final double flySpeed;
   private final double diveSpeed;
   private final int maxChaseTime;
   private LivingEntity target;
   private Vec3 targetPosition;
   private int chaseTimer;
   private BossAerialDiveGoal.Phase currentPhase = BossAerialDiveGoal.Phase.CHARGE;
   private int phaseTimer = 0;

   public BossAerialDiveGoal(
      BaseBossEntity mob, double requiredHeightDifference, double jumpStrength, double boosterSpeed, double flySpeed, double diveSpeed, int maxChaseTime
   ) {
      super(mob);
      this.requiredHeightDifference = requiredHeightDifference;
      this.jumpStrength = jumpStrength;
      this.boosterSpeed = boosterSpeed;
      this.flySpeed = flySpeed;
      this.diveSpeed = diveSpeed;
      this.maxChaseTime = maxChaseTime;
      this.m_7021_(EnumSet.of(Flag.MOVE, Flag.JUMP));
   }

   @Override
   public boolean canUseInternal() {
      this.target = this.mob.m_5448_();
      if (this.target != null) {
         double heightDiff = this.target.m_20186_() - this.mob.m_20186_();
         if (heightDiff >= this.requiredHeightDifference) {
            Entity vehicle = this.target.m_20202_();
            return vehicle != null ? vehicle.m_20096_() : this.target.m_20096_();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean m_8045_() {
      return this.target != null
         && this.target.m_6084_()
         && (this.currentPhase != BossAerialDiveGoal.Phase.DIVE || !this.mob.m_20096_())
         && this.currentPhase != BossAerialDiveGoal.Phase.TIMEOUT;
   }

   public void m_8056_() {
      this.currentPhase = BossAerialDiveGoal.Phase.CHARGE;
      this.phaseTimer = 0;
      this.chaseTimer = 0;
      if (this.target != null) {
         this.targetPosition = this.target.m_20182_();
      }
   }

   public void m_8037_() {
      if (this.target != null && this.target.m_6084_()) {
         this.mob.rotateToTarget(this.target);
         this.phaseTimer++;
         this.chaseTimer++;
         if (this.chaseTimer >= this.maxChaseTime) {
            this.currentPhase = BossAerialDiveGoal.Phase.TIMEOUT;
            this.phaseTimer = 0;
         }

         switch (this.currentPhase) {
            case CHARGE:
               this.handleChargePhase();
               break;
            case JUMP:
               this.handleJumpPhase();
               break;
            case BOOST:
               this.handleBoostPhase();
               break;
            case FLY:
               this.handleFlyPhase();
               break;
            case DIVE:
               this.handleDivePhase();
               break;
            case TIMEOUT:
               this.handleTimeoutPhase();
         }

         if (this.currentPhase != BossAerialDiveGoal.Phase.DIVE) {
            this.updateRotation();
         }
      }
   }

   private void handleChargePhase() {
      if (this.phaseTimer == 1) {
         this.mob.triggerAnim("action_controller", "jump");
      } else if (this.phaseTimer == 10) {
         this.currentPhase = BossAerialDiveGoal.Phase.BOOST;
         this.phaseTimer = 0;
         this.performInitialJump();
      }
   }

   private void performInitialJump() {
      Vec3 jumpVec = new Vec3(0.0, this.jumpStrength, 0.0);
      this.mob.m_20256_(jumpVec);
      this.mob.m_20242_(true);
      this.mob.f_19812_ = true;
   }

   private void handleJumpPhase() {
      if (this.mob.m_20184_().f_82480_ <= 0.0) {
         this.currentPhase = BossAerialDiveGoal.Phase.BOOST;
         this.phaseTimer = 0;
      }
   }

   private void handleBoostPhase() {
      Vec3 currentMovement = this.mob.m_20184_();
      Vec3 boostMovement = new Vec3(currentMovement.f_82479_ * 0.9, this.boosterSpeed, currentMovement.f_82481_ * 0.9);
      this.mob.m_20256_(boostMovement);
      if (this.mob.m_20186_() > this.target.m_20186_() + 5.0 || this.phaseTimer > 60) {
         this.currentPhase = BossAerialDiveGoal.Phase.FLY;
         this.phaseTimer = 0;
         this.targetPosition = this.target.m_20182_().m_82520_(0.0, 2.0, 0.0);
      }
   }

   private void handleFlyPhase() {
      this.targetPosition = this.target.m_20182_().m_82520_(0.0, 2.0, 0.0);
      Vec3 toTarget = this.targetPosition.m_82546_(this.mob.m_20182_());
      Vec3 horizontalDir = new Vec3(toTarget.f_82479_, 0.0, toTarget.f_82481_).m_82541_();
      double currentY = this.mob.m_20186_();
      double targetY = this.target.m_20186_() + 8.0;
      double verticalAdjustment = (targetY - currentY) * 0.1;
      Vec3 flyMovement = horizontalDir.m_82490_(this.flySpeed).m_82520_(0.0, verticalAdjustment, 0.0);
      this.mob.m_20256_(flyMovement);
      double horizontalDistance = Math.sqrt(toTarget.f_82479_ * toTarget.f_82479_ + toTarget.f_82481_ * toTarget.f_82481_);
      if (horizontalDistance < 15.0 || this.phaseTimer > 80) {
         this.currentPhase = BossAerialDiveGoal.Phase.DIVE;
         this.phaseTimer = 0;
      }
   }

   private void handleDivePhase() {
      this.mob.m_20242_(false);
      if (this.mob.m_20096_() || this.isNearGround()) {
         this.mob.getActionController().getAction("onground").tryAction();
      }
   }

   private void handleTimeoutPhase() {
      this.mob.m_20242_(false);
   }

   private void updateRotation() {
      Vec3 toTarget = this.target.m_20182_().m_82546_(this.mob.m_20182_());
      float desiredYaw = (float)(Mth.m_14136_(toTarget.f_82479_, toTarget.f_82481_) * (-180.0 / Math.PI));
      this.mob.m_146922_(this.rotLerp(this.mob.m_146908_(), desiredYaw, 5.0F));
      this.mob.f_20883_ = this.mob.m_146908_();
      if (this.currentPhase == BossAerialDiveGoal.Phase.FLY || this.currentPhase == BossAerialDiveGoal.Phase.BOOST) {
         double horizontalDist = Math.sqrt(toTarget.f_82479_ * toTarget.f_82479_ + toTarget.f_82481_ * toTarget.f_82481_);
         float desiredPitch = (float)(Math.atan2(-toTarget.f_82480_, horizontalDist) * (180.0 / Math.PI));
         this.mob.m_146926_(this.rotLerp(this.mob.m_146909_(), desiredPitch, 3.0F));
      }
   }

   private float rotLerp(float current, float target, float maxChange) {
      float delta = Mth.m_14177_(target - current);
      if (delta > maxChange) {
         delta = maxChange;
      }

      if (delta < -maxChange) {
         delta = -maxChange;
      }

      return current + delta;
   }

   private boolean isNearGround() {
      BlockPos belowPos = new BlockPos((int)this.mob.m_20185_(), (int)(this.mob.m_20186_() - this.diveSpeed), (int)this.mob.m_20189_());
      return !this.mob.m_9236_().m_8055_(belowPos).m_60795_();
   }

   public void m_8041_() {
      this.currentPhase = BossAerialDiveGoal.Phase.CHARGE;
      this.phaseTimer = 0;
      this.chaseTimer = 0;
      this.target = null;
      this.targetPosition = null;
      this.mob.m_20242_(false);
   }

   public BossAerialDiveGoal.Phase getCurrentPhase() {
      return this.currentPhase;
   }

   public int getChaseTimer() {
      return this.chaseTimer;
   }

   private enum Phase {
      CHARGE,
      JUMP,
      BOOST,
      FLY,
      DIVE,
      TIMEOUT;
   }
}
