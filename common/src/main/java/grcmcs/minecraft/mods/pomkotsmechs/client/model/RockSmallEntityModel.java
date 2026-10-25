package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.RockSmallEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RockSmallEntityModel extends GeoModel<RockSmallEntity> {
   public ResourceLocation getAnimationResource(RockSmallEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/rocksmall.animation.json");
   }

   public ResourceLocation getModelResource(RockSmallEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/rocksmall.geo.json");
   }

   public ResourceLocation getTextureResource(RockSmallEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/rock.png");
   }
}
