package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;

public class BulletGrenadeLargeEntity extends BulletGrenadeEntity {
   public static float scale = 6.0F;

   public BulletGrenadeLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      this(entityType, level, null, 80, BattleBalance.MECH_BULLET_GRENADE_DAMAGE, 15);
   }

   public BulletGrenadeLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, float damage) {
      this(entityType, level, shooter, 80, damage, 20);
   }

   public BulletGrenadeLargeEntity(
      EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, int maxLifeTicks, float damage, int stun
   ) {
      super(entityType, level, shooter, maxLifeTicks, damage, stun);
   }
}
