package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.UguisuItem;
import net.minecraft.resources.ResourceLocation;

public class UguisuItemModel extends BasePartsItemModel<UguisuItem> {
   public ResourceLocation getModelResource(UguisuItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/uguisu.geo.json");
   }
}
