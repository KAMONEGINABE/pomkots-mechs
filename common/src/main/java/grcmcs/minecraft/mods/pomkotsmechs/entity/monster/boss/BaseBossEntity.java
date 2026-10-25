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
   private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), BossBarColor.RED, BossBarOverlay.PROGRESS);
   private Vec3 clientSeatPos = Vec3.ZERO;
   private DriverInput driverInput = null;
   private boolean isActivated = true;
   private static final EntityDataAccessor<Boolean> MODE = SynchedEntityData.defineId(BaseBossEntity.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> AI_MODE = SynchedEntityData.defineId(BaseBossEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> STUN_POINT = SynchedEntityData.defineId(BaseBossEntity.class, EntityDataSerializers.INT);
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
      this.setMaxUpStep(this.getMechData().maxStepUp);
      this.setSpeed(this.getMechData().speed);
      this.setPersistenceRequired();
      this.setNoGravity(false);
      this.setYRot(0.0F);
      this.goalDice = new GoalDice(this);
      this.registerTargetSelectorGoals();
   }

   protected void registerTargetSelectorGoals() {
      this.hateTargetGoal = new HateTargetGoal(this, this.getAttributeValue(Attributes.FOLLOW_RANGE), 30.0);
      this.targetSelector.addGoal(2, this.hateTargetGoal);
   }

   protected void registerAdditionalHitBox(BossHitBoxEntity box) {
      this.hitBoxes.add(box);
   }

   @Override
   public void tick() {
      if (this.isServerSide()) {
         if (this.isCarrying && this.onGround()) {
            this.isCarrying = false;
         }

         this.updateAiMode();
         Vec3 offset = this.position();
         if (this.firstTick) {
            for (BossHitBoxEntity hitBox : this.hitBoxes) {
               hitBox.setPos(hitBox.getRelativeParentPos().add(offset));
               this.level().addFreshEntity(hitBox);
            }
         } else {
            for (BossHitBoxEntity hitBox : this.hitBoxes) {
               hitBox.setPos(hitBox.getRelativeParentPos().add(offset));
            }
         }

         this.updateStunPoint();
      }

      super.tick();
      this.actionController.tick();
      if (this.isAlive() && this.isVehicle() && this.rideCoolTick == 0) {
         this.applyPlayerControll();
      }

      if (this.isServerSide()) {
         this.goalDice.tick();
         if (!this.onGround() && !this.isNoGravity()) {
            this.push(0.0, -0.17640000343322754, 0.0);
         }
      }
   }

   protected void registerActionGoal(BaseBossGoal goal, int[] availableMode, int weight) {
      this.goalSelector.addGoal(1, goal);
      this.goalDice.registerGoal(goal, availableMode, -1.0F, -1.0F, weight);
   }

   protected void registerActionGoal(BaseBossGoal goal, int[] availableMode, float availableDistanceMin, float availableDistanceMax, int weight) {
      this.goalSelector.addGoal(1, goal);
      this.goalDice.registerGoal(goal, availableMode, availableDistanceMin, availableDistanceMax, weight);
   }

   public abstract void registerControllers(ControllerRegistrar var1);

   protected abstract void applyPlayerControll();

   protected void rangeAttack(Vec3 aabb1, Vec3 aabb2, float damage, int knockBack) {
      Vec3 pilePos1 = aabb1.yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
      Vec3 pilePos2 = aabb2.yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
      Level world = this.level();
      Vec3 kbVel = new Vec3(0.0, 0.0, -1.0).yRot((float)Math.toRadians(-1.0 * this.getYRot()));

      for (Entity ent : world.getEntities(null, new AABB(pilePos1, pilePos2))) {
         if (!this.isSelf(ent) && ent instanceof LivingEntity le) {
            le.knockback(knockBack, kbVel.x, kbVel.z);
            DamageSource ds = this.damageSources().generic();
            le.hurt(ds, damage);
         }
      }
   }

   public void aiStep() {
      if (this.isServerSide()) {
         if (this.waitTicks > 0) {
            this.waitTicks--;
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoAi(true);
            return;
         }

         if (this.getAiMode() == -2) {
            if (this.bootTicks > 0) {
               this.bootTicks--;
               this.setDeltaMovement(Vec3.ZERO);
               this.setNoAi(true);
               return;
            }

            if (this.bootTicks == 0) {
               this.isActivated = true;
               this.setAiMode(1);
            }
         } else if (this.isNoAi()) {
            this.setNoAi(false);
         }
      }

      super.aiStep();
   }

   public void boot() {
      this.bootTicks = 98;
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      return Utils.isSystemicDamage(source) ? super.hurt(source, amount) : false;
   }

   public boolean hurtFromAdditionalHitBox(DamageSource source, float amount) {
      if (this.getAiMode() == -2) {
         return false;
      }

      if (source.is(DamageTypes.EXPLOSION)) {
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

      return super.hurt(source, amount);
   }

   protected void actuallyHurt(DamageSource damageSource, float f) {
      float beforeRatio = this.getHealth() / this.getMaxHealth();
      super.actuallyHurt(damageSource, f);
      if (!this.level().isClientSide && !this.isStunning()) {
         float afterRatio = this.getHealth() / this.getMaxHealth();
         int beforeStage = (int)(beforeRatio * 5.0F);
         int afterStage = (int)(afterRatio * 5.0F);
         if (afterStage != 4 && afterStage < beforeStage) {
            this.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);
            this.onSmallDown();
            this.burstArmorDrops(1.0F, true);
         }
      }
   }

   private void burstArmorDrops(float ratio, boolean dropBlueprint) {
      if (this.level() instanceof ServerLevel serverLevel) {
         LootTable var12 = serverLevel.getServer().getLootData().getLootTable(this.getLootTable());
         LootParams params = new Builder(serverLevel)
            .withParameter(LootContextParams.THIS_ENTITY, this)
            .withParameter(LootContextParams.ORIGIN, this.position())
            .withParameter(LootContextParams.DAMAGE_SOURCE, this.damageSources().generic())
            .create(LootContextParamSets.ENTITY);
         ObjectArrayList<ItemStack> drops = var12.getRandomItems(params);
         RandomSource random = this.random;

         for (ItemStack stack : drops) {
            if (!(stack.getItem() instanceof BluePrintItem) || dropBlueprint && random.nextInt() % 8 >= 7) {
               Vec3 pos = new Vec3(this.getX(), this.getY() + this.getBbHeight() * 0.7, this.getZ());
               stack.setCount(1);
               ItemEntity item = new ItemEntity(this.level(), pos.x, pos.y, pos.z, stack.copy());
               item.setDeltaMovement(random.nextGaussian() * 0.5, random.nextDouble() * 0.5 + 0.2, random.nextGaussian() * 0.5);
               item.setPickUpDelay(40);
               this.level().addFreshEntity(item);
            }
         }
      }
   }

   private LivingEntity getCause(DamageSource source) {
      Entity sourceEntity = source.getEntity();
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

   public void startSeenByPlayer(ServerPlayer player) {
      super.startSeenByPlayer(player);
      this.bossInfo.addPlayer(player);
      this.bossInfo.setVisible(false);
   }

   public void stopSeenByPlayer(ServerPlayer player) {
      super.stopSeenByPlayer(player);
      this.bossInfo.removePlayer(player);
   }

   public void customServerAiStep() {
      super.customServerAiStep();
      this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
   }

   public void choiMove() {
      this.addDeltaMovement(new Vec3(0.0, 0.0, 0.001).yRot((float)Math.toRadians(-1.0 * this.getYRot())));
   }

   @Override
   public void rotateToTarget(LivingEntity target) {
      if (target != null) {
         Vec3 tgtPos = target.position();
         Vec3 slfPos = this.position();
         Vec3 v = slfPos.vectorTo(tgtPos);
         this.rotateDegree((float)Math.toDegrees(Mth.atan2(v.z, v.x)) - 90.0F);
      }
   }

   public void rotateDegree(float degree) {
      this.setYRot(degree);
      this.setYBodyRot(this.getYRot());
      this.setYHeadRot(this.getYRot());
      this.yRotO = this.getYRot();
      this.yBodyRotO = this.getYRot();
      this.yHeadRotO = this.getYRot();
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

   public double getPassengersRidingOffset() {
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
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putBoolean(PomkotsMechs.nbtName("BossActivated"), this.isActivated);
      compound.putBoolean(PomkotsMechs.nbtName("IsCarrying"), this.isCarrying);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains(PomkotsMechs.nbtName("BossActivated"))) {
         this.isActivated = compound.getBoolean(PomkotsMechs.nbtName("BossActivated"));
      } else {
         this.isActivated = true;
      }

      if (compound.contains(PomkotsMechs.nbtName("IsCarrying"))) {
         this.isCarrying = compound.getBoolean(PomkotsMechs.nbtName("IsCarrying"));
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
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(MODE, true);
      this.entityData.define(AI_MODE, 1);
      this.entityData.define(STUN_POINT, 0);
   }

   private void setMainMode(boolean value) {
      this.entityData.set(MODE, value);
   }

   public boolean isMainMode() {
      return (Boolean)this.entityData.get(MODE);
   }

   protected void updateAiMode() {
      int nextMode;
      if (this.isCarrying) {
         nextMode = -3;
      } else if (this.isActivated) {
         if (this.aiMode > 0) {
            float healthPercentage = this.getHealth() / this.getMaxHealth();
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
            ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(this.getDisplayName());
            ClientboundSetSubtitleTextPacket subtitlePacket = new ClientboundSetSubtitleTextPacket(Component.literal("Activated!!"));

            for (ServerPlayer player : this.bossInfo.getPlayers()) {
               player.connection.send(timesPacket);
               player.connection.send(titlePacket);
               player.connection.send(subtitlePacket);
            }

            this.bossInfo.setPlayBossMusic(true);
         }
      }

      this.setAiMode(nextMode);
   }

   public void setAiMode(int value) {
      this.entityData.set(AI_MODE, value);
   }

   public int getAiMode() {
      return (Integer)this.entityData.get(AI_MODE);
   }

   public void addStunPoint(int point) {
      int current = this.getStunPoint();
      if (current < 200) {
         current += point;
         if (current >= 200) {
            current = 300;
            this.getAttribute(Attributes.ARMOR).setBaseValue(0.0);
            this.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);
            this.actionController.reset();
            this.onStun();
         }
      }

      this.setStunPoint(current);
   }

   public void updateStunPoint() {
      int stunPoint = this.getStunPoint();
      if (stunPoint > 0 && this.tickCount % 5 == 0) {
         if (stunPoint < 200) {
            stunPoint--;
         } else {
            stunPoint -= 5;
         }

         if (stunPoint == 210) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(this.mechData.armor);
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
      this.entityData.set(STUN_POINT, value);
   }

   public int getStunPoint() {
      return (Integer)this.entityData.get(STUN_POINT);
   }

   public boolean isStunning() {
      return this.getStunPoint() >= 200;
   }

   public BaseBossEntity.MoveDirection getDominantMoveDirection() {
      Vec3 velocity = this.getDeltaMovement();
      if (velocity.lengthSqr() < 0.001) {
         return BaseBossEntity.MoveDirection.NONE;
      } else {
         float yawDeg = this.getYRot();
         double yawRad = Math.toRadians(yawDeg);
         Vec3 forward = new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad));
         Vec3 right = new Vec3(Math.cos(yawRad), 0.0, Math.sin(yawRad));
         double forwardDot = velocity.dot(forward);
         double rightDot = velocity.dot(right);
         double absForward = Math.abs(forwardDot);
         double absRight = Math.abs(rightDot);
         if (absForward >= absRight) {
            return forwardDot > 0.0 ? BaseBossEntity.MoveDirection.FORWARD : BaseBossEntity.MoveDirection.BACKWARD;
         } else {
            return rightDot > 0.0 ? BaseBossEntity.MoveDirection.RIGHT : BaseBossEntity.MoveDirection.LEFT;
         }
      }
   }

   public boolean ignoreExplosion() {
      return true;
   }

   public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   public boolean isSelf(Entity ent) {
      boolean res = false;

      for (BossHitBoxEntity hitBox : this.hitBoxes) {
         res |= hitBox.equals(ent);
      }

      return res || this.equals(ent);
   }

   protected void checkInsideBlocks() {
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

   public AABB getBoundingBoxForCulling() {
      return this.getBoundingBox().inflate(20.0, 0.0, 20.0);
   }

   public enum MoveDirection {
      FORWARD,
      BACKWARD,
      RIGHT,
      LEFT,
      NONE;
   }
}
