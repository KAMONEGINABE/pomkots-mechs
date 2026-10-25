package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmv03EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv03Entity;
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

public class Pmv03EntityRenderer extends GeoEntityRenderer<Pmv03Entity> {
   private static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");

   public Pmv03EntityRenderer(Context renderManager) {
      this(renderManager, new Pmv03EntityModel());
   }

   public Pmv03EntityRenderer(Context renderManager, Pmv03EntityModel model) {
      super(renderManager, model);
      this.addRenderLayer(new AutoGlowingGeoLayer(this));
   }

   public void actuallyRender(
      PoseStack poseStack,
      Pmv03Entity animatable,
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
         animatable.setMainCameraPosition(this.getMainCameraPosition(model, animatable, partialTick, Pmv03Entity.DEFAULT_SCALE));
      }

      RenderUtils.renderAdditionalHud(poseStack, animatable, this.f_114476_.m_253208_(), bufferSource);
   }

   private Vec3 getSeatPosition(BakedGeoModel model) {
      if (model != null) {
         GeoBone vcSeat = (GeoBone)model.getBone("head").get();
         GeoBone vcRoot = (GeoBone)model.getBone("root").get();
         Vector3d seatPos = vcSeat.getLocalPosition();
         Vector3d rootPos = vcRoot.getLocalPosition();
         return new Vec3(
            seatPos.x * Pmv03Entity.DEFAULT_SCALE - rootPos.x * Pmv03Entity.DEFAULT_SCALE,
            seatPos.y * Pmv03Entity.DEFAULT_SCALE - rootPos.y * Pmv03Entity.DEFAULT_SCALE - 6.0,
            seatPos.z * Pmv03Entity.DEFAULT_SCALE - rootPos.z * Pmv03Entity.DEFAULT_SCALE
         );
      } else {
         return Vec3.f_82478_;
      }
   }

   private Vec3 getMainCameraPosition(BakedGeoModel model, Pmv03Entity entity, float partialTick, float scale) {
      if (model != null) {
         GeoBone vcSeat = (GeoBone)model.getBone("maincam").get();
         Vector3d seatPos = vcSeat.getLocalPosition();
         return new Vec3(seatPos.x * scale, seatPos.y * scale, seatPos.z * scale);
      } else {
         return Vec3.f_82478_;
      }
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      Pmv03Entity animatable,
      BakedGeoModel model,
      RenderType renderType,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      this.scaleHeight = Pmv03Entity.DEFAULT_SCALE;
      this.scaleWidth = Pmv03Entity.DEFAULT_SCALE;
      this.setBoosterVisibilitty(model, animatable);
      super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
   }

   private void setBoosterVisibilitty(BakedGeoModel model, Pmv03Entity ent) {
      Vec3 vel = ent.m_20184_();
      vel = vel.m_82524_((float)Math.toRadians(ent.m_146908_()));
      if (vel.f_82479_ > 1.0) {
         ((GeoBone)model.getBone("fire_r1").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_l1").get()).setHidden(true);
      } else if (vel.f_82479_ < -1.0) {
         ((GeoBone)model.getBone("fire_r1").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_l1").get()).setHidden(false);
      } else {
         ((GeoBone)model.getBone("fire_r1").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_l1").get()).setHidden(true);
      }

      if (vel.f_82481_ > 1.0) {
         ((GeoBone)model.getBone("fire_r3").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_l3").get()).setHidden(false);
      } else {
         ((GeoBone)model.getBone("fire_r3").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_l3").get()).setHidden(true);
      }

      if (ent.getDriverInput() != null && ent.getDriverInput().isJumpPressed()) {
         ((GeoBone)model.getBone("fire_r2").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_l2").get()).setHidden(false);
      } else {
         ((GeoBone)model.getBone("fire_r2").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_l2").get()).setHidden(true);
      }
   }
}
