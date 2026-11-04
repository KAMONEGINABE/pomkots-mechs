package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.GrenadeLargeEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeLargeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GrenadeLargeEntityRenderer extends GeoEntityRenderer<GrenadeLargeEntity> {
    public GrenadeLargeEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GrenadeLargeEntityModel());
    }

    @Override
    public void actuallyRender(PoseStack poseStack, GrenadeLargeEntity animatable,
                               BakedGeoModel model, RenderType renderType,
                               MultiBufferSource bufferSource, VertexConsumer buffer,
                               boolean isReRender, float partialTick, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        // See GrenadeEntityRenderer: keep the projectile bright without using
        // GeckoLib's shared automatic glow-mask cache.
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource,
                buffer, isReRender, partialTick, LightTexture.FULL_BRIGHT,
                packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, GrenadeLargeEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.scaleHeight = 4;
        this.scaleWidth = 4;
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }
}
