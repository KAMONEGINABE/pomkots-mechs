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
      this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   @Override
   public boolean canUseInternal() {
      return !this.hasStarted;
   }

   public boolean m_8045_() {
      if (this.target != null && this.target.m_6084_()) {
         if (this.mob.m_20270_(this.target) <= this.stopDistance) {
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

   public void m_8056_() {
      this.target = this.mob.m_5448_();
      this.hasStarted = true;
      this.dashTimer = 0;
      this.initialYaw = this.mob.m_146908_();
      Vec3 toTarget = this.target.m_20182_().m_82546_(this.mob.m_20182_()).m_82541_();
      this.dashDirection = new Vec3(toTarget.f_82479_, 0.0, toTarget.f_82481_).m_82541_();
      this.mob.triggerAnim("action_controller", "dash");
   }

   public void m_8037_() {
      if (this.target != null && this.target.m_6084_()) {
         this.dashTimer++;
         Vec3 updatedDirection = this.calculateHomingDirection();
         Vec3 limitedDirection = this.applyRotationLimit(updatedDirection);
         Vec3 groundMovement = this.calculateGroundMovement(limitedDirection);
         this.mob.m_20256_(groundMovement);
         this.updateRotation(limitedDirection);
      }
   }

   protected Vec3 calculateHomingDirection() {
      Vec3 currentDirection = this.dashDirection;
      if (this.homingStrength > 0.0) {
         Vec3 toTarget = this.target.m_20182_().m_82546_(this.mob.m_20182_());
         Vec3 targetDirection = new Vec3(toTarget.f_82479_, 0.0, toTarget.f_82481_).m_82541_();
         Vec3 newDirection = currentDirection.m_82490_(1.0 - this.homingStrength).m_82549_(targetDirection.m_82490_(this.homingStrength)).m_82541_();
         this.dashDirection = newDirection;
      }

      return this.dashDirection;
   }

   protected Vec3 applyRotationLimit(Vec3 desiredDirection) {
      float currentYaw = this.mob.m_146908_();
      float desiredYaw = (float)(Mth.m_14136_(desiredDirection.f_82479_, desiredDirection.f_82481_) * (-180.0 / Math.PI));
      float angleDiff = Mth.m_14177_(desiredYaw - currentYaw);
      float limitedAngleDiff = Mth.m_14036_(angleDiff, -((float)this.maxRotationSpeed), (float)this.maxRotationSpeed);
      float limitedYaw = currentYaw + limitedAngleDiff;
      float radians = limitedYaw * (float) (Math.PI / 180.0);
      return new Vec3(-Math.sin(radians), 0.0, Math.cos(radians)).m_82541_();
   }

   protected Vec3 calculateGroundMovement(Vec3 horizontalDirection) {
      Vec3 currentPos = this.mob.m_20182_();
      Vec3 targetHorizontalPos = currentPos.m_82549_(horizontalDirection.m_82490_(this.dashSpeed));
      double groundY = this.findGroundLevel(targetHorizontalPos.f_82479_, targetHorizontalPos.f_82481_);
      double yMovement = this.calculateYMovement(currentPos.f_82480_, groundY);
      return new Vec3(horizontalDirection.f_82479_ * this.dashSpeed, yMovement, horizontalDirection.f_82481_ * this.dashSpeed);
   }

   protected double findGroundLevel(double x, double z) {
      Level level = this.mob.m_9236_();
      int startY = (int)this.mob.m_20186_();
      int searchRange = 30;

      for (int y = startY; y >= startY - searchRange; y--) {
         BlockPos pos = new BlockPos((int)x, y, (int)z);
         BlockState state = level.m_8055_(pos);
         if (!state.m_60795_() && state.m_60804_(level, pos)) {
            return y + 1.0;
         }
      }

      for (int y = startY + 1; y <= startY + searchRange; y++) {
         BlockPos pos = new BlockPos((int)x, y - 1, (int)z);
         BlockState state = level.m_8055_(pos);
         if (!state.m_60795_() && state.m_60804_(level, pos)) {
            return y;
         }
      }

      return this.mob.m_20186_() - 20.0;
   }

   private boolean canMoveToPosition(double x, double y, double z) {
      Level level = this.mob.m_9236_();
      AABB entityBB = this.mob.m_20191_();
      double width = entityBB.m_82362_();
      double height = entityBB.m_82376_();
      AABB targetBB = new AABB(x - width / 2.0, y, z - width / 2.0, x + width / 2.0, y + height, z + width / 2.0);
      return level.m_45756_(this.mob, targetBB);
   }

   protected double calculateYMovement(double currentY, double groundY) {
      double yDiff = groundY - currentY;
      double maxStepHeight = this.mob.m_274421_();
      if (yDiff > 0.0 && yDiff <= maxStepHeight) {
         return yDiff;
      } else if (yDiff > maxStepHeight) {
         return Math.min(0.5, yDiff * 0.3);
      } else {
         return yDiff < -0.1 ? this.applyGravity() : yDiff * 0.2;
      }
   }

   protected double applyGravity() {
      double currentVerticalVelocity = this.mob.m_20184_().f_82480_;
      double gravity = 0.08;
      return !this.mob.m_20096_() ? Math.max(currentVerticalVelocity - gravity, -1.5) : 0.0;
   }

   protected void updateRotation(Vec3 direction) {
      float desiredYaw = (float)(Mth.m_14136_(direction.f_82479_, direction.f_82481_) * (-180.0 / Math.PI));
      this.mob.m_146922_(desiredYaw);
      this.mob.f_20883_ = desiredYaw;
      this.mob.f_20885_ = desiredYaw;
   }

   private void performDashStartEffects() {
      if (this.mob.m_9236_() instanceof ServerLevel serverLevel) {
         Vec3 pos = this.mob.m_20182_();
         serverLevel.m_8767_(ParticleTypes.f_123796_, pos.f_82479_, pos.f_82480_ + 0.5, pos.f_82481_, 8, 0.5, 0.3, 0.5, 0.1);
         serverLevel.m_8767_(ParticleTypes.f_123759_, pos.f_82479_, pos.f_82480_, pos.f_82481_, 5, 0.3, 0.1, 0.3, 0.05);
      }
   }

   private void performDashEffects() {
      Level level = this.mob.m_9236_();
      Vec3 pos = this.mob.m_20182_();
      if (level instanceof ServerLevel serverLevel) {
         serverLevel.m_8767_(ParticleTypes.f_123766_, pos.f_82479_, pos.f_82480_ + this.mob.m_20206_() * 0.5, pos.f_82481_, 1, 0.2, 0.2, 0.2, 0.0);
         if (this.mob.m_20096_()) {
            serverLevel.m_8767_(ParticleTypes.f_123762_, pos.f_82479_, pos.f_82480_ + 0.1, pos.f_82481_, 2, 0.3, 0.1, 0.3, 0.02);
         }
      }
   }

   public void m_8041_() {
      this.hasStarted = false;
      this.dashTimer = 0;
      this.dashDirection = null;
      Vec3 currentMovement = this.mob.m_20184_();
      this.mob.m_20256_(currentMovement.m_82542_(0.3, 1.0, 0.3));
      this.mob.triggerAnim("action_controller", "stop");
   }

   private void performDashStopEffects() {
      Level level = this.mob.m_9236_();
      Vec3 pos = this.mob.m_20182_();
      if (level instanceof ServerLevel serverLevel) {
         serverLevel.m_8767_(ParticleTypes.f_123813_, pos.f_82479_, pos.f_82480_ + 0.5, pos.f_82481_, 3, 0.5, 0.3, 0.5, 0.0);
      }
   }

   public int getDashTimer() {
      return this.dashTimer;
   }

   public Vec3 getDashDirection() {
      return this.dashDirection;
   }

   public boolean isActive() {
      return this.hasStarted && this.m_8045_();
   }
}
