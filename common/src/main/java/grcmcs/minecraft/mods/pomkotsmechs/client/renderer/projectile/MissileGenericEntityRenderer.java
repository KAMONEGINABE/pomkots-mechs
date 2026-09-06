package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile.MissileProjectileModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Lightweight one-buffer renderer for generic missiles. */
public class MissileGenericEntityRenderer extends EntityRenderer<MissileGenericEntity> {
    private static final ResourceLocation TEXTURE =
            PomkotsMechs.id("textures/entity/missile2.png");
    private static final int FULL_BRIGHT = LightTexture.pack(15, 15);

    private final MissileProjectileModel<MissileGenericEntity> model;

    public MissileGenericEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MissileProjectileModel<>(MissileProjectileModel.createLayer().bakeRoot());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(MissileGenericEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        float scale = entity.getScale();
        poseStack.scale(scale, scale, scale);

        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(entityYaw));
        // MissileGeneric stores upward pitch as positive, while the vanilla model
        // coordinate system rotates +Z downward for a positive X rotation.
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));

        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        model.renderFlame(poseStack, buffer, FULL_BRIGHT, entity.tickCount + partialTick);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MissileGenericEntity entity) {
        return TEXTURE;
    }
}
