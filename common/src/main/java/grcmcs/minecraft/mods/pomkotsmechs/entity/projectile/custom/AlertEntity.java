package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AlertEntity extends ThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 60;
   private int lifeTicks = 0;

   public AlertEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      super(entityType, level);
      this.m_20242_(true);
      this.f_19811_ = true;
      this.f_19794_ = true;
   }

   public boolean m_6000_(double a, double b, double c) {
      return true;
   }

   public boolean m_6783_(double e) {
      return true;
   }

   public void m_8119_() {
      this.m_20242_(true);
      super.m_8119_();
      if (this.lifeTicks++ >= 60) {
         this.m_146870_();
      }
   }

   protected void m_8097_() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               "rotation",
               0,
               event -> {
                  if (this.lifeTicks < 10) {
                     return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.alert.idle"));
                  } else if (this.lifeTicks == 10) {
                     this.m_9236_()
                        .m_7785_(
                           this.m_20185_(), this.m_20186_(), this.m_20189_(), (SoundEvent)PomkotsMechs.SE_ALERT.get(), SoundSource.PLAYERS, 0.3F, 1.0F, false
                        );
                     return event.setAndContinue(RawAnimation.begin().thenPlay("animation.alert.start").thenLoop("animation.alert.alert"));
                  } else {
                     return this.lifeTicks == 50 ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.alert.end")) : PlayState.CONTINUE;
                  }
               }
            )
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
