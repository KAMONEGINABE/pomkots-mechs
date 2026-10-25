package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb01mk2Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmb01mk2EntityModel extends GeoModel<Pmb01mk2Entity> {
   public ResourceLocation getAnimationResource(Pmb01mk2Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmb01mk2.animation.json");
   }

   public ResourceLocation getModelResource(Pmb01mk2Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmb01mk2.geo.json");
   }

   public ResourceLocation getTextureResource(Pmb01mk2Entity animatable) {
      return animatable.getAiMode() == -2
         ? new ResourceLocation("pomkotsmechs", "textures/entity/pmb01inactive.png")
         : new ResourceLocation("pomkotsmechs", "textures/entity/pmb01.png");
   }
}
