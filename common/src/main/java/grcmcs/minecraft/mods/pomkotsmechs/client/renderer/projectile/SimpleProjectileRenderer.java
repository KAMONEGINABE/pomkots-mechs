package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** One-pass, full-bright renderer for simple projectile meshes. */
public class SimpleProjectileRenderer<T extends Entity> extends EntityRenderer<T> {
    private static final int FULL_BRIGHT = LightTexture.pack(15, 15);
    private final EntityModel<T> model;
    private final ResourceLocation texture;
    private final float scale;
    private final float lengthScale;
    private final boolean followsRotation;
    private final boolean usesVelocityYaw;

    protected SimpleProjectileRenderer(EntityRendererProvider.Context context,
                                       EntityModel<T> model,
                                       ResourceLocation texture,
                                       float scale,
                                       boolean followsRotation) {
        this(context, model, texture, scale, 1.0F, followsRotation, false);
    }

    protected SimpleProjectileRenderer(EntityRendererProvider.Context context,
                                       EntityModel<T> model,
                                       ResourceLocation texture,
                                       float scale,
                                       boolean followsRotation,
                                       boolean usesVelocityYaw) {
        this(context, model, texture, scale, 1.0F, followsRotation, usesVelocityYaw);
    }

    protected SimpleProjectileRenderer(EntityRendererProvider.Context context,
                                       EntityModel<T> model,
                                       ResourceLocation texture,
                                       float scale,
                                       float lengthScale,
                                       boolean followsRotation,
                                       boolean usesVelocityYaw) {
        super(context);
        this.model = model;
        this.texture = texture;
        this.scale = scale;
        this.lengthScale = lengthScale;
        this.followsRotation = followsRotation;
        this.usesVelocityYaw = usesVelocityYaw;
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
        MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        if (followsRotation) {
            Vec3 velocity = entity.getDeltaMovement();
            double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            float pitch = velocity.lengthSqr() > 1.0E-10D
                    ? (float) -Math.toDegrees(Math.atan2(velocity.y, horizontal))
                    : entity.getXRot();
            float yaw = usesVelocityYaw && velocity.lengthSqr() > 1.0E-10D
                    ? (float)Math.toDegrees(Math.atan2(velocity.x, velocity.z))
                    : entityYaw;
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        }
        // Apply the longitudinal scale in model-local space after aiming.
        poseStack.scale(scale, scale, scale * lengthScale);

        model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTick, 0.0F, 0.0F);
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        model.renderToBuffer(poseStack, buffer, FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, FULL_BRIGHT);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }
}
