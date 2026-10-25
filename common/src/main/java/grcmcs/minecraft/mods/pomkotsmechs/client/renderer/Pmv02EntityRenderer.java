package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmv02EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv02Entity;
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

public class Pmv02EntityRenderer extends GeoEntityRenderer<Pmv02Entity> {
   private static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");

   public Pmv02EntityRenderer(Context renderManager) {
      this(renderManager, new Pmv02EntityModel());
   }

   public Pmv02EntityRenderer(Context renderManager, Pmv02EntityModel model) {
      super(renderManager, model);
      this.addRenderLayer(new AutoGlowingGeoLayer(this));
   }

   public void actuallyRender(
      PoseStack poseStack,
      Pmv02Entity animatable,
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

      RenderUtils.renderAdditionalHud(poseStack, animatable, this.f_114476_.m_253208_(), bufferSource);
   }

   private Vec3 getSeatPosition(BakedGeoModel model) {
      if (model != null) {
         GeoBone vcSeat = (GeoBone)model.getBone("head").get();
         GeoBone vcRoot = (GeoBone)model.getBone("trot").get();
         Vector3d seatPos = vcSeat.getLocalPosition();
         Vector3d rootPos = vcRoot.getLocalPosition();
         return new Vec3(seatPos.x * 0.5 - rootPos.x * 0.5, seatPos.y * 0.5 - rootPos.y * 0.5 - 2.8F, seatPos.z * 0.5 - rootPos.z * 0.5);
      } else {
         return Vec3.f_82478_;
      }
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      Pmv02Entity animatable,
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
      if (animatable.isDrilling() && animatable.getDrivingPassenger() != null) {
         Vec3 look = animatable.getDrivingPassenger().m_20154_();
         float pitch = (float)Math.asin(look.f_82480_);
         ((GeoBone)model.getBone("left_upper_arm").get()).setRotX(pitch);
      }

      this.setBoosterVisibilitty(model, animatable);
      super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
   }

   private void setBoosterVisibilitty(BakedGeoModel model, Pmv02Entity ent) {
      Vec3 vel = ent.m_20184_();
      vel = vel.m_82524_((float)Math.toRadians(ent.m_146908_()));
      if (vel.f_82479_ > 1.0) {
         ((GeoBone)model.getBone("fire_rsb").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_lsb").get()).setHidden(true);
      } else if (vel.f_82479_ < -1.0) {
         ((GeoBone)model.getBone("fire_rsb").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_lsb").get()).setHidden(false);
      } else {
         ((GeoBone)model.getBone("fire_rsb").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_lsb").get()).setHidden(true);
      }

      if (vel.f_82481_ > 1.0) {
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
