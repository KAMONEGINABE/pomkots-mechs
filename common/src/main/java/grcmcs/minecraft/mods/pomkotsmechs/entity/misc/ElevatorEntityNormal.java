package grcmcs.minecraft.mods.pomkotsmechs.entity.misc;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
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

public class ElevatorEntityNormal extends LivingEntity implements GeoEntity, GeoAnimatable {
   private static final float WIDTH = 7.0F;
   private static final float HEIGHT = 0.5F;
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private BlockPos srcBlockPos = BlockPos.f_121853_;
   private BlockPos dstBlockPos = BlockPos.f_121853_;
   private Vec3 moveVelocity = Vec3.f_82478_;
   private int curModeTicks = 0;
   private int moveTicks = 0;
   private boolean moveStarted = false;
   private boolean arrivalShakeDone = false;
   private static final double MOVE_SPEED = 0.7;
   private ElevatorEntityNormal.MovePhase movePhase = ElevatorEntityNormal.MovePhase.START_SHAKE;
   private int phaseTicks = 0;
   private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.m_135353_(ElevatorEntityNormal.class, EntityDataSerializers.f_135028_);
   private static final EntityDataAccessor<Float> YAW = SynchedEntityData.m_135353_(ElevatorEntityNormal.class, EntityDataSerializers.f_135029_);

   public static Builder createMobAttributes() {
      return LivingEntity.m_21183_().m_22266_(Attributes.f_22282_).m_22268_(Attributes.f_22278_, 1.0).m_22268_(Attributes.f_22276_, 50.0);
   }

   public ElevatorEntityNormal(EntityType<? extends LivingEntity> type, Level level) {
      super(type, level);
      this.m_20242_(true);
   }

   public void m_8119_() {
      this.m_20242_(true);
      super.m_8119_();
      this.lockYaw();
      if (!this.m_9236_().m_5776_()) {
         this.curModeTicks++;
         switch (this.getMode()) {
            case IDLE:
               this.moveVelocity = Vec3.f_82478_;
               break;
            case WAITING:
               this.moveVelocity = Vec3.f_82478_;
               this.tickWaiting();
               break;
            case MOVING_TO_DST:
               this.tickMoveTo(this.dstBlockPos);
               break;
            case MOVING_TO_SRC:
               this.tickMoveTo(this.srcBlockPos);
            case LOCKING:
         }

         Vec3 prevPos = this.m_20182_();
         this.m_6478_(MoverType.SELF, this.moveVelocity);
      }
   }

   private void lockYaw() {
      this.m_146922_(this.getFixedYaw());
      this.f_19859_ = this.getFixedYaw();
      this.f_20883_ = this.getFixedYaw();
      this.f_20884_ = this.getFixedYaw();
      this.f_20885_ = this.getFixedYaw();
      this.f_20886_ = this.getFixedYaw();
   }

