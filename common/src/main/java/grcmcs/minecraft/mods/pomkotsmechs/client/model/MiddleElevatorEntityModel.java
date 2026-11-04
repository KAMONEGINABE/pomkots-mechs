package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.MiddleElevatorEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class MiddleElevatorEntityModel extends GeoModel<MiddleElevatorEntity> {
    @Override
    public ResourceLocation getAnimationResource(MiddleElevatorEntity entity) {
        return PomkotsMechs.id("animations/elevator_middle.animation.json");
    }
    @Override
    public ResourceLocation getModelResource(MiddleElevatorEntity entity) {
        return PomkotsMechs.id("geo/elevator_middle.geo.json");
    }
    @Override
    public ResourceLocation getTextureResource(MiddleElevatorEntity entity) {
        return PomkotsMechs.id("textures/entity/elevator_large.png");
    }
}
