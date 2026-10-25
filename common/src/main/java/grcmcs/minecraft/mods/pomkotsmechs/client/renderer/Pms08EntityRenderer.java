package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pms08EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms08Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pms08EntityRenderer extends GeoEntityRenderer<Pms08Entity> {
   public Pms08EntityRenderer(Context renderManager) {
      super(renderManager, new Pms08EntityModel());
   }

   public void render(Pms08Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 1.3F);
   }
}
