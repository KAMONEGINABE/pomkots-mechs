package grcmcs.minecraft.mods.pomkotsmechs.entity.misc;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** 32 x 1 x 16 mission-scale variant of the reusable elevator. */
public final class LargeElevatorEntity extends GenericElevatorEntity {
    private static final float WIDTH = 32.0F;
    private static final float HEIGHT = 1.0F;

    public LargeElevatorEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isLargeVariant() {
        return true;
    }

    @Override
    public double platformHorizontalCenterOffset() {
        return 0.0D;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(WIDTH, HEIGHT);
    }

    @Override
    protected AABB makeBoundingBox() {
        // EntityDimensions only supports a square horizontal footprint, so keep
        // the actual platform collision rectangular and rotate it with fixed yaw.
        boolean quarterTurn = Math.abs(Math.sin(Math.toRadians(getYRot()))) > 0.5D;
        double sizeX = quarterTurn ? 16.0D : 32.0D;
        double sizeZ = quarterTurn ? 32.0D : 16.0D;
        return new AABB(
                getX() - sizeX * 0.5D, getY(), getZ() - sizeZ * 0.5D,
                getX() + sizeX * 0.5D, getY() + HEIGHT, getZ() + sizeZ * 0.5D);
    }
}
