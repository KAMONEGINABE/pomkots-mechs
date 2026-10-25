package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pms01EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms01Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pms01EntityRenderer extends GeoEntityRenderer<Pms01Entity> {
   public Pms01EntityRenderer(Context renderManager) {
      super(renderManager, new Pms01EntityModel());
   }

   public void render(Pms01Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.m_7392_(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.f_114476_, 1.3F);
   }
}
