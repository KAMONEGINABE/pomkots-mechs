package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb04Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmb04EntityModel extends GeoModel<Pmb04Entity> {
   public ResourceLocation getAnimationResource(Pmb04Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmb04.animation.json");
   }

   public ResourceLocation getModelResource(Pmb04Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmb04.geo.json");
   }

   public ResourceLocation getTextureResource(Pmb04Entity animatable) {
      return animatable.getAiMode() == -2
         ? new ResourceLocation("pomkotsmechs", "textures/entity/pmb01inactive.png")
         : new ResourceLocation("pomkotsmechs", "textures/entity/pmb04.png");
   }
}
