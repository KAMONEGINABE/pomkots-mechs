package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.DaigomaruItem;
import net.minecraft.resources.ResourceLocation;

public class DaigomaruItemModel extends BasePartsItemModel<DaigomaruItem> {
   public ResourceLocation getModelResource(DaigomaruItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/daigomaru.geo.json");
   }
}
