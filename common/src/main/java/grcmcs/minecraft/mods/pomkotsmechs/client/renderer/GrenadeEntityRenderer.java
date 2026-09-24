package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.GrenadeEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GrenadeEntityRenderer extends GeoEntityRenderer<GrenadeEntity> {
    public GrenadeEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GrenadeEntityModel());
    }

    @Override
    public void actuallyRender(PoseStack poseStack, GrenadeEntity animatable,
                               BakedGeoModel model, RenderType renderType,
                               MultiBufferSource bufferSource, VertexConsumer buffer,
                               boolean isReRender, float partialTick, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        // Do not use AutoGlowingGeoLayer here. Its glow-mask lookup shares the
        // grenade base texture with the lightweight projectile renderer and can
        // poison GeckoLib's texture/render-type cache on Forge.
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource,
                buffer, isReRender, partialTick, LightTexture.FULL_BRIGHT,
                packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, GrenadeEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.scaleHeight = 2;
        this.scaleWidth = 2;
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }
}
