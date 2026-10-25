package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.JinbaItem;
import net.minecraft.resources.ResourceLocation;

public class JinbaItemModel extends BasePartsItemModel<JinbaItem> {
   public ResourceLocation getModelResource(JinbaItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/jinba.geo.json");
   }

   public ResourceLocation getAnimationResource(JinbaItem animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/jinba.animation.json");
   }
}
