package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile.BulletRifleEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletRifleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class BulletRifleEntityRenderer extends GeoEntityRenderer<BulletRifleEntity> {
   public BulletRifleEntityRenderer(Context renderManager) {
      super(renderManager, new BulletRifleEntityModel());
      this.addRenderLayer(new AutoGlowingGeoLayer(this));
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      BulletRifleEntity animatable,
      BakedGeoModel model,
      RenderType renderType,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      this.scaleHeight = 1.2F;
      this.scaleWidth = 1.2F;
      super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
      ((GeoBone)model.getBone("root").get()).setRotX((float)Math.toRadians(animatable.getXRot()));
      ((GeoBone)model.getBone("root").get()).setRotY((float)Math.toRadians(animatable.getYRot()));
   }
}
