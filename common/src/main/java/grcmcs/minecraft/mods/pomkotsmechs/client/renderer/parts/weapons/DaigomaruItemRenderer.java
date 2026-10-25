package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.DaigomaruItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.DaigomaruItem;

public class DaigomaruItemRenderer extends BasePartsItemRenderer.Weapon<DaigomaruItem> {
   public DaigomaruItemRenderer() {
      super(new DaigomaruItemModel());
   }
}
