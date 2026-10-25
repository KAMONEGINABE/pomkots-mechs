package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.ElevatorEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ElevetorEntityModel extends GeoModel<ElevatorEntity> {
   public ResourceLocation getAnimationResource(ElevatorEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/elevator.animation.json");
   }

   public ResourceLocation getModelResource(ElevatorEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/elevator.geo.json");
   }

   public ResourceLocation getTextureResource(ElevatorEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pms01.png");
   }
}
