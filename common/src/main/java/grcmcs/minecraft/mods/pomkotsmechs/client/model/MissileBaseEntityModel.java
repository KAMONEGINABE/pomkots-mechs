package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileBaseEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MissileBaseEntityModel extends GeoModel<MissileBaseEntity> {
   public ResourceLocation getAnimationResource(MissileBaseEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/missile.animation.json");
   }

   public ResourceLocation getModelResource(MissileBaseEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/missile.geo.json");
   }

   public ResourceLocation getTextureResource(MissileBaseEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/missile.png");
   }
}
