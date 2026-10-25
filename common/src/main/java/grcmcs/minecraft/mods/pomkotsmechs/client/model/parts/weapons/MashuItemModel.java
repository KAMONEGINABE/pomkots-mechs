package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.MashuItem;
import net.minecraft.resources.ResourceLocation;

public class MashuItemModel extends BasePartsItemModel<MashuItem> {
   public ResourceLocation getModelResource(MashuItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/mashu.geo.json");
   }
}
