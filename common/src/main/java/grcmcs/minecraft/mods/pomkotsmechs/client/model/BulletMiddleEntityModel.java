package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletMiddleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletMiddleEntityModel extends GeoModel<BulletMiddleEntity> {
   public ResourceLocation getAnimationResource(BulletMiddleEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/bulletmiddle.animation.json");
   }

   public ResourceLocation getModelResource(BulletMiddleEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/bulletmiddle.geo.json");
   }

   public ResourceLocation getTextureResource(BulletMiddleEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/bulletmiddle.png");
   }
}
