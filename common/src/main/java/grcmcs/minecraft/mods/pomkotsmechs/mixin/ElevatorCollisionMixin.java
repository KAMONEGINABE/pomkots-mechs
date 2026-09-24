package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.GenericElevatorEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Includes large elevator platforms whose centers lie outside the usual
 * small-entity section query.
 *
 * <p>This is a concrete override on Level because Forge 1.20.1's Mixin 0.8.5
 * does not support injection handlers on interface mixins.</p>
 */
@Mixin(Level.class)
public abstract class ElevatorCollisionMixin implements EntityGetter {
    @Unique
    private static final double POMKOTSMECHS_ELEVATOR_QUERY_INFLATION = 18.0D;

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB area) {
        if (area.getSize() < 1.0E-7D) return List.of();

        Predicate<Entity> collisionFilter = entity == null
                ? EntitySelector.CAN_BE_COLLIDED_WITH
                : EntitySelector.NO_SPECTATORS.and(entity::canCollideWith);
        List<Entity> vanillaEntities = getEntities(
                entity, area.inflate(1.0E-7D), collisionFilter);
        List<VoxelShape> result = new ArrayList<>(vanillaEntities.size() + 1);
        for (Entity candidate : vanillaEntities) {
            result.add(Shapes.create(candidate.getBoundingBox()));
        }

        for (GenericElevatorEntity elevator : getEntitiesOfClass(GenericElevatorEntity.class,
                area.inflate(POMKOTSMECHS_ELEVATOR_QUERY_INFLATION, 2.0D,
                        POMKOTSMECHS_ELEVATOR_QUERY_INFLATION),
                platform -> !platform.isRemoved()
                        && platform != entity && platform.canBeCollidedWith()
                        && platform.getBoundingBox().intersects(area)
                        && (entity == null || !platform.isPassengerOfSameVehicle(entity)))) {
            AABB box = elevator.getBoundingBox();
            if (elevator.usesClientRiderCollision(entity)) {
                result.removeIf(shape -> !shape.isEmpty() && shape.bounds().equals(box));
                continue;
            }
            boolean present = result.stream()
                    .anyMatch(shape -> !shape.isEmpty() && shape.bounds().equals(box));
            if (present) continue;
            result.add(Shapes.create(box));
        }
        return result.isEmpty() ? List.of() : result;
    }
}
