package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.phys.Vec3;

public class StraightRushGoal extends Goal {
   private final Mob mob;
   private final double speed;
   private Vec3 rushDirection;
   private double fixedYLevel;
   private boolean isRushing;
   private int rushDuration;
   private final int maxRushDuration;
   private static final double MIN_SPEED = 0.1;
   private static final double MAX_SPEED = 2.0;
   private static final int DEFAULT_MAX_RUSH_DURATION = 200;

   public StraightRushGoal(Mob mob, double speed) {
      this.mob = mob;
      this.speed = Mth.clamp(speed, 0.1, 2.0);
      this.maxRushDuration = 200;
      this.setFlags(EnumSet.of(Flag.MOVE));
   }

   public boolean canUse() {
      return this.isRushing ? true : this.hasTarget();
   }

   private boolean hasTarget() {
      return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
   }

   public boolean canContinueToUse() {
      return this.isRushing && this.rushDuration < this.maxRushDuration && this.mob.isAlive() && this.hasTarget();
   }

   public void start() {
      if (!this.isRushing && this.hasTarget()) {
         this.initializeRush();
      }

      this.mob.setNoGravity(true);
   }

   public void stop() {
      this.isRushing = false;
      this.rushDirection = null;
      this.rushDuration = 0;
      this.mob.setDeltaMovement(Vec3.ZERO);
   }

   public void tick() {
      if (!this.hasTarget()) {
         this.stop();
      } else {
         if (!this.isRushing) {
            this.initializeRush();
         }

         if (this.isRushing && this.rushDirection != null) {
            this.executeRush();
            this.rushDuration++;
         }
      }
   }

   private void initializeRush() {
      if (this.hasTarget()) {
         Vec3 mobPos = this.mob.position();
         Vec3 targetPos = this.mob.getTarget().position();
         this.fixedYLevel = mobPos.y;
         Vec3 horizontalDirection = new Vec3(targetPos.x - mobPos.x, 0.0, targetPos.z - mobPos.z).normalize();
         this.rushDirection = horizontalDirection.scale(this.speed);
         this.faceTarget(targetPos);
         this.isRushing = true;
         this.rushDuration = 0;
      }
   }

   private void executeRush() {
      if (this.rushDirection != null) {
         Vec3 currentPos = this.mob.position();
         Vec3 newVelocity = new Vec3(this.rushDirection.x, 0.0, this.rushDirection.z);
         this.mob.setDeltaMovement(newVelocity);
         if (Math.abs(currentPos.y - this.fixedYLevel) > 0.1) {
            this.mob.setPos(currentPos.x, this.fixedYLevel, currentPos.z);
         }

         this.maintainRushDirection();
      }
   }

   private void faceTarget(Vec3 targetPos) {
      Vec3 mobPos = this.mob.position();
      double deltaX = targetPos.x - mobPos.x;
      double deltaZ = targetPos.z - mobPos.z;
      float yaw = (float)(Mth.atan2(deltaZ, deltaX) * (180.0 / Math.PI)) - 90.0F;
      this.mob.setYRot(yaw);
      this.mob.setYHeadRot(yaw);
      this.mob.yRotO = yaw;
      this.mob.yHeadRotO = yaw;
   }

   private void maintainRushDirection() {
      if (this.rushDirection != null) {
         double deltaX = this.rushDirection.x;
         double deltaZ = this.rushDirection.z;
         float yaw = (float)(Mth.atan2(deltaZ, deltaX) * (180.0 / Math.PI)) - 90.0F;
         this.mob.setYRot(yaw);
         this.mob.setYHeadRot(yaw);
      }
   }
}
