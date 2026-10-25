package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms09Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pms09EntityModel extends GeoModel<Pms09Entity> {
   public ResourceLocation getAnimationResource(Pms09Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pms09.animation.json");
   }

   public ResourceLocation getModelResource(Pms09Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pms09.geo.json");
   }

   public ResourceLocation getTextureResource(Pms09Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pms01.png");
   }
}
