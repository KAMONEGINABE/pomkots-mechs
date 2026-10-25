package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.boosters;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.resources.ResourceLocation;

public class BoosterItemModel extends BasePartsItemModel<BasePartsItem.Booster> {
   public ResourceLocation getModelResource(BasePartsItem.Booster animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/" + animatable.getPartsSeriesName() + ".geo.json");
   }
}
