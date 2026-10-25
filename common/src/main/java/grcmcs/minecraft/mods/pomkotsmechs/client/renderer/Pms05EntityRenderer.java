package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pms05EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms05Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pms05EntityRenderer extends GeoEntityRenderer<Pms05Entity> {
   public Pms05EntityRenderer(Context renderManager) {
      super(renderManager, new Pms05EntityModel());
   }

   public void render(Pms05Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 1.3F);
   }
}
