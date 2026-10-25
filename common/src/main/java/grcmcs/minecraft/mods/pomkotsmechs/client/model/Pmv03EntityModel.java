package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv03Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmv03EntityModel extends GeoModel<Pmv03Entity> {
   public ResourceLocation getAnimationResource(Pmv03Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmv03.animation.json");
   }

   public ResourceLocation getModelResource(Pmv03Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmv03.geo.json");
   }

   public ResourceLocation getTextureResource(Pmv03Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pmv03.png");
   }
}
