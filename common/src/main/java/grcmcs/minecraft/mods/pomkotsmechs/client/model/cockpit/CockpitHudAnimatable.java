package grcmcs.minecraft.mods.pomkotsmechs.client.model.cockpit;

import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CockpitHudAnimatable implements GeoAnimatable {
   protected final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "attack", 2, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.cockpit.idle")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public double getTick(Object object) {
      return 0.0;
   }
}
