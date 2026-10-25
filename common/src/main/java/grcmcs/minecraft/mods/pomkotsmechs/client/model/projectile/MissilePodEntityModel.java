package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissilePodEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MissilePodEntityModel extends GeoModel<MissilePodEntity> {
   public ResourceLocation getAnimationResource(MissilePodEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/projectile/missilepod.animation.json");
   }

   public ResourceLocation getModelResource(MissilePodEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/projectile/missilepod.geo.json");
   }

   public ResourceLocation getTextureResource(MissilePodEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pmb02.png");
   }
}
