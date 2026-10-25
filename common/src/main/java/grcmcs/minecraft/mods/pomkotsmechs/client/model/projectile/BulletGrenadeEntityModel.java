package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletGrenadeEntityModel extends GeoModel<BulletGrenadeEntity> {
   public ResourceLocation getAnimationResource(BulletGrenadeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/grenade.animation.json");
   }

   public ResourceLocation getModelResource(BulletGrenadeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/grenade.geo.json");
   }

   public ResourceLocation getTextureResource(BulletGrenadeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/grenade.png");
   }
}
