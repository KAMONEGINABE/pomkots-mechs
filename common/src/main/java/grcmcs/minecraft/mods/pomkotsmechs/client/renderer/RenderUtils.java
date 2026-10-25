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
   public static final RenderType NO_DEPTH_TRIANGLES = RenderType.m_173215_(
      "no_depth_triangles",
      DefaultVertexFormat.f_85815_,
      Mode.TRIANGLES,
      131072,
      false,
      true,
      CompositeState.m_110628_()
         .m_173292_(RenderStateShard.f_173104_)
         .m_110685_(RenderStateShard.f_110139_)
         .m_110663_(RenderStateShard.f_110111_)
         .m_110661_(RenderStateShard.f_110110_)
         .m_110691_(false)
   );

   public static void renderAdditionalHud2(
      PomkotsVehicleBase entity, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher erd, float heightOffset
   ) {
      Minecraft client = Minecraft.m_91087_();
      if (!client.f_91066_.f_92062_
         && PomkotsMechs.CONFIG.enableHudHealthBar
         && !entity.equals(client.f_91074_.m_20202_())
         && !(entity instanceof Pmvc01Entity pmvc01 && !pmvc01.showCustomHealthBar())) {
         double distance = client.f_91063_.m_109153_().m_90583_().m_82554_(entity.m_20182_());
         if (!(distance > 4096.0)) {
            renderVehicleBars(entity, poseStack, bufferSource, erd, heightOffset);
         }
      }
   }

   public static void renderVehicleBars(
      PomkotsVehicleBase entity, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher erd, float heightOffset
   ) {
      Minecraft client = Minecraft.m_91087_();
      if (!client.f_91066_.f_92062_) {
         poseStack.m_85836_();
         poseStack.m_85837_(0.0, entity.m_20206_() + heightOffset, 0.0);
         poseStack.m_252781_(erd.m_253208_());
         Vec3 camPos = erd.f_114358_.m_90583_();
         double dist = camPos.m_82554_(entity.m_20182_());
         float baseScale = 0.002F;
         float scale = (float)(baseScale * dist);
         scale = Math.min(scale, 0.1F);
         poseStack.m_85841_(-scale, -scale, scale);
         Pose pose = poseStack.m_85850_();
         VertexConsumer vc = bufferSource.m_6299_(RenderType.m_269166_());
         float barWidth = 40.0F;
         float barHeight = 3.0F;
         float gap = 3.0F;
         float hpRatio = entity.m_21223_() / entity.m_21233_();
         float hpY = -(barHeight + gap);
         float spY = 0.0F;
         drawBar(vc, pose, barWidth, barHeight, hpY, hpRatio, 0, 0, 0, 224, 224, 224);
         poseStack.m_85849_();
      }
   }

   public static void renderBossBars(BaseBossEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher erd, float heightOffset) {
      Minecraft client = Minecraft.m_91087_();
      if (entity.getAiMode() != -2 && !client.f_91066_.f_92062_ && entity.f_19797_ >= 5) {
         poseStack.m_85836_();
         poseStack.m_85837_(0.0, entity.m_20206_() + heightOffset, 0.0);
         poseStack.m_252781_(erd.m_253208_());
         Vec3 camPos = erd.f_114358_.m_90583_();
         double dist = camPos.m_82554_(entity.m_20182_());
         float baseScale = 0.002F;
         float scale = (float)(baseScale * dist);
         poseStack.m_85841_(-scale, -scale, scale);
         Pose pose = poseStack.m_85850_();
         VertexConsumer vc = bufferSource.m_6299_(RenderType.m_269166_());
         float barWidth = 40.0F;
         float barHeight = 3.0F;
         float gap = 3.0F;
         float hpRatio = entity.m_21223_() / entity.m_21233_();
         float spRatio = Math.min(entity.getStunPoint(), 200) / 200.0F;
         float hpY = -(barHeight + gap);
         float spY = 0.0F;
         drawBar(vc, pose, barWidth, barHeight, hpY, hpRatio, 0, 0, 0, 224, 224, 224);
         if (entity.isStunning()) {
            drawBar(vc, pose, barWidth, barHeight, spY, spRatio, 0, 0, 0, 128, 0, 0);
         } else {
            drawBar(vc, pose, barWidth, barHeight, spY, spRatio, 0, 0, 0, 224, 224, 224);
         }

         poseStack.m_85849_();
      }
   }

   public static void renderMobBars(
      GenericPomkotsMonster entity, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher erd, float heightOffset
   ) {
      Minecraft mc = Minecraft.m_91087_();
      if (!mc.f_91066_.f_92062_ && entity.f_19797_ >= 5) {
         poseStack.m_85836_();
         poseStack.m_85837_(0.0, entity.m_20206_() + heightOffset, 0.0);
         poseStack.m_252781_(erd.m_253208_());
         Vec3 camPos = erd.f_114358_.m_90583_();
         double dist = camPos.m_82554_(entity.m_20182_());
         float baseScale = 0.002F;
         float scale = (float)(baseScale * dist);
         poseStack.m_85841_(-scale, -scale, scale);
         Pose pose = poseStack.m_85850_();
         VertexConsumer vc = bufferSource.m_6299_(RenderType.m_269166_());
         float barWidth = 20.0F;
         float barHeight = 3.0F;
         float gap = 3.0F;
         float hpRatio = entity.m_21223_() / entity.m_21233_();
         float hpY = -(barHeight + gap);
         drawBar(vc, pose, barWidth, barHeight, hpY, hpRatio, 0, 0, 0, 224, 224, 224);
         poseStack.m_85837_(0.0, -15.0, 0.0);
         if (entity.isInEvent()) {
            VertexConsumer vc2 = bufferSource.m_6299_(NO_DEPTH_TRIANGLES);
            triangle(vc2, pose, 7.0F, 0.0F, 255, 0, 0, 255);
         }

         poseStack.m_85849_();
      }
   }

   public static void renderMarking(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderDispatcher erd, float heightOffset) {
      Minecraft mc = Minecraft.m_91087_();
      if (!mc.f_91066_.f_92062_ && entity.f_19797_ >= 5) {
         poseStack.m_85836_();
         poseStack.m_85837_(0.0, entity.m_20206_() + heightOffset, 0.0);
         poseStack.m_252781_(erd.m_253208_());
         Vec3 camPos = erd.f_114358_.m_90583_();
         double dist = camPos.m_82554_(entity.m_20182_());
         float baseScale = 0.002F;
         float scale = (float)(baseScale * dist);
         poseStack.m_85841_(-scale, -scale, scale);
         Pose pose = poseStack.m_85850_();
         poseStack.m_85837_(0.0, -15.0, 0.0);
         VertexConsumer vc2 = bufferSource.m_6299_(NO_DEPTH_TRIANGLES);
         triangle(vc2, pose, 7.0F, 0.0F, 255, 0, 0, 255);
         poseStack.m_85849_();
      }
   }

   private static void triangle(VertexConsumer vc, Pose pose, float size, float z, int r, int g, int b, int a) {
      vc.m_252986_(pose.m_252922_(), 0.0F, size / 2.0F, z).m_6122_(r, g, b, a).m_5752_();
      vc.m_252986_(pose.m_252922_(), -size, -size / 2.0F, z).m_6122_(r, g, b, a).m_5752_();
      vc.m_252986_(pose.m_252922_(), size, -size / 2.0F, z).m_6122_(r, g, b, a).m_5752_();
   }

   private static void drawBar(
      VertexConsumer vc, Pose pose, float width, float height, float y, float ratio, int bgR, int bgG, int bgB, int fgR, int fgG, int fgB
   ) {
      ratio = Mth.m_14036_(ratio, 0.0F, 1.0F);
      float left = -width;
      float right = width;
      float filledRight = left + (right - left) * ratio;
      quad(vc, pose, left, y, right, y + height, 1.0F, bgR, bgG, bgB, 128);
      quad(vc, pose, left, y, filledRight, y + height, -1.0F, fgR, fgG, fgB, 253);
   }

   private static void quad(VertexConsumer vc, Pose pose, float x1, float y1, float x2, float y2, float z, int r, int g, int b, int a) {
      vc.m_252986_(pose.m_252922_(), x1, y1, z).m_6122_(r, g, b, a).m_5752_();
      vc.m_252986_(pose.m_252922_(), x1, y2, z).m_6122_(r, g, b, a).m_5752_();
      vc.m_252986_(pose.m_252922_(), x2, y2, z).m_6122_(r, g, b, a).m_5752_();
      vc.m_252986_(pose.m_252922_(), x2, y1, z).m_6122_(r, g, b, a).m_5752_();
   }

   public static void renderAdditionalHud(PoseStack matrixStack, LivingEntity entity, Quaternionf rotation, MultiBufferSource buffer) {
      Minecraft client = Minecraft.m_91087_();
      if (!client.f_91066_.f_92062_) {
         double distance = client.f_91063_.m_109153_().m_90583_().m_82554_(entity.m_20182_());
         if (distance > 4096.0) {
            return;
         }

         if (!entity.equals(client.f_91074_.m_20202_()) && PomkotsMechs.CONFIG.enableHudHealthBar) {
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
      matrixStack.m_85836_();
      matrixStack.m_252880_(0.0F, entity.m_20206_() / 2.0F, 0.0F);
      matrixStack.m_85841_(-0.1F, -0.1F, -0.1F);
      matrixStack.m_252781_(Minecraft.m_91087_().m_91290_().m_253208_());
      Tesselator tesselator = Tesselator.m_85913_();
      BufferBuilder bufferBuilder = tesselator.m_85915_();
      bufferBuilder.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85817_);
      RenderSystem.setShader(GameRenderer::m_172817_);
      RenderSystem.setShaderTexture(0, reticleTexture);
      RenderSystem.enableBlend();
      RenderSystem.enableDepthTest();
      float size = 32.0F;
      addQuadTex(bufferBuilder, matrixStack, -size / 2.0F, -size / 2.0F, size / 2.0F, size / 2.0F, 40);
      tesselator.m_85914_();
      matrixStack.m_85849_();
   }

   private static void addQuadTex(BufferBuilder buffer, PoseStack poseStack, float minX, float minY, float maxX, float maxY, int depth) {
      buffer.m_252986_(poseStack.m_85850_().m_252922_(), minX, minY, depth).m_7421_(0.0F, 0.0F).m_5752_();
      buffer.m_252986_(poseStack.m_85850_().m_252922_(), minX, maxY, depth).m_7421_(0.0F, 1.0F).m_5752_();
      buffer.m_252986_(poseStack.m_85850_().m_252922_(), maxX, maxY, depth).m_7421_(1.0F, 1.0F).m_5752_();
      buffer.m_252986_(poseStack.m_85850_().m_252922_(), maxX, minY, depth).m_7421_(1.0F, 0.0F).m_5752_();
   }

   private static void renderHealthBar(PoseStack matrixStack, LivingEntity entity, Quaternionf rotation, MultiBufferSource buffer) {
      matrixStack.m_85836_();
      matrixStack.m_85837_(0.0, entity.m_20206_() + 2.0, 0.0);
      matrixStack.m_85841_(-0.1F, -0.1F, -0.1F);
      matrixStack.m_252781_(Minecraft.m_91087_().m_91290_().m_253208_());
      int barWidth = 40;
      int barHeight = 3;
      int maxHealth = (int)entity.m_21233_();
      int currentHealth = (int)entity.m_21223_();
      int healthBarWidth = (int)((float)currentHealth / maxHealth * barWidth);
      Tesselator tesselator = Tesselator.m_85913_();
      BufferBuilder bufferBuilder = tesselator.m_85915_();
      bufferBuilder.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
      RenderSystem.setShader(GameRenderer::m_172811_);
      RenderSystem.enableBlend();
      RenderSystem.disableDepthTest();
      addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0.0F, barWidth / 2, barHeight, 1431655765);
      if (entity instanceof PomkotsVehicle) {
         addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0.0F, -barWidth / 2 + healthBarWidth, barHeight, -1728053078);
      } else {
         addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0.0F, -barWidth / 2 + healthBarWidth, barHeight, -1713504256);
      }

      tesselator.m_85914_();
      matrixStack.m_85849_();
   }

   private static void addQuad(BufferBuilder buffer, PoseStack poseStack, float minX, float minY, float maxX, float maxY, int color) {
      float a = (color >> 24 & 0xFF) / 255.0F;
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      buffer.m_252986_(poseStack.m_85850_().m_252922_(), minX, minY, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(poseStack.m_85850_().m_252922_(), minX, maxY, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(poseStack.m_85850_().m_252922_(), maxX, maxY, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(poseStack.m_85850_().m_252922_(), maxX, minY, 0.0F).m_85950_(r, g, b, a).m_5752_();
   }

   public static void renderBlocks(BlockState state, Vector3d offset, Float yaw, PoseStack pose, MultiBufferSource buffer, int packedLight) {
      BlockRenderDispatcher dispatcher = Minecraft.m_91087_().m_91289_();
      pose.m_85836_();
      pose.m_85837_(offset.x, offset.y, offset.z);
      pose.m_252781_(Axis.f_252436_.m_252977_(-yaw));

      for (int x = -4; x <= 4; x++) {
         for (int y = -4; y <= 4; y++) {
            for (int z = -1; z <= 1; z++) {
               pose.m_85836_();
               pose.m_252880_(x, y, z);
               dispatcher.m_110912_(state, pose, buffer, packedLight, OverlayTexture.f_118083_);
               pose.m_85849_();
            }
         }
      }

      pose.m_85849_();
   }
}
