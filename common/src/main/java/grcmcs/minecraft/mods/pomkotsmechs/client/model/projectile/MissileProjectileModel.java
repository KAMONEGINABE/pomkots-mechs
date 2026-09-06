package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/** Lightweight missile body plus a procedurally animated, full-bright exhaust. */
public class MissileProjectileModel<T extends Entity> extends EntityModel<T> {
    private static final float FLAME_U0 = 46.0F / 64.0F;
    private static final float FLAME_U1 = 58.0F / 64.0F;
    private static final float FLAME_V0 = 29.5F / 64.0F;
    private static final float FLAME_V1 = 59.5F / 64.0F;
    private static final float FLAME_HALF_WIDTH = 2.14265F / 16.0F;
    private static final float FLAME_BASE_LENGTH = 14.28432F / 16.0F;

    private final ModelPart body;

    public MissileProjectileModel(ModelPart root) {
        this.body = root.getChild("body");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(0, 14)
                        .addBox(-3.0F, -2.5F, -4.0F, 5.0F, 5.0F, 9.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        // The original Gecko model's body UV faces the opposite local Z direction.
        // Flip only the body; the procedural exhaust is already on the correct tail.
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.popPose();
    }

    public void renderFlame(PoseStack poseStack, VertexConsumer buffer, int fullBright,
                            float ageInTicks) {
        float widthScale;
        float lengthScale;
        float rotation;

        if (ageInTicks < 15.0F) {
            float grow = Mth.clamp(ageInTicks / 4.166F, 0.0F, 1.0F);
            float settle = Mth.clamp((ageInTicks - 4.166F) / 10.834F, 0.0F, 1.0F);
            widthScale = Mth.lerp(smooth(grow), 1.0F, 2.3F);
            lengthScale = grow < 1.0F
                    ? Mth.lerp(smooth(grow), 0.7F, 5.1F)
                    : Mth.lerp(smooth(settle), 5.1F, 3.4F);
            rotation = ageInTicks * 24.0F;
        } else {
            float cycle = (ageInTicks - 15.0F) % 10.0F;
            widthScale = 2.3F;
            lengthScale = 3.4F + 0.8F * Mth.sin((float) Math.PI * cycle / 10.0F);
            rotation = cycle * 36.0F;
        }

        poseStack.pushPose();
        // The missile model points toward +Z, so the exhaust starts at the -Z tail.
        poseStack.translate(-0.5F / 16.0F, 0.0F, -4.0F / 16.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));

        for (int i = 0; i < 4; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(i * 45.0F));
            drawFlameQuad(poseStack.last(), buffer,
                    FLAME_HALF_WIDTH * widthScale,
                    FLAME_BASE_LENGTH * lengthScale,
                    fullBright);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static float smooth(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    private static void drawFlameQuad(PoseStack.Pose pose, VertexConsumer buffer,
                                      float halfWidth, float length, int light) {
        vertex(buffer, pose, -halfWidth, 0.0F, 0.0F, FLAME_U0, FLAME_V0, light);
        vertex(buffer, pose, halfWidth, 0.0F, 0.0F, FLAME_U1, FLAME_V0, light);
        vertex(buffer, pose, halfWidth, 0.0F, -length, FLAME_U1, FLAME_V1, light);
        vertex(buffer, pose, -halfWidth, 0.0F, -length, FLAME_U0, FLAME_V1, light);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose,
                               float x, float y, float z, float u, float v, int light) {
        buffer.vertex(pose.pose(), x, y, z)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(pose.normal(), 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
