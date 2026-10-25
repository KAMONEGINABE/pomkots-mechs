package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.scan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockPurpleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockRedEntity;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockYellowEntity;
import grcmcs.minecraft.mods.pomkotsmechs.block.ScanTargetBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.scan.ClientScanManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ScanBlockEntityHighlightRenderer {
   private static final float[] COLOR_BLUE = new float[]{0.0F, 0.8F, 1.0F};
   private static final float[] COLOR_YELLOW = new float[]{1.0F, 1.0F, 0.0F};
   private static final float[] COLOR_RED = new float[]{1.0F, 0.0F, 0.0F};
   private static final float[] COLOR_PURPLE = new float[]{0.6F, 0.0F, 1.0F};

   private ScanBlockEntityHighlightRenderer() {
   }

   public static void render(PoseStack poseStack, Camera camera, BufferSource bufferSource) {
      ClientLevel level = Minecraft.m_91087_().f_91073_;
      if (level != null) {
         if (Minecraft.m_91087_().f_91074_ != null) {
            if (!ClientScanManager.isBlockHighlightEmpty()) {
               Vec3 cameraPos = camera.m_90583_();
               RenderSystem.disableDepthTest();
               RenderSystem.depthMask(false);

               try {
                  VertexConsumer consumer = bufferSource.m_6299_(ScanRenderTypes.SCAN_HIGHLIGHT_LINES);
                  boolean rendered = false;

                  for (BlockPos pos : ClientScanManager.getHighlightedBlockEntities()) {
                     BlockEntity blockEntity = level.m_7702_(pos);
                     if (blockEntity instanceof ScanTargetBlockEntity scanTarget) {
                        AABB renderBox = createHighlightBox(blockEntity).m_82400_(0.02).m_82386_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
                        float[] color = getColor(blockEntity);
                        LevelRenderer.m_109646_(poseStack, consumer, renderBox, color[0], color[1], color[2], 0.65F);
                        rendered = true;
                     }
                  }

                  if (rendered) {
                     bufferSource.m_109912_(ScanRenderTypes.SCAN_HIGHLIGHT_LINES);
                  }
               } finally {
                  RenderSystem.depthMask(true);
                  RenderSystem.enableDepthTest();
               }
            }
         }
      }
   }

   private static float[] getColor(BlockEntity be) {
      if (be instanceof PomkotsCubeBlockPurpleEntity) {
         return COLOR_PURPLE;
      } else if (be instanceof PomkotsCubeBlockRedEntity) {
         return COLOR_RED;
      } else {
         return be instanceof PomkotsCubeBlockYellowEntity ? COLOR_YELLOW : COLOR_BLUE;
      }
   }

   private static AABB createHighlightBox(BlockEntity blockEntity) {
      BlockPos pos = blockEntity.m_58899_();
      return new AABB(pos);
   }
}
