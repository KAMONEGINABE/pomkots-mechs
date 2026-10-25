package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeLargeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletGrenadeLargeEntityModel extends GeoModel<BulletGrenadeLargeEntity> {
   public ResourceLocation getAnimationResource(BulletGrenadeLargeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/grenade.animation.json");
   }

   public ResourceLocation getModelResource(BulletGrenadeLargeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/grenade.geo.json");
   }

   public ResourceLocation getTextureResource(BulletGrenadeLargeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/grenade.png");
   }
}
