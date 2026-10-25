package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletEntityModel extends GeoModel<BulletEntity> {
   public ResourceLocation getAnimationResource(BulletEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/bullet.animation.json");
   }

   public ResourceLocation getModelResource(BulletEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/bullet.geo.json");
   }

   public ResourceLocation getTextureResource(BulletEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/bullet.png");
   }
}
