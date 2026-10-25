package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.SuwaItem;
import net.minecraft.resources.ResourceLocation;

public class SuwaItemModel extends BasePartsItemModel<SuwaItem> {
   public ResourceLocation getModelResource(SuwaItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/suwa.geo.json");
   }

   public ResourceLocation getAnimationResource(SuwaItem animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/suwa.animation.json");
   }
}
