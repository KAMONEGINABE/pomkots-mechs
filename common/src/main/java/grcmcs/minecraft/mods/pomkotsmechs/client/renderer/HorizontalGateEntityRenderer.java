package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.HorizontalGateEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.HorizontalGateEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class HorizontalGateEntityRenderer extends GeoEntityRenderer<HorizontalGateEntity> {
    public HorizontalGateEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HorizontalGateEntityModel());
    }
    @Override public boolean shouldShowName(HorizontalGateEntity entity) { return false; }
}
