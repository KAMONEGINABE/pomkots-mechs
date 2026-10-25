package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.RockLargeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RockLargeEntityModel extends GeoModel<RockLargeEntity> {
   public ResourceLocation getAnimationResource(RockLargeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/rocklarge.animation.json");
   }

   public ResourceLocation getModelResource(RockLargeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/rocklarge.geo.json");
   }

   public ResourceLocation getTextureResource(RockLargeEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/rock.png");
   }
}
