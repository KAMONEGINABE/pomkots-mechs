package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KawasemiItem;
import net.minecraft.resources.ResourceLocation;

public class KawasemiItemModel extends BasePartsItemModel<KawasemiItem> {
   public ResourceLocation getModelResource(KawasemiItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/kawasemi.geo.json");
   }
}
