package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.scan;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.misc.scan.ScanPulseEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class ScanPulseEntityRenderer extends EntityRenderer<ScanPulseEntity> {
   private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation("pomkotsmechs", "textures/misc/empty.png");

   public ScanPulseEntityRenderer(Context context) {
      super(context);
   }

   public void render(ScanPulseEntity pulse, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      Player localPlayer = Minecraft.m_91087_().f_91074_;
      if (localPlayer != null && pulse.isVisibleTo(localPlayer)) {
         float radius = pulse.getRadius(partialTick);
         float maxRadius = pulse.getMaxRadius();
         float fadeStartRadius = maxRadius * 0.8F;
         float alphaMultiplier;
         if (radius <= fadeStartRadius) {
            alphaMultiplier = 1.0F;
         } else {
            alphaMultiplier = 1.0F - Mth.m_14036_((radius - fadeStartRadius) / (maxRadius - fadeStartRadius), 0.0F, 1.0F);
         }

         ScanDomeRenderer.render(poseStack, bufferSource, radius, alphaMultiplier);
         super.m_7392_(pulse, entityYaw, partialTick, poseStack, bufferSource, packedLight);
      }
   }

   public ResourceLocation getTextureLocation(ScanPulseEntity entity) {
      return EMPTY_TEXTURE;
   }

   public boolean shouldRender(ScanPulseEntity pulse, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
      Player localPlayer = Minecraft.m_91087_().f_91074_;
      if (localPlayer != null && pulse.isVisibleTo(localPlayer)) {
         double radius = pulse.getMaxRadius();
         AABB domeBox = new AABB(
            pulse.m_20185_() - radius,
            pulse.m_20186_() - radius,
            pulse.m_20189_() - radius,
            pulse.m_20185_() + radius,
            pulse.m_20186_() + radius,
            pulse.m_20189_() + radius
         );
         return frustum.m_113029_(domeBox);
      } else {
         return false;
      }
   }
}
