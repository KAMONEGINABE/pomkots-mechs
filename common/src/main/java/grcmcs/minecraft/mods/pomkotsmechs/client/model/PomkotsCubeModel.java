package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PomkotsCubeModel extends GeoModel<PomkotsCubeBlockEntity> {
   public ResourceLocation getModelResource(PomkotsCubeBlockEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pomkotscube.geo.json");
   }

   public ResourceLocation getTextureResource(PomkotsCubeBlockEntity animatable) {
      switch (animatable.getMode()) {
         case 0:
            return new ResourceLocation("pomkotsmechs", "textures/block/pomkotscube.png");
         case 1:
            return new ResourceLocation("pomkotsmechs", "textures/block/pomkotscubeyellow.png");
         case 2:
            return new ResourceLocation("pomkotsmechs", "textures/block/pomkotscubered.png");
         case 3:
            return new ResourceLocation("pomkotsmechs", "textures/block/pomkotscubepurple.png");
         default:
            return new ResourceLocation("pomkotsmechs", "textures/block/pomkotscube.png");
      }
   }

   public ResourceLocation getAnimationResource(PomkotsCubeBlockEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pomkotscube.animation.json");
   }
}
