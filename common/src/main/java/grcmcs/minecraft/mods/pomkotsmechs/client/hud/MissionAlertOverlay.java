package grcmcs.minecraft.mods.pomkotsmechs.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/** Client-only, full-screen warning-light effect used by mission events. */
public final class MissionAlertOverlay {
    public static final MissionAlertOverlay INSTANCE = new MissionAlertOverlay();

    private int colorRgb;
    private float maxAlpha;
    private int durationTicks;
    private int periodTicks;
    private int edgeFadeTicks;
    private int elapsedTicks;
    private boolean active;

    private MissionAlertOverlay() {
    }

    public void start(int colorRgb, float maxAlpha, int durationTicks, int periodTicks, int edgeFadeTicks) {
        this.colorRgb = colorRgb & 0xFFFFFF;
        this.maxAlpha = Mth.clamp(maxAlpha, 0.0F, 1.0F);
        this.durationTicks = Math.max(1, durationTicks);
        this.periodTicks = Math.max(1, periodTicks);
        this.edgeFadeTicks = Math.max(0, Math.min(edgeFadeTicks, durationTicks / 2));
        this.elapsedTicks = 0;
        this.active = true;
    }

    public void tick(Minecraft minecraft) {
        if (!active || minecraft.isPaused()) return;
        if (++elapsedTicks >= durationTicks) active = false;
    }

    public void render(GuiGraphics graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!active || minecraft.options.hideGui) return;

        float time = Math.min(durationTicks, elapsedTicks + partialTick);
        float pulse = 0.5F - 0.5F * Mth.cos((float) (Math.PI * 2.0D * time / periodTicks));
        float envelope = 1.0F;
        if (edgeFadeTicks > 0) {
            envelope = Math.min(envelope, Mth.clamp(time / edgeFadeTicks, 0.0F, 1.0F));
            envelope = Math.min(envelope,
                    Mth.clamp((durationTicks - time) / edgeFadeTicks, 0.0F, 1.0F));
        }

        int alpha = Mth.clamp(Math.round(255.0F * maxAlpha * pulse * envelope), 0, 255);
        if (alpha == 0) return;

        graphics.pose().pushPose();
        // Keep the warning tint above ordinary HUD elements but below the
        // radio (Z=1000) and mission presentation (Z=1200).
        graphics.pose().translate(0.0F, 0.0F, 900.0F);
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), alpha << 24 | colorRgb);
        graphics.pose().popPose();
    }
}
