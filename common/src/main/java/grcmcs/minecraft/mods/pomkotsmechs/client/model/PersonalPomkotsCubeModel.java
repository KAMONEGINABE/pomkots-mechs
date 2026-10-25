package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.block.PersonalPomkotsCubeBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PersonalPomkotsCubeModel extends GeoModel<PersonalPomkotsCubeBlockEntity> {
   public ResourceLocation getModelResource(PersonalPomkotsCubeBlockEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pomkotscube.geo.json");
   }

   public ResourceLocation getTextureResource(PersonalPomkotsCubeBlockEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/block/personal_pomkots_cube.png");
   }

   public ResourceLocation getAnimationResource(PersonalPomkotsCubeBlockEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pomkotscube.animation.json");
   }
}
