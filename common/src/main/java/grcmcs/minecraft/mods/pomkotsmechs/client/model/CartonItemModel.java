package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.items.CartonItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CartonItemModel extends GeoModel<CartonItem> {
   public ResourceLocation getAnimationResource(CartonItem animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/carton.animation.json");
   }

   public ResourceLocation getTextureResource(CartonItem animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/item/carton.png");
   }

   public ResourceLocation getModelResource(CartonItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/carton.geo.json");
   }
}
