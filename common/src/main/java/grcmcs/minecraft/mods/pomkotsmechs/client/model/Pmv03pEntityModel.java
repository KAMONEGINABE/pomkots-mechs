package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv03pEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmv03pEntityModel extends GeoModel<Pmv03pEntity> {
   public ResourceLocation getAnimationResource(Pmv03pEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmv03p.animation.json");
   }

   public ResourceLocation getModelResource(Pmv03pEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmv03p.geo.json");
   }

   public ResourceLocation getTextureResource(Pmv03pEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pmv03p.png");
   }
}
