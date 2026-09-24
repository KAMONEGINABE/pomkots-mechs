package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Client-only, short-lived ribbons following the four animated flight wing tips. */
final class Pmvc01FlightTrailRenderer {
    private static final String[] BONES = {
            "flight_trail_wing_right", "flight_trail_wing_right2",
            "flight_trail_wing_left", "flight_trail_wing_left2"
    };
    private static final double MAX_VIEW_DISTANCE_SQR = 180.0D * 180.0D;
    private static final double MAX_SEGMENT_LENGTH_SQR = 16.0D * 16.0D;
    private static final float LIFETIME_TICKS = 9.0F;
    private static final int MAX_SAMPLES = 24;
    private static final double TRAIL_HALF_WIDTH = 0.12D;
    private static final float TRAIL_ALPHA = 0.25F;
    private static final RenderType TRAIL_RENDER_TYPE = RenderType.debugQuads();
    private static final Map<Pmvc01Entity, TrailState> TRAILS = new WeakHashMap<>();

    private Pmvc01FlightTrailRenderer() {}

    static void enableBoneTracking(BakedGeoModel model, boolean enabled) {
        if (!enabled) return;
        for (String name : BONES) {
            model.getBone(name).ifPresent(GeoBone::getWorldSpaceMatrix);
        }
    }

    static void captureBone(Pmvc01Entity mech, GeoBone bone, float partialTick) {
        if (!mech.isFlightMode() || !mech.hasFlightUnit()) return;
        for (int index = 0; index < BONES.length; index++) {
            if (!BONES[index].equals(bone.getName())) continue;
            // This callback runs after GeckoLib applies the complete animated
            // parent chain and updates the bone's world-space matrix. Subtract
            // the root from the same render pass: GeckoLib's absolute matrix
            // can include camera/render translations that vary while turning.
            GeoBone root = bone;
            while (root.getParent() != null) root = root.getParent();
            var world = bone.getWorldPosition();
            var rootWorld = root.getWorldPosition();
            Vec3 renderedOrigin = new Vec3(
                    Mth.lerp(partialTick, mech.xo, mech.getX()),
                    Mth.lerp(partialTick, mech.yo, mech.getY()),
                    Mth.lerp(partialTick, mech.zo, mech.getZ()));
            Vec3 tip = renderedOrigin.add(
                    world.x - rootWorld.x,
                    world.y - rootWorld.y,
                    world.z - rootWorld.z);
            if (!Double.isFinite(tip.x) || !Double.isFinite(tip.y)
                    || !Double.isFinite(tip.z)
                    || tip.distanceToSqr(renderedOrigin) > 20.0D * 20.0D) return;
            TrailState state = TRAILS.computeIfAbsent(mech, ignored -> new TrailState());
            state.captured[index] = tip;
            state.captureTime[index] = mech.level().getGameTime() + partialTick;
            return;
        }
    }

    static void render(PoseStack poseStack, Pmvc01Entity mech,
                       MultiBufferSource buffers, float partialTick) {
        if (mech.isRemoved()) {
            TRAILS.remove(mech);
            return;
        }

        boolean emitting = mech.isFlightMode() && mech.hasFlightUnit();

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        if (mech.distanceToSqr(camera) > MAX_VIEW_DISTANCE_SQR) {
            TRAILS.remove(mech);
            return;
        }

        float now = mech.level().getGameTime() + partialTick;
        Vec3 renderedOrigin = new Vec3(
                Mth.lerp(partialTick, mech.xo, mech.getX()),
                Mth.lerp(partialTick, mech.yo, mech.getY()),
                Mth.lerp(partialTick, mech.zo, mech.getZ()));
        TrailState state = emitting ? TRAILS.computeIfAbsent(mech, ignored -> new TrailState())
                : TRAILS.get(mech);
        if (state == null) return;
        VertexConsumer vertices = buffers.getBuffer(TRAIL_RENDER_TYPE);
        Matrix4f matrix = poseStack.last().pose();
        boolean anySamplesLeft = false;

        for (int index = 0; index < BONES.length; index++) {
            Deque<Sample> samples = state.trails.get(index);
            if (emitting) {
                Vec3 tip = state.captured[index];
                if (tip != null && Math.abs(now - state.captureTime[index]) < 0.01F) {
                    addSample(samples, tip, now);
                }
            }
            while (!samples.isEmpty() && now - samples.peekFirst().time > LIFETIME_TICKS) {
                samples.removeFirst();
            }
            anySamplesLeft |= !samples.isEmpty();
            renderRibbon(vertices, matrix, samples, camera, renderedOrigin, now);
        }
        if (!emitting && !anySamplesLeft) TRAILS.remove(mech);
    }

