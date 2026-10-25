package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.AlertEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.AlertRedEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AlertEntityModel extends GeoModel<AlertEntity> {
   public ResourceLocation getAnimationResource(AlertEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/alert.animation.json");
   }

   public ResourceLocation getModelResource(AlertEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/alert.geo.json");
   }

   public ResourceLocation getTextureResource(AlertEntity animatable) {
      return animatable instanceof AlertRedEntity
         ? new ResourceLocation("pomkotsmechs", "textures/entity/alertred.png")
         : new ResourceLocation("pomkotsmechs", "textures/entity/alert.png");
   }
}
