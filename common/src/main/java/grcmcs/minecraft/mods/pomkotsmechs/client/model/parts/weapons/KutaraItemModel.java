package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KutaraItem;
import net.minecraft.resources.ResourceLocation;

public class KutaraItemModel extends BasePartsItemModel<KutaraItem> {
    @Override
    public ResourceLocation getModelResource(KutaraItem animatable) {
        return PomkotsMechs.id("geo/kutara.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(KutaraItem animatable) {
        return PomkotsMechs.id("animations/kutara.animation.json");
    }
}
