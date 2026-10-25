package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.Pmc01Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmc01EntityModel extends GeoModel<Pmc01Entity> {
   public ResourceLocation getAnimationResource(Pmc01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmc01.animation.json");
   }

   public ResourceLocation getModelResource(Pmc01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmc01.geo.json");
   }

   public ResourceLocation getTextureResource(Pmc01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pmc01.png");
   }
}
