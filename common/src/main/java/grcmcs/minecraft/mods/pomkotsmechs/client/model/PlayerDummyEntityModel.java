package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PlayerDummyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlayerDummyEntityModel extends GeoModel<PlayerDummyEntity> {
   public ResourceLocation getAnimationResource(PlayerDummyEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/ossan.animation.json");
   }

   public ResourceLocation getModelResource(PlayerDummyEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/ossan.geo.json");
   }

   public ResourceLocation getTextureResource(PlayerDummyEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/ossan-genba.png");
   }
}
