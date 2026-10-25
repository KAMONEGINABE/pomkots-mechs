package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.BiwaItem;
import net.minecraft.resources.ResourceLocation;

public class BiwaItemModel extends BasePartsItemModel<BiwaItem> {
   public ResourceLocation getModelResource(BiwaItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/biwa.geo.json");
   }

   public ResourceLocation getAnimationResource(BiwaItem animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/biwa.animation.json");
   }
}
