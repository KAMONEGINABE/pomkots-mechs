package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.BiwaItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.BiwaItem;

public class BiwaItemRenderer extends BasePartsItemRenderer.Weapon<BiwaItem> {
   public BiwaItemRenderer() {
      super(new BiwaItemModel());
   }
}
