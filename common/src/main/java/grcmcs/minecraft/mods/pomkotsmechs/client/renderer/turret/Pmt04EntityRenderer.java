package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.turret.Pmt04EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.RenderUtils;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt04Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmt04EntityRenderer extends GeoEntityRenderer<Pmt04Entity> {
   public Pmt04EntityRenderer(Context renderManager) {
      super(renderManager, new Pmt04EntityModel());
   }

   public void render(Pmt04Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      super.m_7392_(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.f_114476_, 1.3F);
   }
}
