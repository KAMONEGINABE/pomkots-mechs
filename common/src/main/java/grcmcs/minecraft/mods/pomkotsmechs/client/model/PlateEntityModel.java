package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PlateEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlateEntityModel extends GeoModel<PlateEntity> {
   public ResourceLocation getAnimationResource(PlateEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/plate.animation.json");
   }

   public ResourceLocation getModelResource(PlateEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/plate.geo.json");
   }

   public ResourceLocation getTextureResource(PlateEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/bossbox.png");
   }
}
