package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeLargeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GrenadeLargeEntityModel extends GeoModel<GrenadeLargeEntity> {
   public ResourceLocation getAnimationResource(GrenadeLargeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/grenade.animation.json");
   }

   public ResourceLocation getModelResource(GrenadeLargeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/grenade.geo.json");
   }

   public ResourceLocation getTextureResource(GrenadeLargeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/grenade.png");
   }
}
