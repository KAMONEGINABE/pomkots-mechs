package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.ReusableLaserEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.misc.MissionMarkerRenderTypes;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.ReusableLaserEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class ReusableLaserEntityRenderer extends GeoEntityRenderer<ReusableLaserEntity> {
    public ReusableLaserEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new ReusableLaserEntityModel());
    }

    @Override
    public RenderType getRenderType(ReusableLaserEntity entity, net.minecraft.resources.ResourceLocation texture,
                                    MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public boolean shouldRender(ReusableLaserEntity entity, Frustum frustum,
                                double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    @Override
    public void render(ReusableLaserEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Vec3 vanillaPosition = entity.getPosition(partialTick);
        Vec3 renderPosition = entity.getInterpolatedRenderPosition(partialTick);
        Vec3 correction = renderPosition.subtract(vanillaPosition);
        poseStack.pushPose();
        poseStack.translate(correction.x, correction.y, correction.z);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    @Override
    public void actuallyRender(PoseStack poseStack, ReusableLaserEntity entity, BakedGeoModel model,
                               RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                               boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        GeoBone root = getGeoModel().getBone("root").orElse(null);
        Vec3 direction = entity.getInterpolatedRenderDirection(partialTick);
        if (root != null && direction.lengthSqr() >= 1.0E-8D) {
            direction = direction.normalize();
            double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
            root.setRotX((float) Mth.atan2(direction.y, horizontal));
            root.setRotY((float) Mth.atan2(direction.x, direction.z));
        }

        GeoBone beam = getGeoModel().getBone("beam").orElse(null);
        if (beam != null) {
            beam.setScaleZ(Math.max(0.001F, entity.getBeamLength()));
            float diameterScale = Math.max(0.001F,
                    entity.getBeamWidth() * 2.0F * entity.getVisualWidthScale(partialTick));
            beam.setScaleX(diameterScale);
            beam.setScaleY(diameterScale);
        }
        GeoBone core = getGeoModel().getBone("beam_core").orElse(null);
        GeoBone outer = getGeoModel().getBone("beam_outer").orElse(null);
        if (core != null) core.setHidden(false);
        // Temporary visual test: keep the translucent outer beam disabled.
        // Set this back to false to restore the red outer haze pass.
        if (outer != null) outer.setHidden(true);
        super.actuallyRender(poseStack, entity, model, renderType, bufferSource, buffer,
                isReRender, partialTick, 0xF000F0, packedOverlay, red, green, blue, alpha);
        if (core != null) core.setHidden(false);
        if (!isReRender) renderHalo(poseStack, bufferSource, entity, direction, partialTick);
    }

    private void renderHalo(PoseStack poseStack, MultiBufferSource bufferSource,
                            ReusableLaserEntity entity, Vec3 direction, float partialTick) {
        if (direction.lengthSqr() < 1.0E-8D || entity.getBeamLength() <= 0.0F) return;

        Vec3 origin = entity.getInterpolatedRenderPosition(partialTick);
        Vec3 toCamera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().subtract(origin);
        Vec3 side = direction.cross(toCamera);
        if (side.lengthSqr() < 1.0E-8D) side = direction.cross(new Vec3(0, 1, 0));
        if (side.lengthSqr() < 1.0E-8D) side = direction.cross(new Vec3(1, 0, 0));
        side = side.normalize();

        float widthScale = entity.getVisualWidthScale(partialTick);
        double innerRadius = entity.getBeamWidth() * 0.75D * widthScale;
        double outerRadius = innerRadius + Math.max(0.35D, entity.getBeamWidth() * 0.65D) * widthScale;
        Vec3 end = direction.scale(entity.getBeamLength());
        VertexConsumer halo = bufferSource.getBuffer(MissionMarkerRenderTypes.GLOWING_PANELS);
        int color = entity.getHaloColor();
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;

        renderHaloStrip(halo, poseStack, end, side.scale(innerRadius), side.scale(outerRadius), red, green, blue);
        renderHaloStrip(halo, poseStack, end, side.scale(-innerRadius), side.scale(-outerRadius), red, green, blue);
    }

    private static void renderHaloStrip(VertexConsumer buffer, PoseStack poseStack, Vec3 end,
                                        Vec3 inner, Vec3 outer, int red, int green, int blue) {
        var pose = poseStack.last().pose();
        // Outer edge is fully transparent; the faint color is concentrated at the core silhouette.
        buffer.vertex(pose, (float) outer.x, (float) outer.y, (float) outer.z)
                .color(red, green, blue, 0).endVertex();
        buffer.vertex(pose, (float) (end.x + outer.x), (float) (end.y + outer.y), (float) (end.z + outer.z))
                .color(red, green, blue, 0).endVertex();
        buffer.vertex(pose, (float) (end.x + inner.x), (float) (end.y + inner.y), (float) (end.z + inner.z))
                .color(red, green, blue, 72).endVertex();
        buffer.vertex(pose, (float) inner.x, (float) inner.y, (float) inner.z)
                .color(red, green, blue, 72).endVertex();
    }
}
