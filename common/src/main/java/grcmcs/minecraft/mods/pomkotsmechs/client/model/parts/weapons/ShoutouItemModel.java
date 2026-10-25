package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShoutouItem;
import net.minecraft.resources.ResourceLocation;

public class ShoutouItemModel extends BasePartsItemModel<ShoutouItem> {
   public ResourceLocation getModelResource(ShoutouItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/shoutou.geo.json");
   }
}
