package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;
import net.minecraft.resources.ResourceLocation;

public class ShakujiItemModel extends BasePartsItemModel<ShakujiItem> {
   public ResourceLocation getModelResource(ShakujiItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/shakuji.geo.json");
   }
}
