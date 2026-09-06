package grcmcs.minecraft.mods.pomkotsmechs.entity.misc;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/** A reusable two-endpoint elevator controlled by GenericElevatorControllerBlockEntity. */
public class GenericElevatorEntity extends LivingEntity implements GeoEntity {
    private static Consumer<GenericElevatorEntity> clientTickHandler = elevator -> { };
    private static final float WIDTH = 6.5F;
    private static final float HEIGHT = 0.5F;
    private static final double MOVE_SPEED = 0.35D;
    private static final double ARRIVAL_DISTANCE = 0.08D;

    private static final EntityDataAccessor<Boolean> MOVING =
            SynchedEntityData.defineId(GenericElevatorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> FIXED_YAW =
            SynchedEntityData.defineId(GenericElevatorEntity.class, EntityDataSerializers.FLOAT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private String controllerId = "";
    private BlockPos startController = BlockPos.ZERO;
    private BlockPos endController = BlockPos.ZERO;
    private BlockPos startPlatform = BlockPos.ZERO;
    private BlockPos endPlatform = BlockPos.ZERO;
    private BlockPos targetController = BlockPos.ZERO;
    private final Map<UUID, Integer> carriedEntityGraceTicks = new HashMap<>();
    private long groundSupportCheckTick = Long.MIN_VALUE;
    private boolean groundSupportCheckResult;

    public GenericElevatorEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setInvulnerable(true);
        noCulling = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1024.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    /** Installed only from client initialization; the dedicated server keeps the no-op handler. */
    public static void setClientTickHandler(Consumer<GenericElevatorEntity> handler) {
        clientTickHandler = handler == null ? elevator -> { } : handler;
    }

    public void configure(String controllerId, BlockPos start, BlockPos end,
                          BlockPos startPlatform, BlockPos endPlatform, float yaw) {
        this.controllerId = controllerId == null ? "" : controllerId;
        this.startController = start.immutable();
        this.endController = end.immutable();
        this.startPlatform = startPlatform.immutable();
        this.endPlatform = endPlatform.immutable();
        setFixedYaw(yaw);
        setChangedForSync();
    }

    public boolean matchesController(String id) {
        return !controllerId.isEmpty() && controllerId.equals(id);
    }

    /** Used by the shared controller to avoid pairing a normal and large platform. */
    public boolean isLargeVariant() {
        return false;
    }

    /**
     * Odd/fractional platforms are centered on a block, while an even-sized
     * platform is centered on the boundary between blocks.
     */
    public double platformHorizontalCenterOffset() {
        return 0.5D;
    }

    public boolean isMoving() {
        return entityData.get(MOVING);
    }

    public boolean beginMove() {
        if (isMoving()) return false;
        BlockPos destination;
        if (isAtController(startController)) {
            destination = endController;
        } else if (isAtController(endController)) {
            destination = startController;
        } else {
            return false;
        }
        targetController = destination;
        entityData.set(MOVING, true);
        level().playSound(null, blockPosition(), PomkotsMechs.SE_ELEVATOR.get(),
                SoundSource.BLOCKS, 1.0F, 0.7F);
        setChangedForSync();
        return true;
    }

    public boolean isAtController(BlockPos controllerPos) {
        return position().distanceToSqr(platformPositionForController(controllerPos)) <= 0.25D;
    }

    @Override
    public void tick() {
        setNoGravity(true);
        super.tick();
        lockYaw();
        setDeltaMovement(Vec3.ZERO);
        if (level().isClientSide) {
            clientTickHandler.accept(this);
            return;
        }
        if (!isMoving()) {
            stabilizeStationaryContents();
            return;
        }

        Vec3 target = platformPosition(targetController.equals(startController) ? startPlatform : endPlatform);
        Vec3 difference = target.subtract(position());
        double distance = difference.length();
        Vec3 requested = distance <= MOVE_SPEED
                ? difference : difference.scale(MOVE_SPEED / distance);
        movePlatformAndContents(requested);

        if (position().distanceToSqr(target) <= ARRIVAL_DISTANCE * ARRIVAL_DISTANCE) {
            Vec3 snap = target.subtract(position());
            movePlatformAndContents(snap);
            entityData.set(MOVING, false);
            level().playSound(null, blockPosition(), PomkotsMechs.SE_ELEVATOR.get(),
                    SoundSource.BLOCKS, 1.0F, 0.7F);
            setChangedForSync();
        }
    }

    private void stabilizeStationaryContents() {
        AABB platform = getBoundingBox();
        double platformTop = platform.maxY;
        for (Entity entity : entitiesOnTop()) {
            if (entity instanceof ServerPlayer || entity.isRemoved()
                    || entity.getDeltaMovement().y > 0.05D) continue;
            double correction = platformTop - entity.getBoundingBox().minY;
            if (Math.abs(correction) > 1.25D) continue;
            if (Math.abs(correction) > 1.0E-5D) {
                entity.setPos(entity.getX(), entity.getY() + correction, entity.getZ());
            }
            Vec3 velocity = entity.getDeltaMovement();
            entity.setDeltaMovement(velocity.x, 0.0D, velocity.z);
            entity.setOnGround(true);
            entity.fallDistance = 0.0F;
        }
    }

    private void movePlatformAndContents(Vec3 requested) {
        if (requested.lengthSqr() < 1.0E-10D) return;
        Set<Entity> carried = entitiesOnTop();
        Vec3 before = position();
        move(MoverType.SELF, requested);
        Vec3 actual = position().subtract(before);
        if (actual.lengthSqr() < 1.0E-10D) return;
        for (Entity entity : carried) {
            // LocalPlayer is moved from the client using the platform's rendered delta.
            // Moving it here as well causes server correction and client prediction to fight.
            if (!entity.isRemoved() && !(entity instanceof ServerPlayer)) {
                entity.move(MoverType.SHULKER_BOX, actual);
                Vec3 velocity = entity.getDeltaMovement();
                if (velocity.y <= 0.0D) {
                    entity.setDeltaMovement(velocity.x, 0.0D, velocity.z);
                    entity.setOnGround(true);
                    entity.fallDistance = 0.0F;
                }
            }
        }
    }

    private Set<Entity> entitiesOnTop() {
        AABB box = getBoundingBox();
        AABB search = new AABB(
                box.minX - 0.35D, box.maxY - 0.75D, box.minZ - 0.35D,
                box.maxX + 0.35D, box.maxY + 3.0D, box.maxZ + 0.35D);
        Set<Entity> roots = new LinkedHashSet<>();
        carriedEntityGraceTicks.replaceAll((uuid, ticks) -> ticks - 1);
        for (Entity entity : level().getEntities(this, search,
                candidate -> !candidate.isSpectator()
                        && !candidate.isRemoved()
                        && !(candidate instanceof HorizontalGateEntity))) {
            Entity root = entity.getRootVehicle();
            AABB rootBox = root.getBoundingBox();
            if (root != this
                    && !isJumpingAway(root, box)
                    && rootBox.maxY > box.maxY - 0.75D
                    && rootBox.minY <= box.maxY + 0.75D) {
                roots.add(root);
                boolean newlyCarried = !carriedEntityGraceTicks.containsKey(root.getUUID());
                carriedEntityGraceTicks.put(root.getUUID(), 6);
                if (newlyCarried && !(root instanceof ServerPlayer)
                        && rootBox.minY < box.maxY
                        && rootBox.minY >= box.maxY - 1.25D) {
                    root.setPos(root.getX(), box.maxY, root.getZ());
                }
            }
        }
        if (level() instanceof ServerLevel serverLevel) {
            Iterator<Map.Entry<UUID, Integer>> iterator = carriedEntityGraceTicks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                Entity entity = serverLevel.getEntity(entry.getKey());
                if (entry.getValue() <= 0 || entity == null || entity.isRemoved()
                        || entity instanceof HorizontalGateEntity
                        || isJumpingAway(entity, box)
                        || !isStillNearPlatform(entity, box)) {
                    iterator.remove();
                } else {
                    roots.add(entity);
                }
            }
        }
        return roots;
    }

    private static boolean isJumpingAway(Entity entity, AABB platformBox) {
        // Vehicles and mobs can receive upward velocity from the rising platform itself.
        // Only a standalone player is detached by this server-side fallback.
        return entity instanceof ServerPlayer
                && entity.getDeltaMovement().y > 0.05D
                && entity.getBoundingBox().minY >= platformBox.maxY - 0.05D;
    }

    private static boolean isStillNearPlatform(Entity entity, AABB platformBox) {
        AABB entityBox = entity.getBoundingBox();
        boolean horizontalOverlap = entityBox.maxX >= platformBox.minX - 0.75D
                && entityBox.minX <= platformBox.maxX + 0.75D
                && entityBox.maxZ >= platformBox.minZ - 0.75D
                && entityBox.minZ <= platformBox.maxZ + 0.75D;
        return horizontalOverlap
                && entityBox.minY >= platformBox.maxY - 1.25D
                && entityBox.minY <= platformBox.maxY + 3.5D;
    }

    private Vec3 platformPositionForController(BlockPos controller) {
        return platformPosition(controller.equals(startController) ? startPlatform : endPlatform);
    }

    private Vec3 platformPosition(BlockPos platform) {
        double horizontalOffset = platformHorizontalCenterOffset();
        return new Vec3(
                platform.getX() + horizontalOffset,
                platform.getY() + 0.2D,
                platform.getZ() + horizontalOffset);
    }

    public boolean isStoppedOnGround() {
        if (isMoving()) return false;
        long gameTime = level().getGameTime();
        if (groundSupportCheckTick == gameTime) return groundSupportCheckResult;
        groundSupportCheckTick = gameTime;
        AABB box = getBoundingBox();
        AABB supportProbe = new AABB(
                box.minX + 0.1D, box.minY - 0.35D, box.minZ + 0.1D,
                box.maxX - 0.1D, box.minY + 0.05D, box.maxZ - 0.1D);
        groundSupportCheckResult = level().getBlockCollisions(this, supportProbe).iterator().hasNext();
        return groundSupportCheckResult;
    }

    public void setFixedYaw(float yaw) {
        entityData.set(FIXED_YAW, yaw);
        lockYaw();
        refreshDimensions();
        // refreshDimensions() recreates a square horizontal box from
        // EntityDimensions. LargeElevatorEntity overrides makeBoundingBox() to
        // restore its real 32 x 16 rectangular footprint.
        setBoundingBox(makeBoundingBox());
    }

    private void lockYaw() {
        float yaw = entityData.get(FIXED_YAW);
        setYRot(yaw);
        yRotO = yaw;
        yBodyRot = yaw;
        yBodyRotO = yaw;
        yHeadRot = yaw;
        yHeadRotO = yaw;
    }

    private void setChangedForSync() {
        hasImpulse = true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MOVING, false);
        entityData.define(FIXED_YAW, 0.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(PomkotsMechs.nbtName("ElevatorControllerId"), controllerId);
        tag.putLong(PomkotsMechs.nbtName("ElevatorStartPos"), startController.asLong());
        tag.putLong(PomkotsMechs.nbtName("ElevatorEndPos"), endController.asLong());
        tag.putLong(PomkotsMechs.nbtName("ElevatorStartPlatformPos"), startPlatform.asLong());
        tag.putLong(PomkotsMechs.nbtName("ElevatorEndPlatformPos"), endPlatform.asLong());
        tag.putLong(PomkotsMechs.nbtName("ElevatorTargetPos"), targetController.asLong());
        tag.putBoolean(PomkotsMechs.nbtName("ElevatorMoving"), isMoving());
        tag.putFloat(PomkotsMechs.nbtName("ElevatorYaw"), entityData.get(FIXED_YAW));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        controllerId = tag.getString(PomkotsMechs.nbtName("ElevatorControllerId"));
        startController = BlockPos.of(tag.getLong(PomkotsMechs.nbtName("ElevatorStartPos")));
        endController = BlockPos.of(tag.getLong(PomkotsMechs.nbtName("ElevatorEndPos")));
        startPlatform = BlockPos.of(tag.getLong(PomkotsMechs.nbtName("ElevatorStartPlatformPos")));
        endPlatform = BlockPos.of(tag.getLong(PomkotsMechs.nbtName("ElevatorEndPlatformPos")));
        targetController = BlockPos.of(tag.getLong(PomkotsMechs.nbtName("ElevatorTargetPos")));
        entityData.set(MOVING, tag.getBoolean(PomkotsMechs.nbtName("ElevatorMoving")));
        setFixedYaw(tag.getFloat(PomkotsMechs.nbtName("ElevatorYaw")));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0,
                state -> state.setAndContinue(
                        RawAnimation.begin().thenPlay("animation.elevator.idle"))));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return geoCache; }
    @Override public boolean canBeCollidedWith() { return !isStoppedOnGround(); }
    @Override public boolean isPushable() { return false; }
    @Override protected void pushEntities() { }
    @Override public PushReaction getPistonPushReaction() { return PushReaction.IGNORE; }
    @Override public boolean isInWall() { return false; }
    @Override protected void checkInsideBlocks() { }
    @Override public boolean isAffectedByFluids() { return false; }
    @Override public int decreaseAirSupply(int air) { return air; }
    @Override public EntityDimensions getDimensions(Pose pose) { return EntityDimensions.fixed(WIDTH, HEIGHT); }
    @Override public Iterable<ItemStack> getArmorSlots() { return NonNullList.withSize(4, ItemStack.EMPTY); }
    @Override public ItemStack getItemBySlot(EquipmentSlot slot) { return ItemStack.EMPTY; }
    @Override public void setItemSlot(EquipmentSlot slot, ItemStack stack) { }
    @Override public HumanoidArm getMainArm() { return HumanoidArm.RIGHT; }
    @Override public boolean shouldBeSaved() { return false; }

}
