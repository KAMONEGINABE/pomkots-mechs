package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.radar;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.PomkotsRadarItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.RadarTarget;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderStateShard.LineStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class RadarArrowRenderer {
   public static final RenderType WIREFRAME = RenderType.m_173215_(
      "radar_arrow_wireframe",
      DefaultVertexFormat.f_166851_,
      Mode.LINES,
      131072,
      false,
      true,
      CompositeState.m_110628_()
         .m_173292_(RenderStateShard.f_173095_)
         .m_110685_(RenderStateShard.f_110139_)
         .m_110663_(RenderStateShard.f_110111_)
         .m_110661_(RenderStateShard.f_110110_)
         .m_110673_(new LineStateShard(OptionalDouble.of(4.0)))
         .m_110691_(false)
   );
   public static final RenderType TRIANGLE = RenderType.m_173215_(
      "radar_cone",
      DefaultVertexFormat.f_85815_,
      Mode.TRIANGLES,
      131072,
      false,
      true,
      CompositeState.m_110628_()
         .m_173292_(RenderStateShard.f_173104_)
         .m_110685_(RenderStateShard.f_110139_)
         .m_110663_(RenderStateShard.f_110113_)
         .m_110661_(RenderStateShard.f_110158_)
         .m_110691_(false)
   );
   private static final int COLOR_R = 0;
   private static final int COLOR_G = 80;
   private static final int COLOR_B = 255;
   private static final int COLOR_A = 128;
   private static final float ARROW_BASE_SIZE = 0.4F;
   private static final float FORWARD_OFFSET = 3.0F;
   private static final Map<UUID, WeakReference<Entity>> entityCache = new HashMap<>();
   private static final Set<UUID> notFoundCache = new HashSet<>();

   public static void onRenderWorld(PoseStack poseStack, BufferSource bufferSource, Camera camera, float partialTick) {
      Minecraft mc = Minecraft.m_91087_();
      Player player = mc.f_91074_;
      if (player != null) {
         ItemStack stack = player.m_21205_();
         RadarTarget target = null;
         if (stack.m_41720_() instanceof PomkotsRadarItem) {
            target = PomkotsRadarItem.getActiveTarget(stack).orElse(null);
         } else if (stack.m_41720_() instanceof PomkotsDatapadItem) {
            target = PomkotsDatapadItem.getActiveTarget(stack).orElse(null);
         }

         if (target != null) {
            Vec3 camPos = camera.m_90583_();
            Vec3 centerPos = player.m_20318_(partialTick);
            Vec3 targetPos = resolveTargetPos(target, mc.f_91073_, partialTick);
            if (targetPos == null) {
               int blinkAlpha = (int)(getBlinkAlpha((ClientLevel)player.m_9236_(), partialTick) * 200.0F);
               if (blinkAlpha < 10) {
                  return;
               }

               Vec3 crossPos = centerPos.m_82520_(0.0, player.m_20206_() + 1.0, 0.0);
               poseStack.m_85836_();
               poseStack.m_85837_(crossPos.f_82479_ - camPos.f_82479_, crossPos.f_82480_ - camPos.f_82480_, crossPos.f_82481_ - camPos.f_82481_);
               poseStack.m_252781_(camera.m_253121_());
               poseStack.m_85841_(0.5F, 0.5F, 0.5F);
               VertexConsumer vc = bufferSource.m_6299_(WIREFRAME);
               renderCross(vc, poseStack.m_85850_(), 255, 80, 80, blinkAlpha);
               poseStack.m_85849_();
            } else {
               float time = (float)player.m_9236_().m_46467_() + partialTick;
               Vec3 diff = targetPos.m_82546_(centerPos);
               double dist = diff.m_82553_();
               if (dist < 1.0) {
                  return;
               }

               Vec3 dir = diff.m_82541_();
               Vec3 playerCenter = centerPos.m_82520_(0.0, player.m_20206_() * 0.5, 0.0);
               float bobOffset = (float)(Math.sin(time / 10.0) * 0.5);
               Vec3 arrowWorldPos = playerCenter.m_82549_(dir.m_82490_(4.0F + bobOffset));
               float scale = (float)Mth.m_14008_(dist / 30.0, 0.5, 2.0) * 0.4F;
               poseStack.m_85836_();
               poseStack.m_85837_(arrowWorldPos.f_82479_ - camPos.f_82479_, arrowWorldPos.f_82480_ - camPos.f_82480_, arrowWorldPos.f_82481_ - camPos.f_82481_);
               alignToDirection(poseStack, dir);
               float rotation = (float)(time / 20.0 * Math.PI);
               poseStack.m_252781_(new Quaternionf().rotateZ(rotation));
               poseStack.m_85841_(scale, scale, scale);
               VertexConsumer vc = bufferSource.m_6299_(TRIANGLE);
               renderCone(vc, poseStack.m_85850_(), 0, 80, 255, 128);
               poseStack.m_85849_();
            }
         }
      }
   }

   private static void alignToDirection(PoseStack poseStack, Vec3 direction) {
      Vec3 from = new Vec3(0.0, 0.0, 1.0);
      Vec3 axis = from.m_82537_(direction);
      double axisLen = axis.m_82553_();
      if (axisLen < 1.0E-6) {
         if (direction.f_82481_ < 0.0) {
            poseStack.m_252781_(new Quaternionf().rotateY((float) Math.PI));
         }
      } else {
         double angle = Math.acos(Mth.m_14008_(from.m_82526_(direction), -1.0, 1.0));
         Vec3 normalizedAxis = axis.m_82541_();
         poseStack.m_252781_(
            new Quaternionf().rotateAxis((float)angle, (float)normalizedAxis.f_82479_, (float)normalizedAxis.f_82480_, (float)normalizedAxis.f_82481_)
         );
      }
   }

   private static float getBlinkAlpha(ClientLevel level, float partialTick) {
      float time = ((float)level.m_46467_() + partialTick) / 10.0F;
      return (float)(Math.sin(time * Math.PI) * 0.5 + 0.5);
   }

   private static void renderCross(VertexConsumer vc, Pose pose, int r, int g, int b, int a) {
      float s = 0.5F;
      line(vc, pose, -s, s, 0.0F, s, -s, 0.0F, r, g, b, a);
      line(vc, pose, s, s, 0.0F, -s, -s, 0.0F, r, g, b, a);
   }

   private static void line(VertexConsumer vc, Pose pose, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int b, int a) {
      float nx = x2 - x1;
      float ny = y2 - y1;
      float nz = z2 - z1;
      float len = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
      nx /= len;
      ny /= len;
      nz /= len;
      vc.m_252986_(pose.m_252922_(), x1, y1, z1).m_6122_(r, g, b, a).m_252939_(pose.m_252943_(), nx, ny, nz).m_5752_();
      vc.m_252986_(pose.m_252922_(), x2, y2, z2).m_6122_(r, g, b, a).m_252939_(pose.m_252943_(), nx, ny, nz).m_5752_();
   }

   private static void renderCone(VertexConsumer vc, Pose pose, int r, int g, int b, int a) {
      float tipZ = 2.5F;
      float baseR = 0.4F;
      float tp = 0.0F;
      float ty = 0.0F;
      float tz = tipZ;
      float v1x = (float)(-Math.sqrt(3.0) / 2.0 * baseR);
      float v1y = -baseR * 0.5F;
      float v2x = (float)(Math.sqrt(3.0) / 2.0 * baseR);
      float v2y = -baseR * 0.5F;
      float v3x = 0.0F;
      float v3y = baseR;
      triShaded(vc, pose, tp, ty, tz, v3x, v3y, 0.0F, v1x, v1y, 0.0F, r, g, b, a, 0.5F);
      triShaded(vc, pose, tp, ty, tz, v2x, v2y, 0.0F, v3x, v3y, 0.0F, r, g, b, a, 0.7F);
      triShaded(vc, pose, tp, ty, tz, v1x, v1y, 0.0F, v2x, v2y, 0.0F, r, g, b, a, 0.9F);
      triShaded(vc, pose, v1x, v1y, 0.0F, v3x, v3y, 0.0F, v2x, v2y, 0.0F, r, g, b, a, 0.3F);
   }

   private static void triShaded(
      VertexConsumer vc,
      Pose pose,
      float x1,
      float y1,
      float z1,
      float x2,
      float y2,
      float z2,
      float x3,
      float y3,
      float z3,
      int r,
      int g,
      int b,
      int a,
      float brightness
   ) {
      r = (int)(r * brightness);
      g = (int)(g * brightness);
      b = (int)(b * brightness);
      vc.m_252986_(pose.m_252922_(), x1, y1, z1).m_6122_(r, g, b, a).m_5752_();
      vc.m_252986_(pose.m_252922_(), x2, y2, z2).m_6122_(r, g, b, a).m_5752_();
      vc.m_252986_(pose.m_252922_(), x3, y3, z3).m_6122_(r, g, b, a).m_5752_();
   }

   private static Vec3 resolveTargetPos(RadarTarget target, ClientLevel level, float partialTick) {
      if (target instanceof RadarTarget.CoordTarget c) {
         return Vec3.m_82512_(c.pos());
      } else if (target instanceof RadarTarget.EntityTarget e) {
         Entity cached = Optional.ofNullable(entityCache.get(e.uuid())).map(Reference::get).orElse(null);
         if (cached != null && cached.m_6084_()) {
            return cached.m_20318_(partialTick);
         } else if (notFoundCache.contains(e.uuid()) && level.m_46467_() % 100L != 0L) {
            return null;
         } else {
            Entity found = (Entity)level.m_6443_(Entity.class, new AABB(level.m_220360_()).m_82400_(999999.0), en -> en.m_20148_().equals(e.uuid()))
               .stream()
               .findFirst()
               .orElse(null);
            if (found != null) {
               entityCache.put(e.uuid(), new WeakReference<>(found));
               notFoundCache.remove(e.uuid());
               return found.m_20318_(partialTick);
            } else {
               entityCache.remove(e.uuid());
               notFoundCache.add(e.uuid());
               return null;
            }
         }
      } else {
         return null;
      }
   }
}
