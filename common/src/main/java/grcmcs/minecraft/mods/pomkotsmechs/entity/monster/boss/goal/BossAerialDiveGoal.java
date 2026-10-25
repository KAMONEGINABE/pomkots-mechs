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
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
   }

   @Override
   public boolean canUseInternal() {
      this.target = this.mob.getTarget();
      if (this.target != null) {
         double heightDiff = this.target.getY() - this.mob.getY();
         if (heightDiff >= this.requiredHeightDifference) {
            Entity vehicle = this.target.getVehicle();
            return vehicle != null ? vehicle.onGround() : this.target.onGround();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean canContinueToUse() {
      return this.target != null
         && this.target.isAlive()
         && (this.currentPhase != BossAerialDiveGoal.Phase.DIVE || !this.mob.onGround())
         && this.currentPhase != BossAerialDiveGoal.Phase.TIMEOUT;
   }

   public void start() {
      this.currentPhase = BossAerialDiveGoal.Phase.CHARGE;
      this.phaseTimer = 0;
      this.chaseTimer = 0;
      if (this.target != null) {
         this.targetPosition = this.target.position();
      }
   }

   public void tick() {
      if (this.target != null && this.target.isAlive()) {
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
      this.mob.setDeltaMovement(jumpVec);
      this.mob.setNoGravity(true);
      this.mob.hasImpulse = true;
   }

   private void handleJumpPhase() {
      if (this.mob.getDeltaMovement().y <= 0.0) {
         this.currentPhase = BossAerialDiveGoal.Phase.BOOST;
         this.phaseTimer = 0;
      }
   }

   private void handleBoostPhase() {
      Vec3 currentMovement = this.mob.getDeltaMovement();
      Vec3 boostMovement = new Vec3(currentMovement.x * 0.9, this.boosterSpeed, currentMovement.z * 0.9);
      this.mob.setDeltaMovement(boostMovement);
      if (this.mob.getY() > this.target.getY() + 5.0 || this.phaseTimer > 60) {
         this.currentPhase = BossAerialDiveGoal.Phase.FLY;
         this.phaseTimer = 0;
         this.targetPosition = this.target.position().add(0.0, 2.0, 0.0);
      }
   }

   private void handleFlyPhase() {
      this.targetPosition = this.target.position().add(0.0, 2.0, 0.0);
      Vec3 toTarget = this.targetPosition.subtract(this.mob.position());
      Vec3 horizontalDir = new Vec3(toTarget.x, 0.0, toTarget.z).normalize();
      double currentY = this.mob.getY();
      double targetY = this.target.getY() + 8.0;
      double verticalAdjustment = (targetY - currentY) * 0.1;
      Vec3 flyMovement = horizontalDir.scale(this.flySpeed).add(0.0, verticalAdjustment, 0.0);
      this.mob.setDeltaMovement(flyMovement);
      double horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
      if (horizontalDistance < 15.0 || this.phaseTimer > 80) {
         this.currentPhase = BossAerialDiveGoal.Phase.DIVE;
         this.phaseTimer = 0;
      }
   }

   private void handleDivePhase() {
      this.mob.setNoGravity(false);
      if (this.mob.onGround() || this.isNearGround()) {
         this.mob.getActionController().getAction("onground").tryAction();
      }
   }

   private void handleTimeoutPhase() {
      this.mob.setNoGravity(false);
   }

   private void updateRotation() {
      Vec3 toTarget = this.target.position().subtract(this.mob.position());
      float desiredYaw = (float)(Mth.atan2(toTarget.x, toTarget.z) * (-180.0 / Math.PI));
      this.mob.setYRot(this.rotLerp(this.mob.getYRot(), desiredYaw, 5.0F));
      this.mob.yBodyRot = this.mob.getYRot();
      if (this.currentPhase == BossAerialDiveGoal.Phase.FLY || this.currentPhase == BossAerialDiveGoal.Phase.BOOST) {
         double horizontalDist = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
         float desiredPitch = (float)(Math.atan2(-toTarget.y, horizontalDist) * (180.0 / Math.PI));
         this.mob.setXRot(this.rotLerp(this.mob.getXRot(), desiredPitch, 3.0F));
      }
   }

   private float rotLerp(float current, float target, float maxChange) {
      float delta = Mth.wrapDegrees(target - current);
      if (delta > maxChange) {
         delta = maxChange;
      }

      if (delta < -maxChange) {
         delta = -maxChange;
      }

      return current + delta;
   }

   private boolean isNearGround() {
      BlockPos belowPos = new BlockPos((int)this.mob.getX(), (int)(this.mob.getY() - this.diveSpeed), (int)this.mob.getZ());
      return !this.mob.level().getBlockState(belowPos).isAir();
   }

   public void stop() {
      this.currentPhase = BossAerialDiveGoal.Phase.CHARGE;
      this.phaseTimer = 0;
      this.chaseTimer = 0;
      this.target = null;
      this.targetPosition = null;
      this.mob.setNoGravity(false);
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
