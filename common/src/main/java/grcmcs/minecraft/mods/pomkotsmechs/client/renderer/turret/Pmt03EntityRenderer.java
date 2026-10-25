package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.turret.Pmt03EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.RenderUtils;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt03Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmt03EntityRenderer extends GeoEntityRenderer<Pmt03Entity> {
   public Pmt03EntityRenderer(Context renderManager) {
      super(renderManager, new Pmt03EntityModel());
   }

   public void render(Pmt03Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 1.3F);
   }
}
