package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmb01mk2EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb01mk2Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmb01mk2EntityRenderer extends GeoEntityRenderer<Pmb01mk2Entity> {
   public Pmb01mk2EntityRenderer(Context renderManager) {
      super(renderManager, new Pmb01mk2EntityModel());
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      Pmb01mk2Entity animatable,
      BakedGeoModel model,
      RenderType renderType,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
      this.scaleHeight = 2.0F;
      this.scaleWidth = 2.0F;
   }

   public void actuallyRender(
      PoseStack poseStack,
      Pmb01mk2Entity animatable,
      BakedGeoModel model,
      RenderType renderType,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      boolean isReRender,
      float partialTick,
      int packedLight,
      int packedOverlay,
      float red,
      float green,
      float blue,
      float alpha
   ) {
      super.actuallyRender(
         poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha
      );
   }

   public void render(Pmb01mk2Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      RenderUtils.renderBossBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 7.0F);
   }
}
