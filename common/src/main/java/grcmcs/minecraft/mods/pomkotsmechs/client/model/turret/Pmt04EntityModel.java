package grcmcs.minecraft.mods.pomkotsmechs.client.model.turret;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt04Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmt04EntityModel extends GeoModel<Pmt04Entity> {
   public ResourceLocation getAnimationResource(Pmt04Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmt04.animation.json");
   }

   public ResourceLocation getModelResource(Pmt04Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmt04.geo.json");
   }

   public ResourceLocation getTextureResource(Pmt04Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pms01.png");
   }
}
