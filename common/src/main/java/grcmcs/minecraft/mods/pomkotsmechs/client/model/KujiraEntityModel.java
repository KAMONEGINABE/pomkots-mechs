package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.KujiraEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KujiraEntityModel extends GeoModel<KujiraEntity> {
   public ResourceLocation getAnimationResource(KujiraEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/kujira.animation.json");
   }

   public ResourceLocation getModelResource(KujiraEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/kujira.geo.json");
   }

   public ResourceLocation getTextureResource(KujiraEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/kujira.png");
   }
}
