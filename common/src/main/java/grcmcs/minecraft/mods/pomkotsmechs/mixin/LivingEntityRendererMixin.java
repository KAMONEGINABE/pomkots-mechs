package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
   private static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");

   protected LivingEntityRendererMixin(Context ctx) {
      super(ctx);
   }

   @Inject(method = "render", at = @At("HEAD"))
   public void onRender(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
      if (!(entity instanceof Player)) {
      }
   }

   private void renderHealthBar(PoseStack matrixStack, T entity, Quaternionf rotation, MultiBufferSource buffer) {
      Minecraft client = Minecraft.m_91087_();
      double distance = client.f_91063_.m_109153_().m_90583_().m_82554_(entity.m_20182_());
      if (!(distance > 4096.0)) {
         matrixStack.m_85836_();
         matrixStack.m_85837_(0.0, entity.m_20206_() + 0.5, 0.0);
         matrixStack.m_85841_(-0.1F, 0.1F, 0.1F);
         matrixStack.m_252781_(rotation);
         int barWidth = 40;
         int barHeight = 6;
         int maxHealth = (int)entity.m_21233_();
         int currentHealth = (int)entity.m_21223_();
         int healthBarWidth = (int)((float)currentHealth / maxHealth * barWidth);
         Tesselator tesselator = Tesselator.m_85913_();
         BufferBuilder bufferBuilder = tesselator.m_85915_();
         bufferBuilder.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
         RenderSystem.setShader(GameRenderer::m_172811_);
         RenderSystem.enableBlend();
         RenderSystem.disableDepthTest();
         this.addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0.0F, barWidth / 2, barHeight, 1431655850);
         this.addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0.0F, -barWidth / 2 + healthBarWidth, barHeight, -16733441);
         tesselator.m_85914_();
         matrixStack.m_85849_();
      }
   }

   private void addQuad(BufferBuilder buffer, PoseStack poseStack, float minX, float minY, float maxX, float maxY, int color) {
      float a = (color >> 24 & 0xFF) / 255.0F;
      float r = (color >> 16 & 0xFF) / 255.0F;
      float g = (color >> 8 & 0xFF) / 255.0F;
      float b = (color & 0xFF) / 255.0F;
      buffer.m_252986_(poseStack.m_85850_().m_252922_(), minX, minY, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(poseStack.m_85850_().m_252922_(), minX, maxY, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(poseStack.m_85850_().m_252922_(), maxX, maxY, 0.0F).m_85950_(r, g, b, a).m_5752_();
      buffer.m_252986_(poseStack.m_85850_().m_252922_(), maxX, minY, 0.0F).m_85950_(r, g, b, a).m_5752_();
   }
}
