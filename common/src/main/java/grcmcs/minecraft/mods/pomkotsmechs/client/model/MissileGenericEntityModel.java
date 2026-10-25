package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MissileGenericEntityModel extends GeoModel<MissileGenericEntity> {
   public ResourceLocation getAnimationResource(MissileGenericEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/missile.animation.json");
   }

   public ResourceLocation getModelResource(MissileGenericEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/missile.geo.json");
   }

   public ResourceLocation getTextureResource(MissileGenericEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/missile.png");
   }
}
