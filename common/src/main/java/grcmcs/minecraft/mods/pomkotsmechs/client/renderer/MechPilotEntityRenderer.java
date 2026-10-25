package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.MechPilotEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MechPilotEntityRenderer extends GeoEntityRenderer<MechPilotEntity> {
   public MechPilotEntityRenderer(Context renderManager) {
      super(renderManager, new MechPilotEntityModel());
   }

   public void actuallyRender(
      PoseStack poseStack,
      MechPilotEntity animatable,
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
      if (animatable.m_20202_() instanceof PomkotsVehicle robot) {
         Vec3 seatPos = robot.getClientSeatPos(animatable);
         poseStack.m_85836_();
         if (robot instanceof Pmvc01Entity pmvc && pmvc.getHeadParts().m_41720_() instanceof BasePartsItem.Head h && h.isFullCovered()) {
            poseStack.m_85837_(seatPos.m_7096_(), seatPos.m_7098_() - 1.0, seatPos.m_7094_());
         } else {
            poseStack.m_85837_(seatPos.m_7096_(), seatPos.m_7098_(), seatPos.m_7094_());
         }

         super.actuallyRender(
            poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha
         );
         poseStack.m_85849_();
      } else {
         super.actuallyRender(
            poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha
         );
      }
   }
}
