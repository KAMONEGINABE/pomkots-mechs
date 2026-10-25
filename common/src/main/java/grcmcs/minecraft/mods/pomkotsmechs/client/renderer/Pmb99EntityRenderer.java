package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmb99EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb99Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmb99EntityRenderer extends GeoEntityRenderer<Pmb99Entity> {
   public Pmb99EntityRenderer(Context renderManager) {
      super(renderManager, new Pmb99EntityModel());
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      Pmb99Entity animatable,
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

   public void actuallyRender(
      PoseStack poseStack,
      Pmb99Entity animatable,
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
      if (animatable.getControllingPassenger() != null) {
         animatable.seatPos = this.getSeatPosition(model);
         animatable.seatRots[0] = this.getSeatPitch(model);
      }
   }

   private Vec3 getSeatPosition(BakedGeoModel model) {
      if (model != null) {
         GeoBone vcSeat = (GeoBone)model.getBone("seet").get();
         GeoBone vcRoot = (GeoBone)model.getBone("root").get();
         Vector3d seatPos = vcSeat.getLocalPosition();
         Vector3d rootPos = vcRoot.getLocalPosition();
         return new Vec3(
            seatPos.x * 1.0 - rootPos.x * 1.0, seatPos.y * 1.0 - rootPos.y * 1.0 - ((Pmb99Entity)this.animatable).getPassengersRidingOffset(), seatPos.z * 1.0 - rootPos.z * 1.0
         );
      } else {
         return Vec3.ZERO;
      }
   }

   private float getSeatPitch(BakedGeoModel model) {
      if (model != null) {
         GeoBone vcSeat = (GeoBone)model.getBone("seet").get();
         return vcSeat != null ? vcSeat.getRotX() : 0.0F;
      } else {
         return 0.0F;
      }
   }

   private void seatInfo(BakedGeoModel model, Pmb99Entity animatable) {
      if (model != null) {
         GeoBone vcSeat = (GeoBone)model.getBone("seet").get();
         Vector3d seatPos = vcSeat.getWorldPosition();
         animatable.seatPos = new Vec3(seatPos.x, seatPos.y, seatPos.z);
         animatable.seatRots[0] = vcSeat.getRotX();
         animatable.seatRots[1] = vcSeat.getRotY();
      } else {
         animatable.seatPos = Vec3.ZERO;
         animatable.seatRots[0] = 0.0F;
         animatable.seatRots[1] = 0.0F;
      }
   }

   public void render(Pmb99Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      RenderUtils.renderBossBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 7.0F);
   }
}
