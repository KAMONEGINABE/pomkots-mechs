package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.BlockPlacementPreviewEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public class BlockPlacementPreviewRenderer extends EntityRenderer<BlockPlacementPreviewEntity> {
   int i = 0;

   public BlockPlacementPreviewRenderer(Context context) {
      super(context);
   }

   public void render(BlockPlacementPreviewEntity entity, float yaw, float partial, PoseStack poseStack, MultiBufferSource buffer, int light) {
      poseStack.m_85836_();
      poseStack.m_85837_(0.0, 0.0, 0.0);
      VertexConsumer vc = buffer.m_6299_(RenderType.m_110504_());
      int q = entity.yawToQuarter(entity.m_146908_());

      for (BlockPos bPos : entity.getCurrentPattern()) {
         BlockPos bp = entity.rotate(bPos, q);
         poseStack.m_85836_();
         poseStack.m_252880_(bp.m_123341_(), bp.m_123342_(), bp.m_123343_());
         LevelRenderer.m_109646_(poseStack, vc, new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0), 0.0F, 1.0F, 0.0F, 1.0F);
         poseStack.m_85849_();
      }

      poseStack.m_85849_();
   }

   public ResourceLocation getTextureLocation(BlockPlacementPreviewEntity entity) {
      return null;
   }
}
