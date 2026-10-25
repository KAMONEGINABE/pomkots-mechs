package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagamiItem;
import net.minecraft.resources.ResourceLocation;

public class KagamiItemModel extends BasePartsItemModel<KagamiItem> {
   public ResourceLocation getModelResource(KagamiItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/kagami.geo.json");
   }
}
