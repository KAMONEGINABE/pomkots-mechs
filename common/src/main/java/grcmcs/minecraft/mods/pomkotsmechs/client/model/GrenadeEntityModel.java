package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GrenadeEntityModel extends GeoModel<GrenadeEntity> {
   public ResourceLocation getAnimationResource(GrenadeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/grenade.animation.json");
   }

   public ResourceLocation getModelResource(GrenadeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/grenade.geo.json");
   }

   public ResourceLocation getTextureResource(GrenadeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/grenade.png");
   }
}
