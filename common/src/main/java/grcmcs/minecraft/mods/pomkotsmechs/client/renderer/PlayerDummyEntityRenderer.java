package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PlayerDummyEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PlayerDummyEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PlayerDummyEntityRenderer extends GeoEntityRenderer<PlayerDummyEntity> {
   public PlayerDummyEntityRenderer(Context renderManager) {
      super(renderManager, new PlayerDummyEntityModel());
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      PlayerDummyEntity animatable,
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
}
