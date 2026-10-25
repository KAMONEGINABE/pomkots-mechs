package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.GassanItem;
import net.minecraft.resources.ResourceLocation;

public class GassanItemModel extends BasePartsItemModel<GassanItem> {
   public ResourceLocation getModelResource(GassanItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/gassan.geo.json");
   }
}
