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
      this.speed = Mth.m_14008_(speed, 0.1, 2.0);
      this.maxRushDuration = 200;
      this.m_7021_(EnumSet.of(Flag.MOVE));
   }

   public boolean m_8036_() {
      return this.isRushing ? true : this.hasTarget();
   }

   private boolean hasTarget() {
      return this.mob.m_5448_() != null && this.mob.m_5448_().m_6084_();
   }

   public boolean m_8045_() {
      return this.isRushing && this.rushDuration < this.maxRushDuration && this.mob.m_6084_() && this.hasTarget();
   }

   public void m_8056_() {
      if (!this.isRushing && this.hasTarget()) {
         this.initializeRush();
      }

      this.mob.m_20242_(true);
   }

   public void m_8041_() {
      this.isRushing = false;
      this.rushDirection = null;
      this.rushDuration = 0;
      this.mob.m_20256_(Vec3.f_82478_);
   }

   public void m_8037_() {
      if (!this.hasTarget()) {
         this.m_8041_();
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
         Vec3 mobPos = this.mob.m_20182_();
         Vec3 targetPos = this.mob.m_5448_().m_20182_();
         this.fixedYLevel = mobPos.f_82480_;
         Vec3 horizontalDirection = new Vec3(targetPos.f_82479_ - mobPos.f_82479_, 0.0, targetPos.f_82481_ - mobPos.f_82481_).m_82541_();
         this.rushDirection = horizontalDirection.m_82490_(this.speed);
         this.faceTarget(targetPos);
         this.isRushing = true;
         this.rushDuration = 0;
      }
   }

   private void executeRush() {
      if (this.rushDirection != null) {
         Vec3 currentPos = this.mob.m_20182_();
         Vec3 newVelocity = new Vec3(this.rushDirection.f_82479_, 0.0, this.rushDirection.f_82481_);
         this.mob.m_20256_(newVelocity);
         if (Math.abs(currentPos.f_82480_ - this.fixedYLevel) > 0.1) {
            this.mob.m_6034_(currentPos.f_82479_, this.fixedYLevel, currentPos.f_82481_);
         }

         this.maintainRushDirection();
      }
   }

   private void faceTarget(Vec3 targetPos) {
      Vec3 mobPos = this.mob.m_20182_();
      double deltaX = targetPos.f_82479_ - mobPos.f_82479_;
      double deltaZ = targetPos.f_82481_ - mobPos.f_82481_;
      float yaw = (float)(Mth.m_14136_(deltaZ, deltaX) * (180.0 / Math.PI)) - 90.0F;
      this.mob.m_146922_(yaw);
      this.mob.m_5616_(yaw);
      this.mob.f_19859_ = yaw;
      this.mob.f_20886_ = yaw;
   }

   private void maintainRushDirection() {
      if (this.rushDirection != null) {
         double deltaX = this.rushDirection.f_82479_;
         double deltaZ = this.rushDirection.f_82481_;
         float yaw = (float)(Mth.m_14136_(deltaZ, deltaX) * (180.0 / Math.PI)) - 90.0F;
         this.mob.m_146922_(yaw);
         this.mob.m_5616_(yaw);
      }
   }
}
