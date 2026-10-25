package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms10Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pms10EntityModel extends GeoModel<Pms10Entity> {
   public ResourceLocation getAnimationResource(Pms10Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pms10.animation.json");
   }

   public ResourceLocation getModelResource(Pms10Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pms10.geo.json");
   }

   public ResourceLocation getTextureResource(Pms10Entity animatable) {
      if (animatable.getMode() == Pms10Entity.MODE_BLUE) {
         return new ResourceLocation("pomkotsmechs", "textures/entity/pms01blue.png");
      } else {
         return animatable.getMode() == Pms10Entity.MODE_RED
            ? new ResourceLocation("pomkotsmechs", "textures/entity/pms01.png")
            : new ResourceLocation("pomkotsmechs", "textures/entity/pms01yellow.png");
      }
   }
}
