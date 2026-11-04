package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.GenericElevatorEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class GenericElevatorEntityModel extends GeoModel<GenericElevatorEntity> {
    @Override
    public ResourceLocation getAnimationResource(GenericElevatorEntity animatable) {
        return PomkotsMechs.id("animations/elevator.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(GenericElevatorEntity animatable) {
        return PomkotsMechs.id("geo/elevator.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GenericElevatorEntity animatable) {
        return PomkotsMechs.id("textures/entity/pms01.png");
    }
}
