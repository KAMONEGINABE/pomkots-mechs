package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.scan;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public final class ScanDomeRenderer {
   private static final int LATITUDE_SEGMENTS = 24;
   private static final int LONGITUDE_SEGMENTS = 48;

   private ScanDomeRenderer() {
   }

   public static void render(PoseStack poseStack, MultiBufferSource bufferSource, float radius, float alphaMultiplier) {
      if (!(radius <= 0.01F) && !(alphaMultiplier <= 0.001F)) {
         VertexConsumer consumer = bufferSource.m_6299_(ScanRenderTypes.SCAN_DOME);
         Matrix4f matrix = poseStack.m_85850_().m_252922_();
         float red = 0.1F;
         float green = 1.0F;
         float blue = 0.3F;
         float baseAlpha = 0.4F;
         float alpha = baseAlpha * alphaMultiplier;

         for (int latitude = 0; latitude < 24; latitude++) {
            double theta1 = (-Math.PI / 2) + Math.PI * latitude / 24.0;
            double theta2 = (-Math.PI / 2) + Math.PI * (latitude + 1) / 24.0;

            for (int longitude = 0; longitude < 48; longitude++) {
               double phi1 = (Math.PI * 2) * longitude / 48.0;
               double phi2 = (Math.PI * 2) * (longitude + 1) / 48.0;
               addSphereVertex(consumer, matrix, radius, theta1, phi1, red, green, blue, alpha);
               addSphereVertex(consumer, matrix, radius, theta2, phi1, red, green, blue, alpha);
               addSphereVertex(consumer, matrix, radius, theta2, phi2, red, green, blue, alpha);
               addSphereVertex(consumer, matrix, radius, theta1, phi2, red, green, blue, alpha);
            }
         }
      }
   }

   private static void addSphereVertex(
      VertexConsumer consumer, Matrix4f matrix, float radius, double latitude, double longitude, float red, float green, float blue, float alpha
   ) {
      double cosLatitude = Math.cos(latitude);
      float x = (float)(radius * cosLatitude * Math.cos(longitude));
      float y = (float)(radius * Math.sin(latitude));
      float z = (float)(radius * cosLatitude * Math.sin(longitude));
      consumer.m_252986_(matrix, x, y, z).m_85950_(red, green, blue, alpha).m_5752_();
   }
}
