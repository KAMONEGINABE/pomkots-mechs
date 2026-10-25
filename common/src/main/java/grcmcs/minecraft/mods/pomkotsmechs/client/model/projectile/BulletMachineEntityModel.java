package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletMachineEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletMachineEntityModel extends GeoModel<BulletMachineEntity> {
   public ResourceLocation getAnimationResource(BulletMachineEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/projectile/bulletmachine.animation.json");
   }

   public ResourceLocation getModelResource(BulletMachineEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/projectile/bulletmachine.geo.json");
   }

   public ResourceLocation getTextureResource(BulletMachineEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/projectile/bulletmachine.png");
   }
}
