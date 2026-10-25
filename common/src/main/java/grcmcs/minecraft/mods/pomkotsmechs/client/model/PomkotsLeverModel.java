package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsLeverBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PomkotsLeverModel extends GeoModel<PomkotsLeverBlockEntity> {
   public ResourceLocation getModelResource(PomkotsLeverBlockEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/lever.geo.json");
   }

   public ResourceLocation getTextureResource(PomkotsLeverBlockEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/block/pomkotslever.png");
   }

   public ResourceLocation getAnimationResource(PomkotsLeverBlockEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/lever.animation.json");
   }
}
