package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.MashuItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.MashuItem;

public class MashuItemRenderer extends BasePartsItemRenderer.Weapon<MashuItem> {
   public MashuItemRenderer() {
      super(new MashuItemModel());
   }
}
