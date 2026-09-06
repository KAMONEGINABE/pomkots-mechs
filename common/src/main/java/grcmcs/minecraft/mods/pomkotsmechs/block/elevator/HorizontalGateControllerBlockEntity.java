package grcmcs.minecraft.mods.pomkotsmechs.block.elevator;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.HorizontalGateEntity;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public final class HorizontalGateControllerBlockEntity extends BlockEntity {
    public static final String NBT_CONTROLLER_ID = PomkotsMechs.nbtName("GateControllerId");
    public static final String NBT_GATE_OFFSET = PomkotsMechs.nbtName("GateOffset");
    public static final String NBT_SLIDE_OFFSET = PomkotsMechs.nbtName("GateSlideOffset");
    private static final Map<ServerLevel, Map<String, BlockPos>> LOADED_CONTROLLERS = new WeakHashMap<>();

    private String controllerId = "";
    private BlockPos gateOffset = new BlockPos(0, 1, 0);
    private BlockPos slideOffset = BlockPos.ZERO;
    private UUID gateUuid;
    private boolean wasPowered;
    private String registeredId = "";

    public HorizontalGateControllerBlockEntity(BlockPos pos, BlockState state) {
        super(PomkotsMechs.HORIZONTAL_GATE_CONTROLLER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, HorizontalGateControllerBlockEntity controller) {
        if (!(level instanceof ServerLevel)) return;
        controller.refreshRegistration();
        controller.updateRedstoneSignal();
        if (level.getGameTime() % 20L == Math.floorMod(pos.asLong(), 20L)) controller.ensureGate();
    }

    public void updateRedstoneSignal() {
        if (!(level instanceof ServerLevel)) return;
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (powered && !wasPowered) triggerGate();
        if (powered != wasPowered) {
            wasPowered = powered;
            setChanged();
        }
    }

    public static boolean triggerById(ServerLevel level, String id) {
        if (id == null || id.isBlank()) return false;
        BlockPos pos;
        synchronized (LOADED_CONTROLLERS) {
            Map<String, BlockPos> controllers = LOADED_CONTROLLERS.get(level);
            pos = controllers == null ? null : controllers.get(id.trim());
        }
        if (pos == null || !(level.getBlockEntity(pos) instanceof HorizontalGateControllerBlockEntity controller)) {
            return false;
        }
        controller.ensureGate();
        return controller.triggerGate();
    }

    private boolean triggerGate() {
        HorizontalGateEntity gate = findGate();
        return gate != null && gate.beginMove();
    }

    private void ensureGate() {
        if (!(level instanceof ServerLevel serverLevel) || controllerId.isEmpty() || slideOffset.equals(BlockPos.ZERO)) return;
        if (findGate() != null) return;
        HorizontalGateEntity gate = PomkotsMechs.GATE_HORIZONTAL.get().create(serverLevel);
        if (gate == null) return;
        BlockPos closedBlock = worldPosition.offset(gateOffset);
        Vec3Pair positions = new Vec3Pair(
                new net.minecraft.world.phys.Vec3(closedBlock.getX(), closedBlock.getY(), closedBlock.getZ()),
                new net.minecraft.world.phys.Vec3(
                        closedBlock.getX() + slideOffset.getX(),
                        closedBlock.getY() + slideOffset.getY(),
                        closedBlock.getZ() + slideOffset.getZ()));
        gate.configure(controllerId, positions.closed, positions.open, blockYaw());
        gate.moveTo(positions.closed.x, positions.closed.y, positions.closed.z);
        if (serverLevel.addFreshEntity(gate)) {
            gateUuid = gate.getUUID();
            setChanged();
        }
    }

    private HorizontalGateEntity findGate() {
        if (!(level instanceof ServerLevel serverLevel) || gateUuid == null) return null;
        Entity entity = serverLevel.getEntity(gateUuid);
        return entity instanceof HorizontalGateEntity gate && gate.matchesController(controllerId) ? gate : null;
    }

    private float blockYaw() {
        Direction facing = getBlockState().getValue(HorizontalGateControllerBlock.FACING);
        return switch (facing) {
            case SOUTH -> 0.0F;
            case WEST -> 90.0F;
            case NORTH -> 180.0F;
            case EAST -> -90.0F;
            default -> 0.0F;
        };
    }

    private void refreshRegistration() {
        if (!(level instanceof ServerLevel serverLevel) || registeredId.equals(controllerId)) return;
        unregister(serverLevel);
        registeredId = controllerId;
        if (!controllerId.isEmpty()) {
            synchronized (LOADED_CONTROLLERS) {
                LOADED_CONTROLLERS.computeIfAbsent(serverLevel, ignored -> new HashMap<>())
                        .put(controllerId, worldPosition.immutable());
            }
        }
    }

    private void unregister(ServerLevel serverLevel) {
        if (registeredId.isEmpty()) return;
        synchronized (LOADED_CONTROLLERS) {
            Map<String, BlockPos> controllers = LOADED_CONTROLLERS.get(serverLevel);
            if (controllers != null && worldPosition.equals(controllers.get(registeredId))) {
                controllers.remove(registeredId);
                if (controllers.isEmpty()) LOADED_CONTROLLERS.remove(serverLevel);
            }
        }
    }

    @Override public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) unregister(serverLevel);
        super.setRemoved();
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(NBT_CONTROLLER_ID, controllerId);
        tag.put(NBT_GATE_OFFSET, NbtUtils.writeBlockPos(gateOffset));
        tag.put(NBT_SLIDE_OFFSET, NbtUtils.writeBlockPos(slideOffset));
        if (gateUuid != null) tag.putUUID(PomkotsMechs.nbtName("GateUuid"), gateUuid);
        tag.putBoolean(PomkotsMechs.nbtName("GateWasPowered"), wasPowered);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        controllerId = tag.getString(NBT_CONTROLLER_ID).trim();
        gateOffset = tag.contains(NBT_GATE_OFFSET, Tag.TAG_COMPOUND)
                ? NbtUtils.readBlockPos(tag.getCompound(NBT_GATE_OFFSET)) : new BlockPos(0, 1, 0);
        slideOffset = tag.contains(NBT_SLIDE_OFFSET, Tag.TAG_COMPOUND)
                ? NbtUtils.readBlockPos(tag.getCompound(NBT_SLIDE_OFFSET)) : BlockPos.ZERO;
        String uuidKey = PomkotsMechs.nbtName("GateUuid");
        gateUuid = tag.hasUUID(uuidKey) ? tag.getUUID(uuidKey) : null;
        wasPowered = tag.getBoolean(PomkotsMechs.nbtName("GateWasPowered"));
    }

    private record Vec3Pair(net.minecraft.world.phys.Vec3 closed, net.minecraft.world.phys.Vec3 open) { }
}
