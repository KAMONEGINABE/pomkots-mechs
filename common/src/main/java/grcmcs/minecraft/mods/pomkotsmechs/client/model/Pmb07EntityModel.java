package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb07Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmb07EntityModel extends GeoModel<Pmb07Entity> {
   public ResourceLocation getAnimationResource(Pmb07Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmb07.animation.json");
   }

   public ResourceLocation getModelResource(Pmb07Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmb07.geo.json");
   }

   public ResourceLocation getTextureResource(Pmb07Entity animatable) {
      return animatable.getAiMode() == -2
         ? new ResourceLocation("pomkotsmechs", "textures/entity/pmb07inactive.png")
         : new ResourceLocation("pomkotsmechs", "textures/entity/pmb07.png");
   }
}
