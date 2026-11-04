package grcmcs.minecraft.mods.pomkotsmechs.entity.misc;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** 16 x 1 x 16 variant of the reusable elevator. */
public final class MiddleElevatorEntity extends GenericElevatorEntity {
    public MiddleElevatorEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public double platformHorizontalCenterOffset() { return 0.0D; }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(16.0F, 1.0F);
    }

    @Override
    protected AABB makeBoundingBox() {
        return new AABB(getX() - 8, getY(), getZ() - 8,
                getX() + 8, getY() + 1, getZ() + 8);
    }
}
