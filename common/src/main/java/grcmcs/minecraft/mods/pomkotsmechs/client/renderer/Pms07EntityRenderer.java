package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pms07EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms07Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pms07EntityRenderer extends GeoEntityRenderer<Pms07Entity> {
   public Pms07EntityRenderer(Context renderManager) {
      super(renderManager, new Pms07EntityModel());
   }

   public void render(Pms07Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 1.3F);
   }
}
