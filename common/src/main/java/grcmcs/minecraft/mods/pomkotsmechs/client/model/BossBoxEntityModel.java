package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BossBoxEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BossBoxEntityModel extends GeoModel<BossBoxEntity> {
   public ResourceLocation getAnimationResource(BossBoxEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/bossbox.animation.json");
   }

   public ResourceLocation getModelResource(BossBoxEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/bossbox.geo.json");
   }

   public ResourceLocation getTextureResource(BossBoxEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/bossbox.png");
   }
}
