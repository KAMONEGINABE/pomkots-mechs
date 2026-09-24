package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.flight;

import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class FlightMovementController {
    public static final double MIN_CRUISE_SPEED = 1.15D;
    public static final double MAX_CRUISE_SPEED = 2.8D;
    public static final double ACCELERATION_PER_TICK = 0.045D;
    public static final double DECELERATION_PER_TICK = 0.055D;
    public static final double GERWALK_HORIZONTAL_SPEED = 0.42D;
    public static final double GERWALK_VERTICAL_RESPONSE = 0.18D;

    private FlightMovementController() {}

    public static double updateCruiseSpeed(double currentSpeed, DriverInput input) {
        double speed = Mth.clamp(currentSpeed, MIN_CRUISE_SPEED, MAX_CRUISE_SPEED);
        if (input != null && input.isForwardPressed()) speed += ACCELERATION_PER_TICK;
        if (input != null && input.isBackPressed()) speed -= DECELERATION_PER_TICK;
        return Mth.clamp(speed, MIN_CRUISE_SPEED, MAX_CRUISE_SPEED);
    }

    public static Vec3 flightVelocity(Quaternionf orientation, double speed) {
        return FlightOrientation.forward(orientation).normalize().scale(speed);
    }

    public static Vec3 gerwalkVelocity(float yawDegrees, DriverInput input, Vec3 current) {
        if (input == null) return current.scale(0.75D);
        double forward = (input.isForwardPressed() ? 1.0D : 0.0D)
                - (input.isBackPressed() ? 1.0D : 0.0D);
        double strafe = (input.isLeftPressed() ? 1.0D : 0.0D)
                - (input.isRightPressed() ? 1.0D : 0.0D);
        Vec3 local = new Vec3(strafe, 0.0D, forward);
        if (local.lengthSqr() > 1.0D) local = local.normalize();
        Vec3 horizontal = local.yRot(-yawDegrees * Mth.DEG_TO_RAD)
                .scale(GERWALK_HORIZONTAL_SPEED);
        double vertical = Mth.lerp(GERWALK_VERTICAL_RESPONSE, current.y, 0.0D);
        return new Vec3(horizontal.x, vertical, horizontal.z);
    }
}
