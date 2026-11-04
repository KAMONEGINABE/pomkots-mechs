package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.LargeElevatorEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class LargeElevatorEntityModel extends GeoModel<LargeElevatorEntity> {
    @Override
    public ResourceLocation getAnimationResource(LargeElevatorEntity entity) {
        return PomkotsMechs.id("animations/elevator_large.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(LargeElevatorEntity entity) {
        return PomkotsMechs.id("geo/elevator_large.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LargeElevatorEntity entity) {
        return PomkotsMechs.id("textures/entity/elevator_large.png");
    }
}
