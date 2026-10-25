package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.SenzokuItem;
import net.minecraft.resources.ResourceLocation;

public class SenzokuItemModel extends BasePartsItemModel<SenzokuItem> {
   public ResourceLocation getModelResource(SenzokuItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/senzoku.geo.json");
   }
}
