package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.PlacementPreviewEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public class PlacementPreviewRenderer extends EntityRenderer<PlacementPreviewEntity> {
   int i = 0;

   public PlacementPreviewRenderer(Context ctx) {
      super(ctx);
   }

   public void render2(PlacementPreviewEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
      poseStack.pushPose();
      poseStack.translate(0.0, 0.0, 0.0);
      poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot()));
      AABB box = new AABB(-4.5, -4.5, -4.5, 4.5, 4.5, 4.5);
      VertexConsumer solid = buffer.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));
      LevelRenderer.renderLineBox(poseStack, solid, box, 1.0F, 0.0F, 0.0F, 0.25F);
      VertexConsumer lines = buffer.getBuffer(RenderType.lines());
      LevelRenderer.renderLineBox(poseStack, lines, box, 1.0F, 0.2F, 0.2F, 0.9F);
      poseStack.popPose();
      super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
   }

   public void render(PlacementPreviewEntity entity, float yaw, float partial, PoseStack poseStack, MultiBufferSource buffer, int light) {
      poseStack.pushPose();
      poseStack.translate(0.0, 0.0, 0.0);
      VertexConsumer vc = buffer.getBuffer(RenderType.lines());
      LevelRenderer.renderLineBox(poseStack, vc, new AABB(-1.0, -1.0, -1.0, 1.0, 1.0, 1.0), 0.0F, 1.0F, 0.0F, 1.0F);
      poseStack.popPose();
   }

   public ResourceLocation getTextureLocation(PlacementPreviewEntity entity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