   private void tickWaiting() {
      if (this.curModeTicks >= 100) {
         AABB area = this.m_20191_().m_82400_(2.0);

         for (Pmvc01Entity mech : this.m_9236_().m_45976_(Pmvc01Entity.class, area)) {
            if (!mech.m_20159_()) {
               mech.m_7998_(this, true);
               this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GASHAN.get());
               this.beginMove();
               this.setMode(this.getNextMoveMode());
               break;
            }
         }
      }
   }

   public ElevatorEntityNormal.Mode getNextMoveMode() {
      double srcDistance = this.m_20238_(Vec3.m_82512_(this.srcBlockPos));
      double dstDistance = this.m_20238_(Vec3.m_82512_(this.dstBlockPos));
      return srcDistance <= dstDistance ? ElevatorEntityNormal.Mode.MOVING_TO_DST : ElevatorEntityNormal.Mode.MOVING_TO_SRC;
   }

   private void beginMove() {
      this.phaseTicks = 0;
      this.movePhase = ElevatorEntityNormal.MovePhase.START_SHAKE;
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
      if (this.phaseTicks >= 20) {
         if (this.phaseTicks < 40) {
            if (this.phaseTicks == 20) {
               this.playSoundEffect((SoundEvent)PomkotsMechs.SE_ELEVATOR.get());
            }

            double x = Mth.m_14031_(this.phaseTicks * 3.0F) * 0.1;
            double z = Mth.m_14089_(this.phaseTicks * 3.0F) * 0.1;
            double y = Mth.m_14031_(this.phaseTicks * 5.0F) * 0.6;
            this.m_6478_(MoverType.SELF, new Vec3(x, y, z));
         }

         this.phaseTicks = 0;
         this.movePhase = ElevatorEntityNormal.MovePhase.MOVING;
      }
   }

   private void tickMoving(BlockPos targetPos) {
      this.phaseTicks++;
      Vec3 target = Vec3.m_82512_(targetPos);
      Vec3 diff = target.m_82546_(this.m_20182_());
      double distance = diff.m_82553_();
      if (distance < 0.3) {
         this.moveVelocity = Vec3.f_82478_;
         this.phaseTicks = 0;
         this.movePhase = ElevatorEntityNormal.MovePhase.END_SHAKE;
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_ELEVATOR.get());
      } else {
         double t = this.phaseTicks % 5 * 0.2;
         Vec3 shake = new Vec3((this.f_19796_.m_188500_() - 0.5) * 0.02, Math.sin(t * Math.PI) * 0.01, (this.f_19796_.m_188500_() - 0.5) * 0.02);
         this.moveVelocity = diff.m_82541_().m_82490_(0.7).m_82549_(shake);
      }
   }

   private void tickEndShake() {
      this.phaseTicks++;
      double x = Mth.m_14031_(this.phaseTicks * 3.0F) * 0.1;
      double z = Mth.m_14089_(this.phaseTicks * 3.0F) * 0.1;
      double y = Mth.m_14031_(this.phaseTicks * 5.0F) * 0.6;
      this.m_6478_(MoverType.SELF, new Vec3(x, y, z));
      if (this.phaseTicks >= 8) {
         this.phaseTicks = 0;
         this.movePhase = ElevatorEntityNormal.MovePhase.START_SHAKE;
         this.m_20153_();
         this.setMode(ElevatorEntityNormal.Mode.WAITING);
      }
   }

   private void playSoundEffect(SoundEvent se) {
      this.m_9236_().m_5594_(null, this.m_20183_(), se, SoundSource.BLOCKS, 1.0F, 0.7F);
   }

   public void setMoveVelocity(Vec3 velocity) {
      this.moveVelocity = velocity;
   }

   private void moveEntitiesOnTop(Vec3 delta) {
      if (!(delta.m_82556_() < 1.0E-4)) {
         AABB area = this.m_20191_().m_82386_(0.0, 0.1, 0.0).m_82377_(0.0, 1.5, 0.0);

         for (Entity entity : this.m_9236_().m_45933_(this, area)) {
            if (!entity.m_20159_()) {
               entity.m_6478_(MoverType.SHULKER_BOX, delta);
            }
         }
      }
   }

   public boolean m_5829_() {
      return true;
   }

   public PushReaction m_7752_() {
      return PushReaction.IGNORE;
   }

   public boolean m_7310_(Entity passenger) {
      return true;
   }

   public double m_6048_() {
      return 0.55;
   }

   public EntityDimensions m_6972_(Pose pose) {
      return EntityDimensions.m_20398_(7.0F, 0.5F);
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

   public void setMode(ElevatorEntityNormal.Mode mode) {
      this.curModeTicks = 0;
      this.moveTicks = 0;
      this.moveStarted = false;
      this.arrivalShakeDone = false;
      this.f_19804_.m_135381_(MODE, mode.getCode());
   }

   public ElevatorEntityNormal.Mode getMode() {
      return ElevatorEntityNormal.Mode.byCode((Integer)this.f_19804_.m_135370_(MODE));
   }

   public float getFixedYaw() {
      return (Float)this.f_19804_.m_135370_(YAW);
   }

   public void setFixedYaw(float fixedYaw) {
      this.f_19804_.m_135381_(YAW, fixedYaw);
   }

   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(MODE, ElevatorEntityNormal.Mode.WAITING.code);
      this.f_19804_.m_135372_(YAW, 0.0F);
   }

   public void m_7378_(CompoundTag tag) {
      super.m_7378_(tag);
      this.setMode(ElevatorEntityNormal.Mode.byCode(tag.m_128451_(PomkotsMechs.nbtName("Mode"))));
      this.dstBlockPos = Utils.loadBlockPos("Dst", tag);
      this.srcBlockPos = Utils.loadBlockPos("Src", tag);
      this.setFixedYaw(tag.m_128457_(PomkotsMechs.nbtName("FixedYaw")));
   }

   public void m_7380_(CompoundTag tag) {
      super.m_7380_(tag);
      tag.m_128405_(PomkotsMechs.nbtName("Mode"), this.getMode().code);
      Utils.saveBlockPos(tag, "Dst", this.dstBlockPos);
      Utils.saveBlockPos(tag, "Src", this.srcBlockPos);
      tag.m_128350_(PomkotsMechs.nbtName("FixedYaw"), this.getFixedYaw());
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

   public boolean m_5830_() {
      return false;
   }

   protected void m_20101_() {
   }

   public boolean m_6129_() {
      return false;
   }

   public int m_7302_(int air) {
      return 0;
   }

   public Iterable<ItemStack> m_6168_() {
      return NonNullList.m_122780_(4, ItemStack.f_41583_);
   }

   public ItemStack m_6844_(EquipmentSlot equipmentSlot) {
      return ItemStack.f_41583_;
   }

   public void m_8061_(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public boolean m_6094_() {
      return false;
   }

   public HumanoidArm m_5737_() {
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

      public static ElevatorEntityNormal.Mode byCode(int modeCode) {
         for (ElevatorEntityNormal.Mode mode : values()) {
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
