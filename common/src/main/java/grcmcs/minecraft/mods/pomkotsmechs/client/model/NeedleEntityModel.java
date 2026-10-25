package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.NeedleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NeedleEntityModel extends GeoModel<NeedleEntity> {
   public ResourceLocation getAnimationResource(NeedleEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/needle.animation.json");
   }

   public ResourceLocation getModelResource(NeedleEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/needle.geo.json");
   }

   public ResourceLocation getTextureResource(NeedleEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/needle.png");
   }
}
