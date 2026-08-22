package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.KutaraItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KutaraItem;

public class KutaraItemRenderer extends BasePartsItemRenderer.Weapon<KutaraItem> {
    public KutaraItemRenderer() {
        super(new KutaraItemModel());
    }
}
