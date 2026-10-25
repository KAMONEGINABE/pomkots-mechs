package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagenobuItem;
import net.minecraft.resources.ResourceLocation;

public class KagenobuItemModel extends BasePartsItemModel<KagenobuItem> {
   public ResourceLocation getModelResource(KagenobuItem animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/kagenobu.geo.json");
   }

   public ResourceLocation getAnimationResource(KagenobuItem animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/kagenobu.animation.json");
   }
}