    private static void addSample(Deque<Sample> samples, Vec3 tip, float now) {
        Sample last = samples.peekLast();
        if (last != null) {
            if (now < last.time || now - last.time > LIFETIME_TICKS
                    || tip.distanceToSqr(last.position) > MAX_SEGMENT_LENGTH_SQR) {
                samples.clear();
            } else if (now - last.time < 0.005F) {
                // Iris may render shadows before the main pass at the same
                // timestamp. Keep the latest (visible-pass) position.
                samples.removeLast();
            } else if (now - last.time < 0.35F && tip.distanceToSqr(last.position) < 0.04D) {
                return;
            }
        }
        samples.addLast(new Sample(tip, now));
        while (samples.size() > MAX_SAMPLES) samples.removeFirst();
    }

    private static void renderRibbon(VertexConsumer vertices, Matrix4f matrix,
                                     Deque<Sample> samples, Vec3 camera,
                                     Vec3 renderedOrigin, float now) {
        Sample[] points = samples.toArray(new Sample[0]);
        if (points.length < 2) return;
        Vec3[] sides = new Vec3[points.length];
        for (int i = 0; i < points.length; i++) {
            Vec3 before = points[Math.max(0, i - 1)].position;
            Vec3 after = points[Math.min(points.length - 1, i + 1)].position;
            Vec3 tangent = after.subtract(before);
            Vec3 across = tangent.cross(camera.subtract(points[i].position));
            if (across.lengthSqr() < 1.0E-6D) across = tangent.cross(new Vec3(0, 1, 0));
            if (across.lengthSqr() < 1.0E-6D) across = tangent.cross(new Vec3(1, 0, 0));
            across = across.normalize();
            if (i > 0 && across.dot(sides[i - 1]) < 0) across = across.scale(-1);
            sides[i] = across;
        }
        for (int i = 1; i < points.length; i++) {
            Sample previous = points[i - 1];
            Sample current = points[i];
            if (current.position.distanceToSqr(previous.position) < 1.0E-5D) continue;
            float oldFade = Mth.clamp(1.0F - (now - previous.time) / LIFETIME_TICKS, 0, 1);
            float newFade = Mth.clamp(1.0F - (now - current.time) / LIFETIME_TICKS, 0, 1);
            emitSegment(vertices, matrix, previous.position.subtract(renderedOrigin),
                    current.position.subtract(renderedOrigin), sides[i - 1], sides[i], TRAIL_HALF_WIDTH,
                    255, 255, 255, oldFade * TRAIL_ALPHA, newFade * TRAIL_ALPHA);
        }
    }

    private static void emitSegment(VertexConsumer vertices, Matrix4f matrix,
                                    Vec3 from, Vec3 to, Vec3 fromAcross, Vec3 toAcross, double halfWidth,
                                    int red, int green, int blue, float fromAlpha, float toAlpha) {
        Vec3 fromSide = fromAcross.scale(halfWidth);
        Vec3 toSide = toAcross.scale(halfWidth);
        vertex(vertices, matrix, from.subtract(fromSide), red, green, blue, fromAlpha);
        vertex(vertices, matrix, from.add(fromSide), red, green, blue, fromAlpha);
        vertex(vertices, matrix, to.add(toSide), red, green, blue, toAlpha);
        vertex(vertices, matrix, to.subtract(toSide), red, green, blue, toAlpha);
    }

    private static void vertex(VertexConsumer vertices, Matrix4f matrix, Vec3 position,
                               int red, int green, int blue, float alpha) {
        vertices.vertex(matrix, (float)position.x, (float)position.y, (float)position.z)
                .color(red, green, blue, Mth.clamp((int)(alpha * 255.0F), 0, 255))
                .endVertex();
    }

    private record Sample(Vec3 position, float time) {}

    private static final class TrailState {
        private final List<Deque<Sample>> trails = List.of(
                new ArrayDeque<>(), new ArrayDeque<>(),
                new ArrayDeque<>(), new ArrayDeque<>());
        private final Vec3[] captured = new Vec3[BONES.length];
        private final float[] captureTime = new float[BONES.length];
    }
}
