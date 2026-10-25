package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BossDashAttackGoal extends BossDashGoal {
   public BossDashAttackGoal(
      BaseBossEntity mob, double dashSpeed, double maxRotationSpeed, double startDistance, double stopDistance, int maxDuration, double homingStrength
   ) {
      super(mob, dashSpeed, maxRotationSpeed, startDistance, stopDistance, maxDuration, homingStrength);
   }

   @Override
   public boolean canContinueToUse() {
      return this.target == null || !this.target.isAlive() ? false : this.dashTimer < this.maxDuration;
   }

   @Override
   public void start() {
      this.target = this.mob.getTarget();
      this.hasStarted = true;
      this.dashTimer = 0;
      this.initialYaw = this.mob.getYRot();
      Vec3 toTarget = this.target.position().subtract(this.mob.position()).normalize();
      this.dashDirection = new Vec3(toTarget.x, 0.0, toTarget.z).normalize();
      this.mob.triggerAnim("action_controller", "dash_attack");
   }

   @Override
   public void tick() {
      super.tick();
      if (this.dashTimer % 10 == 0) {
         this.dealDashDamage();
      }
   }

   protected void dealDashDamage() {
      Level level = this.mob.level();
      AABB damageArea = this.mob.getBoundingBox().inflate(10.0);

      for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, damageArea)) {
         if (!this.mob.isSelf(entity) && this.mob.distanceTo(entity) <= 20.0F) {
            entity.hurt(this.mob.damageSources().mobAttack(this.mob), this.mob.getMechData().meleeDamage);
            Vec3 knockback = entity.getDeltaMovement().normalize().scale(3.0);
            entity.knockback(10.0, -knockback.x, -knockback.y);
         }
      }
   }
}
