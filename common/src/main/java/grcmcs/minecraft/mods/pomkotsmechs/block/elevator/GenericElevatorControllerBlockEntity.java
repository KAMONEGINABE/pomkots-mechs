package grcmcs.minecraft.mods.pomkotsmechs.block.elevator;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.GenericElevatorEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class GenericElevatorControllerBlockEntity extends BlockEntity {
    public static final String NBT_CONTROLLER_ID = PomkotsMechs.nbtName("ElevatorControllerId");
    public static final String NBT_ENDPOINT = PomkotsMechs.nbtName("ElevatorEndpoint");
    public static final String NBT_PLATFORM_OFFSET = PomkotsMechs.nbtName("ElevatorPlatformOffset");
    public static final String NBT_LINKED_OFFSET = PomkotsMechs.nbtName("ElevatorLinkedOffset");
    public static final String ENDPOINT_START = "start";
    public static final String ENDPOINT_END = "end";

    private static final Map<ServerLevel, Map<String, EnumMap<Endpoint, BlockPos>>> LOADED_CONTROLLERS =
            new WeakHashMap<>();

    private String controllerId = "";
    private Endpoint endpoint = Endpoint.NONE;
    private BlockPos linkedPos;
    private BlockPos platformOffset = new BlockPos(0, 1, 0);
    private BlockPos linkedOffset;
    private UUID elevatorUuid;
    private boolean wasPowered;
    private String registeredId = "";
    private Endpoint registeredEndpoint = Endpoint.NONE;

    public GenericElevatorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(PomkotsMechs.GENERIC_ELEVATOR_CONTROLLER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  GenericElevatorControllerBlockEntity controller) {
        if (!(level instanceof ServerLevel)) return;
        controller.refreshRegistration();
        controller.updateRedstoneSignal();
        if (level.getGameTime() % 20L != Math.floorMod(pos.asLong(), 20L)) return;
        controller.resolveLinkedController();
        if (controller.endpoint == Endpoint.START) controller.ensureElevator();
    }

    public void updateRedstoneSignal() {
        if (!(level instanceof ServerLevel)) return;
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (powered && !wasPowered) triggerElevator();
        if (powered != wasPowered) {
            wasPowered = powered;
            setChanged();
        }
    }

    private void triggerElevator() {
        GenericElevatorEntity elevator = findElevator();
        if (elevator != null) elevator.beginMove();
    }

    private void ensureElevator() {
        if (!(level instanceof ServerLevel serverLevel) || linkedPos == null || controllerId.isEmpty()) return;
        if (findElevator() != null) return;

        GenericElevatorEntity elevator = PomkotsMechs.GENERIC_ELEVATOR.get().create(serverLevel);
        if (elevator == null) return;
        BlockPos linkedPlatform = linkedPos.above();
        if (serverLevel.getBlockEntity(linkedPos) instanceof GenericElevatorControllerBlockEntity linked) {
            linkedPlatform = linked.platformPos();
        }
        elevator.configure(controllerId, worldPosition, linkedPos, platformPos(), linkedPlatform, blockYaw());
        BlockPos spawn = platformPos();
        elevator.moveTo(spawn.getX() + 0.5D, spawn.getY() + 0.2D, spawn.getZ() + 0.5D);
        if (!serverLevel.addFreshEntity(elevator)) return;
        setElevatorUuidForPair(elevator.getUUID());
    }

    private GenericElevatorEntity findElevator() {
        if (!(level instanceof ServerLevel serverLevel) || controllerId.isEmpty()) return null;
        if (elevatorUuid != null) {
            Entity entity = serverLevel.getEntity(elevatorUuid);
            if (entity instanceof GenericElevatorEntity elevator
                    && elevator.matchesController(controllerId)) return elevator;
        }

        GenericElevatorEntity nearby = findNear(serverLevel, platformPos());
        if (nearby == null && linkedPos != null) {
            BlockPos linkedPlatform = linkedPos.above();
            if (serverLevel.getBlockEntity(linkedPos) instanceof GenericElevatorControllerBlockEntity linked) {
                linkedPlatform = linked.platformPos();
            }
            nearby = findNear(serverLevel, linkedPlatform);
        }
        if (nearby != null) setElevatorUuidForPair(nearby.getUUID());
        return nearby;
    }

    private GenericElevatorEntity findNear(ServerLevel level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(5.0D, 4.0D, 5.0D);
        return level.getEntitiesOfClass(GenericElevatorEntity.class, area,
                        elevator -> elevator.matchesController(controllerId))
                .stream().findFirst().orElse(null);
    }

    private void setElevatorUuidForPair(UUID uuid) {
        elevatorUuid = uuid;
        setChanged();
        if (linkedPos != null && level != null
                && level.getBlockEntity(linkedPos) instanceof GenericElevatorControllerBlockEntity linked
                && linked.controllerId.equals(controllerId)) {
            linked.elevatorUuid = uuid;
            linked.setChanged();
        }
    }

    private void resolveLinkedController() {
        if (!(level instanceof ServerLevel serverLevel) || controllerId.isEmpty()
                || endpoint == Endpoint.NONE) return;
        BlockPos candidate = null;
        if (linkedOffset != null) {
            BlockPos relativeCandidate = worldPosition.offset(linkedOffset);
            if (serverLevel.getBlockEntity(relativeCandidate) instanceof GenericElevatorControllerBlockEntity linked
                    && linked.endpoint == endpoint.opposite()
                    && controllerId.equals(linked.controllerId)) {
                candidate = relativeCandidate;
            }
        }
        if (candidate == null) candidate = lookup(serverLevel, controllerId, endpoint.opposite());
        if (candidate == null || candidate.equals(worldPosition)) return;
        if (!candidate.equals(linkedPos)) {
            linkedPos = candidate.immutable();
            setChanged();
        }
    }

    private BlockPos platformPos() {
        return worldPosition.offset(platformOffset);
    }

    private void refreshRegistration() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (registeredId.equals(controllerId) && registeredEndpoint == endpoint) return;
        unregister(serverLevel, registeredId, registeredEndpoint, worldPosition);
        registeredId = controllerId;
        registeredEndpoint = endpoint;
        if (!controllerId.isEmpty() && endpoint != Endpoint.NONE) {
            synchronized (LOADED_CONTROLLERS) {
                LOADED_CONTROLLERS
                        .computeIfAbsent(serverLevel, ignored -> new HashMap<>())
                        .computeIfAbsent(controllerId, ignored -> new EnumMap<>(Endpoint.class))
                        .put(endpoint, worldPosition.immutable());
            }
        }
    }

    private static BlockPos lookup(ServerLevel level, String id, Endpoint endpoint) {
        synchronized (LOADED_CONTROLLERS) {
            Map<String, EnumMap<Endpoint, BlockPos>> byId = LOADED_CONTROLLERS.get(level);
            if (byId == null) return null;
            Map<Endpoint, BlockPos> endpoints = byId.get(id);
            return endpoints == null ? null : endpoints.get(endpoint);
        }
    }

    private static void unregister(ServerLevel level, String id, Endpoint endpoint, BlockPos pos) {
        if (id.isEmpty() || endpoint == Endpoint.NONE) return;
        synchronized (LOADED_CONTROLLERS) {
            Map<String, EnumMap<Endpoint, BlockPos>> byId = LOADED_CONTROLLERS.get(level);
            if (byId == null) return;
            EnumMap<Endpoint, BlockPos> endpoints = byId.get(id);
            if (endpoints != null && pos.equals(endpoints.get(endpoint))) {
                endpoints.remove(endpoint);
                if (endpoints.isEmpty()) byId.remove(id);
            }
            if (byId.isEmpty()) LOADED_CONTROLLERS.remove(level);
        }
    }

    private float blockYaw() {
        Direction facing = getBlockState().getValue(GenericElevatorControllerBlock.FACING);
        return switch (facing) {
            case SOUTH -> 0.0F;
            case WEST -> 90.0F;
            case NORTH -> 180.0F;
            case EAST -> -90.0F;
            default -> 0.0F;
        };
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            unregister(serverLevel, registeredId, registeredEndpoint, worldPosition);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(NBT_CONTROLLER_ID, controllerId);
        tag.putString(NBT_ENDPOINT, endpoint.serializedName);
        tag.put(NBT_PLATFORM_OFFSET, NbtUtils.writeBlockPos(platformOffset));
        if (linkedOffset != null) tag.put(NBT_LINKED_OFFSET, NbtUtils.writeBlockPos(linkedOffset));
        if (linkedPos != null) tag.putLong(PomkotsMechs.nbtName("ElevatorLinkedPos"), linkedPos.asLong());
        if (elevatorUuid != null) tag.putUUID(PomkotsMechs.nbtName("ElevatorUuid"), elevatorUuid);
        tag.putBoolean(PomkotsMechs.nbtName("ElevatorWasPowered"), wasPowered);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        controllerId = tag.getString(NBT_CONTROLLER_ID).trim();
        endpoint = Endpoint.parse(tag.getString(NBT_ENDPOINT));
        platformOffset = tag.contains(NBT_PLATFORM_OFFSET, Tag.TAG_COMPOUND)
                ? NbtUtils.readBlockPos(tag.getCompound(NBT_PLATFORM_OFFSET)) : new BlockPos(0, 1, 0);
        linkedOffset = tag.contains(NBT_LINKED_OFFSET, Tag.TAG_COMPOUND)
                ? NbtUtils.readBlockPos(tag.getCompound(NBT_LINKED_OFFSET)) : null;
        String linkedKey = PomkotsMechs.nbtName("ElevatorLinkedPos");
        linkedPos = tag.contains(linkedKey) ? BlockPos.of(tag.getLong(linkedKey)) : null;
        String uuidKey = PomkotsMechs.nbtName("ElevatorUuid");
        elevatorUuid = tag.hasUUID(uuidKey) ? tag.getUUID(uuidKey) : null;
        wasPowered = tag.getBoolean(PomkotsMechs.nbtName("ElevatorWasPowered"));
    }

    private enum Endpoint {
        NONE(""), START(ENDPOINT_START), END(ENDPOINT_END);

        private final String serializedName;
        Endpoint(String serializedName) { this.serializedName = serializedName; }
        private Endpoint opposite() { return this == START ? END : this == END ? START : NONE; }
        private static Endpoint parse(String value) {
            if (ENDPOINT_START.equalsIgnoreCase(value)) return START;
            if (ENDPOINT_END.equalsIgnoreCase(value)) return END;
            return NONE;
        }
    }
}
