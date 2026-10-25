package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.MissileBaseEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileBaseEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class MissileBaseEntityRenderer extends GeoEntityRenderer<MissileBaseEntity> {
   public MissileBaseEntityRenderer(Context renderManager) {
      super(renderManager, new MissileBaseEntityModel());
      this.addRenderLayer(new AutoGlowingGeoLayer(this));
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      MissileBaseEntity animatable,
      BakedGeoModel model,
      RenderType renderType,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      this.scaleHeight = animatable.getScale();
      this.scaleWidth = animatable.getScale();
      super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
      ((GeoBone)model.getBone("bone").get()).setRotX((float)Math.toRadians(animatable.m_146909_()));
      ((GeoBone)model.getBone("bone").get()).setRotY((float)Math.toRadians(animatable.m_146908_()));
   }
}
