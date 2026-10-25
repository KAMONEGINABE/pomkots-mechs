package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmv01EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class Pmv01EntityRenderer extends GeoEntityRenderer<Pmv01Entity> {
   private static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");

   public Pmv01EntityRenderer(Context renderManager) {
      this(renderManager, new Pmv01EntityModel());
   }

   public Pmv01EntityRenderer(Context renderManager, Pmv01EntityModel model) {
      super(renderManager, model);
      this.addRenderLayer(new AutoGlowingGeoLayer(this));
   }

   public void actuallyRender(
      PoseStack poseStack,
      Pmv01Entity animatable,
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
      if (animatable.getDrivingPassenger() != null) {
         animatable.setClientSeatPos(this.getSeatPosition(model));
      }

      RenderUtils.renderAdditionalHud(poseStack, animatable, this.entityRenderDispatcher.cameraOrientation(), bufferSource);
   }

   private Vec3 getSeatPosition(BakedGeoModel model) {
      if (model != null) {
         GeoBone vcSeat = (GeoBone)model.getBone("head").get();
         GeoBone vcRoot = (GeoBone)model.getBone("trot").get();
         Vector3d seatPos = vcSeat.getLocalPosition();
         Vector3d rootPos = vcRoot.getLocalPosition();
         return new Vec3(seatPos.x * 0.5 - rootPos.x * 0.5, seatPos.y * 0.5 - rootPos.y * 0.5 - 2.8F, seatPos.z * 0.5 - rootPos.z * 0.5);
      } else {
         return Vec3.ZERO;
      }
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      Pmv01Entity animatable,
      BakedGeoModel model,
      RenderType renderType,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      this.scaleHeight = 0.5F;
      this.scaleWidth = 0.5F;
      this.setHummerVisibility(model, animatable.isSubMode());
      this.setWeaponsVisibility(model, animatable.isMainMode());
      this.setBoosterVisibilitty(model, animatable);
      super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
   }

   private void setBoosterVisibilitty(BakedGeoModel model, Pmv01Entity ent) {
      Vec3 vel = ent.getDeltaMovement();
      vel = vel.yRot((float)Math.toRadians(ent.getYRot()));
      if (vel.x > 1.0) {
         ((GeoBone)model.getBone("fire_rsb").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_lsb").get()).setHidden(true);
      } else if (vel.x < -1.0) {
         ((GeoBone)model.getBone("fire_rsb").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_lsb").get()).setHidden(false);
      } else {
         ((GeoBone)model.getBone("fire_rsb").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_lsb").get()).setHidden(true);
      }

      if (vel.z > 1.0) {
         ((GeoBone)model.getBone("fire_bpcr").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_bpcl").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_rl").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_ll").get()).setHidden(false);
      } else {
         ((GeoBone)model.getBone("fire_bpcr").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_bpcl").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_rl").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_ll").get()).setHidden(true);
      }

      if (ent.getDriverInput() != null && ent.getDriverInput().isJumpPressed()) {
         ((GeoBone)model.getBone("fire_bpr").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_bpl").get()).setHidden(false);
      } else {
         ((GeoBone)model.getBone("fire_bpr").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_bpl").get()).setHidden(true);
      }
   }

   private void setHummerVisibility(BakedGeoModel model, boolean b) {
      ((GeoBone)model.getBone("hummer").get()).setHidden(!b);
   }

   private void setWeaponsVisibility(BakedGeoModel model, boolean b) {
      ((GeoBone)model.getBone("gun").get()).setHidden(!b);
      ((GeoBone)model.getBone("pilebunker").get()).setHidden(!b);
      ((GeoBone)model.getBone("vmissile").get()).setHidden(!b);
      ((GeoBone)model.getBone("granadecanon").get()).setHidden(!b);
   }
}
