package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile.BulletGrenadeLargeEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeLargeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class BulletGrenadeLargeEntityRenderer extends GeoEntityRenderer<BulletGrenadeLargeEntity> {
   public BulletGrenadeLargeEntityRenderer(Context renderManager) {
      super(renderManager, new BulletGrenadeLargeEntityModel());
      this.addRenderLayer(new AutoGlowingGeoLayer(this));
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      BulletGrenadeLargeEntity animatable,
      BakedGeoModel model,
      RenderType renderType,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      this.scaleHeight = BulletGrenadeLargeEntity.scale;
      this.scaleWidth = BulletGrenadeLargeEntity.scale;
      super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
   }
}
