package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MineEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MineEntityModel extends GeoModel<MineEntity> {
   public ResourceLocation getAnimationResource(MineEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/mine.animation.json");
   }

   public ResourceLocation getModelResource(MineEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/mine.geo.json");
   }

   public ResourceLocation getTextureResource(MineEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pms01.png");
   }
}
