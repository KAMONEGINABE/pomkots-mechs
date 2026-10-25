package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidObjectiveEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class RaidObjectiveEntityRenderer extends EntityRenderer<RaidObjectiveEntity> {
   public RaidObjectiveEntityRenderer(Context context) {
      super(context);
   }

   public void render(RaidObjectiveEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
   }

   public ResourceLocation getTextureLocation(RaidObjectiveEntity entity) {
      return null;
   }
}
