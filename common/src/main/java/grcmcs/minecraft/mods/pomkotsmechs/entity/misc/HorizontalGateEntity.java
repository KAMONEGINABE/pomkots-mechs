package grcmcs.minecraft.mods.pomkotsmechs.entity.misc;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** A single-controller 16 x 1 x 16 gate that toggles between two positions. */
public final class HorizontalGateEntity extends LivingEntity implements GeoEntity {
    private static final double MOVE_SPEED = 0.35D;
    private static final double VERTICAL_RELEASE_SPEED = 0.65D;
    private static final double ARRIVAL_DISTANCE_SQR = 0.08D * 0.08D;
    private static final EntityDataAccessor<Boolean> MOVING =
            SynchedEntityData.defineId(HorizontalGateEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> FIXED_YAW =
            SynchedEntityData.defineId(HorizontalGateEntity.class, EntityDataSerializers.FLOAT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private String controllerId = "";
    private Vec3 closedPosition = Vec3.ZERO;
    private Vec3 openPosition = Vec3.ZERO;
    private Vec3 targetPosition = Vec3.ZERO;
    private Vec3 finalPosition = Vec3.ZERO;
    private boolean open;
    private MovePhase movePhase = MovePhase.IDLE;

    public HorizontalGateEntity(EntityType<? extends LivingEntity> type, Level level) {
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

    public void configure(String id, Vec3 closedPosition, Vec3 openPosition, float yaw) {
        controllerId = id == null ? "" : id;
        this.closedPosition = closedPosition;
        this.openPosition = openPosition;
        targetPosition = closedPosition;
        entityData.set(FIXED_YAW, yaw);
        lockYaw();
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

    public boolean matchesController(String id) {
        return !controllerId.isEmpty() && controllerId.equals(id);
    }

    public boolean beginMove() {
        if (entityData.get(MOVING)) return false;
        open = !open;
        finalPosition = open ? openPosition : closedPosition;
        targetPosition = position().add(0.0D, -1.0D, 0.0D);
        movePhase = MovePhase.LOWERING;
        entityData.set(MOVING, true);
        level().playSound(null, blockPosition(), PomkotsMechs.SE_ELEVATOR.get(),
                SoundSource.BLOCKS, 1.0F, 0.7F);
        hasImpulse = true;
        return true;
    }

    @Override
    public void tick() {
        setNoGravity(true);
        super.tick();
        lockYaw();
        setDeltaMovement(Vec3.ZERO);
        if (level().isClientSide || !entityData.get(MOVING)) return;

        Vec3 difference = targetPosition.subtract(position());
        double distance = difference.length();
        double speed = movePhase == MovePhase.SLIDING ? MOVE_SPEED : VERTICAL_RELEASE_SPEED;
        Vec3 step = distance <= speed ? difference : difference.scale(speed / distance);
        // The gate deliberately does not carry entities standing on it.
        setPos(position().add(step));
        hasImpulse = true;

        if (position().distanceToSqr(targetPosition) <= ARRIVAL_DISTANCE_SQR) {
            setPos(targetPosition);
            switch (movePhase) {
                case LOWERING -> {
                    movePhase = MovePhase.SLIDING;
                    targetPosition = new Vec3(finalPosition.x, getY(), finalPosition.z);
                }
                case SLIDING -> {
                    movePhase = MovePhase.RAISING;
                    targetPosition = finalPosition;
                }
                case RAISING -> {
                    movePhase = MovePhase.IDLE;
                    entityData.set(MOVING, false);
                    level().playSound(null, blockPosition(), PomkotsMechs.SE_ELEVATOR.get(),
                            SoundSource.BLOCKS, 1.0F, 0.7F);
                }
                default -> entityData.set(MOVING, false);
            }
        }
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
        tag.putString(PomkotsMechs.nbtName("GateControllerId"), controllerId);
        putVec(tag, "GateClosed", closedPosition);
        putVec(tag, "GateOpen", openPosition);
        putVec(tag, "GateTarget", targetPosition);
        putVec(tag, "GateFinal", finalPosition);
        tag.putBoolean(PomkotsMechs.nbtName("GateIsOpen"), open);
        tag.putBoolean(PomkotsMechs.nbtName("GateMoving"), entityData.get(MOVING));
        tag.putString(PomkotsMechs.nbtName("GateMovePhase"), movePhase.name());
        tag.putFloat(PomkotsMechs.nbtName("GateYaw"), entityData.get(FIXED_YAW));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        controllerId = tag.getString(PomkotsMechs.nbtName("GateControllerId"));
        closedPosition = getVec(tag, "GateClosed");
        openPosition = getVec(tag, "GateOpen");
        targetPosition = getVec(tag, "GateTarget");
        finalPosition = getVec(tag, "GateFinal");
        open = tag.getBoolean(PomkotsMechs.nbtName("GateIsOpen"));
        entityData.set(MOVING, tag.getBoolean(PomkotsMechs.nbtName("GateMoving")));
        try {
            movePhase = MovePhase.valueOf(tag.getString(PomkotsMechs.nbtName("GateMovePhase")));
        } catch (IllegalArgumentException ignored) {
            movePhase = MovePhase.IDLE;
        }
        entityData.set(FIXED_YAW, tag.getFloat(PomkotsMechs.nbtName("GateYaw")));
        lockYaw();
    }

    private static void putVec(CompoundTag tag, String name, Vec3 value) {
        CompoundTag vec = new CompoundTag();
        vec.putDouble("X", value.x);
        vec.putDouble("Y", value.y);
        vec.putDouble("Z", value.z);
        tag.put(PomkotsMechs.nbtName(name), vec);
    }

    private static Vec3 getVec(CompoundTag tag, String name) {
        CompoundTag vec = tag.getCompound(PomkotsMechs.nbtName(name));
        return new Vec3(vec.getDouble("X"), vec.getDouble("Y"), vec.getDouble("Z"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0,
                state -> state.setAndContinue(
                        RawAnimation.begin().thenLoop("animation.elevator.idle"))));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return geoCache; }
    @Override public EntityDimensions getDimensions(Pose pose) { return EntityDimensions.fixed(16.0F, 1.0F); }
    @Override public boolean canBeCollidedWith() { return true; }
    @Override public boolean isPushable() { return false; }
    @Override protected void pushEntities() { }
    @Override public PushReaction getPistonPushReaction() { return PushReaction.IGNORE; }
    @Override public boolean isInWall() { return false; }
    @Override protected void checkInsideBlocks() { }
    @Override public boolean isAffectedByFluids() { return false; }
    @Override public int decreaseAirSupply(int air) { return air; }
    @Override public Iterable<ItemStack> getArmorSlots() { return NonNullList.withSize(4, ItemStack.EMPTY); }
    @Override public ItemStack getItemBySlot(EquipmentSlot slot) { return ItemStack.EMPTY; }
    @Override public void setItemSlot(EquipmentSlot slot, ItemStack stack) { }
    @Override public HumanoidArm getMainArm() { return HumanoidArm.RIGHT; }
    @Override public boolean shouldBeSaved() { return false; }

    private enum MovePhase {
        IDLE,
        LOWERING,
        SLIDING,
        RAISING
    }
}
