package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;

/** Machine-gun projectile box with the same per-face UV region as the old Geo model. */
public class MachineProjectileModel<T extends Entity> extends EntityModel<T> {
    private static final float X0 = -5.0F / 16.0F;
    private static final float X1 = 5.0F / 16.0F;
    private static final float Y0 = -5.0F / 16.0F;
    private static final float Y1 = 5.0F / 16.0F;
    private static final float Z0 = -16.0F / 16.0F;
    private static final float Z1 = 15.0F / 16.0F;
    private static final float U0 = 6.0F / 16.0F;
    private static final float U1 = 11.0F / 16.0F;
    private static final float V0 = 3.0F / 16.0F;
    private static final float V1 = 7.0F / 16.0F;

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        quad(buffer, pose, X0, Y0, Z0, X1, Y0, Z0, X1, Y1, Z0, X0, Y1, Z0, 0, 0, -1, packedLight, red, green, blue, alpha);
        quad(buffer, pose, X1, Y0, Z1, X0, Y0, Z1, X0, Y1, Z1, X1, Y1, Z1, 0, 0, 1, packedLight, red, green, blue, alpha);
        quad(buffer, pose, X0, Y0, Z1, X0, Y0, Z0, X0, Y1, Z0, X0, Y1, Z1, -1, 0, 0, packedLight, red, green, blue, alpha);
        quad(buffer, pose, X1, Y0, Z0, X1, Y0, Z1, X1, Y1, Z1, X1, Y1, Z0, 1, 0, 0, packedLight, red, green, blue, alpha);
        quad(buffer, pose, X0, Y0, Z1, X1, Y0, Z1, X1, Y0, Z0, X0, Y0, Z0, 0, -1, 0, packedLight, red, green, blue, alpha);
        quad(buffer, pose, X0, Y1, Z0, X1, Y1, Z0, X1, Y1, Z1, X0, Y1, Z1, 0, 1, 0, packedLight, red, green, blue, alpha);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3,
                             float nx, float ny, float nz, int light,
                             float red, float green, float blue, float alpha) {
        vertex(buffer, pose, x0, y0, z0, U0, V1, nx, ny, nz, light, red, green, blue, alpha);
        vertex(buffer, pose, x1, y1, z1, U1, V1, nx, ny, nz, light, red, green, blue, alpha);
        vertex(buffer, pose, x2, y2, z2, U1, V0, nx, ny, nz, light, red, green, blue, alpha);
        vertex(buffer, pose, x3, y3, z3, U0, V0, nx, ny, nz, light, red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose,
                               float x, float y, float z, float u, float v,
                               float nx, float ny, float nz, int light,
                               float red, float green, float blue, float alpha) {
        buffer.vertex(pose.pose(), x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(pose.normal(), nx, ny, nz)
                .endVertex();
    }
}
