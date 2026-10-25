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
   private static final EntityDataAccessor<Boolean> MODE = SynchedEntityData.m_135353_(PomkotsVehicleBase.class, EntityDataSerializers.f_135035_);
   protected static final EntityDataAccessor<Boolean> IS_BOOST = SynchedEntityData.m_135353_(Pmvc01Entity.class, EntityDataSerializers.f_135035_);
   protected LockTargets lockTargets = new LockTargets();
   private static final int MAX_ENERGY = 100;
   private int energy = 100;
   private Vec3 clientSeatPos = Vec3.f_82478_;
   protected LinkedList<Vec3> posHistory = new LinkedList<>();
   protected float forwardIntention = 0.0F;
   protected float sidewayIntention = 0.0F;

   public static Builder createMobAttributes() {
      return LivingEntity.m_21183_().m_22266_(Attributes.f_22282_).m_22268_(Attributes.f_22276_, BattleBalance.MECH_HEALTH);
   }

   protected abstract String getMechName();

   public PomkotsVehicleBase(EntityType<? extends LivingEntity> entityType, Level world) {
      super(entityType, world);
      this.f_19811_ = true;
      this.m_146922_(0.0F);
      if (this.isServerSide()) {
         this.m_20242_(false);
      }

      this.registerActions();
      this.setupProperties();
   }

   protected void registerActions() {
      this.actionController.registerAction(0, new Action(15, 0, 10), ActionController.ActionType.BASE);
      this.actionController.registerAction(1, new Action(10, 6, 4), ActionController.ActionType.BASE);
   }

   protected void setupProperties() {
      this.m_274367_(2.0F);
   }

   public void m_8119_() {
      this.updatePosHistory(this.m_20182_());
      this.soundEffectVolume = -1.0F;
      if (!this.isClientSide() && !this.m_20160_() && this.isBoost()) {
         this.setBoost(false);
      }

      super.m_8119_();
      this.actionController.tick();
      if (!this.m_6084_() || !this.m_20160_() || !this.canWork()) {
         this.m_20242_(false);
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
      this.onGroundPrev = this.m_20096_();
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
      if (this.m_20184_().f_82481_ == 0.0 && this.m_20184_().f_82479_ == 0.0) {
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
            vel = new Vec3(this.sidewayIntention, 0.0, this.forwardIntention).m_82541_();
         }

         vel = vel.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         float distance = this.getHorizontalBoostAcceleration();
         vel = vel.m_82490_(distance);
         this.m_5997_(vel.f_82479_, vel.f_82480_, vel.f_82481_);
      }
   }

   protected void applyPlayerInputJump(DriverInput driverInput) {
      if (driverInput.isJumpPressed() && this.m_20096_()) {
         this.actionController.getAction(1).startAction();
      }

      if (this.actionController.getAction(1).isOnFire() && this.isServerSide()) {
         this.m_5997_(0.0, this.getJumpSpeed(), 0.0);
      }
   }

   protected void applyPlayerInputInAirActions(DriverInput driverInput) {
      if (!this.m_20096_()) {
         if (driverInput.isJumpPressed()) {
            this.m_20242_(true);
            if (!this.tryVerticalBoost() && this.isServerSide()) {
               this.m_5997_(0.0, -0.17640000343322754, 0.0);
            }
         } else if (this.isServerSide()) {
            this.m_5997_(0.0, -0.17640000343322754, 0.0);
         }
      } else {
         this.m_20242_(false);
      }
   }

   protected boolean tryVerticalBoost() {
      if (this.useEnergy(5) && this.isServerSide() && this.m_20184_().m_7098_() < this.getVerticalBoostMaxSpeed()) {
         this.m_5997_(0.0, this.getVerticalBoostAcceleration(), 0.0);
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
         Vec3 positionA = bullet.m_20182_();
         Vec3 positionB = lockTarget.m_20191_().m_82399_();
         double deltaX = positionB.f_82479_ - positionA.f_82479_;
         double deltaY = positionB.f_82480_ - positionA.f_82480_;
         double deltaZ = positionB.f_82481_ - positionA.f_82481_;
         yRot = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0;
         double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
         xRot = -Math.toDegrees(Math.atan2(deltaY, distanceXZ));
      } else {
         LivingEntity driver = this.getDrivingPassenger();
         Vec3 lookAngle = driver.m_20154_();
         xRot = -Math.toDegrees(Math.asin(lookAngle.f_82480_)) - 7.5;
         yRot = Math.toDegrees(Math.atan2(lookAngle.f_82481_, lookAngle.f_82479_)) - 90.0;
      }

      return new float[]{(float)xRot, (float)yRot};
   }

   public void m_7023_(Vec3 pos) {
      if (this.m_6084_() && this.m_20160_() && this.canWork()) {
         LivingEntity pilot = this.getDrivingPassenger();
         this.m_146922_(pilot.m_146908_());
         this.f_19859_ = this.m_146908_();
         this.m_146926_(pilot.m_146909_() * 0.5F);
         this.m_19915_(this.m_146908_(), this.m_146909_());
         this.m_5618_(this.m_146908_());
         this.m_5616_(this.m_146908_());
         float f = pilot.f_20900_ * 0.5F;
         float f1 = pilot.f_20902_ * 0.5F;
         if (this.actionController.isBoost()) {
            this.m_7910_(this.getRunSpeed());
         } else {
            this.m_7910_(this.getWalkSpeed());
         }

         this.f_19812_ = true;
         super.m_7023_(new Vec3(f, pos.f_82480_, f1));
      } else {
         super.m_7023_(pos);
      }
   }

   protected void travelBypass(Vec3 pos) {
      super.m_7023_(pos);
   }

   protected float getWalkSpeed() {
      return 0.5F;
   }

   protected float getRunSpeed() {
      return 1.5F;
   }

   protected float m_274460_() {
      if (!this.isServerSide() || !this.m_20068_()) {
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

   public boolean m_6469_(DamageSource ds, float a) {
      if (ds.m_7639_() != null && ds.m_7639_().equals(this.getDrivingPassenger())) {
         return false;
      }

      if (this.isClientSide() && !(this instanceof Pmvc01Entity)) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_HIT_EVENT.get());
         ParticleUtil.addParticles(ds, this);
      }

      return super.m_6469_(ds, a);
   }

   public Vec3 m_7688_(LivingEntity passenger) {
      if (passenger instanceof Player player) {
         if (player.m_21224_()) {
            player.m_150110_().f_35936_ = true;
         }

         player.f_19802_ = 40;
      }

      this.lockTargets.clearLockTargets();
      return new Vec3(this.m_20185_(), this.m_20191_().f_82292_, this.m_20189_());
   }

   public InteractionResult m_6096_(Player player, InteractionHand hand) {
      if (this.getDrivingPassenger() == null) {
         if (this.isServerSide() && player.m_20202_() == null) {
            player.m_146922_(this.m_146908_());
            player.m_146926_(this.m_146909_());
            player.m_20329_(this);
         }

         this.rideCoolTick = 3;
         this.actionController.reset();
         return InteractionResult.SUCCESS;
      } else {
         return super.m_6096_(player, hand);
      }
   }

   protected boolean m_7310_(Entity passenger) {
      return this.m_20197_().isEmpty();
   }

   public LivingEntity getDrivingPassenger() {
      if (this.m_20197_().isEmpty()) {
         return null;
      }

      Entity driver = (Entity)this.m_20197_().get(0);
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
            return this.m_20068_()
               ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".idle"))
               : event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".walk"));
         }
      } else {
         if (this.f_19859_ != this.m_146908_()) {
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
         return ((PomkotsVehicleBase)event.getAnimatable()).m_20096_()
            ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"))
            : event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".flylegs"));
      }
   }

   protected PlayState controllAnimationRotation(AnimationState<PomkotsVehicleBase> event) {
      if (event.isMoving()) {
         if (this.m_20068_() && !this.actionController.isBoost() && !this.actionController.getAction(0).isInAction()) {
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
      if (!this.m_9236_().f_46443_) {
         this.setAnimData(DRIVER_INPUT_SERIALIZABLE_DATA_TICKET, this.driverInput.getStatus());
         if (di.isModeChangePressed()) {
            this.setMainMode(!this.isMainMode());
         }
      }
   }

   public DriverInput getDriverInput() {
      return this.driverInput;
   }

   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(MODE, true);
      this.f_19804_.m_135372_(IS_BOOST, false);
   }

   private void setMainMode(boolean value) {
      this.f_19804_.m_135381_(MODE, value);
   }

   public boolean isMainMode() {
      return (Boolean)this.f_19804_.m_135370_(MODE);
   }

   public boolean isSubMode() {
      return !(Boolean)this.f_19804_.m_135370_(MODE);
   }

   public boolean isBoost() {
      return (Boolean)this.f_19804_.m_135370_(IS_BOOST);
   }

   public void setBoost(boolean broken) {
      this.f_19804_.m_135381_(IS_BOOST, broken);
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

   public boolean m_142535_(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   public Iterable<ItemStack> m_6168_() {
      return NonNullList.m_122780_(4, ItemStack.f_41583_);
   }

   public ItemStack m_6844_(EquipmentSlot equipmentSlot) {
      return ItemStack.f_41583_;
   }

   public void m_8061_(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public HumanoidArm m_5737_() {
      return null;
   }

   public double m_6048_() {
      return 2.5;
   }

   public boolean m_6094_() {
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
         this.m_9236_().m_7785_(this.m_20185_(), this.m_20186_(), this.m_20189_(), event, SoundSource.PLAYERS, volume * this.soundEffectVolume, 1.0F, false);
      }
   }

   public void playSoundPublic(SoundEvent event) {
      this.playSoundEffect(event);
   }

   private float computeVolume(double maxDistance) {
      Minecraft mc = Minecraft.m_91087_();
      LocalPlayer player = mc.f_91074_;
      if (player == null) {
         return 0.0F;
      }

      double distance = player.m_20270_(this);
      if (distance > maxDistance) {
         return 0.0F;
      }

      float volume = 1.0F - (float)(distance / maxDistance);
      return Mth.m_14036_(volume, 0.0F, 1.0F);
   }

   public void m_7355_(BlockPos blockPos, BlockState blockState) {
   }

   private void updatePosHistory(Vec3 v) {
      Vec3 vec = new Vec3(v.f_82479_, v.f_82480_, v.f_82481_);
      if (this.posHistory.size() > 3) {
         this.posHistory.remove(0);
      }

      this.posHistory.add(vec);
   }

   protected boolean justLanded(Entity ent) {
      return ent.m_20184_().f_82480_ == 0.0 && ent.f_19791_ - ent.m_20186_() > 0.4;
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
      return this.m_9236_().m_5776_();
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

   public boolean m_6051_() {
      return false;
   }
}
