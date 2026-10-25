package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv02Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmv02EntityModel extends GeoModel<Pmv02Entity> {
   public ResourceLocation getAnimationResource(Pmv02Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmv02.animation.json");
   }

   public ResourceLocation getModelResource(Pmv02Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmv02.geo.json");
   }

   public ResourceLocation getTextureResource(Pmv02Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pmv02.png");
   }
}
