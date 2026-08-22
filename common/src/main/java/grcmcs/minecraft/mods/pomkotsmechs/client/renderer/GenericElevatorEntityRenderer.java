package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.GenericElevatorEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.GenericElevatorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class GenericElevatorEntityRenderer extends GeoEntityRenderer<GenericElevatorEntity> {
    public GenericElevatorEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new GenericElevatorEntityModel());
    }

    @Override
    public boolean shouldShowName(GenericElevatorEntity entity) {
        return false;
    }
}
