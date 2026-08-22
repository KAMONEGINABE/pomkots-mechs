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

    private ClientElevatorRider() { }

    public static void onElevatorTick(GenericElevatorEntity elevator) {
        LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        Vec3 current = elevator.position();
        Vec3 previous = PREVIOUS_POSITIONS.put(elevator, current);
        Vec3 delta = previous == null ? Vec3.ZERO : current.subtract(previous);
        boolean movingNow = elevator.isMoving() || delta.lengthSqr() > 1.0E-8D;
        boolean wasMoving = Boolean.TRUE.equals(PREVIOUS_MOVING.put(elevator, movingNow));
        boolean finishingMove = !movingNow && wasMoving;
        if (player == null || player.getRootVehicle() != player) return;
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
            FALLING_TOWARD_PLATFORM.put(elevator, false);
            return;
        }
        if (!isOnPlatform(player, elevator)) return;

        if (Boolean.TRUE.equals(FALLING_TOWARD_PLATFORM.get(elevator))) {
            landOnce(player, elevator);
            FALLING_TOWARD_PLATFORM.put(elevator, false);
            return;
        }

        double movedY = player.getY() + delta.y;
        if (delta.y > 0.0D) {
            movedY = Math.max(movedY, elevator.getBoundingBox().maxY);
        }
        player.setPos(player.getX() + delta.x, movedY, player.getZ() + delta.z);
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

        double platformTop = elevator.getBoundingBox().maxY;
        if (player.getY() <= platformTop + 0.2D) {
            player.setPos(player.getX(), platformTop, player.getZ());
            Vec3 velocity = player.getDeltaMovement();
            player.setDeltaMovement(velocity.x, 0.0D, velocity.z);
            player.setOnGround(true);
            player.fallDistance = 0.0F;
        }
        return true;
    }

    private static void landOnce(LocalPlayer player, GenericElevatorEntity elevator) {
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
