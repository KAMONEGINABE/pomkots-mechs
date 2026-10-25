package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;

public abstract class PomkotsCustomDecayedThrowableProjectile extends PomkotsCustomThrowableProjectile {
   public PomkotsCustomDecayedThrowableProjectile(
      EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, int maxLifeTicks, float damage, int stun
   ) {
      super(entityType, world, shooter, maxLifeTicks, damage, stun);
   }

   @Override
   protected float calcFinalDamage(float baseDamage) {
      int finalDistance = this.calcFinalDistance(this.lifeTicks);
      PomkotsCustomThrowableProjectile.RangeCategory range = this.calcRangeCategory(finalDistance);
      return baseDamage * this.getDamageModifier(range);
   }

   protected int calcFinalDistance(int forwardingTicks) {
      return (int)(forwardingTicks * this.initialSpeed);
   }

   protected PomkotsCustomThrowableProjectile.RangeCategory calcRangeCategory(int distance) {
      return PomkotsCustomThrowableProjectile.RangeCategory.OPTIMAL;
   }

   protected float getDamageModifier(PomkotsCustomThrowableProjectile.RangeCategory range) {
      return 1.0F;
   }
}
