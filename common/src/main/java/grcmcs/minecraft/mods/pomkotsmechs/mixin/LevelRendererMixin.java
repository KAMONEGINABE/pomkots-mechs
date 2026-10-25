package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.radar.RadarArrowRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.scan.ScanBlockEntityHighlightRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
   @Inject(method = "renderLevel", at = @At("TAIL"))
   private void onRenderLevelTail(
      PoseStack poseStack,
      float partialTick,
      long finishNanoTime,
      boolean renderBlockOutline,
      Camera camera,
      GameRenderer gameRenderer,
      LightTexture lightTexture,
      Matrix4f projectionMatrix,
      CallbackInfo ci
   ) {
      BufferSource bufferSource = Minecraft.m_91087_().m_91269_().m_110104_();
      RadarArrowRenderer.onRenderWorld(poseStack, bufferSource, camera, partialTick);
      bufferSource.m_109912_(RadarArrowRenderer.WIREFRAME);
      bufferSource.m_109912_(RadarArrowRenderer.TRIANGLE);
      ScanBlockEntityHighlightRenderer.render(poseStack, camera, bufferSource);
   }
}
