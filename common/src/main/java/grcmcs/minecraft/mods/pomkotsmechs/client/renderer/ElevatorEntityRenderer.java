package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.ElevetorEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.ElevatorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ElevatorEntityRenderer extends GeoEntityRenderer<ElevatorEntity> {
   public ElevatorEntityRenderer(Context renderManager) {
      super(renderManager, new ElevetorEntityModel());
   }

   public void render(ElevatorEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.m_7392_(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      if (entity.getMode() == ElevatorEntity.Mode.WAITING && entity.m_20197_().isEmpty()) {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null && mc.f_91074_.m_20148_().equals(entity.getPassengerUUID())) {
            RenderUtils.renderMarking(entity, poseStack, bufferSource, this.f_114476_, 1.3F);
         }
      }
   }

   public boolean shouldShowName(ElevatorEntity entity) {
      return false;
   }
}
