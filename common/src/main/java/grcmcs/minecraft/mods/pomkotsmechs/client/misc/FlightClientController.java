package grcmcs.minecraft.mods.pomkotsmechs.client.misc;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.flight.FlightControlType;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

/** Client prediction and C2S synchronization for unrestricted flight attitude. */
public final class FlightClientController {
    /** Maximum camera turn in degrees per second in SUPER_BOOST_STYLE flight. */
    public static double MAX_ARCADE_YAW_DEGREES_PER_SECOND = 100.0D;
    public static double MAX_ARCADE_PITCH_DEGREES_PER_SECOND = 80.0D;
    private static double pendingYawDegrees;
    private static double pendingPitchDegrees;
    private static final float ROLL_SPEED_DEGREES = 2.8F;
    private static int arcadeTurnPlayerId = Integer.MIN_VALUE;
    private static long lastArcadeTurnNanos;

    private FlightClientController() {}

    public record TurnInput(double yaw, double pitch) {}

    /** Limits the player's actual turn, keeping camera, flight direction and aim together. */
    public static TurnInput limitArcadeTurn(LocalPlayer player, double yawInput, double pitchInput) {
        if (!(player.getVehicle() instanceof Pmvc01Entity mech) || !mech.isFlightMode()
                || controlType() != FlightControlType.SUPER_BOOST_STYLE) {
            lastArcadeTurnNanos = 0L;
            return new TurnInput(yawInput, pitchInput);
        }

        long now = System.nanoTime();
        if (arcadeTurnPlayerId != player.getId()) {
            arcadeTurnPlayerId = player.getId();
            lastArcadeTurnNanos = 0L;
        }
        // MouseHandler calls Entity#turn at render frequency. A tick-based
        // budget produces 20 Hz steps at high FPS, so cap by elapsed frame time.
        // Clamp stalls and the first frame to avoid a sudden turn after a pause.
        double elapsedSeconds = lastArcadeTurnNanos == 0L
                ? 0.05D : Mth.clamp((now - lastArcadeTurnNanos) / 1.0E9D, 0.0D, 0.05D);
        lastArcadeTurnNanos = now;

        // Entity#turn converts the mouse deltas into degrees with factor 0.15.
        double yawDegrees = Mth.clamp(yawInput * 0.15D,
                -Math.max(0.0D, MAX_ARCADE_YAW_DEGREES_PER_SECOND) * elapsedSeconds,
                Math.max(0.0D, MAX_ARCADE_YAW_DEGREES_PER_SECOND) * elapsedSeconds);
        double pitchDegrees = Mth.clamp(pitchInput * 0.15D,
                -Math.max(0.0D, MAX_ARCADE_PITCH_DEGREES_PER_SECOND) * elapsedSeconds,
                Math.max(0.0D, MAX_ARCADE_PITCH_DEGREES_PER_SECOND) * elapsedSeconds);
        return new TurnInput(yawDegrees / 0.15D, pitchDegrees / 0.15D);
    }

    public static void captureTurn(LocalPlayer player, double yawInput, double pitchInput) {
        if (!(player.getVehicle() instanceof Pmvc01Entity mech) || !mech.isFlightMode()) return;
        if (controlType() == FlightControlType.SUPER_BOOST_STYLE) return;
        // Entity#turn applies this same factor after receiving the processed mouse delta.
        pendingYawDegrees += yawInput * 0.15D;
        pendingPitchDegrees += pitchInput * 0.15D;
    }

    public static void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null || !(player.getVehicle() instanceof Pmvc01Entity mech) || !mech.isFlightMode()) {
            pendingYawDegrees = 0.0D;
            pendingPitchDegrees = 0.0D;
            return;
        }
        if (controlType() == FlightControlType.SUPER_BOOST_STYLE) {
            pendingYawDegrees = 0.0D;
            pendingPitchDegrees = 0.0D;
            return;
        }

        Quaternionf orientation = mech.getFlightOrientation();
        float pitch = (float)Mth.clamp(pendingPitchDegrees, -35.0D, 35.0D) * Mth.DEG_TO_RAD;
        float yaw = (float)Mth.clamp(pendingYawDegrees, -35.0D, 35.0D) * Mth.DEG_TO_RAD;
        pendingYawDegrees = 0.0D;
        pendingPitchDegrees = 0.0D;

        DriverInput input = mech.getDriverInput();
        float rollDegrees = 0.0F;
        if (input != null) {
            if (input.isLeftPressed()) rollDegrees += ROLL_SPEED_DEGREES;
            if (input.isRightPressed()) rollDegrees -= ROLL_SPEED_DEGREES;
        }

        if (pitch != 0.0F) orientation.rotateLocalX(pitch);
        if (yaw != 0.0F) orientation.rotateLocalY(-yaw);
        if (rollDegrees != 0.0F) orientation.rotateLocalZ(rollDegrees * Mth.DEG_TO_RAD);
        orientation.normalize();

        mech.setFlightOrientation(orientation);
        sendOrientation(mech, orientation);
    }

    private static FlightControlType controlType() {
        return FlightControlType.fromName(PomkotsMechs.CONFIG.flightControlType);
    }

    private static void sendOrientation(Pmvc01Entity mech, Quaternionf orientation) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(mech.getId());
        buf.writeFloat(orientation.x);
        buf.writeFloat(orientation.y);
        buf.writeFloat(orientation.z);
        buf.writeFloat(orientation.w);
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_FLIGHT_ORIENTATION), buf);
    }
}
