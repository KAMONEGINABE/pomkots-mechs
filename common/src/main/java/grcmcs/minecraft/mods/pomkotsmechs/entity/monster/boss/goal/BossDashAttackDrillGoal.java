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
   public boolean canContinueToUse() {
      return this.target == null || !this.target.isAlive() ? false : this.dashTimer < this.maxDuration;
   }

   @Override
   public void start() {
      this.target = this.mob.getTarget();
      this.hasStarted = true;
      this.dashTimer = 0;
      this.initialYaw = this.mob.getYRot();
      this.chargeTimer = 0;
      Vec3 toTarget = this.target.position().subtract(this.mob.position()).normalize();
      this.dashDirection = new Vec3(toTarget.x, 0.0, toTarget.z).normalize();
      this.mob.triggerAnim("action_controller", "dash_attack");
   }

   @Override
   public void tick() {
      if (this.chargeTimer < 18) {
         this.chargeTimer++;
      } else {
         super.tick();
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
         Utils.destroyBlockSphere(11, new Vec3(0.0, 11.0, 5.0), this.mob.blockPosition(), this.mob.getYRot(), this.mob.level(), true);
      }
   }

   protected void dealDashDamage() {
      Level level = this.mob.level();
      AABB damageArea = this.mob.getBoundingBox().inflate(20.0);

      for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, damageArea)) {
         if (!this.mob.isSelf(entity) && this.mob.distanceTo(entity) <= 20.0F) {
            entity.hurt(this.mob.damageSources().mobAttack(this.mob), this.mob.getMechData().meleeDamage);
            Vec3 knockback = entity.getDeltaMovement().normalize().scale(3.0);
            entity.knockback(10.0, -knockback.x, -knockback.y);
         }
      }
   }
}
