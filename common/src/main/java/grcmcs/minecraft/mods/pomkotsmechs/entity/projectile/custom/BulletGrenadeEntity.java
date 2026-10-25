package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BulletGrenadeEntity extends PomkotsCustomThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private int explosionScale = 2;

   public BulletGrenadeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      this(entityType, level, null, 80, BattleBalance.MECH_BULLET_GRENADE_DAMAGE, 15);
   }

   public BulletGrenadeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, float damage) {
      this(entityType, level, shooter, 80, damage, 20);
   }

   public BulletGrenadeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, int maxLifeTicks, float damage, int stun) {
      super(entityType, level, shooter, maxLifeTicks, damage, stun);
   }

   public void setExplosionScale(int scale) {
      this.explosionScale = scale;
   }

   @Override
   public void m_8119_() {
      super.m_8119_();
      this.f_19812_ = true;
   }

   @Override
   protected void m_5790_(EntityHitResult entityHitResult) {
      this.createExplosion(entityHitResult.m_82450_());
      entityHitResult.m_82443_().f_19802_ = 0;
      super.m_5790_(entityHitResult);
   }

   @Override
   protected void m_8060_(BlockHitResult blockHitResult) {
      this.createExplosion(blockHitResult.m_82450_());
      super.m_8060_(blockHitResult);
   }

   private void createExplosion(Vec3 pos) {
      Level world = this.m_9236_();
      if (Utils.isBlockDestructionAllowed(this.shooter)) {
         Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, this.explosionScale, false, ExplosionInteraction.BLOCK, this.m_9236_());
      } else {
         Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, this.explosionScale, false, ExplosionInteraction.NONE, this.m_9236_());
      }
   }

   @Override
   public void m_142036_() {
      if (this.m_9236_().f_46443_) {
         Vec3 pos = this.m_20182_();
         if (!(this.explosionScale < 2.0F)) {
            this.m_9236_().m_7106_(ParticleTypes.f_123812_, pos.f_82479_, pos.f_82480_, pos.f_82481_, 1.0, 0.0, 0.0);
         } else {
            this.m_9236_().m_7106_(ParticleTypes.f_123813_, pos.f_82479_, pos.f_82480_, pos.f_82481_, 1.0, 0.0, 0.0);
         }
      }
   }

   private void addParticles(Vec3 offset) {
      Level world = this.m_9236_();

      for (int i = 0; i < 5; i++) {
         int rad = Math.abs(i - 3);

         for (int j = 0; j < 5 - rad; j++) {
            for (int k = 0; k < 5 - rad; k++) {
               world.m_7106_(
                  ParticleTypes.f_123813_,
                  offset.f_82479_ - (k - (3 - rad / 2)) * 2,
                  offset.f_82480_ - (i - 3) * 2,
                  offset.f_82481_ - (j - (3 - rad / 2)) * 2,
                  0.0,
                  0.0,
                  0.0
               );
            }
         }
      }
   }

   protected void m_8097_() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.grenade.idle")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
