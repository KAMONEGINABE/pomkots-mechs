package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb99Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmb99EntityModel extends GeoModel<Pmb99Entity> {
   public ResourceLocation getAnimationResource(Pmb99Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmb99.animation.json");
   }

   public ResourceLocation getModelResource(Pmb99Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmb99.geo.json");
   }

   public ResourceLocation getTextureResource(Pmb99Entity animatable) {
      if (animatable.getAiMode() == -2) {
         return new ResourceLocation("pomkotsmechs", "textures/entity/pmb07inactive.png");
      } else {
         return animatable.getRiding()
            ? new ResourceLocation("pomkotsmechs", "textures/entity/pmb99red.png")
            : new ResourceLocation("pomkotsmechs", "textures/entity/pmb99.png");
      }
   }
}
