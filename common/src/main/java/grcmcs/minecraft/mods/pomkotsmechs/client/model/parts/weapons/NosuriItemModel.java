package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.NosuriItem;
import net.minecraft.resources.ResourceLocation;

public class NosuriItemModel extends BasePartsItemModel<NosuriItem> {
   public ResourceLocation getModelResource(NosuriItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/nosuri.geo.json");
   }
}
