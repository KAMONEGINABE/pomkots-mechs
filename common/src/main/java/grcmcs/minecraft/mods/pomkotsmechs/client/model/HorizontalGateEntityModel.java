package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.HorizontalGateEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class HorizontalGateEntityModel extends GeoModel<HorizontalGateEntity> {
    @Override public ResourceLocation getAnimationResource(HorizontalGateEntity entity) {
        return PomkotsMechs.id("animations/gate_horizontal.animation.json");
    }
    @Override public ResourceLocation getModelResource(HorizontalGateEntity entity) {
        return PomkotsMechs.id("geo/gate_horizontal.geo.json");
    }
    @Override public ResourceLocation getTextureResource(HorizontalGateEntity entity) {
        return PomkotsMechs.id("textures/entity/elevator_large.png");
    }
}
