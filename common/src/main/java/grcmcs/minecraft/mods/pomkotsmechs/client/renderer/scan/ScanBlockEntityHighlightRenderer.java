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
      ClientLevel level = Minecraft.getInstance().level;
      if (level != null) {
         if (Minecraft.getInstance().player != null) {
            if (!ClientScanManager.isBlockHighlightEmpty()) {
               Vec3 cameraPos = camera.getPosition();
               RenderSystem.disableDepthTest();
               RenderSystem.depthMask(false);

               try {
                  VertexConsumer consumer = bufferSource.getBuffer(ScanRenderTypes.SCAN_HIGHLIGHT_LINES);
                  boolean rendered = false;

                  for (BlockPos pos : ClientScanManager.getHighlightedBlockEntities()) {
                     BlockEntity blockEntity = level.getBlockEntity(pos);
                     if (blockEntity instanceof ScanTargetBlockEntity scanTarget) {
                        AABB renderBox = createHighlightBox(blockEntity).inflate(0.02).move(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                        float[] color = getColor(blockEntity);
                        LevelRenderer.renderLineBox(poseStack, consumer, renderBox, color[0], color[1], color[2], 0.65F);
                        rendered = true;
                     }
                  }

                  if (rendered) {
                     bufferSource.endBatch(ScanRenderTypes.SCAN_HIGHLIGHT_LINES);
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
      BlockPos pos = blockEntity.getBlockPos();
      return new AABB(pos);
   }
}
