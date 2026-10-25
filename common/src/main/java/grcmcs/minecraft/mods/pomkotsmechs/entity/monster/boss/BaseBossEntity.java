package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonsterPercistant;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BaseBossGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.GoalDice;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.HateTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BluePrintItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class BaseBossEntity extends GenericPomkotsMonsterPercistant implements GeoEntity, GeoAnimatable, PomkotsControllable {
   public static final float DEFAULT_SCALE = 1.0F;
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public boolean isCarrying = false;
   protected final BossActionController actionController;
   protected final List<BossHitBoxEntity> hitBoxes = new ArrayList<>();
   public final GoalDice goalDice;
   protected HateTargetGoal hateTargetGoal = null;
   protected float gravity = 0.98F;
   private int waitTicks = 60;
   private int bootTicks = -1;
   private final ServerBossEvent bossInfo = new ServerBossEvent(this.m_5446_(), BossBarColor.RED, BossBarOverlay.PROGRESS);
   private Vec3 clientSeatPos = Vec3.f_82478_;
   private DriverInput driverInput = null;
   private boolean isActivated = true;
   private static final EntityDataAccessor<Boolean> MODE = SynchedEntityData.m_135353_(BaseBossEntity.class, EntityDataSerializers.f_135035_);
   private static final EntityDataAccessor<Integer> AI_MODE = SynchedEntityData.m_135353_(BaseBossEntity.class, EntityDataSerializers.f_135028_);
   private static final EntityDataAccessor<Integer> STUN_POINT = SynchedEntityData.m_135353_(BaseBossEntity.class, EntityDataSerializers.f_135028_);
   protected int aiMode = 1;
   public static final int AI_MODE_CARRYING = -3;
   public static final int AI_MODE_INACTIVE = -2;
   public static final int AI_MODE_PATROL = -1;
   public static final int AI_MODE_BATTLE_PHASE_1 = 1;
   public static final int AI_MODE_BATTLE_PHASE_2 = 2;
   public static final int AI_MODE_BATTLE_PHASE_3 = 3;
   public static final int AI_MODE_BATTLE_PHASE_4 = 4;
   public static final int[] AI_MODE_ALL = new int[]{-1, 1, 2, 3, 4};
   public static final int STUN_STUN_START = 200;
   public static final int STUN_MAX = 300;

   public BossActionController getActionController() {
      return this.actionController;
   }

   public BaseBossEntity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.actionController = new BossActionController();
      this.m_274367_(this.getMechData().maxStepUp);
      this.m_7910_(this.getMechData().speed);
      this.m_21530_();
      this.m_20242_(false);
      this.m_146922_(0.0F);
      this.goalDice = new GoalDice(this);
      this.registerTargetSelectorGoals();
   }

   protected void registerTargetSelectorGoals() {
      this.hateTargetGoal = new HateTargetGoal(this, this.m_21133_(Attributes.f_22277_), 30.0);
      this.f_21346_.m_25352_(2, this.hateTargetGoal);
   }

   protected void registerAdditionalHitBox(BossHitBoxEntity box) {
      this.hitBoxes.add(box);
   }

   @Override
   public void m_8119_() {
      if (this.isServerSide()) {
         if (this.isCarrying && this.m_20096_()) {
            this.isCarrying = false;
         }

         this.updateAiMode();
         Vec3 offset = this.m_20182_();
         if (this.f_19803_) {
            for (BossHitBoxEntity hitBox : this.hitBoxes) {
               hitBox.m_146884_(hitBox.getRelativeParentPos().m_82549_(offset));
               this.m_9236_().m_7967_(hitBox);
            }
         } else {
            for (BossHitBoxEntity hitBox : this.hitBoxes) {
               hitBox.m_146884_(hitBox.getRelativeParentPos().m_82549_(offset));
            }
         }

         this.updateStunPoint();
      }

      super.m_8119_();
      this.actionController.tick();
      if (this.m_6084_() && this.m_20160_() && this.rideCoolTick == 0) {
         this.applyPlayerControll();
      }

      if (this.isServerSide()) {
         this.goalDice.tick();
         if (!this.m_20096_() && !this.m_20068_()) {
            this.m_5997_(0.0, -0.17640000343322754, 0.0);
         }
      }
   }

   protected void registerActionGoal(BaseBossGoal goal, int[] availableMode, int weight) {
      this.f_21345_.m_25352_(1, goal);
      this.goalDice.registerGoal(goal, availableMode, -1.0F, -1.0F, weight);
   }

   protected void registerActionGoal(BaseBossGoal goal, int[] availableMode, float availableDistanceMin, float availableDistanceMax, int weight) {
      this.f_21345_.m_25352_(1, goal);
      this.goalDice.registerGoal(goal, availableMode, availableDistanceMin, availableDistanceMax, weight);
   }

   public abstract void registerControllers(ControllerRegistrar var1);

   protected abstract void applyPlayerControll();

   protected void rangeAttack(Vec3 aabb1, Vec3 aabb2, float damage, int knockBack) {
      Vec3 pilePos1 = aabb1.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_())).m_82549_(this.m_20182_());
      Vec3 pilePos2 = aabb2.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_())).m_82549_(this.m_20182_());
      Level world = this.m_9236_();
      Vec3 kbVel = new Vec3(0.0, 0.0, -1.0).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));

      for (Entity ent : world.m_45933_(null, new AABB(pilePos1, pilePos2))) {
         if (!this.isSelf(ent) && ent instanceof LivingEntity le) {
            le.m_147240_(knockBack, kbVel.f_82479_, kbVel.f_82481_);
            DamageSource ds = this.m_269291_().m_269264_();
            le.m_6469_(ds, damage);
         }
      }
   }

   public void m_8107_() {
      if (this.isServerSide()) {
         if (this.waitTicks > 0) {
            this.waitTicks--;
            this.m_20256_(Vec3.f_82478_);
            this.m_21557_(true);
            return;
         }

         if (this.getAiMode() == -2) {
            if (this.bootTicks > 0) {
               this.bootTicks--;
               this.m_20256_(Vec3.f_82478_);
               this.m_21557_(true);
               return;
            }

            if (this.bootTicks == 0) {
               this.isActivated = true;
               this.setAiMode(1);
            }
         } else if (this.m_21525_()) {
            this.m_21557_(false);
         }
      }

      super.m_8107_();
   }

   public void boot() {
      this.bootTicks = 98;
   }

   @Override
   public boolean m_6469_(DamageSource source, float amount) {
      return Utils.isSystemicDamage(source) ? super.m_6469_(source, amount) : false;
   }

   public boolean hurtFromAdditionalHitBox(DamageSource source, float amount) {
      if (this.getAiMode() == -2) {
         return false;
      }

      if (source.m_276093_(DamageTypes.f_268565_)) {
         amount *= this.getMechData().explosionDamageModifier;
      }

      if (!this.isStunning()) {
         amount *= this.getMechData().baseDamageModifier;
      } else {
         amount *= this.getMechData().baseDamageModifier;
      }

      ParticleUtil.addParticles(source, this);
      if (this.isServerSide() && this.hateTargetGoal != null) {
         LivingEntity cause = this.getCause(source);
         if (cause != null) {
            this.hateTargetGoal.addDamageHate(cause, amount);
         }
      }

      return super.m_6469_(source, amount);
   }

   protected void m_6475_(DamageSource damageSource, float f) {
      float beforeRatio = this.m_21223_() / this.m_21233_();
      super.m_6475_(damageSource, f);
      if (!this.m_9236_().f_46443_ && !this.isStunning()) {
         float afterRatio = this.m_21223_() / this.m_21233_();
         int beforeStage = (int)(beforeRatio * 5.0F);
         int afterStage = (int)(afterRatio * 5.0F);
         if (afterStage != 4 && afterStage < beforeStage) {
            this.f_21345_.m_25386_().forEach(WrappedGoal::m_8041_);
            this.onSmallDown();
            this.burstArmorDrops(1.0F, true);
         }
      }
   }

   private void burstArmorDrops(float ratio, boolean dropBlueprint) {
      if (this.m_9236_() instanceof ServerLevel serverLevel) {
         LootTable var12 = serverLevel.m_7654_().m_278653_().m_278676_(this.m_5743_());
         LootParams params = new Builder(serverLevel)
            .m_287286_(LootContextParams.f_81455_, this)
            .m_287286_(LootContextParams.f_81460_, this.m_20182_())
            .m_287286_(LootContextParams.f_81457_, this.m_269291_().m_269264_())
            .m_287235_(LootContextParamSets.f_81415_);
         ObjectArrayList drops = var12.m_287195_(params);
         RandomSource random = this.f_19796_;

         for (ItemStack stack : drops) {
            if (!(stack.m_41720_() instanceof BluePrintItem) || dropBlueprint && random.m_188502_() % 8 >= 7) {
               Vec3 pos = new Vec3(this.m_20185_(), this.m_20186_() + this.m_20206_() * 0.7, this.m_20189_());
               stack.m_41764_(1);
               ItemEntity item = new ItemEntity(this.m_9236_(), pos.f_82479_, pos.f_82480_, pos.f_82481_, stack.m_41777_());
               item.m_20334_(random.m_188583_() * 0.5, random.m_188500_() * 0.5 + 0.2, random.m_188583_() * 0.5);
               item.m_32010_(40);
               this.m_9236_().m_7967_(item);
            }
         }
      }
   }

   private LivingEntity getCause(DamageSource source) {
      Entity sourceEntity = source.m_7639_();
      LivingEntity res;
      if (sourceEntity instanceof Projectile p) {
         res = Utils.getProjectileOwner(p);
      } else if (sourceEntity instanceof LivingEntity livingEntity) {
         if (livingEntity instanceof PomkotsVehicleBase pv) {
            res = pv.getDrivingPassenger();
         } else {
            res = livingEntity;
         }
      } else {
         res = null;
      }

      return res;
   }

   public void addHateToEntity(LivingEntity entity, float hate) {
      if (this.hateTargetGoal != null) {
         this.hateTargetGoal.addCustomHate(entity, hate);
      }
   }

   public float getEntityHate(LivingEntity entity) {
      return this.hateTargetGoal != null ? this.hateTargetGoal.getHate(entity) : 0.0F;
   }

   public void clearAllHate() {
      if (this.hateTargetGoal != null) {
         this.hateTargetGoal.clearHate();
      }
   }

   public void clearEntityHate(LivingEntity entity) {
      if (this.hateTargetGoal != null) {
         this.hateTargetGoal.clearHate(entity);
      }
   }

   public void m_6457_(ServerPlayer player) {
      super.m_6457_(player);
      this.bossInfo.m_6543_(player);
      this.bossInfo.m_8321_(false);
   }

   public void m_6452_(ServerPlayer player) {
      super.m_6452_(player);
      this.bossInfo.m_6539_(player);
   }

   public void m_8024_() {
      super.m_8024_();
      this.bossInfo.m_142711_(this.m_21223_() / this.m_21233_());
   }

   public void choiMove() {
      this.m_246865_(new Vec3(0.0, 0.0, 0.001).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_())));
   }

   @Override
   public void rotateToTarget(LivingEntity target) {
      if (target != null) {
         Vec3 tgtPos = target.m_20182_();
         Vec3 slfPos = this.m_20182_();
         Vec3 v = slfPos.m_82505_(tgtPos);
         this.rotateDegree((float)Math.toDegrees(Mth.m_14136_(v.f_82481_, v.f_82479_)) - 90.0F);
      }
   }

   public void rotateDegree(float degree) {
      this.m_146922_(degree);
      this.m_5618_(this.m_146908_());
      this.m_5616_(this.m_146908_());
      this.f_19859_ = this.m_146908_();
      this.f_20884_ = this.m_146908_();
      this.f_20886_ = this.m_146908_();
   }

   @Override
   public boolean tryAttack() {
      return false;
   }

   @Override
   public void doAttack() {
   }

   @Override
   public int getMaxAttackCooltime() {
      return 100;
   }

   public double m_6048_() {
      return 19.0;
   }

   public void setClientSeatPos(Vec3 seatPos) {
      this.clientSeatPos = seatPos;
   }

   public Vec3 getClientSeatPos() {
      return this.clientSeatPos;
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   @Override
   public void setDriverInput(DriverInput di) {
      this.driverInput = new DriverInput(di.getStatus(), this.driverInput);
      if (this.isServerSide() && di.isModeChangePressed()) {
         this.setMainMode(!this.isMainMode());
      }
   }

   @Override
   public void m_7380_(CompoundTag compound) {
      super.m_7380_(compound);
      compound.m_128379_(PomkotsMechs.nbtName("BossActivated"), this.isActivated);
      compound.m_128379_(PomkotsMechs.nbtName("IsCarrying"), this.isCarrying);
   }

   @Override
   public void m_7378_(CompoundTag compound) {
      super.m_7378_(compound);
      if (compound.m_128441_(PomkotsMechs.nbtName("BossActivated"))) {
         this.isActivated = compound.m_128471_(PomkotsMechs.nbtName("BossActivated"));
      } else {
         this.isActivated = true;
      }

      if (compound.m_128441_(PomkotsMechs.nbtName("IsCarrying"))) {
         this.isCarrying = compound.m_128471_(PomkotsMechs.nbtName("IsCarrying"));
      } else {
         this.isCarrying = false;
      }
   }

   public void setActivated(boolean value) {
      this.isActivated = value;
      if (!value) {
         this.bootTicks = -1;
      }
   }

   public boolean isActivated() {
      return this.isActivated;
   }

   @Override
   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(MODE, true);
      this.f_19804_.m_135372_(AI_MODE, 1);
      this.f_19804_.m_135372_(STUN_POINT, 0);
   }

   private void setMainMode(boolean value) {
      this.f_19804_.m_135381_(MODE, value);
   }

   public boolean isMainMode() {
      return (Boolean)this.f_19804_.m_135370_(MODE);
   }

   protected void updateAiMode() {
      int nextMode;
      if (this.isCarrying) {
         nextMode = -3;
      } else if (this.isActivated) {
         if (this.aiMode > 0) {
            float healthPercentage = this.m_21223_() / this.m_21233_();
            if (healthPercentage > 0.8) {
               nextMode = 1;
            } else if (healthPercentage > 0.6) {
               nextMode = 2;
            } else if (healthPercentage > 0.2) {
               nextMode = 3;
            } else {
               nextMode = 4;
            }
         } else {
            nextMode = this.aiMode;
         }
      } else {
         nextMode = -2;
      }

      if (this.isServerSide()) {
         int currentMode = this.getAiMode();
         if (currentMode == -2 && nextMode != -2) {
            ClientboundSetTitlesAnimationPacket timesPacket = new ClientboundSetTitlesAnimationPacket(20, 60, 20);
            ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(this.m_5446_());
            ClientboundSetSubtitleTextPacket subtitlePacket = new ClientboundSetSubtitleTextPacket(Component.m_237113_("Activated!!"));

            for (ServerPlayer player : this.bossInfo.m_8324_()) {
               player.f_8906_.m_9829_(timesPacket);
               player.f_8906_.m_9829_(titlePacket);
               player.f_8906_.m_9829_(subtitlePacket);
            }

            this.bossInfo.m_7005_(true);
         }
      }

      this.setAiMode(nextMode);
   }

   public void setAiMode(int value) {
      this.f_19804_.m_135381_(AI_MODE, value);
   }

   public int getAiMode() {
      return (Integer)this.f_19804_.m_135370_(AI_MODE);
   }

   public void addStunPoint(int point) {
      int current = this.getStunPoint();
      if (current < 200) {
         current += point;
         if (current >= 200) {
            current = 300;
            this.m_21051_(Attributes.f_22284_).m_22100_(0.0);
            this.f_21345_.m_25386_().forEach(WrappedGoal::m_8041_);
            this.actionController.reset();
            this.onStun();
         }
      }

      this.setStunPoint(current);
   }

   public void updateStunPoint() {
      int stunPoint = this.getStunPoint();
      if (stunPoint > 0 && this.f_19797_ % 5 == 0) {
         if (stunPoint < 200) {
            stunPoint--;
         } else {
            stunPoint -= 5;
         }

         if (stunPoint == 210) {
            this.m_21051_(Attributes.f_22284_).m_22100_(this.mechData.armor);
            this.offStun();
         } else if (stunPoint == 200) {
            stunPoint = 0;
         }

         this.setStunPoint(stunPoint);
      }
   }

   protected void onStun() {
   }

   protected void offStun() {
   }

   protected void onSmallDown() {
   }

   public void setStunPoint(int value) {
      this.f_19804_.m_135381_(STUN_POINT, value);
   }

   public int getStunPoint() {
      return (Integer)this.f_19804_.m_135370_(STUN_POINT);
   }

   public boolean isStunning() {
      return this.getStunPoint() >= 200;
   }

   public BaseBossEntity.MoveDirection getDominantMoveDirection() {
      Vec3 velocity = this.m_20184_();
      if (velocity.m_82556_() < 0.001) {
         return BaseBossEntity.MoveDirection.NONE;
      } else {
         float yawDeg = this.m_146908_();
         double yawRad = Math.toRadians(yawDeg);
         Vec3 forward = new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad));
         Vec3 right = new Vec3(Math.cos(yawRad), 0.0, Math.sin(yawRad));
         double forwardDot = velocity.m_82526_(forward);
         double rightDot = velocity.m_82526_(right);
         double absForward = Math.abs(forwardDot);
         double absRight = Math.abs(rightDot);
         if (absForward >= absRight) {
            return forwardDot > 0.0 ? BaseBossEntity.MoveDirection.FORWARD : BaseBossEntity.MoveDirection.BACKWARD;
         } else {
            return rightDot > 0.0 ? BaseBossEntity.MoveDirection.RIGHT : BaseBossEntity.MoveDirection.LEFT;
         }
      }
   }

   public boolean m_6128_() {
      return true;
   }

   public boolean m_142535_(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   public boolean isSelf(Entity ent) {
      boolean res = false;

      for (BossHitBoxEntity hitBox : this.hitBoxes) {
         res |= hitBox.equals(ent);
      }

      return res || this.equals(ent);
   }

   protected void m_20101_() {
   }

   protected void playSounds(SoundKeyframeEvent event) {
      if ("saber".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_SABER.get());
      } else if ("missile".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());
      } else if ("canon".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GRENADE_EVENT.get());
      } else if ("dash".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BOOSTER_EVENT.get());
      } else if ("walk_large".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_WALK_LARGE.get());
      } else if ("mech".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MACHINE.get());
      } else if ("jump".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_WALK_LARGE.get());
      } else if ("onground".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_WALK_LARGE.get());
      } else if ("gatling".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_HIT_EVENT.get());
      } else if ("stomp".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BOSSDOWN_EVENT.get());
      } else if ("chainsaw".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_CHAINSAW.get());
      } else if ("impact1".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_IMPACT_1.get());
      } else if ("impact2".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_IMPACT_2.get());
      } else if ("laser1".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BEAM1.get());
      } else if ("laser2".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BEAM2.get());
      } else if ("grenade".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GRENADE_EVENT.get());
      } else if ("gashon".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_STEP.get());
      } else if ("roller".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_ROLLER1.get(), 0.2F);
      } else if ("charge".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_CHARGE.get());
      } else if ("se_heri".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_HERI.get());
      }
   }

   public AABB m_6921_() {
      return this.m_20191_().m_82377_(20.0, 0.0, 20.0);
   }

   public enum MoveDirection {
      FORWARD,
      BACKWARD,
      RIGHT,
      LEFT,
      NONE;
   }
}
