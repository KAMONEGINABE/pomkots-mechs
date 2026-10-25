package grcmcs.minecraft.mods.pomkotsmechs.entity.misc;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ElevatorEntity extends LivingEntity implements GeoEntity, GeoAnimatable {
   public static final ElevatorEntity.Mode INITIAL_MODE = ElevatorEntity.Mode.IDLE;
   private static final float WIDTH = 6.5F;
   private static final float HEIGHT = 0.5F;
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private BlockPos srcBlockPos = BlockPos.ZERO;
   private BlockPos dstBlockPos = BlockPos.ZERO;
   private Vec3 moveVelocity = Vec3.ZERO;
   private int curModeTicks = 0;
   private int moveTicks = 0;
   private boolean moveStarted = false;
   private boolean arrivalShakeDone = false;
   private static final double MOVE_SPEED = 0.7;
   private ElevatorEntity.MovePhase movePhase = ElevatorEntity.MovePhase.START_SHAKE;
   private int phaseTicks = 0;
   private static final EntityDataAccessor<Optional<UUID>> UUID = SynchedEntityData.defineId(ElevatorEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(ElevatorEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(ElevatorEntity.class, EntityDataSerializers.FLOAT);

   public static Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_KNOCKBACK).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.MAX_HEALTH, 1024.0);
   }

   public ElevatorEntity(EntityType<? extends LivingEntity> type, Level level) {
      super(type, level);
      this.setNoGravity(true);
      this.noCulling = true;
   }

   public void tick() {
      this.setNoGravity(true);
      super.tick();
      this.lockYaw();
      if (!this.level().isClientSide()) {
         this.curModeTicks++;
         switch (this.getMode()) {
            case IDLE:
               this.moveVelocity = Vec3.ZERO;
               if (!this.getPassengers().isEmpty()) {
                  this.ejectPassengers();
               }
               break;
            case WAITING:
               this.moveVelocity = Vec3.ZERO;
               this.tickWaiting();
               break;
            case MOVING_TO_DST:
               this.tickMoveTo(this.dstBlockPos);
               break;
            case MOVING_TO_SRC:
               this.tickMoveTo(this.srcBlockPos);
               break;
            case LOCKING:
               this.tickLocking();
         }

         this.move(MoverType.SELF, this.moveVelocity);
      }
   }

   private void lockYaw() {
      this.setYRot(this.getFixedYaw());
      this.yRotO = this.getFixedYaw();
      this.yBodyRot = this.getFixedYaw();
      this.yBodyRotO = this.getFixedYaw();
      this.yHeadRot = this.getFixedYaw();
      this.yHeadRotO = this.getFixedYaw();
   }

   private void tickWaiting() {
      if (this.curModeTicks >= 20 && this.getPassengers().isEmpty()) {
         AABB area = this.getBoundingBox().inflate(2.0);

         for (Pmvc01Entity mech : this.level().getEntitiesOfClass(Pmvc01Entity.class, area)) {
            if (!mech.isPassenger() && mech.getDrivingPassenger() != null && mech.getDrivingPassenger().getUUID().equals(this.getPassengerUUID())) {
               mech.startRiding(this, true);
               this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GASHAN.get());
               break;
            }
         }
      }
   }

   public ElevatorEntity.Mode getNextMoveMode() {
      double srcDistance = this.distanceToSqr(Vec3.atCenterOf(this.srcBlockPos));
      double dstDistance = this.distanceToSqr(Vec3.atCenterOf(this.dstBlockPos));
      return srcDistance <= dstDistance ? ElevatorEntity.Mode.MOVING_TO_DST : ElevatorEntity.Mode.MOVING_TO_SRC;
   }

   public void beginMove() {
      this.phaseTicks = 0;
      this.setMode(this.getNextMoveMode());
      this.movePhase = ElevatorEntity.MovePhase.START_SHAKE;
   }

   private void tickMoveTo(BlockPos targetPos) {
      switch (this.movePhase) {
         case START_SHAKE:
            this.tickStartShake();
            break;
         case MOVING:
            this.tickMoving(targetPos);
            break;
         case END_SHAKE:
            this.tickEndShake();
      }
   }

   private void tickStartShake() {
      this.phaseTicks++;
      if (this.phaseTicks < 20) {
         if (this.phaseTicks == 1) {
            this.playSoundEffect((SoundEvent)PomkotsMechs.SE_ELEVATOR.get());
         }

         double x = Mth.sin(this.phaseTicks * 3.0F) * 0.1;
         double z = Mth.cos(this.phaseTicks * 3.0F) * 0.1;
         double y = Mth.sin(this.phaseTicks * 5.0F) * 0.6;
         this.move(MoverType.SELF, new Vec3(x, y, z));
      }

      this.phaseTicks = 0;
      this.movePhase = ElevatorEntity.MovePhase.MOVING;
   }

   private void tickMoving(BlockPos targetPos) {
      this.phaseTicks++;
      Vec3 target = Vec3.atCenterOf(targetPos);
      Vec3 diff = target.subtract(this.position());
      double distance = diff.length();
      if (distance < 0.3) {
         this.moveVelocity = Vec3.ZERO;
         this.phaseTicks = 0;
         this.movePhase = ElevatorEntity.MovePhase.END_SHAKE;
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_ELEVATOR.get());
      } else {
         double t = this.phaseTicks % 5 * 0.2;
         Vec3 shake = new Vec3((this.random.nextDouble() - 0.5) * 0.02, Math.sin(t * Math.PI) * 0.01, (this.random.nextDouble() - 0.5) * 0.02);
         this.moveVelocity = diff.normalize().scale(0.7).add(shake);
      }
   }

   private void tickEndShake() {
      this.phaseTicks++;
      double x = Mth.sin(this.phaseTicks * 3.0F) * 0.1;
      double z = Mth.cos(this.phaseTicks * 3.0F) * 0.1;
      double y = Mth.sin(this.phaseTicks * 5.0F) * 0.6;
      this.move(MoverType.SELF, new Vec3(x, y, z));
      if (this.phaseTicks >= 8) {
         this.phaseTicks = 0;
         this.movePhase = ElevatorEntity.MovePhase.START_SHAKE;
         this.setMode(ElevatorEntity.Mode.LOCKING);
      }
   }

   private void tickLocking() {
      if (this.curModeTicks > 100) {
         this.ejectPassengers();
         this.setMode(ElevatorEntity.Mode.IDLE);
      }
   }

   private void playSoundEffect(SoundEvent se) {
      this.level().playSound(null, this.blockPosition(), se, SoundSource.BLOCKS, 1.0F, 0.7F);
   }

   public void setMoveVelocity(Vec3 velocity) {
      this.moveVelocity = velocity;
   }

   private void moveEntitiesOnTop(Vec3 delta) {
      if (!(delta.lengthSqr() < 1.0E-4)) {
         AABB area = this.getBoundingBox().move(0.0, 0.1, 0.0).inflate(0.0, 1.5, 0.0);

         for (Entity entity : this.level().getEntities(this, area)) {
            if (!entity.isPassenger()) {
               entity.move(MoverType.SHULKER_BOX, delta);
            }
         }
      }
   }

   public boolean canBeCollidedWith() {
      return true;
   }

   public PushReaction getPistonPushReaction() {
      return PushReaction.IGNORE;
   }

   public boolean canAddPassenger(Entity passenger) {
      return true;
   }

   public double getPassengersRidingOffset() {
      return 0.55;
   }

   public EntityDimensions getDimensions(Pose pose) {
      return EntityDimensions.fixed(6.5F, 0.5F);
   }

   public BlockPos getDstBlockPos() {
      return this.dstBlockPos;
   }

   public void setDstBlockPos(BlockPos dstBlockPos) {
      this.dstBlockPos = dstBlockPos;
   }

   public BlockPos getSrcBlockPos() {
      return this.srcBlockPos;
   }

   public void setSrcBlockPos(BlockPos srcBlockPos) {
      this.srcBlockPos = srcBlockPos;
   }

   public void setMode(ElevatorEntity.Mode mode) {
      this.curModeTicks = 0;
      this.moveTicks = 0;
      this.moveStarted = false;
      this.arrivalShakeDone = false;
      this.entityData.set(MODE, mode.getCode());
   }

   public ElevatorEntity.Mode getMode() {
      return ElevatorEntity.Mode.byCode((Integer)this.entityData.get(MODE));
   }

   public float getFixedYaw() {
      return (Float)this.entityData.get(YAW);
   }

   public void setFixedYaw(float fixedYaw) {
      this.entityData.set(YAW, fixedYaw);
   }

   public UUID getPassengerUUID() {
      Optional<UUID> u = (Optional<UUID>)this.entityData.get(UUID);
      return u.orElse(null);
   }

   public void setPassengerUUID(UUID uuid) {
      this.entityData.set(UUID, Optional.of(uuid));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(MODE, INITIAL_MODE.code);
      this.entityData.define(YAW, 0.0F);
      this.entityData.define(UUID, Optional.empty());
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setMode(ElevatorEntity.Mode.byCode(tag.getInt(PomkotsMechs.nbtName("Mode"))));
      this.dstBlockPos = Utils.loadBlockPos("Dst", tag);
      this.srcBlockPos = Utils.loadBlockPos("Src", tag);
      this.setFixedYaw(tag.getFloat(PomkotsMechs.nbtName("FixedYaw")));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt(PomkotsMechs.nbtName("Mode"), this.getMode().code);
      Utils.saveBlockPos(tag, "Dst", this.dstBlockPos);
      Utils.saveBlockPos(tag, "Src", this.srcBlockPos);
      tag.putFloat(PomkotsMechs.nbtName("FixedYaw"), this.getFixedYaw());
   }

   public boolean shouldBeSaved() {
      return false;
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "basic_move", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlay("animation.elevator.idle")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public boolean isInWall() {
      return false;
   }

   protected void checkInsideBlocks() {
   }

   public boolean isAffectedByFluids() {
      return false;
   }

   public int decreaseAirSupply(int air) {
      return 0;
   }

   public Iterable<ItemStack> getArmorSlots() {
      return NonNullList.withSize(4, ItemStack.EMPTY);
   }

   public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
      return ItemStack.EMPTY;
   }

   public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public boolean isPushable() {
      return false;
   }

   public HumanoidArm getMainArm() {
      return null;
   }

   public enum Mode {
      IDLE(0),
      WAITING(1),
      MOVING_TO_DST(2),
      MOVING_TO_SRC(3),
      LOCKING(4);

      private final int code;

      Mode(int code) {
         this.code = code;
      }

      public int getCode() {
         return this.code;
      }

      public static ElevatorEntity.Mode byCode(int modeCode) {
         for (ElevatorEntity.Mode mode : values()) {
            if (mode.code == modeCode) {
               return mode;
            }
         }

         return IDLE;
      }
   }

   private enum MovePhase {
      START_SHAKE,
      MOVING,
      END_SHAKE;
   }
}
