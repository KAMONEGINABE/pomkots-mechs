package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.MiddleElevatorEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.MiddleElevatorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class MiddleElevatorEntityRenderer extends GeoEntityRenderer<MiddleElevatorEntity> {
    public MiddleElevatorEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new MiddleElevatorEntityModel());
    }
    @Override
    public boolean shouldShowName(MiddleElevatorEntity entity) { return false; }
}
