package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pms04EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms04Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pms04EntityRenderer extends GeoEntityRenderer<Pms04Entity> {
   public Pms04EntityRenderer(Context renderManager) {
      super(renderManager, new Pms04EntityModel());
   }

   public void render(Pms04Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.m_7392_(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.f_114476_, 1.3F);
   }
}
