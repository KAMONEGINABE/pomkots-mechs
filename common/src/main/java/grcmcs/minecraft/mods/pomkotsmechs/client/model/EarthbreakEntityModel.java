package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthbreakEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EarthbreakEntityModel extends GeoModel<EarthbreakEntity> {
   public ResourceLocation getAnimationResource(EarthbreakEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/earthbreak.animation.json");
   }

   public ResourceLocation getModelResource(EarthbreakEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/earthbreak.geo.json");
   }

   public ResourceLocation getTextureResource(EarthbreakEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/earthbreak.png");
   }
}
