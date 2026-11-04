package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.flight;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Quaternion helpers shared by server movement and client rendering. */
public final class FlightOrientation {
    private FlightOrientation() {}

    public static Quaternionf fromVanillaRotation(float yawDegrees, float pitchDegrees) {
        return new Quaternionf().rotationYXZ(
                -yawDegrees * Mth.DEG_TO_RAD,
                pitchDegrees * Mth.DEG_TO_RAD,
                0.0F).normalize();
    }

    public static Quaternionf normalized(float x, float y, float z, float w) {
        Quaternionf value = new Quaternionf(x, y, z, w);
        if (!Float.isFinite(value.lengthSquared()) || value.lengthSquared() < 1.0E-6F) {
            return new Quaternionf();
        }
        return value.normalize();
    }

    public static Vec3 forward(Quaternionf orientation) {
        Vector3f vector = orientation.transform(new Vector3f(0.0F, 0.0F, 1.0F));
        return new Vec3(vector.x, vector.y, vector.z);
    }

    public static Vec3 up(Quaternionf orientation) {
        Vector3f vector = orientation.transform(new Vector3f(0.0F, 1.0F, 0.0F));
        return new Vec3(vector.x, vector.y, vector.z);
    }

    public static Vec3 right(Quaternionf orientation) {
        Vector3f vector = orientation.transform(new Vector3f(1.0F, 0.0F, 0.0F));
        return new Vec3(vector.x, vector.y, vector.z);
    }

    public static Vec3 rotate(Quaternionf orientation, Vec3 vector) {
        Vector3f rotated = orientation.transform(
                new Vector3f((float)vector.x, (float)vector.y, (float)vector.z));
        return new Vec3(rotated.x, rotated.y, rotated.z);
    }

    public static float yawDegrees(Quaternionf orientation) {
        Vec3 forward = forward(orientation);
        return (float)(Mth.atan2(-forward.x, forward.z) * Mth.RAD_TO_DEG);
    }

    public static float pitchDegrees(Quaternionf orientation) {
        Vec3 forward = forward(orientation);
        return (float)(-Mth.atan2(forward.y,
                Math.sqrt(forward.x * forward.x + forward.z * forward.z)) * Mth.RAD_TO_DEG);
    }
}
