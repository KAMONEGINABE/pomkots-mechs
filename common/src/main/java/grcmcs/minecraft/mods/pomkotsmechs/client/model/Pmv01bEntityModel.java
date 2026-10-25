package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import net.minecraft.resources.ResourceLocation;

public class Pmv01bEntityModel extends Pmv01EntityModel {
   @Override
   public ResourceLocation getAnimationResource(Pmv01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmv01b.animation.json");
   }

   @Override
   public ResourceLocation getModelResource(Pmv01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmv01b.geo.json");
   }

   @Override
   public ResourceLocation getTextureResource(Pmv01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pmv01b.png");
   }
}
