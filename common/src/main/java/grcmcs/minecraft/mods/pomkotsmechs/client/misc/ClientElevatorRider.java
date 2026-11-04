package grcmcs.minecraft.mods.pomkotsmechs.client.misc;

import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.GenericElevatorEntity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

/** Keeps the locally controlled player on a non-passenger moving elevator. */
public final class ClientElevatorRider {
    private static final Map<GenericElevatorEntity, Vec3> PREVIOUS_POSITIONS = new WeakHashMap<>();
    private static final Map<GenericElevatorEntity, Boolean> PREVIOUS_MOVING = new WeakHashMap<>();
    private static final Map<GenericElevatorEntity, Boolean> FALLING_TOWARD_PLATFORM = new WeakHashMap<>();
    private static final Map<GenericElevatorEntity, Integer> STOP_SETTLE_TICKS = new WeakHashMap<>();
    private static final Map<GenericElevatorEntity, Boolean> SUPPORTED_LAST_TICK = new WeakHashMap<>();

    private ClientElevatorRider() { }

    public static void onElevatorTick(GenericElevatorEntity elevator) {
        LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        Vec3 current = elevator.position();
        Vec3 previous = PREVIOUS_POSITIONS.put(elevator, current);
        Vec3 delta = previous == null ? Vec3.ZERO : current.subtract(previous);
        boolean movingNow = elevator.isMoving() || delta.lengthSqr() > 1.0E-8D;
        boolean wasMoving = Boolean.TRUE.equals(PREVIOUS_MOVING.put(elevator, movingNow));
        boolean finishingMove = !movingNow && wasMoving;
        if (player == null || player.getRootVehicle() != player) {
            SUPPORTED_LAST_TICK.remove(elevator);
            return;
        }
        boolean wasSupported = Boolean.TRUE.equals(SUPPORTED_LAST_TICK.get(elevator));
        boolean overlaps = overlapsPlatform(player, elevator);
        boolean onTop = overlaps && Math.abs(player.getY() - elevator.getBoundingBox().maxY) <= 0.25D;
        SUPPORTED_LAST_TICK.put(elevator, onTop);
        if (finishingMove) STOP_SETTLE_TICKS.put(elevator, 5);

        if (isHorizontallyOverPlatform(player, elevator)
                && player.getDeltaMovement().y < -0.12D && player.fallDistance > 1.0F) {
            FALLING_TOWARD_PLATFORM.put(elevator, true);
        }

        if (!movingNow && !finishingMove) {
            if (Boolean.TRUE.equals(FALLING_TOWARD_PLATFORM.get(elevator))
                    && isOnPlatform(player, elevator)) {
                landOnce(player, elevator);
                FALLING_TOWARD_PLATFORM.put(elevator, false);
            } else if (settleAfterStop(player, elevator)) {
                return;
            } else {
                stabilizeOnGroundedPlatform(player, elevator);
            }
            return;
        }

        if (player.input.jumping) {
            SUPPORTED_LAST_TICK.remove(elevator);
            FALLING_TOWARD_PLATFORM.put(elevator, false);
            return;
        }
        // A rising platform can already have passed the feet by the time its
        // interpolated position is applied. Test the previous top as well.
        boolean wasOnPreviousTop = previous != null && overlapsPlatform(player, elevator)
                && player.getY() >= elevator.getBoundingBox().maxY - delta.y - 0.25D
                && player.getY() <= elevator.getBoundingBox().maxY - delta.y + 0.25D;
        boolean retainRisingSupport = wasSupported && overlaps && delta.y > 0
                && player.getY() >= elevator.getBoundingBox().maxY - delta.y - 1.25D
                && player.getY() <= elevator.getBoundingBox().maxY + 0.25D;
        if (!isOnPlatform(player, elevator) && !wasOnPreviousTop && !retainRisingSupport) {
            return;
        }

        if (!retainRisingSupport && Boolean.TRUE.equals(FALLING_TOWARD_PLATFORM.get(elevator))) {
            landOnce(player, elevator);
            FALLING_TOWARD_PLATFORM.put(elevator, false);
            return;
        }

        double movedY = player.getY() + delta.y;
        if (delta.y > 0.0D) {
            movedY = Math.max(movedY, elevator.getBoundingBox().maxY);
        }
        player.setPos(player.getX() + delta.x, movedY, player.getZ() + delta.z);
        SUPPORTED_LAST_TICK.put(elevator, overlaps
                && Math.abs(movedY - elevator.getBoundingBox().maxY) <= 0.25D);
        FALLING_TOWARD_PLATFORM.put(elevator, false);
        Vec3 velocity = player.getDeltaMovement();
        if (velocity.y <= 0.0D) {
            player.setDeltaMovement(velocity.x, 0.0D, velocity.z);
            player.setOnGround(true);
            player.fallDistance = 0.0F;
        }
    }

    private static boolean settleAfterStop(LocalPlayer player, GenericElevatorEntity elevator) {
        int ticks = STOP_SETTLE_TICKS.getOrDefault(elevator, 0);
        if (ticks <= 0) return false;
        STOP_SETTLE_TICKS.put(elevator, ticks - 1);
        if (!isOnPlatform(player, elevator) || player.input.jumping) return true;

        landOnce(player, elevator);
        return true;
    }

    private static void landOnce(LocalPlayer player, GenericElevatorEntity elevator) {
        double correction = elevator.getBoundingBox().maxY - player.getY();
        if (correction <= 1.0E-5D || player.input.jumping
                || !overlapsPlatform(player, elevator)
                || !player.level().noCollision(player, player.getBoundingBox().move(0, correction, 0).deflate(0.001D))) return;
        player.setPos(player.getX(), elevator.getBoundingBox().maxY, player.getZ());
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.x, 0.0D, velocity.z);
        player.setOnGround(true);
        player.fallDistance = 0.0F;
    }

    private static void stabilizeOnGroundedPlatform(LocalPlayer player, GenericElevatorEntity elevator) {
        if (!elevator.isStoppedOnGround()
                || player.input.jumping
                || !isOnPlatform(player, elevator)) return;
        landOnce(player, elevator);
    }

    private static boolean overlapsPlatform(LocalPlayer player, GenericElevatorEntity elevator) {
        AABB p = player.getBoundingBox();
        AABB e = elevator.getBoundingBox();
        return p.maxX > e.minX && p.minX < e.maxX && p.maxZ > e.minZ && p.minZ < e.maxZ;
    }

    private static boolean isHorizontallyOverPlatform(LocalPlayer player, GenericElevatorEntity elevator) {
        AABB platform = elevator.getBoundingBox();
        AABB playerBox = player.getBoundingBox();
        return playerBox.maxX >= platform.minX - 0.75D
                && playerBox.minX <= platform.maxX + 0.75D
                && playerBox.maxZ >= platform.minZ - 0.75D
                && playerBox.minZ <= platform.maxZ + 0.75D;
    }

    private static boolean isOnPlatform(LocalPlayer player, GenericElevatorEntity elevator) {
        AABB platform = elevator.getBoundingBox();
        AABB playerBox = player.getBoundingBox();
        return isHorizontallyOverPlatform(player, elevator)
                && playerBox.minY >= platform.maxY - 1.25D
                && playerBox.minY <= platform.maxY + 0.75D;
    }

}
