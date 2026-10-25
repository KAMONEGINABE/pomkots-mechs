package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.TargetLocker;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public class RenderUtils {
   public static final RenderType NO_DEPTH_TRIANGLES = RenderType.create(
      "no_depth_triangles",
      DefaultVertexFormat.POSITION_COLOR,
      Mode.TRIANGLES,
      131072,
      false,
      true,
      CompositeState.builder()
         .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
         .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
         .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
         .setCullState(RenderStateShard.NO_CULL)
         .createCompositeState(false)
   );

   public static void renderAdditionalHud2(
      PomkotsVehicleBase entity, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher erd, float heightOffset
   ) {
      Minecraft client = Minecraft.getInstance();
      if (!client.options.hideGui
         && PomkotsMechs.CONFIG.enableHudHealthBar
         && !entity.equals(client.player.getVehicle())
         && !(entity instanceof Pmvc01Entity pmvc01 && !pmvc01.showCustomHealthBar())) {
         double distance = client.gameRenderer.getMainCamera().getPosition().distanceTo(entity.position());
         if (!(distance > 4096.0)) {
            renderVehicleBars(entity, poseStack, bufferSource, erd, heightOffset);
         }
      }
   }

   public static void renderVehicleBars(
      PomkotsVehicleBase entity, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher erd, float heightOffset
   ) {
      Minecraft client = Minecraft.getInstance();
      if (!client.options.hideGui) {
         poseStack.pushPose();
         poseStack.translate(0.0, entity.getBbHeight() + heightOffset, 0.0);
         poseStack.mulPose(erd.cameraOrientation());
         Vec3 camPos = erd.camera.getPosition();
         double dist = camPos.distanceTo(entity.position());
         float baseScale = 0.002F;
         float scale = (float)(baseScale * dist);
         scale = Math.min(scale, 0.1F);
         poseStack.scale(-scale, -scale, scale);
         Pose pose = poseStack.last();
         VertexConsumer vc = bufferSource.getBuffer(RenderType.debugQuads());
         float barWidth = 40.0F;
         float barHeight = 3.0F;
         float gap = 3.0F;
         float hpRatio = entity.getHealth() / entity.getMaxHealth();
         float hpY = -(barHeight + gap);
         float spY = 0.0F;
         drawBar(vc, pose, barWidth, barHeight, hpY, hpRatio, 0, 0, 0, 224, 224, 224);
         poseStack.popPose();
      }
   }

   public static void renderBossBars(BaseBossEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher erd, float heightOffset) {
      Minecraft client = Minecraft.getInstance();
      if (entity.getAiMode() != -2 && !client.options.hideGui && entity.tickCount >= 5) {
         poseStack.pushPose();
         poseStack.translate(0.0, entity.getBbHeight() + heightOffset, 0.0);
         poseStack.mulPose(erd.cameraOrientation());
         Vec3 camPos = erd.camera.getPosition();
         double dist = camPos.distanceTo(entity.position());
         float baseScale = 0.002F;
         float scale = (float)(baseScale * dist);
         poseStack.scale(-scale, -scale, scale);
         Pose pose = poseStack.last();
         VertexConsumer vc = bufferSource.getBuffer(RenderType.debugQuads());
         float barWidth = 40.0F;
         float barHeight = 3.0F;
         float gap = 3.0F;
         float hpRatio = entity.getHealth() / entity.getMaxHealth();
         float spRatio = Math.min(entity.getStunPoint(), 200) / 200.0F;
         float hpY = -(barHeight + gap);
         float spY = 0.0F;
         drawBar(vc, pose, barWidth, barHeight, hpY, hpRatio, 0, 0, 0, 224, 224, 224);
         if (entity.isStunning()) {
            drawBar(vc, pose, barWidth, barHeight, spY, spRatio, 0, 0, 0, 128, 0, 0);
         } else {
            drawBar(vc, pose, barWidth, barHeight, spY, spRatio, 0, 0, 0, 224, 224, 224);
         }

         poseStack.popPose();
      }
   }

   public static void renderMobBars(
      GenericPomkotsMonster entity, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher erd, float heightOffset
   ) {
      Minecraft mc = Minecraft.getInstance();
      if (!mc.options.hideGui && entity.tickCount >= 5) {
         poseStack.pushPose();
         poseStack.translate(0.0, entity.getBbHeight() + heightOffset, 0.0);
         poseStack.mulPose(erd.cameraOrientation());
         Vec3 camPos = erd.camera.getPosition();
         double dist = camPos.distanceTo(entity.position());
         float baseScale = 0.002F;
         float scale = (float)(baseScale * dist);
         poseStack.scale(-scale, -scale, scale);
         Pose pose = poseStack.last();
         VertexConsumer vc = bufferSource.getBuffer(RenderType.debugQuads());
         float barWidth = 20.0F;
         float barHeight = 3.0F;
         float gap = 3.0F;
         float hpRatio = entity.getHealth() / entity.getMaxHealth();
         float hpY = -(barHeight + gap);
         drawBar(vc, pose, barWidth, barHeight, hpY, hpRatio, 0, 0, 0, 224, 224, 224);
         poseStack.translate(0.0, -15.0, 0.0);
         if (entity.isInEvent()) {
            VertexConsumer vc2 = bufferSource.getBuffer(NO_DEPTH_TRIANGLES);
            triangle(vc2, pose, 7.0F, 0.0F, 255, 0, 0, 255);
         }

         poseStack.popPose();
      }
   }

   public static void renderMarking(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher erd, float heightOffset) {
      Minecraft mc = Minecraft.getInstance();
      if (!mc.options.hideGui && entity.tickCount >= 5) {
         poseStack.pushPose();
         poseStack.translate(0.0, entity.getBbHeight() + heightOffset, 0.0);
         poseStack.mulPose(erd.cameraOrientation());
         Vec3 camPos = erd.camera.getPosition();
         double dist = camPos.distanceTo(entity.position());
         float baseScale = 0.002F;
         float scale = (float)(baseScale * dist);
         poseStack.scale(-scale, -scale, scale);
         Pose pose = poseStack.last();
         poseStack.translate(0.0, -15.0, 0.0);
         VertexConsumer vc2 = bufferSource.getBuffer(NO_DEPTH_TRIANGLES);
         triangle(vc2, pose, 7.0F, 0.0F, 255, 0, 0, 255);
         poseStack.popPose();
      }
   }

   private static void triangle(VertexConsumer vc, Pose pose, float size, float z, int r, int g, int b, int a) {
      vc.vertex(pose.pose(), 0.0F, size / 2.0F, z).color(r, g, b, a).endVertex();
      vc.vertex(pose.pose(), -size, -size / 2.0F, z).color(r, g, b, a).endVertex();
      vc.vertex(pose.pose(), size, -size / 2.0F, z).color(r, g, b, a).endVertex();
   }

   private static void drawBar(
      VertexConsumer vc, Pose pose, float width, float height, float y, float ratio, int bgR, int bgG, int bgB, int fgR, int fgG, int fgB
   ) {
      ratio = Mth.clamp(ratio, 0.0F, 1.0F);
      float left = -width;
      float right = width;
      float filledRight = left + (right - left) * ratio;
      quad(vc, pose, left, y, right, y + height, 1.0F, bgR, bgG, bgB, 128);
      quad(vc, pose, left, y, filledRight, y + height, -1.0F, fgR, fgG, fgB, 253);
   }

   private static void quad(VertexConsumer vc, Pose pose, float x1, float y1, float x2, float y2, float z, int r, int g, int b, int a) {
      vc.vertex(pose.pose(), x1, y1, z).color(r, g, b, a).endVertex();
      vc.vertex(pose.pose(), x1, y2, z).color(r, g, b, a).endVertex();
      vc.vertex(pose.pose(), x2, y2, z).color(r, g, b, a).endVertex();
      vc.vertex(pose.pose(), x2, y1, z).color(r, g, b, a).endVertex();
   }

   public static void renderAdditionalHud(PoseStack matrixStack, LivingEntity entity, Quaternionf rotation, MultiBufferSource buffer) {
      Minecraft client = Minecraft.getInstance();
      if (!client.options.hideGui) {
         double distance = client.gameRenderer.getMainCamera().getPosition().distanceTo(entity.position());
         if (distance > 4096.0) {
            return;
         }

         if (!entity.equals(client.player.getVehicle()) && PomkotsMechs.CONFIG.enableHudHealthBar) {
            renderHealthBar(matrixStack, entity, rotation, buffer);
         }
      }
   }

   private static void renderTargetLock(PoseStack matrixStack, LivingEntity entity, Quaternionf rotation, MultiBufferSource buffer) {
      int lockState = TargetLocker.getInstance().isEntityLocked(entity);
      switch (lockState) {
         case 0:
            renderTargetLockTexture("crosshair1.png", matrixStack, entity, rotation, buffer);
            break;
         case 1:
            renderTargetLockTexture("crosshair2.png", matrixStack, entity, rotation, buffer);
            break;
         case 2:
            renderTargetLockTexture("crosshair3.png", matrixStack, entity, rotation, buffer);
         case 3:
      }
   }

   private static void renderTargetLockTexture(String textureName, PoseStack matrixStack, LivingEntity entity, Quaternionf rotation, MultiBufferSource buffer) {
      ResourceLocation reticleTexture = new ResourceLocation("pomkotsmechs", "textures/crosshair/" + textureName);
      matrixStack.pushPose();
      matrixStack.translate(0.0F, entity.getBbHeight() / 2.0F, 0.0F);
      matrixStack.scale(-0.1F, -0.1F, -0.1F);
      matrixStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferBuilder = tesselator.getBuilder();
      bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, reticleTexture);
      RenderSystem.enableBlend();
      RenderSystem.enableDepthTest();
      float size = 32.0F;
      addQuadTex(bufferBuilder, matrixStack, -size / 2.0F, -size / 2.0F, size / 2.0F, size / 2.0F, 40);
      tesselator.end();
      matrixStack.popPose();
   }

   private static void addQuadTex(BufferBuilder buffer, PoseStack poseStack, float minX, float minY, float maxX, float maxY, int depth) {
      buffer.vertex(poseStack.last().pose(), minX, minY, depth).uv(0.0F, 0.0F).endVertex();
      buffer.vertex(poseStack.last().pose(), minX, maxY, depth).uv(0.0F, 1.0F).endVertex();
      buffer.vertex(poseStack.last().pose(), maxX, maxY, depth).uv(1.0F, 1.0F).endVertex();
      buffer.vertex(poseStack.last().pose(), maxX, minY, depth).uv(1.0F, 0.0F).endVertex();
   }

   private static void renderHealthBar(PoseStack matrixStack, LivingEntity entity, Quaternionf rotation, MultiBufferSource buffer) {
      matrixStack.pushPose();
      matrixStack.translate(0.0, entity.getBbHeight() + 2.0, 0.0);
      matrixStack.scale(-0.1F, -0.1F, -0.1F);
      matrixStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
      int barWidth = 40;
      int barHeight = 3;
      int maxHealth = (int)entity.getMaxHealth();
      int currentHealth = (int)entity.getHealth();
      int healthBarWidth = (int)((float)currentHealth / maxHealth * barWidth);
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferBuilder = tesselator.getBuilder();
      bufferBuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      RenderSystem.enableBlend();
      RenderSystem.disableDepthTest();
      addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0.0F, barWidth / 2, barHeight, 1431655765);
      if (entity instanceof PomkotsVehicle) {
         addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0.0F, -barWidth / 2 + healthBarWidth, barHeight, -1728053078);
      } else {
         addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0.0F, -barWidth / 2 + healthBarWidth, barHeight, -1713504256);
      }

      tesselator.end();
      matrixStack.popPose();
   }

   private static void addQuad(BufferBuilder buffer, PoseStack poseStack, float minX, float minY, float maxX, float maxY, int color) {
      float a = (color >> 24 & 0xFF) / 255.0F;
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      buffer.vertex(poseStack.last().pose(), minX, minY, 0.0F).color(r, g, b, a).endVertex();
      buffer.vertex(poseStack.last().pose(), minX, maxY, 0.0F).color(r, g, b, a).endVertex();
      buffer.vertex(poseStack.last().pose(), maxX, maxY, 0.0F).color(r, g, b, a).endVertex();
      buffer.vertex(poseStack.last().pose(), maxX, minY, 0.0F).color(r, g, b, a).endVertex();
   }

   public static void renderBlocks(BlockState state, Vector3d offset, Float yaw, PoseStack pose, MultiBufferSource buffer, int packedLight) {
      BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
      pose.pushPose();
      pose.translate(offset.x, offset.y, offset.z);
      pose.mulPose(Axis.YP.rotationDegrees(-yaw));

      for (int x = -4; x <= 4; x++) {
         for (int y = -4; y <= 4; y++) {
            for (int z = -1; z <= 1; z++) {
               pose.pushPose();
               pose.translate(x, y, z);
               dispatcher.renderSingleBlock(state, pose, buffer, packedLight, OverlayTexture.NO_OVERLAY);
               pose.popPose();
            }
         }
      }

      pose.popPose();
   }
}
