package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmss03EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.Pmss03Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmss03EntityRenderer extends GeoEntityRenderer<Pmss03Entity> {
   public Pmss03EntityRenderer(Context renderManager) {
      super(renderManager, new Pmss03EntityModel());
   }

   public void render(Pmss03Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.m_7392_(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.f_114476_, 1.0F);
   }

   public int getPackedOverlay(Pmss03Entity entity, float packedLight, float partialTick) {
      if (!entity.isSelfDestructing()) {
         return super.getPackedOverlay(entity, packedLight, partialTick);
      }

      float fuse = entity.getFuse() - partialTick;
      return (int)(fuse / 5.0F) % 2 == 0 ? OverlayTexture.m_118093_(OverlayTexture.m_118088_(1.0F), 10) : OverlayTexture.f_118083_;
   }
}
