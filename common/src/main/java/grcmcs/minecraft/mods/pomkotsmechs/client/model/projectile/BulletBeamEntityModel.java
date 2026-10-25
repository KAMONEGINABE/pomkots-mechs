package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletBeamEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletBeamEntityModel extends GeoModel<BulletBeamEntity> {
   public ResourceLocation getAnimationResource(BulletBeamEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/projectile/bulletbeam.animation.json");
   }

   public ResourceLocation getModelResource(BulletBeamEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/projectile/bulletbeam.geo.json");
   }

   public ResourceLocation getTextureResource(BulletBeamEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/projectile/bulletbeam.png");
   }
}
