package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletRifleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletRifleEntityModel extends GeoModel<BulletRifleEntity> {
   public ResourceLocation getAnimationResource(BulletRifleEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/projectile/bulletrifle.animation.json");
   }

   public ResourceLocation getModelResource(BulletRifleEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/projectile/bulletrifle.geo.json");
   }

   public ResourceLocation getTextureResource(BulletRifleEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/projectile/bulletrifle.png");
   }
}
