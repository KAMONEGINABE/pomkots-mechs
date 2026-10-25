package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.MitakeItem;
import net.minecraft.resources.ResourceLocation;

public class MitakeItemModel extends BasePartsItemModel<MitakeItem> {
   public ResourceLocation getModelResource(MitakeItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/mitake.geo.json");
   }
}
