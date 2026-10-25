package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.AmagiItem;
import net.minecraft.resources.ResourceLocation;

public class AmagiItemModel extends BasePartsItemModel<AmagiItem> {
   public ResourceLocation getModelResource(AmagiItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/amagi.geo.json");
   }

   public ResourceLocation getAnimationResource(AmagiItem animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/amagi.animation.json");
   }
}
