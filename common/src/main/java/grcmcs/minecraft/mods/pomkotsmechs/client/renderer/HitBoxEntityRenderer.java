package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.HitBoxEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class HitBoxEntityRenderer extends EntityRenderer<HitBoxEntity> {
   public HitBoxEntityRenderer(Context context) {
      super(context);
   }

   public void render(HitBoxEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
   }

   public ResourceLocation getTextureLocation(HitBoxEntity entity) {
      return null;
   }
}
