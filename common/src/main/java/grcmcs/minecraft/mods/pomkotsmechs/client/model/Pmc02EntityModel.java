package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.Pmc02Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmc02EntityModel extends GeoModel<Pmc02Entity> {
   public ResourceLocation getAnimationResource(Pmc02Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/bossbox.animation.json");
   }

   public ResourceLocation getModelResource(Pmc02Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/bossbox.geo.json");
   }

   public ResourceLocation getTextureResource(Pmc02Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/bossbox.png");
   }
}
