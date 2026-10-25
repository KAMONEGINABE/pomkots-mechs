package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmvc01EntityModel extends GeoModel<Pmvc01Entity> {
   public ResourceLocation getAnimationResource(Pmvc01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmvc01.animation.json");
   }

   public ResourceLocation getModelResource(Pmvc01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmvc01.geo.json");
   }

   public ResourceLocation getTextureResource(Pmvc01Entity animatable) {
      return animatable.canWork(false) && !animatable.isBroken()
         ? new ResourceLocation("pomkotsmechs", "textures/entity/pmvc01.png")
         : new ResourceLocation("pomkotsmechs", "textures/entity/pmvc01stop.png");
   }
}
