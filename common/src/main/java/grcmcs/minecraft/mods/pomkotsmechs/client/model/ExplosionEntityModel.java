package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ExplosionEntityModel extends GeoModel<ExplosionEntity> {
   public ResourceLocation getAnimationResource(ExplosionEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/explosion_low.animation.json");
   }

   public ResourceLocation getModelResource(ExplosionEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/explosion_low.geo.json");
   }

   public ResourceLocation getTextureResource(ExplosionEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/explosion.png");
   }
}
