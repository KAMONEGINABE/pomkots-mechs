package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.TenpouItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TenpouItem;

public class TenpouItemRenderer extends BasePartsItemRenderer.Weapon<TenpouItem> {
   public TenpouItemRenderer() {
      super(new TenpouItemModel());
   }
}
