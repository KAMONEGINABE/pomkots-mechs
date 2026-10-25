package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb08Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmb08EntityModel extends GeoModel<Pmb08Entity> {
   public ResourceLocation getAnimationResource(Pmb08Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmb08.animation.json");
   }

   public ResourceLocation getModelResource(Pmb08Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmb08.geo.json");
   }

   public ResourceLocation getTextureResource(Pmb08Entity animatable) {
      return animatable.getAiMode() == -2
         ? new ResourceLocation("pomkotsmechs", "textures/entity/pmb01inactive.png")
         : new ResourceLocation("pomkotsmechs", "textures/entity/pmb01.png");
   }
}
