package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.LargeElevatorEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.LargeElevatorEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class LargeElevatorEntityRenderer extends GeoEntityRenderer<LargeElevatorEntity> {
    public LargeElevatorEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new LargeElevatorEntityModel());
    }

    @Override
    public boolean shouldShowName(LargeElevatorEntity entity) {
        return false;
    }
}
