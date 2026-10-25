package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShinobazuItem;
import net.minecraft.resources.ResourceLocation;

public class ShinobazuItemModel extends BasePartsItemModel<ShinobazuItem> {
   public ResourceLocation getModelResource(ShinobazuItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/shinobazu.geo.json");
   }
}
