package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.SlashEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SlashEntityModel extends GeoModel<SlashEntity> {
   public ResourceLocation getAnimationResource(SlashEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/slash.animation.json");
   }

   public ResourceLocation getModelResource(SlashEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/slash.geo.json");
   }

   public ResourceLocation getTextureResource(SlashEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/slash.png");
   }
}
