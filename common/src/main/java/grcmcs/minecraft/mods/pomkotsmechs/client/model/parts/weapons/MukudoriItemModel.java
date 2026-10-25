package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.MukudoriItem;
import net.minecraft.resources.ResourceLocation;

public class MukudoriItemModel extends BasePartsItemModel<MukudoriItem> {
   public ResourceLocation getModelResource(MukudoriItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/mukudori.geo.json");
   }
}
