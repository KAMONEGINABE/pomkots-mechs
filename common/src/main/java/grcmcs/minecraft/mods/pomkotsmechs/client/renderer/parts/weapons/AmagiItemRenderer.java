package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.AmagiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.AmagiItem;

public class AmagiItemRenderer extends BasePartsItemRenderer.Weapon<AmagiItem> {
   public AmagiItemRenderer() {
      super(new AmagiItemModel());
   }
}
