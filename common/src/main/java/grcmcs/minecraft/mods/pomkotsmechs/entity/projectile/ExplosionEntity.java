package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
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

public class ExplosionEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 50;
   private int lifeTicks = 0;
   private int explosionScale;

   public ExplosionEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      this(entityType, world, 0);
   }

   public ExplosionEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, int explosionScale) {
      super(entityType, world);
      this.m_20242_(true);
      this.explosionScale = explosionScale;
   }

   @Override
   public void m_8119_() {
      this.m_20242_(true);
      super.m_8119_();
      Level level = this.m_9236_();
      if (this.lifeTicks++ >= 50) {
         this.m_146870_();
      } else if (this.lifeTicks == 5) {
         this.generateLava();
         if (level.f_46443_) {
            this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EXPLOSION_EVENT.get());
         }
      } else if (this.lifeTicks == 4 && !level.f_46443_ && this.explosionScale > 0) {
         Vec3 pos = this.m_20182_();
         if (PomkotsMechs.CONFIG.enableEntityBlockDestruction) {
            Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, this.explosionScale, false, ExplosionInteraction.BLOCK, this.m_9236_());
         } else {
            Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, this.explosionScale, false, ExplosionInteraction.NONE, this.m_9236_());
         }
      }
   }

   private void generateLava() {
      if (this.m_9236_().f_46443_) {
         RandomSource random = this.m_9236_().m_213780_();

         for (int i = 0; i < 20; i++) {
            double velocityX = random.m_188500_() * 2.0 - 1.0;
            double velocityY = random.m_188500_() * 2.0 - 1.0;
            double velocityZ = random.m_188500_() * 2.0 - 1.0;
            this.m_9236_().m_7106_(ParticleTypes.f_123756_, this.m_20185_(), this.m_20186_(), this.m_20189_(), velocityX, velocityY, velocityZ);
         }
      }
   }

   protected void m_5790_(EntityHitResult entityHitResult) {
   }

   protected void m_8060_(BlockHitResult blockHitResult) {
   }

   protected void m_8097_() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.explosion.new")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
