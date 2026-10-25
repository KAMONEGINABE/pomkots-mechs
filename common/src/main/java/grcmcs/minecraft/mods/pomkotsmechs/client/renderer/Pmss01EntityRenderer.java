package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmss01EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.Pmss01Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmss01EntityRenderer extends GeoEntityRenderer<Pmss01Entity> {
   public Pmss01EntityRenderer(Context renderManager) {
      super(renderManager, new Pmss01EntityModel());
   }

   public void render(Pmss01Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.m_7392_(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.f_114476_, 1.0F);
   }
}
