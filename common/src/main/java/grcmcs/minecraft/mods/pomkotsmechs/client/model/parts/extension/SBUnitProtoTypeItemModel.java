package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.resources.ResourceLocation;

public class SBUnitProtoTypeItemModel extends BasePartsItemModel<BasePartsItem.Extension> {
   public ResourceLocation getModelResource(BasePartsItem.Extension animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/protosbunit.geo.json");
   }
}
