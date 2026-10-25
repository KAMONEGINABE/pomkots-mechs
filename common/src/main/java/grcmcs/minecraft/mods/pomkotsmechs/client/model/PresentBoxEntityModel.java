package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PresentBoxEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PresentBoxEntityModel extends GeoModel<PresentBoxEntity> {
   public ResourceLocation getAnimationResource(PresentBoxEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/presentbox.animation.json");
   }

   public ResourceLocation getModelResource(PresentBoxEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/presentbox.geo.json");
   }

   public ResourceLocation getTextureResource(PresentBoxEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/presentbox.png");
   }
}
