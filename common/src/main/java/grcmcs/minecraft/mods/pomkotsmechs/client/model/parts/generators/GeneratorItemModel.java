package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.generators;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.resources.ResourceLocation;

public class GeneratorItemModel extends BasePartsItemModel<BasePartsItem.Generator> {
   public ResourceLocation getModelResource(BasePartsItem.Generator animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/" + animatable.getPartsSeriesName() + ".geo.json");
   }
}
