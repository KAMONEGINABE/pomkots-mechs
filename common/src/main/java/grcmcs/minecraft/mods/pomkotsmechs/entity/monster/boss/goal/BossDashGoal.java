package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BossDashGoal extends BaseBossGoal {
   protected final double dashSpeed;
   protected final double maxRotationSpeed;
   protected final double startDistance;
   protected final double stopDistance;
   protected final int maxDuration;
   protected final double homingStrength;
   protected LivingEntity target;
   protected Vec3 dashDirection;
   protected int dashTimer;
   protected float initialYaw;
   protected boolean hasStarted;

   public BossDashGoal(
      BaseBossEntity mob, double dashSpeed, double maxRotationSpeed, double startDistance, double stopDistance, int maxDuration, double homingStrength
   ) {
      super(mob);
      this.dashSpeed = dashSpeed;
      this.maxRotationSpeed = maxRotationSpeed;
      this.startDistance = startDistance;
      this.stopDistance = stopDistance;
      this.maxDuration = maxDuration;
      this.homingStrength = Math.max(0.0, Math.min(1.0, homingStrength));
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   @Override
   public boolean canUseInternal() {
      return !this.hasStarted;
   }

   public boolean canContinueToUse() {
      if (this.target != null && this.target.isAlive()) {
         if (this.mob.distanceTo(this.target) <= this.stopDistance) {
            int s = this.dashTimer % 2;
            if (s == 0) {
               this.mob.getActionController().getAction("saber_tate").tryAction();
            } else {
               this.mob.getActionController().getAction("saber_circle").tryAction();
            }

            return false;
         } else {
            return this.dashTimer < this.maxDuration;
         }
      } else {
         return false;
      }
   }

   public void start() {
      this.target = this.mob.getTarget();
      this.hasStarted = true;
      this.dashTimer = 0;
      this.initialYaw = this.mob.getYRot();
      Vec3 toTarget = this.target.position().subtract(this.mob.position()).normalize();
      this.dashDirection = new Vec3(toTarget.x, 0.0, toTarget.z).normalize();
      this.mob.triggerAnim("action_controller", "dash");
   }

   public void tick() {
      if (this.target != null && this.target.isAlive()) {
         this.dashTimer++;
         Vec3 updatedDirection = this.calculateHomingDirection();
         Vec3 limitedDirection = this.applyRotationLimit(updatedDirection);
         Vec3 groundMovement = this.calculateGroundMovement(limitedDirection);
         this.mob.setDeltaMovement(groundMovement);
         this.updateRotation(limitedDirection);
      }
   }

   protected Vec3 calculateHomingDirection() {
      Vec3 currentDirection = this.dashDirection;
      if (this.homingStrength > 0.0) {
         Vec3 toTarget = this.target.position().subtract(this.mob.position());
         Vec3 targetDirection = new Vec3(toTarget.x, 0.0, toTarget.z).normalize();
         Vec3 newDirection = currentDirection.scale(1.0 - this.homingStrength).add(targetDirection.scale(this.homingStrength)).normalize();
         this.dashDirection = newDirection;
      }

      return this.dashDirection;
   }

   protected Vec3 applyRotationLimit(Vec3 desiredDirection) {
      float currentYaw = this.mob.getYRot();
      float desiredYaw = (float)(Mth.atan2(desiredDirection.x, desiredDirection.z) * (-180.0 / Math.PI));
      float angleDiff = Mth.wrapDegrees(desiredYaw - currentYaw);
      float limitedAngleDiff = Mth.clamp(angleDiff, -((float)this.maxRotationSpeed), (float)this.maxRotationSpeed);
      float limitedYaw = currentYaw + limitedAngleDiff;
      float radians = limitedYaw * (float) (Math.PI / 180.0);
      return new Vec3(-Math.sin(radians), 0.0, Math.cos(radians)).normalize();
   }

   protected Vec3 calculateGroundMovement(Vec3 horizontalDirection) {
      Vec3 currentPos = this.mob.position();
      Vec3 targetHorizontalPos = currentPos.add(horizontalDirection.scale(this.dashSpeed));
      double groundY = this.findGroundLevel(targetHorizontalPos.x, targetHorizontalPos.z);
      double yMovement = this.calculateYMovement(currentPos.y, groundY);
      return new Vec3(horizontalDirection.x * this.dashSpeed, yMovement, horizontalDirection.z * this.dashSpeed);
   }

   protected double findGroundLevel(double x, double z) {
      Level level = this.mob.level();
      int startY = (int)this.mob.getY();
      int searchRange = 30;

      for (int y = startY; y >= startY - searchRange; y--) {
         BlockPos pos = new BlockPos((int)x, y, (int)z);
         BlockState state = level.getBlockState(pos);
         if (!state.isAir() && state.isSolidRender(level, pos)) {
            return y + 1.0;
         }
      }

      for (int y = startY + 1; y <= startY + searchRange; y++) {
         BlockPos pos = new BlockPos((int)x, y - 1, (int)z);
         BlockState state = level.getBlockState(pos);
         if (!state.isAir() && state.isSolidRender(level, pos)) {
            return y;
         }
      }

      return this.mob.getY() - 20.0;
   }

   private boolean canMoveToPosition(double x, double y, double z) {
      Level level = this.mob.level();
      AABB entityBB = this.mob.getBoundingBox();
      double width = entityBB.getXsize();
      double height = entityBB.getYsize();
      AABB targetBB = new AABB(x - width / 2.0, y, z - width / 2.0, x + width / 2.0, y + height, z + width / 2.0);
      return level.noCollision(this.mob, targetBB);
   }

   protected double calculateYMovement(double currentY, double groundY) {
      double yDiff = groundY - currentY;
      double maxStepHeight = this.mob.maxUpStep();
      if (yDiff > 0.0 && yDiff <= maxStepHeight) {
         return yDiff;
      } else if (yDiff > maxStepHeight) {
         return Math.min(0.5, yDiff * 0.3);
      } else {
         return yDiff < -0.1 ? this.applyGravity() : yDiff * 0.2;
      }
   }

   protected double applyGravity() {
      double currentVerticalVelocity = this.mob.getDeltaMovement().y;
      double gravity = 0.08;
      return !this.mob.onGround() ? Math.max(currentVerticalVelocity - gravity, -1.5) : 0.0;
   }

   protected void updateRotation(Vec3 direction) {
      float desiredYaw = (float)(Mth.atan2(direction.x, direction.z) * (-180.0 / Math.PI));
      this.mob.setYRot(desiredYaw);
      this.mob.yBodyRot = desiredYaw;
      this.mob.yHeadRot = desiredYaw;
   }

   private void performDashStartEffects() {
      if (this.mob.level() instanceof ServerLevel serverLevel) {
         Vec3 pos = this.mob.position();
         serverLevel.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y + 0.5, pos.z, 8, 0.5, 0.3, 0.5, 0.1);
         serverLevel.sendParticles(ParticleTypes.POOF, pos.x, pos.y, pos.z, 5, 0.3, 0.1, 0.3, 0.05);
      }
   }

   private void performDashEffects() {
      Level level = this.mob.level();
      Vec3 pos = this.mob.position();
      if (level instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, pos.x, pos.y + this.mob.getBbHeight() * 0.5, pos.z, 1, 0.2, 0.2, 0.2, 0.0);
         if (this.mob.onGround()) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y + 0.1, pos.z, 2, 0.3, 0.1, 0.3, 0.02);
         }
      }
   }

   public void stop() {
      this.hasStarted = false;
      this.dashTimer = 0;
      this.dashDirection = null;
      Vec3 currentMovement = this.mob.getDeltaMovement();
      this.mob.setDeltaMovement(currentMovement.multiply(0.3, 1.0, 0.3));
      this.mob.triggerAnim("action_controller", "stop");
   }

   private void performDashStopEffects() {
      Level level = this.mob.level();
      Vec3 pos = this.mob.position();
      if (level instanceof ServerLevel serverLevel) {
         serverLevel.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y + 0.5, pos.z, 3, 0.5, 0.3, 0.5, 0.0);
      }
   }

   public int getDashTimer() {
      return this.dashTimer;
   }

   public Vec3 getDashDirection() {
      return this.dashDirection;
   }

   public boolean isActive() {
      return this.hasStarted && this.canContinueToUse();
   }
}
