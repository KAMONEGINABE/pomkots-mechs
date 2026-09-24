package grcmcs.minecraft.mods.pomkotsmechs.client.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Lightweight, deterministic flight-speed feedback. No particles or post-processing. */
public final class FlightSpeedEffects {
    private static final int LINE_COUNT = 72;
    private static float boostBlend;
    private static long lastFrameNanos;
    private static float flightBlend;
    private static float verticalDodgeBlend;
    private static long lastHudFrameNanos;

    private FlightSpeedEffects() {}

    public static float boostBlend() {
        Minecraft minecraft = Minecraft.getInstance();
        long now = System.nanoTime();
        float elapsed = lastFrameNanos == 0 ? 0 : Mth.clamp((now - lastFrameNanos) / 1_000_000_000.0F, 0, 0.1F);
        lastFrameNanos = now;
        boolean boosting = minecraft.player != null
                && minecraft.player.getVehicle() instanceof Pmvc01Entity mech
                && mech.isFlightMode() && mech.isFlightSuperBoost();
        boostBlend = Mth.lerp(Math.min(1.0F, elapsed * 5.0F), boostBlend, boosting ? 1.0F : 0.0F);
        return boostBlend;
    }

    public static void render(GuiGraphics graphics, Pmvc01Entity mech, float partialTick) {
        long now = System.nanoTime();
        float elapsed = lastHudFrameNanos == 0 ? 0.0F
                : Mth.clamp((now - lastHudFrameNanos) / 1_000_000_000.0F, 0.0F, 0.1F);
        lastHudFrameNanos = now;
        boolean flying = mech != null && mech.isFlightMode();
        flightBlend = Mth.lerp(Math.min(1.0F, elapsed * 4.0F), flightBlend, flying ? 1.0F : 0.0F);
        // A vertical flip briefly contradicts the forward-flow perspective. Dim the
        // streaks during it, then let them return smoothly instead of snapping.
        boolean verticalDodge = flying && mech.isFlightVerticalEvasion();
        verticalDodgeBlend = Mth.lerp(Math.min(1.0F, elapsed * 10.0F),
                verticalDodgeBlend, verticalDodge ? 1.0F : 0.0F);
        if (flightBlend < 0.005F) return;
        float boost = boostBlend();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        float time = Minecraft.getInstance().level.getGameTime() + partialTick;
        float centerX = width * 0.5F;
        float centerY = height * 0.5F;
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f pose = graphics.pose().last().pose();
        for (int i = 0; i < LINE_COUNT; i++) {
            // Keep geometry and speed independent of boostBlend: changing a multiplier of
            // absolute time here caused every streak to jump during acceleration.
            double angle = (i + hash(i * 19 + 7) * 0.75D) * Math.PI * 2.0D / LINE_COUNT;
            float dirX = (float) Math.cos(angle);
            float dirY = (float) Math.sin(angle);
            float edgeX = dirX * width * 0.58F;
            float edgeY = dirY * height * 0.65F;
            float progress = (time * (0.065F + hash(i * 29 + 5) * 0.020F)
                    + hash(i * 37 + 11)) % 1.0F;
            float start = 0.12F + progress * 0.84F;
            float length = 0.3F + hash(i * 13 + 3) * 0.50F;
            float end = Math.min(1.15F, start + length);
            float x0 = centerX + edgeX * start;
            float y0 = centerY + edgeY * start;
            float x1 = centerX + edgeX * end;
            float y1 = centerY + edgeY * end;
            // Reserve every third angular lane for boost, rather than a contiguous
            // range of indices that leaves one sector empty during normal flight.
            float visibility = (i % 3 == 0 ? boost * 0.85F : 1.0F)
                    * flightBlend * (1.0F - verticalDodgeBlend * 0.82F);
            float strength = 0.55F + hash(i * 43 + 23) * 0.85F;
            float outerWeight = Mth.square(Mth.clamp((end - 0.08F) / 0.78F, 0.0F, 1.0F));
            int alpha = Mth.clamp((int) ((0.015F + outerWeight * (0.105F + boost * 0.025F))
                    * visibility * strength * 255.0F), 0, 255);
            if (alpha == 0) continue;
            boolean light = hash(i * 47 + 21) > 0.48F;
            int red = light ? 213 : 52;
            int green = light ? 227 : 74;
            int blue = light ? 235 : 88;
            float widthStart = 0.4F + start * 0.7F;
            float widthEnd = 1.0F + end * (2.0F + hash(i * 53 + 19) * 2.0F);
            float midX = Mth.lerp(0.72F, x0, x1);
            float midY = Mth.lerp(0.72F, y0, y1);
            float midWidth = Mth.lerp(0.72F, widthStart, widthEnd);
            // Two tapered layers per streak; four quads total, submitted in one batch.
            // Each streak is independently light or dark, not an identical paired outline.
            for (int layer = 0; layer < 2; layer++) {
                float spread = layer == 0 ? 2.0F : 1.0F;
                int layerAlpha = layer == 0 ? alpha / 3 : alpha;
                ribbon(buffer, pose, x0, y0, midX, midY, dirX, dirY,
                        widthStart * spread, midWidth * spread,
                        red, green, blue, 0, layerAlpha);
                ribbon(buffer, pose, midX, midY, x1, y1, dirX, dirY,
                        midWidth * spread, widthEnd * spread,
                        red, green, blue, layerAlpha, 0);
            }
        }
        Tesselator.getInstance().end();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static void ribbon(BufferBuilder buffer, Matrix4f pose,
                               float x0, float y0, float x1, float y1,
                               float dirX, float dirY, float width0, float width1,
                               int red, int green, int blue, int alpha0, int alpha1) {
        float nx = -dirY;
        float ny = dirX;
        buffer.vertex(pose, x0 + nx * width0, y0 + ny * width0, 0.0F).color(red, green, blue, alpha0).endVertex();
        buffer.vertex(pose, x0 - nx * width0, y0 - ny * width0, 0.0F).color(red, green, blue, alpha0).endVertex();
        buffer.vertex(pose, x1 - nx * width1, y1 - ny * width1, 0.0F).color(red, green, blue, alpha1).endVertex();
        buffer.vertex(pose, x1 + nx * width1, y1 + ny * width1, 0.0F).color(red, green, blue, alpha1).endVertex();
    }

    private static float hash(int value) {
        int bits = value * 0x45d9f3b;
        bits ^= bits >>> 16;
        return (bits & 0xffff) / 65535.0F;
    }
}
