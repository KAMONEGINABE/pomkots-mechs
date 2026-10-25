package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.WaveHorizontalEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WaveHorizontalEntityModel extends GeoModel<WaveHorizontalEntity> {
   public ResourceLocation getAnimationResource(WaveHorizontalEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/wave_h.animation.json");
   }

   public ResourceLocation getModelResource(WaveHorizontalEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/wave_h.geo.json");
   }

   public ResourceLocation getTextureResource(WaveHorizontalEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/wave.png");
   }
}
