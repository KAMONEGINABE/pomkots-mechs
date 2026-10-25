package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb03Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmb03EntityModel extends GeoModel<Pmb03Entity> {
   public ResourceLocation getAnimationResource(Pmb03Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmb03.animation.json");
   }

   public ResourceLocation getModelResource(Pmb03Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmb03.geo.json");
   }

   public ResourceLocation getTextureResource(Pmb03Entity animatable) {
      return animatable.getAiMode() == -2
         ? new ResourceLocation("pomkotsmechs", "textures/entity/pmb01inactive.png")
         : new ResourceLocation("pomkotsmechs", "textures/entity/pmb01.png");
   }
}
