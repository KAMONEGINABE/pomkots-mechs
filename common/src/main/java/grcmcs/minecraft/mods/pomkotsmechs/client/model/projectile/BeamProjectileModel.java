package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletBeamEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Lightweight crossed-plane reconstruction of the original bulletbeam Geo model. */
public class BeamProjectileModel extends EntityModel<BulletBeamEntity> {
    private float ageInTicks;

    @Override
    public void setupAnim(BulletBeamEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.ageInTicks = ageInTicks;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        float cycle = (ageInTicks % 10.0F) / 10.0F;
        float pulse = Mth.sin(cycle * Mth.TWO_PI);

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, -pulse * 0.125F);
        poseStack.scale(1.0F, 1.0F, 1.0F - Math.abs(pulse) * 0.1F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(345.0F * cycle));

        for (int i = 0; i < 5; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(i * 37.5F));
            drawBlade(poseStack.last(), buffer, packedLight, red, green, blue, alpha);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void drawBlade(PoseStack.Pose pose, VertexConsumer buffer, int light,
                                  float red, float green, float blue, float alpha) {
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float halfWidth = 1.5F / 16.0F;
        float front = -24.0F / 16.0F;
        float back = 23.0F / 16.0F;

        // The right/lower streak region in bulletbeam.png, normalized from 128 px.
        float u0 = 0.48F;
        float u1 = 1.0F;
        float v0 = 0.48F;
        float v1 = 0.90F;
        vertex(buffer, matrix, normal, -halfWidth, 0.0F, front, u0, v0, light, red, green, blue, alpha);
        vertex(buffer, matrix, normal,  halfWidth, 0.0F, front, u0, v1, light, red, green, blue, alpha);
        vertex(buffer, matrix, normal,  halfWidth, 0.0F, back,  u1, v1, light, red, green, blue, alpha);
        vertex(buffer, matrix, normal, -halfWidth, 0.0F, back,  u1, v0, light, red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, Matrix3f normal,
                               float x, float y, float z, float u, float v, int light,
                               float red, float green, float blue, float alpha) {
        buffer.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
