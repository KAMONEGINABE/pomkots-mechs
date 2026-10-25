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
      poseStack.m_85836_();
      poseStack.m_85837_(0.0, 0.0, 0.0);
      poseStack.m_252781_(Axis.f_252436_.m_252977_(-entity.m_146908_()));
      AABB box = new AABB(-4.5, -4.5, -4.5, 4.5, 4.5, 4.5);
      VertexConsumer solid = buffer.m_6299_(RenderType.m_110470_(TextureAtlas.f_118259_));
      LevelRenderer.m_109646_(poseStack, solid, box, 1.0F, 0.0F, 0.0F, 0.25F);
      VertexConsumer lines = buffer.m_6299_(RenderType.m_110504_());
      LevelRenderer.m_109646_(poseStack, lines, box, 1.0F, 0.2F, 0.2F, 0.9F);
      poseStack.m_85849_();
      super.m_7392_(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
   }

   public void render(PlacementPreviewEntity entity, float yaw, float partial, PoseStack poseStack, MultiBufferSource buffer, int light) {
      poseStack.m_85836_();
      poseStack.m_85837_(0.0, 0.0, 0.0);
      VertexConsumer vc = buffer.m_6299_(RenderType.m_110504_());
      LevelRenderer.m_109646_(poseStack, vc, new AABB(-1.0, -1.0, -1.0, 1.0, 1.0, 1.0), 0.0F, 1.0F, 0.0F, 1.0F);
      poseStack.m_85849_();
   }

   public ResourceLocation getTextureLocation(PlacementPreviewEntity entity) {
      return TextureAtlas.f_118259_;
   }
}
