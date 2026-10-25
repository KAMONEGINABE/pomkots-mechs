package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.HitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class MissileEnemyLargeEntity extends MissileBaseEntity {
   public final float defaultScale = 10.0F;

   public MissileEnemyLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      super(entityType, world);
   }

   public MissileEnemyLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
      super(entityType, world, shooter, null);
   }

   @Override
   protected void m_5790_(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.m_82443_();
      if (entity instanceof LivingEntity) {
         if (entity.equals(this.shooter) || entity instanceof HitBoxEntity) {
            return;
         }

         entity.m_6469_(entity.m_269291_().m_269390_(this, (Entity)(this.m_19749_() != null ? this.m_19749_() : this)), this.getDamage());
         entity.f_19802_ = 0;
      }

      this.createExplosion(this.m_20182_());
      this.m_146870_();
   }

   @Override
   protected int getSwitchTick() {
      return 5;
   }

   @Override
   protected double getSpeed() {
      return 2.0;
   }

   @Override
   protected int getSeekRange() {
      return 100;
   }

   @Override
   protected Class getTargetClass() {
      return Player.class;
   }

   @Override
   protected Vec3 getNonTargetVelocity() {
      return this.m_20184_();
   }

   @Override
   protected float getMaxRotationAnglePerTick() {
      return 5.0F;
   }

   @Override
   protected float getDamage() {
      return 10.0F;
   }

   @Override
   public float getScale() {
      return 10.0F;
   }

   @Override
   protected int getMaxLifeTicks() {
      return 300;
   }

   @Override
   protected void createExplosion(Vec3 pos) {
      Level world = this.m_9236_();
      if (!world.f_46443_) {
         ExplosionInteraction exp = ProjectileUtil.isDestructionAllowed(this) ? ExplosionInteraction.BLOCK : ExplosionInteraction.NONE;
         Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, 8.0F, false, exp, this.m_9236_());
         ExplosionEntity e = new ExplosionEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOSION.get(), world);
         e.m_146884_(this.m_20182_());
         world.m_7967_(e);
      }
   }
}
