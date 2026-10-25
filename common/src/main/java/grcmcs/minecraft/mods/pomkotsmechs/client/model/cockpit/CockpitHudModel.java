package grcmcs.minecraft.mods.pomkotsmechs.client.model.cockpit;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CockpitHudModel extends GeoModel<CockpitHudAnimatable> {
   public ResourceLocation getAnimationResource(CockpitHudAnimatable animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/cockpit.animation.json");
   }

   public ResourceLocation getModelResource(CockpitHudAnimatable animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/cockpit.geo.json");
   }

   public ResourceLocation getTextureResource(CockpitHudAnimatable animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/cockpit/cockpit.png");
   }
}
