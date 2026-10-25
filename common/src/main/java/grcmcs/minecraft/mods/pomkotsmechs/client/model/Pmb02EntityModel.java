package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb02Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmb02EntityModel extends GeoModel<Pmb02Entity> {
   public ResourceLocation getAnimationResource(Pmb02Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmb02.animation.json");
   }

   public ResourceLocation getModelResource(Pmb02Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmb02.geo.json");
   }

   public ResourceLocation getTextureResource(Pmb02Entity animatable) {
      return animatable.getAiMode() == -2
         ? new ResourceLocation("pomkotsmechs", "textures/entity/pmb01inactive.png")
         : new ResourceLocation("pomkotsmechs", "textures/entity/pmb02.png");
   }
}
