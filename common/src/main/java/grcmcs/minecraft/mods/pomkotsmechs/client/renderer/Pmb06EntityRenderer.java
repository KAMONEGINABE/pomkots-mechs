package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmb06EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb06Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmb06EntityRenderer extends GeoEntityRenderer<Pmb06Entity> {
   public Pmb06EntityRenderer(Context renderManager) {
      super(renderManager, new Pmb06EntityModel());
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      Pmb06Entity animatable,
      BakedGeoModel model,
      RenderType renderType,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
      this.scaleHeight = 1.0F;
      this.scaleWidth = 1.0F;
   }

   private void updateLaserBones(Pmb06Entity entity) {
      GeoBone laserBeamBone = (GeoBone)this.getGeoModel().getBone("beam").orElse(null);
      if (laserBeamBone != null) {
         if (entity.getLaserLength() > 0) {
            laserBeamBone.setScaleZ(entity.getLaserLength());
         } else {
            laserBeamBone.setScaleZ(1.0F);
         }
      }
   }

   public void actuallyRender(
      PoseStack poseStack,
      Pmb06Entity animatable,
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
      this.updateLaserBones(animatable);
      super.actuallyRender(
         poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha
      );
   }

   public void render(Pmb06Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.m_7392_(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      RenderUtils.renderBossBars(entity, poseStack, bufferSource, this.f_114476_, 30.0F);
   }
}
