package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TsurugiItem;
import net.minecraft.resources.ResourceLocation;

public class TsurugiItemModel extends BasePartsItemModel<TsurugiItem> {
   public ResourceLocation getModelResource(TsurugiItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/tsurugi.geo.json");
   }

   public ResourceLocation getAnimationResource(TsurugiItem animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/tsurugi.animation.json");
   }
}
