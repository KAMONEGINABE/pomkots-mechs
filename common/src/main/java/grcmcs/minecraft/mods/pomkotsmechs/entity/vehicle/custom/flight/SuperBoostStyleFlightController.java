package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.flight;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/** View-following arcade flight shared by client prediction and the server. */
public final class SuperBoostStyleFlightController {
    private SuperBoostStyleFlightController() {}

    public static Quaternionf orientation(LivingEntity pilot) {
        return FlightOrientation.fromVanillaRotation(pilot.getYRot(), pilot.getXRot());
    }

    public static Vec3 velocity(LivingEntity pilot, double speed) {
        Vec3 look = pilot.getLookAngle();
        return look.lengthSqr() < 1.0E-6D
                ? Vec3.ZERO
                : look.normalize().scale(speed);
    }
}
