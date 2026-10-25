package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthraiseEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EarthraiseEntityModel extends GeoModel<EarthraiseEntity> {
   public ResourceLocation getAnimationResource(EarthraiseEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/earthraise.animation.json");
   }

   public ResourceLocation getModelResource(EarthraiseEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/earthraise.geo.json");
   }

   public ResourceLocation getTextureResource(EarthraiseEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/earthraise.png");
   }
}
