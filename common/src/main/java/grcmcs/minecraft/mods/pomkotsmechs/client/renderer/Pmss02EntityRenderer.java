package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmss02EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.Pmss02Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmss02EntityRenderer extends GeoEntityRenderer<Pmss02Entity> {
   public Pmss02EntityRenderer(Context renderManager) {
      super(renderManager, new Pmss02EntityModel());
   }

   public void render(Pmss02Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 1.0F);
   }
}
