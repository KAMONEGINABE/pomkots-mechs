package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;

public class MissileGenericLargeEntity extends MissileGenericEntity {
   public final float defaultScale = 10.0F;

   public MissileGenericLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      this(entityType, world, null, null);
   }

   public MissileGenericLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target) {
      this(entityType, world, shooter, target, BattleBalance.MECH_MISSILE_DAMAGE, 1.0F);
   }

   public MissileGenericLargeEntity(
      EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target, float damage, float speed
   ) {
      super(entityType, world, shooter, target, damage, speed);
   }

   @Override
   public float getScale() {
      return 10.0F;
   }

   @Override
   protected int getSwitchTick() {
      return 5;
   }

   @Override
   protected float getMaxRotationAnglePerTick() {
      return 5.0F;
   }

   @Override
   protected void createExplosion(Vec3 pos) {
      Level world = this.m_9236_();
      if (!world.f_46443_) {
         if (Utils.isBlockDestructionAllowed(this.shooter)) {
            Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, 12.0F, false, ExplosionInteraction.BLOCK, this.m_9236_());
         } else {
            Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, 12.0F, false, ExplosionInteraction.NONE, this.m_9236_());
         }

         ExplosionEntity e = new ExplosionEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOSION.get(), world);
         e.m_146884_(this.m_20182_());
         world.m_7967_(e);
      }
   }
}
