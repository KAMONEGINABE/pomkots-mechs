package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile.BulletMachineEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletMachineEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class BulletMachineEntityRenderer extends GeoEntityRenderer<BulletMachineEntity> {
   private float scale;

   public BulletMachineEntityRenderer(Context renderManager, float scale) {
      super(renderManager, new BulletMachineEntityModel());
      this.addRenderLayer(new AutoGlowingGeoLayer(this));
      this.scale = scale;
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      BulletMachineEntity animatable,
      BakedGeoModel model,
      RenderType renderType,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      this.scaleHeight = this.scale;
      this.scaleWidth = this.scale;
      super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
      ((GeoBone)model.getBone("root").get()).setRotX((float)Math.toRadians(animatable.m_146909_()));
      ((GeoBone)model.getBone("root").get()).setRotY((float)Math.toRadians(animatable.m_146908_()));
   }
}
