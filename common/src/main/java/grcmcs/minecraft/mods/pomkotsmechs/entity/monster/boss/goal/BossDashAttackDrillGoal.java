package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BossDashAttackDrillGoal extends BossDashGoal {
   private int chargeTimer = 0;

   public BossDashAttackDrillGoal(
      BaseBossEntity mob, double dashSpeed, double maxRotationSpeed, double startDistance, double stopDistance, int maxDuration, double homingStrength
   ) {
      super(mob, dashSpeed, maxRotationSpeed, startDistance, stopDistance, maxDuration, homingStrength);
   }

   @Override
   public boolean m_8045_() {
      return this.target == null || !this.target.m_6084_() ? false : this.dashTimer < this.maxDuration;
   }

   @Override
   public void m_8056_() {
      this.target = this.mob.m_5448_();
      this.hasStarted = true;
      this.dashTimer = 0;
      this.initialYaw = this.mob.m_146908_();
      this.chargeTimer = 0;
      Vec3 toTarget = this.target.m_20182_().m_82546_(this.mob.m_20182_()).m_82541_();
      this.dashDirection = new Vec3(toTarget.f_82479_, 0.0, toTarget.f_82481_).m_82541_();
      this.mob.triggerAnim("action_controller", "dash_attack");
   }

   @Override
   public void m_8037_() {
      if (this.chargeTimer < 18) {
         this.chargeTimer++;
      } else {
         super.m_8037_();
         if (this.dashTimer % 5 == 0) {
            this.dealDashDamage();
         }

         if (this.dashTimer % 10 == 0) {
            this.breakBlocks();
         }
      }
   }

   protected void breakBlocks() {
      if (Utils.isBlockDestructionAllowed(this.mob)) {
         Utils.destroyBlockSphere(11, new Vec3(0.0, 11.0, 5.0), this.mob.m_20183_(), this.mob.m_146908_(), this.mob.m_9236_(), true);
      }
   }

   protected void dealDashDamage() {
      Level level = this.mob.m_9236_();
      AABB damageArea = this.mob.m_20191_().m_82400_(20.0);

      for (LivingEntity entity : level.m_45976_(LivingEntity.class, damageArea)) {
         if (!this.mob.isSelf(entity) && this.mob.m_20270_(entity) <= 20.0F) {
            entity.m_6469_(this.mob.m_269291_().m_269333_(this.mob), this.mob.getMechData().meleeDamage);
            Vec3 knockback = entity.m_20184_().m_82541_().m_82490_(3.0);
            entity.m_147240_(10.0, -knockback.f_82479_, -knockback.f_82480_);
         }
      }
   }
}
