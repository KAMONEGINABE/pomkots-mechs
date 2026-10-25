package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmv01EntityModel extends GeoModel<Pmv01Entity> {
   public ResourceLocation getAnimationResource(Pmv01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmv01.animation.json");
   }

   public ResourceLocation getModelResource(Pmv01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmv01.geo.json");
   }

   public ResourceLocation getTextureResource(Pmv01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pmv01.png");
   }
}
