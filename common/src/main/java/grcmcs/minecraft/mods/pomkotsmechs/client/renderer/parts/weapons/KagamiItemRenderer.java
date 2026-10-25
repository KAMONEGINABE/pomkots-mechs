package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.KagamiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagamiItem;

public class KagamiItemRenderer extends BasePartsItemRenderer.Weapon<KagamiItem> {
   public KagamiItemRenderer() {
      super(new KagamiItemModel());
   }
}
