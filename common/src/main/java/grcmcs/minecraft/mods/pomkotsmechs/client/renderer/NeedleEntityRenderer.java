package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.NeedleEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.NeedleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class NeedleEntityRenderer extends GeoEntityRenderer<NeedleEntity> {
   public NeedleEntityRenderer(Context renderManager) {
      super(renderManager, new NeedleEntityModel());
      this.addRenderLayer(new AutoGlowingGeoLayer(this));
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      NeedleEntity animatable,
      BakedGeoModel model,
      RenderType renderType,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
      ((GeoBone)model.getBone("root").get()).setRotX((float)Math.toRadians(animatable.getXRot()));
      ((GeoBone)model.getBone("root").get()).setRotY((float)Math.toRadians(animatable.getYRot()));
   }
}
