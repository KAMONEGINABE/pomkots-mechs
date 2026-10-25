package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.LockTargets;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController2;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.LinkedList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.network.SerializableDataTicket;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class PomkotsVehicleBase extends LivingEntity implements GeoEntity, GeoAnimatable, PomkotsVehicle {
   public static final float DEFAULT_SCALE = 0.5F;
   public ActionController actionController = new ActionController2(this);
   protected short rideCoolTick = 0;
   protected boolean onGroundPrev = true;
   private float soundEffectVolume = -1.0F;
   protected static final int ACT_EVASION = 0;
   protected static final int ACT_JUMP = 1;
   private DriverInput prevDriverInput = new DriverInput((short)0);
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private DriverInput driverInput = new DriverInput((short)0);
   public static final SerializableDataTicket<Short> DRIVER_INPUT_SERIALIZABLE_DATA_TICKET = GeckoLibUtil.addDataTicket(
      new SerializableDataTicket<Short>("pomkotsmechs".toString() + ".driverInput", Short.class) {
         public void encode(Short data, FriendlyByteBuf buffer) {
            try {
               buffer.writeShort(data);
            } catch (Exception e) {
               Utils.logError("", e);
            }
         }

         public Short decode(FriendlyByteBuf buffer) {
            Object o = null;

            try {
               o = buffer.readShort();
            } catch (Exception e) {
               Utils.logError("", e);
            }

            return (Short)o;
         }
      }
   );
   private static final EntityDataAccessor<Boolean> MODE = SynchedEntityData.defineId(PomkotsVehicleBase.class, EntityDataSerializers.BOOLEAN);
   protected static final EntityDataAccessor<Boolean> IS_BOOST = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
   protected LockTargets lockTargets = new LockTargets();
   private static final int MAX_ENERGY = 100;
   private int energy = 100;
   private Vec3 clientSeatPos = Vec3.ZERO;
   protected LinkedList<Vec3> posHistory = new LinkedList<>();
   protected float forwardIntention = 0.0F;
   protected float sidewayIntention = 0.0F;

   public static Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_KNOCKBACK).add(Attributes.MAX_HEALTH, BattleBalance.MECH_HEALTH);
   }

   protected abstract String getMechName();

   public PomkotsVehicleBase(EntityType<? extends LivingEntity> entityType, Level world) {
      super(entityType, world);
      this.noCulling = true;
      this.setYRot(0.0F);
      if (this.isServerSide()) {
         this.setNoGravity(false);
      }

      this.registerActions();
      this.setupProperties();
   }

   protected void registerActions() {
      this.actionController.registerAction(0, new Action(15, 0, 10), ActionController.ActionType.BASE);
      this.actionController.registerAction(1, new Action(10, 6, 4), ActionController.ActionType.BASE);
   }

   protected void setupProperties() {
      this.setMaxUpStep(2.0F);
   }

   public void tick() {
      this.updatePosHistory(this.position());
      this.soundEffectVolume = -1.0F;
      if (!this.isClientSide() && !this.isVehicle() && this.isBoost()) {
         this.setBoost(false);
      }

      super.tick();
      this.actionController.tick();
      if (!this.isAlive() || !this.isVehicle() || !this.canWork()) {
         this.setNoGravity(false);
      } else if (this.rideCoolTick > 0) {
         this.rideCoolTick--;
      } else {
         this.chargeEnergy();
         if (this.isClientSide()) {
            Short b = (Short)this.getAnimData(DRIVER_INPUT_SERIALIZABLE_DATA_TICKET);
            if (b == null) {
               b = (short)0;
            }

            this.driverInput = new DriverInput(b, this.prevDriverInput);
         } else if (this.driverInput != null) {
            this.driverInput = new DriverInput(this.driverInput.getStatus(), this.prevDriverInput);
         }

         if (this.driverInput != null) {
            this.applyPlayerInput(this.driverInput);
         }

         this.fireWeapons();
      }

      this.prevDriverInput = this.driverInput;
      this.onGroundPrev = this.onGround();
   }

   public boolean canWork() {
      return true;
   }

   protected void applyPlayerInput(DriverInput driverInput) {
      this.getUserIntentionForDirectionFromKey(driverInput);
      this.applyPlayerInputWeapons(driverInput);
      this.applyPlayerInputBoost(driverInput);
      this.applyPlayerInputEvasion(driverInput);
      this.applyPlayerInputJump(driverInput);
      this.applyPlayerInputInAirActions(driverInput);
   }

   protected void applyPlayerInputWeapons(DriverInput driverInput) {
   }

   protected void applyPlayerInputBoost(DriverInput driverInput) {
      if (this.getDeltaMovement().z == 0.0 && this.getDeltaMovement().x == 0.0) {
         this.actionController.setBoost(false);
      }
   }

   protected void applyPlayerInputEvasion(DriverInput driverInput) {
      if (driverInput.isEvasionPressed() && this.actionController.getAction(0).startAction()) {
         this.startEvasion();
         this.actionController.setBoost(true);
      }
   }

   protected void startEvasion() {
      if (this.isServerSide()) {
         Vec3 vel;
         if (this.forwardIntention == 0.0F && this.sidewayIntention == 0.0F) {
            vel = new Vec3(0.0, 0.0, 1.0);
         } else {
            vel = new Vec3(this.sidewayIntention, 0.0, this.forwardIntention).normalize();
         }

         vel = vel.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         float distance = this.getHorizontalBoostAcceleration();
         vel = vel.scale(distance);
         this.push(vel.x, vel.y, vel.z);
      }
   }

   protected void applyPlayerInputJump(DriverInput driverInput) {
      if (driverInput.isJumpPressed() && this.onGround()) {
         this.actionController.getAction(1).startAction();
      }

      if (this.actionController.getAction(1).isOnFire() && this.isServerSide()) {
         this.push(0.0, this.getJumpSpeed(), 0.0);
      }
   }

   protected void applyPlayerInputInAirActions(DriverInput driverInput) {
      if (!this.onGround()) {
         if (driverInput.isJumpPressed()) {
            this.setNoGravity(true);
            if (!this.tryVerticalBoost() && this.isServerSide()) {
               this.push(0.0, -0.17640000343322754, 0.0);
            }
         } else if (this.isServerSide()) {
            this.push(0.0, -0.17640000343322754, 0.0);
         }
      } else {
         this.setNoGravity(false);
      }
   }

   protected boolean tryVerticalBoost() {
      if (this.useEnergy(5) && this.isServerSide() && this.getDeltaMovement().y() < this.getVerticalBoostMaxSpeed()) {
         this.push(0.0, this.getVerticalBoostAcceleration(), 0.0);
         return true;
      } else {
         return false;
      }
   }

   protected void fireWeapons() {
   }

   protected float[] getShootingAngle(Entity bullet, boolean useLockTarget) {
      double xRot = 0.0;
      double yRot = 0.0;
      Entity lockTarget = this.lockTargets.getLockTargetHard() != null ? this.lockTargets.getLockTargetHard() : this.lockTargets.getLockTargetSoft();
      if (useLockTarget && lockTarget != null) {
         Vec3 positionA = bullet.position();
         Vec3 positionB = lockTarget.getBoundingBox().getCenter();
         double deltaX = positionB.x - positionA.x;
         double deltaY = positionB.y - positionA.y;
         double deltaZ = positionB.z - positionA.z;
         yRot = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0;
         double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
         xRot = -Math.toDegrees(Math.atan2(deltaY, distanceXZ));
      } else {
         LivingEntity driver = this.getDrivingPassenger();
         Vec3 lookAngle = driver.getLookAngle();
         xRot = -Math.toDegrees(Math.asin(lookAngle.y)) - 7.5;
         yRot = Math.toDegrees(Math.atan2(lookAngle.z, lookAngle.x)) - 90.0;
      }

      return new float[]{(float)xRot, (float)yRot};
   }

   public void travel(Vec3 pos) {
      if (this.isAlive() && this.isVehicle() && this.canWork()) {
         LivingEntity pilot = this.getDrivingPassenger();
         this.setYRot(pilot.getYRot());
         this.yRotO = this.getYRot();
         this.setXRot(pilot.getXRot() * 0.5F);
         this.setRot(this.getYRot(), this.getXRot());
         this.setYBodyRot(this.getYRot());
         this.setYHeadRot(this.getYRot());
         float f = pilot.xxa * 0.5F;
         float f1 = pilot.zza * 0.5F;
         if (this.actionController.isBoost()) {
            this.setSpeed(this.getRunSpeed());
         } else {
            this.setSpeed(this.getWalkSpeed());
         }

         this.hasImpulse = true;
         super.travel(new Vec3(f, pos.y, f1));
      } else {
         super.travel(pos);
      }
   }

   protected void travelBypass(Vec3 pos) {
      super.travel(pos);
   }

   protected float getWalkSpeed() {
      return 0.5F;
   }

   protected float getRunSpeed() {
      return 1.5F;
   }

   protected float getFlyingSpeed() {
      if (!this.isServerSide() || !this.isNoGravity()) {
         return 0.02F;
      } else {
         return this.actionController.isBoost() ? 0.4F : 0.2F;
      }
   }

   protected float getHorizontalBoostAcceleration() {
      return 7.125F;
   }

   protected float getVerticalBoostAcceleration() {
      return 0.3F;
   }

   protected float getVerticalBoostMaxSpeed() {
      return 0.7F;
   }

   protected float getJumpSpeed() {
      return 2.0F;
   }

   public boolean hurt(DamageSource ds, float a) {
      if (ds.getEntity() != null && ds.getEntity().equals(this.getDrivingPassenger())) {
         return false;
      }

      if (this.isClientSide() && !(this instanceof Pmvc01Entity)) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_HIT_EVENT.get());
         ParticleUtil.addParticles(ds, this);
      }

      return super.hurt(ds, a);
   }

   public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
      if (passenger instanceof Player player) {
         if (player.isDeadOrDying()) {
            player.getAbilities().mayfly = true;
         }

         player.invulnerableTime = 40;
      }

      this.lockTargets.clearLockTargets();
      return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
   }

   public InteractionResult interact(Player player, InteractionHand hand) {
      if (this.getDrivingPassenger() == null) {
         if (this.isServerSide() && player.getVehicle() == null) {
            player.setYRot(this.getYRot());
            player.setXRot(this.getXRot());
            player.startRiding(this);
         }

         this.rideCoolTick = 3;
         this.actionController.reset();
         return InteractionResult.SUCCESS;
      } else {
         return super.interact(player, hand);
      }
   }

   protected boolean canAddPassenger(Entity passenger) {
      return this.getPassengers().isEmpty();
   }

   public LivingEntity getDrivingPassenger() {
      if (this.getPassengers().isEmpty()) {
         return null;
      }

      Entity driver = (Entity)this.getPassengers().get(0);
      return driver instanceof LivingEntity le ? le : null;
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController[]{new AnimationController(this, "controller", 0, event -> {
         if (this.getDrivingPassenger() == null) {
            event.getController().forceAnimationReset();
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".idle"));
         } else {
            PlayState res = this.controllAnimationWeapons(event);
            return res != null ? res : this.controllAnimationBasicMove(event);
         }
      }).setSoundKeyframeHandler(soundKeyframeEvent -> this.registerAnimationSoundHandlers(soundKeyframeEvent))});
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "fly", 1, event -> this.controllAnimationFlyingMotion(event))
               .setSoundKeyframeHandler(soundKeyframeEvent -> this.registerAnimationSoundHandlers(soundKeyframeEvent))
         }
      );
      controllers.add(new AnimationController[]{new AnimationController(this, "rotation", 0, event -> this.controllAnimationRotation(event))});
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this, "boosters", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".booster"))
            )
         }
      );
      this.addExtraAnimationController(controllers);
   }

   protected PlayState controllAnimationWeapons(AnimationState<PomkotsVehicleBase> event) {
      return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".idle"));
   }

   protected PlayState controllAnimationBasicMove(AnimationState<PomkotsVehicleBase> event) {
      if (this.actionController.getAction(1).isInAction()) {
         if (this.actionController.getAction(1).isOnStart()) {
            event.getController().forceAnimationReset();
         }

         return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".jump"));
      } else if (event.isMoving()) {
         if (this.actionController.getAction(0).isInAction()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".evasion"));
         } else if (this.actionController.isBoost()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".dash"));
         } else {
            return this.isNoGravity()
               ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".idle"))
               : event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".walk"));
         }
      } else {
         if (this.yRotO != this.getYRot()) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".walk"));
         }

         event.getController().forceAnimationReset();
         return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".idle"));
      }
   }

   protected PlayState controllAnimationFlyingMotion(AnimationState<PomkotsVehicleBase> event) {
      if (this.justLanded((Entity)event.getAnimatable())) {
         event.getController().forceAnimationReset();
         return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".onground"));
      } else {
         return ((PomkotsVehicleBase)event.getAnimatable()).onGround()
            ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"))
            : event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".flylegs"));
      }
   }

   protected PlayState controllAnimationRotation(AnimationState<PomkotsVehicleBase> event) {
      if (event.isMoving()) {
         if (this.isNoGravity() && !this.actionController.isBoost() && !this.actionController.getAction(0).isInAction()) {
            if (this.forwardIntention > 0.0F) {
               return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".frontfly"));
            } else if (this.forwardIntention < 0.0F) {
               return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".backfly"));
            } else if (this.sidewayIntention < 0.0F) {
               return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".rightfly"));
            } else {
               return this.sidewayIntention > 0.0F
                  ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".leftfly"))
                  : event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"));
            }
         } else if (this.sidewayIntention < 0.0F) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".right"));
         } else {
            return this.sidewayIntention > 0.0F
               ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".left"))
               : event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".forward"));
         }
      } else {
         if (!this.actionController.isInActionAll()) {
            event.getController().forceAnimationReset();
         }

         return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"));
      }
   }

   protected void addExtraAnimationController(ControllerRegistrar controllers) {
   }

   protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
   }

   @Override
   public void setDriverInput(DriverInput di) {
      this.driverInput = di;
      if (!this.level().isClientSide) {
         this.setAnimData(DRIVER_INPUT_SERIALIZABLE_DATA_TICKET, this.driverInput.getStatus());
         if (di.isModeChangePressed()) {
            this.setMainMode(!this.isMainMode());
         }
      }
   }

   public DriverInput getDriverInput() {
      return this.driverInput;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(MODE, true);
      this.entityData.define(IS_BOOST, false);
   }

   private void setMainMode(boolean value) {
      this.entityData.set(MODE, value);
   }

   public boolean isMainMode() {
      return (Boolean)this.entityData.get(MODE);
   }

   public boolean isSubMode() {
      return !(Boolean)this.entityData.get(MODE);
   }

   public boolean isBoost() {
      return (Boolean)this.entityData.get(IS_BOOST);
   }

   public void setBoost(boolean broken) {
      this.entityData.set(IS_BOOST, broken);
   }

   @Override
   public LockTargets getLockTargets() {
      return this.lockTargets;
   }

   public int getMaxEnergy() {
      return 100;
   }

   public int getEnergy() {
      return this.energy;
   }

   protected void chargeEnergy() {
      if (this.energy + 2 > 100) {
         this.energy = 100;
      } else {
         this.energy += 2;
      }
   }

   protected boolean useEnergy(int dec) {
      if (this.energy - dec < 0) {
         this.energy = 0;
         return false;
      } else {
         this.energy -= dec;
         return true;
      }
   }

   public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   public Iterable<ItemStack> getArmorSlots() {
      return NonNullList.withSize(4, ItemStack.EMPTY);
   }

   public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
      return ItemStack.EMPTY;
   }

   public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public HumanoidArm getMainArm() {
      return null;
   }

   public double getPassengersRidingOffset() {
      return 2.5;
   }

   public boolean isPushable() {
      return false;
   }

   public void setClientSeatPos(Vec3 seatPos) {
      this.clientSeatPos = seatPos;
   }

   @Override
   public Vec3 getClientSeatPos(Entity passenger) {
      return this.clientSeatPos;
   }

   protected void playSoundEffect(SoundEvent event) {
      this.playSoundEffect(event, 1.0F);
   }

   protected void playSoundEffect(SoundEvent event, float volume) {
      if (this.soundEffectVolume < 0.0F) {
         this.soundEffectVolume = this.computeVolume(100.0);
      }

      if (this.soundEffectVolume > 0.0F) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, volume * this.soundEffectVolume, 1.0F, false);
      }
   }

   public void playSoundPublic(SoundEvent event) {
      this.playSoundEffect(event);
   }

   private float computeVolume(double maxDistance) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player == null) {
         return 0.0F;
      }

      double distance = player.distanceTo(this);
      if (distance > maxDistance) {
         return 0.0F;
      }

      float volume = 1.0F - (float)(distance / maxDistance);
      return Mth.clamp(volume, 0.0F, 1.0F);
   }

   public void playStepSound(BlockPos blockPos, BlockState blockState) {
   }

   private void updatePosHistory(Vec3 v) {
      Vec3 vec = new Vec3(v.x, v.y, v.z);
      if (this.posHistory.size() > 3) {
         this.posHistory.remove(0);
      }

      this.posHistory.add(vec);
   }

   protected boolean justLanded(Entity ent) {
      return ent.getDeltaMovement().y == 0.0 && ent.yOld - ent.getY() > 0.4;
   }

   protected void getUserIntentionForDirectionFromKey(DriverInput driverInput) {
      if (driverInput.isForwardPressed()) {
         this.forwardIntention = 1.0F;
      } else if (driverInput.isBackPressed()) {
         this.forwardIntention = -1.0F;
      } else {
         this.forwardIntention = 0.0F;
      }

      if (driverInput.isRightPressed()) {
         this.sidewayIntention = -1.0F;
      } else if (driverInput.isLeftPressed()) {
         this.sidewayIntention = 1.0F;
      } else {
         this.sidewayIntention = 0.0F;
      }
   }

   protected boolean isSelf(Entity ent) {
      return ent.equals(this) || ent.equals(this.getDrivingPassenger());
   }

   protected boolean isServerSide() {
      return !this.isClientSide();
   }

   protected boolean isClientSide() {
      return this.level().isClientSide();
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double d) {
      return true;
   }

   @Override
   public boolean shouldRenderDefaultHud(String hudName) {
      return false;
   }

   @Override
   public boolean shouldLockMulti(DriverInput driverInput) {
      return false;
   }

   @Override
   public boolean shouldLockWeak(DriverInput driverInput) {
      return false;
   }

   @Override
   public boolean shouldLockStrong(DriverInput driverInput) {
      return driverInput.isLockPressed();
   }

   public boolean displayFireAnimation() {
      return false;
   }
}
