package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KasumiItem;
import net.minecraft.resources.ResourceLocation;

public class KasumiItemModel extends BasePartsItemModel<KasumiItem> {
   public ResourceLocation getModelResource(KasumiItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/kasumi.geo.json");
   }

   public ResourceLocation getAnimationResource(KasumiItem animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/kasumi.animation.json");
   }
}
