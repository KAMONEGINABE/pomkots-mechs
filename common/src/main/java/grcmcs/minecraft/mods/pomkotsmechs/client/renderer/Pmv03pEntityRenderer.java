package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmv03pEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv03pEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class Pmv03pEntityRenderer extends GeoEntityRenderer<Pmv03pEntity> {
   private static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");

   public Pmv03pEntityRenderer(Context renderManager) {
      this(renderManager, new Pmv03pEntityModel());
   }

   public Pmv03pEntityRenderer(Context renderManager, Pmv03pEntityModel model) {
      super(renderManager, model);
      this.addRenderLayer(new AutoGlowingGeoLayer(this));
   }

   public void actuallyRender(
      PoseStack poseStack,
      Pmv03pEntity animatable,
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
      float xrot = this.interpolateAngle(partialTick, animatable.f_19860_, animatable.m_146909_());
      ((GeoBone)model.getBone("body").get()).setRotX((float)Math.toRadians(-xrot));
      super.actuallyRender(
         poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha
      );
      if (animatable.getDrivingPassenger() != null) {
         Vec3 p1 = this.getSeatPosition(model, 1);
         Vec3 p2 = this.getSeatPosition(model, 2);
         animatable.setClientSeatPos(p1);
         animatable.setClientSeatPos1(p1);
         animatable.setClientSeatPos2(p2);
      }

      RenderUtils.renderAdditionalHud(poseStack, animatable, this.f_114476_.m_253208_(), bufferSource);
   }

   private float interpolateAngle(float partialTicks, float prevAngle, float currentAngle) {
      float wrappedPrev = Mth.m_14177_(prevAngle);
      float wrappedCurrent = Mth.m_14177_(currentAngle);
      float delta = wrappedCurrent - wrappedPrev;
      if (delta < -180.0F) {
         delta += 360.0F;
      }

      if (delta > 180.0F) {
         delta -= 360.0F;
      }

      return Mth.m_14177_(wrappedPrev + partialTicks * delta);
   }

   private Vec3 getSeatPosition(BakedGeoModel model, int num) {
      if (model != null) {
         GeoBone vcSeat = (GeoBone)model.getBone("seat" + num).get();
         GeoBone vcRoot = (GeoBone)model.getBone("trot").get();
         Vector3d seatPos = vcSeat.getLocalPosition();
         Vector3d rootPos = vcRoot.getLocalPosition();
         return new Vec3(seatPos.x * 1.0 - rootPos.x * 1.0, seatPos.y * 1.0 - rootPos.y * 1.0 - 2.8F, seatPos.z * 1.0 - rootPos.z * 1.0);
      } else {
         return Vec3.f_82478_;
      }
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      Pmv03pEntity animatable,
      BakedGeoModel model,
      RenderType renderType,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      this.scaleHeight = 1.0F;
      this.scaleWidth = 1.0F;
      super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
   }
}
