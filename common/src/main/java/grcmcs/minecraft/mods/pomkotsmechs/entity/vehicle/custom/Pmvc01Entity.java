package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.MechWorkbenchBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechWorkbenchMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.client.misc.ClientHudShake;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.client.sound.PomkotsSoundManager;
import grcmcs.minecraft.mods.pomkotsmechs.client.sound.SoundConfig;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.BlockPlacementPreviewEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.ElevatorEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb99Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai.MechAutoController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.rail.RailBindComponent;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.RepairKitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItemStackHelper;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitInstance;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.SkillEffectApplicator;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechFeatureType;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStats;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatus;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.CachedBoneFinder;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.BuilderUnitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.CircuitHardLockItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.CircuitSoftLockItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.CoreDrillItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.GliderUnitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.HoverUnitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.RailSliderItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.SBUnitProtoTypeItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.AmagiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShoutouItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.scan.ScanUtils;
import grcmcs.minecraft.mods.pomkotsmechs.save.PomkotsMechsSaveData;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationProcessor.QueuedAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;

public class Pmvc01Entity extends PomkotsVehicleBase implements HasCustomInventoryScreen, Container, MenuProvider {
   public static final float DEFAULT_SCALE = 1.0F;
   public static final int CONTAINER_SIZE = 82;
   public static final int CONTAINER_GENERAL_ITEM_START_INDEX = 27;
   public static final int CONTAINER_ADDITIONAL_CIRCUIT_START_INDEX = 63;
   public static final double BASE_KNOCKBACK_RESISTANCE = 0.8;
   private NonNullList<ItemStack> itemStacks;
   private int fuel = 0;
   private int inAirTicks = 0;
   private float prevHealth = 0.0F;
   private int damageSoundCooldown = 0;
   private MechAutoController mechAutoController = null;
   private MechStatus modifiedMechStatus = new MechStatus();
   private BlockPlacementPreviewEntity blockPreviewEntity = null;
   private final CachedBoneFinder boneFinder = new CachedBoneFinder();
   private float clientDamageThisTick = 0.0F;
   private boolean prevCanwork = false;
   private boolean prevBroken = false;
   private boolean prevLocked = false;
   private final RailBindComponent railBindComponent = new RailBindComponent();
   private final Map<Integer, Item> weaponMap = new HashMap<>();
   protected static final int ACT_RIGHT_HAND = 2;
   protected static final int ACT_LEFT_HAND = 3;
   protected static final int ACT_RIGHT_SHOULDER = 4;
   protected static final int ACT_LEFT_SHOULDER = 5;
   protected static final int ACT_BIND_RAIL = 6;
   protected static final int ACT_DEACTIVATE = 7;
   protected static final int ACT_INACTIVATE = 8;
   protected static final int ACT_WALL_JUMP = 9;
   protected static final int ACT_SUPER_BOOST = 10;
   protected static final int ACT_PLACE_BLOCK = 11;
   protected static final int ACT_RELOAD_RIGHT_HAND = 12;
   protected static final int ACT_RELOAD_LEFT_HAND = 13;
   protected static final int ACT_RELOAD_RIGHT_SHOULDER = 14;
   protected static final int ACT_RELOAD_LEFT_SHOULDER = 15;
   protected static final int ACT_SCAN = 16;
   protected static final int ACT_GATTAI = 99;
   private boolean isHovering = false;
   private final float hoverHeight = 3.0F;
   private float energy = 0.0F;
   private int overHeatCooltime = 0;
   private int OVER_HEAT_COOL_TIME_MAX = 60;
   public static final int INV_PARTS_HEAD = 0;
   public static final int INV_PARTS_BODY = 1;
   public static final int INV_PARTS_ARMS = 2;
   public static final int INV_PARTS_LEGS = 3;
   public static final int INV_PARTS_GENERATOR = 4;
   public static final int INV_PARTS_BOOSTER = 5;
   public static final int INV_WEAPON_RIGHT_HAND = 6;
   public static final int INV_WEAPON_LEFT_HAND = 7;
   public static final int INV_WEAPON_RIGHT_SHOULDER = 8;
   public static final int INV_WEAPON_LEFT_SHOULDER = 9;
   public static final int INV_WEAPON_EXT1 = 10;
   public static final int INV_WEAPON_EXT2 = 11;
   public static final int INV_AMMO_RA = 12;
   public static final int INV_AMMO_LA = 13;
   public static final int INV_AMMO_RS = 14;
   public static final int INV_AMMO_LS = 15;
   public static final int INV_FUEL = 16;
   public static final int INV_REPAIR_KIT = 17;
   private boolean shouldSave = true;
   private MechWorkbenchBlockEntity consoleAccessor = null;
   private ResourceLocation lootTable;
   private long lootTableSeed;
   private final List<Pmvc01Entity.AmmoManager> ammoManagers;
   protected static final EntityDataAccessor<ItemStack> P_HEAD = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<ItemStack> P_ARMS = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<ItemStack> P_BODY = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<ItemStack> P_LEGS = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<ItemStack> P_GENERATOR = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<ItemStack> P_BOOSTER = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<ItemStack> W_RIGHT_ARM = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<ItemStack> W_LEFT_ARM = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<ItemStack> W_RIGHT_SHOULDER = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<ItemStack> W_LEFT_SHOULDER = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<ItemStack> W_EXTENSION1 = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<ItemStack> W_EXTENSION2 = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
   protected static final EntityDataAccessor<Integer> DURABILITY = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> WEIGHT = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> MAX_WEIGHT = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Float> JUMP = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Integer> MAX_ENERGY = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Float> ENERGY_CHARGE_PER_TICK = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Integer> WORK_SEC_PER_FUEL = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Float> SPEED_MODIFIER_EVASION = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Float> SPEED_MODIFIER_VERTICAL = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);
   protected static final EntityDataAccessor<Integer> ENERGY_CONSUME_EVASION = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> ENERGY_CONSUME_VERTICAL = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Boolean> HAS_LOCK_SOFT = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
   protected static final EntityDataAccessor<Boolean> HAS_LOCK_HARD = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
   protected static final EntityDataAccessor<Integer> RELOAD_RA = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> RELOAD_LA = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> RELOAD_RS = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> RELOAD_LS = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> FUEL_MAX = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> FUEL_NOW = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Integer> REPAIR_KIT_NUM = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Boolean> IS_BOUND_TO_RAIL = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
   protected static final EntityDataAccessor<Integer> TEXTURE_COLOR = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Boolean> IS_BROKEN = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
   protected static final EntityDataAccessor<Boolean> IS_LOCKED = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
   protected static final EntityDataAccessor<Boolean> IS_OVER_HEAT = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
   protected static final EntityDataAccessor<Boolean> IS_SUPER_BOOST = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
   protected static final EntityDataAccessor<Boolean> SHOW_PLAYER_INV_SLOT = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
   protected static final EntityDataAccessor<Boolean> BUILD_MODE = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
   protected static final EntityDataAccessor<Optional<UUID>> HAVING_ENTITY = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.OPTIONAL_UUID);
   protected static final EntityDataAccessor<Boolean> IS_GLIDING = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
   private Vec3 muzzlePosRA;
   private Vec3 muzzlePosLA;
   private Vec3 muzzlePosRS;
   private Vec3 muzzlePosLS;
   protected Vec3 mainCameraPosition;
   private boolean wasHurtClient;
   private boolean hasLoopSound;
   private boolean showCustomHealthBar;

   @Override
   protected String getMechName() {
      return "pmv01";
   }

   public static Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(Attributes.ATTACK_KNOCKBACK)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
         .add(Attributes.MAX_HEALTH, BattleBalance.MECH_HEALTH)
         .add(Attributes.ARMOR, 18.0);
   }

   public Pmvc01Entity(EntityType<? extends LivingEntity> entityType, Level world) {
      super(entityType, world);
      this.ammoManagers = List.of(
         new Pmvc01Entity.AmmoManager(this::getAmmoRAFromInventory, this::getRightArmWeaponFromInventory, RELOAD_RA, this, this.actionController.getAction(12)),
         new Pmvc01Entity.AmmoManager(this::getAmmoLAFromInventory, this::getLeftArmWeaponFromInventory, RELOAD_LA, this, this.actionController.getAction(13)),
         new Pmvc01Entity.AmmoManager(
            this::getAmmoRSFromInventory, this::getRightShoulderWeaponFromInventory, RELOAD_RS, this, this.actionController.getAction(14)
         ),
         new Pmvc01Entity.AmmoManager(
            this::getAmmoLSFromInventory, this::getLeftShoulderWeaponFromInventory, RELOAD_LS, this, this.actionController.getAction(15)
         )
      );
      this.muzzlePosRA = Vec3.ZERO;
      this.muzzlePosLA = Vec3.ZERO;
      this.muzzlePosRS = Vec3.ZERO;
      this.muzzlePosLS = Vec3.ZERO;
      this.mainCameraPosition = null;
      this.wasHurtClient = false;
      this.hasLoopSound = false;
      this.showCustomHealthBar = true;
      this.noCulling = false;
      this.itemStacks = NonNullList.withSize(82, ItemStack.EMPTY);
   }

   public CachedBoneFinder getBoneFinder() {
      return this.boneFinder;
   }

   public void setupBoneFinder(BakedGeoModel model) {
      this.boneFinder.setModel(model);
   }

   public MechStatus getModifiedMechStatus() {
      return this.modifiedMechStatus;
   }

   @Override
   public void tick() {
      if (this.firstTick) {
         this.firstTickEvent();
      }

      if (this.getVehicle() instanceof ElevatorEntity tgt) {
         ;
      }

      if (this.isServerSide()) {
         LivingEntity driver = this.getDrivingPassenger();
         if (driver instanceof Mob mob && this.mechAutoController == null) {
            this.mechAutoController = Utils.createMechAutoController(driver, this);
            mob.setNoAi(true);
         }

         if (this.mechAutoController != null) {
            if (driver != null && !(driver instanceof Player)) {
               this.mechAutoController.tick();
            } else {
               this.mechAutoController = null;
               this.lockTargets.clearLockTargets();
            }
         }
      }

      if (this.isClientSide()) {
         if (this.damageSoundCooldown > 0) {
            this.damageSoundCooldown--;
         }

         float current = this.getHealth();
         this.clientDamageThisTick = this.prevHealth - current;
         if (this.clientDamageThisTick > 0.0F && this.consumeWasHurtClient()) {
            this.onClientDamage(this.clientDamageThisTick);
         }

         this.prevHealth = current;
      }

      if (this.actionController.getAction(99).isInAction()) {
         this.handleGattaiMode();
      } else {
         if (this.onGroundPrev) {
            this.inAirTicks = 0;
         } else {
            this.inAirTicks++;
         }

         this.handleMechActivationAction();
         if (this.areWeaponsChanged()) {
            this.registerWeapons();
         }

         if (this.isServerSide()) {
            if (this.isAlive() && this.isVehicle() && this.tickCount % 20 == 0) {
               this.syncFuels();
            }

            if (this.isHovering() && this.isServerSide()) {
               this.setNoGravity(true);
            }
         }

         this.tickAmmos();
         this.resetAllActionsWhenNotActive();
         super.tick();
         if (this.isServerSide()) {
            if (this.isBoundToRail()) {
               Vec3 currentVelocity = this.getDeltaMovement();
               this.railBindComponent.moveRobotOnRail(this, currentVelocity);
            }

            if (this.isBuildMode()) {
               if (this.getDrivingPassenger() instanceof Player player) {
                  this.updateBuildModePreview(player);
               } else {
                  this.setBuildMode(false);
               }
            }

            if (this.havingEntity().isPresent()) {
               Entity tgt = ((ServerLevel)this.level()).getEntity(this.havingEntity().get());
               if (tgt != null && tgt.isAlive()) {
                  tgt.setDeltaMovement(Vec3.ZERO);
                  Vec3 pos = this.position();
                  tgt.moveTo(pos.x, pos.y + 7.5, pos.z);
                  float rot = this.getYRot();
                  tgt.setYRot(rot);
                  tgt.setYBodyRot(rot);
                  tgt.setYHeadRot(rot);
                  tgt.yRotO = rot;
               } else {
                  this.setHavingEntity(Optional.empty());
               }
            }
         } else if (this.onGround()) {
            if (this.actionController.getAction(0).isInAction()) {
               this.spawnMovingParticles(6, 3.0F, true);
            } else if (this.actionController.isBoost() || this.isSuperBoost()) {
               this.spawnMovingParticles(20, 0.8F, true);
            }
         }
      }
   }

   public void startArena(LivingEntity target) {
      if (this.getDrivingPassenger() instanceof Mob && this.mechAutoController != null) {
         this.mechAutoController.startArenaBattle(target);
      }
   }

   public float getClientDamageThisTick() {
      return this.clientDamageThisTick;
   }

   private void onClientDamage(float damage) {
      if (damage > 50.0F) {
         this.playDamageSound((SoundEvent)PomkotsMechs.SE_HIT_HEAVY_EVENT.get());
         ParticleUtil.addSparkParticles(this.getBoundingBox().getCenter(), this.level(), 15, (ParticleOptions)PomkotsMechs.SPARK.get());
      } else if (damage > 15.0F) {
         this.playDamageSound((SoundEvent)PomkotsMechs.SE_HIT_MIDDLE_EVENT.get());
         ParticleUtil.addSparkParticles(this.getBoundingBox().getCenter(), this.level(), 10, (ParticleOptions)PomkotsMechs.SPARK.get());
      } else {
         this.playDamageSound((SoundEvent)PomkotsMechs.SE_HIT_EVENT.get());
         ParticleUtil.addSparkParticles(this.getBoundingBox().getCenter(), this.level(), 5, (ParticleOptions)PomkotsMechs.SPARK.get());
      }

      if (this.getDrivingPassenger() instanceof LocalPlayer) {
         ClientHudShake.addShake(Math.min(damage * 0.5F, 10.0F));
      }
   }

   private void playDamageSound(SoundEvent se) {
      if (this.damageSoundCooldown <= 0) {
         this.playSoundEffect(se);
         this.damageSoundCooldown = 10;
      }
   }

   protected void playHurtSound(DamageSource damageSource) {
   }

   private void handleGattaiMode() {
      this.setNoGravity(true);
      super.tick();
      Action act = this.actionController.getAction(99);
      if (this.isServerSide()) {
         if (act.currentFireTime < 15) {
            this.setDeltaMovement(0.0, 0.0, 0.0);
         } else {
            this.setDeltaMovement(0.0, -2.0, 0.0);
            if (this.onGround()) {
               act.reset();
               this.setNoGravity(false);
            }

            List<Pmb99Entity> ents = this.level().getEntitiesOfClass(Pmb99Entity.class, this.getBoundingBox());
            if (!ents.isEmpty()) {
               Pmb99Entity tgt = ents.get(0);
               this.startRiding(tgt);
               act.reset();
            }
         }
      } else if (act.currentFireTime >= 15) {
         if (this.onGround()) {
            act.reset();
            this.setNoGravity(false);
         }

         List<Pmb99Entity> ents = this.level().getEntitiesOfClass(Pmb99Entity.class, this.getBoundingBox());
         if (!ents.isEmpty() || this.getVehicle() instanceof Pmb99Entity) {
            this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GASHAN.get());
            Vec3 p1 = new Vec3(-1.0, 7.0, -1.0).yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
            Vec3 p2 = new Vec3(1.0, 7.0, -1.0).yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
            Vec3 p3 = new Vec3(-1.0, 7.0, 1.0).yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
            Vec3 p4 = new Vec3(1.0, 7.0, 1.0).yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
            ParticleUtil.addSparkParticles(p1, this.level());
            ParticleUtil.addSparkParticles(p2, this.level());
            ParticleUtil.addSparkParticles(p3, this.level());
            ParticleUtil.addSparkParticles(p4, this.level());
            act.reset();
         }
      }
   }

   private void spawnMovingParticles(int amount, float height, boolean smoke) {
      Vec3 offset = this.position();
      Vec3 pos1 = new Vec3(1.0, 0.0, 0.0);
      pos1 = pos1.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
      pos1 = offset.add(pos1);
      Vec3 pos2 = new Vec3(-1.0, 0.0, 0.0);
      pos2 = pos2.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
      pos2 = offset.add(pos2);

      for (int i = 0; i < amount; i++) {
         double dx = this.random.nextDouble() - 0.5;
         double dz = this.random.nextDouble() - 0.5;
         double vy = this.random.nextDouble() * height + 1.0;
         if (i < 3) {
            this.level()
               .addAlwaysVisibleParticle((ParticleOptions)PomkotsMechs.MECH_DUST_HEAVY.get(), true, pos1.x + dx, pos1.y, pos1.z + dz, 0.0, vy, 0.0);
            this.level()
               .addAlwaysVisibleParticle((ParticleOptions)PomkotsMechs.MECH_DUST_HEAVY.get(), true, pos2.x + dx, pos2.y, pos2.z + dz, 0.0, vy, 0.0);
         }

         if (smoke) {
            double sy = this.random.nextDouble() * 0.2 + 0.1;
            this.level().addAlwaysVisibleParticle((ParticleOptions)PomkotsMechs.MECH_DUST.get(), true, pos1.x() + dz, pos1.y(), pos1.z() + dx, 0.0, sy, 0.0);
            this.level().addAlwaysVisibleParticle((ParticleOptions)PomkotsMechs.MECH_DUST.get(), true, pos2.x() + dz, pos2.y(), pos2.z() + dx, 0.0, sy, 0.0);
         }
      }
   }

   private void resetAllActionsWhenNotActive() {
      if (this.isBroken() || !this.canWork(false) || !this.isVehicle()) {
         for (Action a : this.actionController.getAllActions()) {
            if (a.isInAction()) {
               a.reset();
            }
         }
      }
   }

   private void updateBuildModePreview(Player player) {
      HitResult hitResult = player.pick(20.0, 0.0F, true);
      Vec3 newPos;
      if (hitResult.getType() == Type.BLOCK) {
         BlockHitResult blockHitResult = (BlockHitResult)hitResult;
         BlockPos blockPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
         newPos = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
      } else {
         Vec3 look = player.getLookAngle();
         Vec3 eyePos = player.getEyePosition(1.0F);
         newPos = eyePos.add(look.scale(20.0));
      }

      if (this.blockPreviewEntity != null) {
         this.blockPreviewEntity.moveTo(newPos);
      }
   }

   protected void firstTickEvent() {
      if (this.isServerSide()) {
         this.syncAllParameter2Client();
         this.initializeAmmoManager(false);
      }

      this.tickAllParts();
   }

   protected void tickAllParts() {
      for (ItemStack parts : this.itemStacks) {
         if (parts.getItem() instanceof BasePartsItem) {
            parts.inventoryTick(this.level(), this, 0, false);
         }
      }
   }

   private void handleMechActivationAction() {
      if (this.firstTick) {
         this.prevLocked = this.isLocked();
         this.prevBroken = this.isBroken();
         this.prevCanwork = this.canWork(false);
      }

      if (this.isBroken()) {
         if (!this.prevBroken) {
            this.actionController.getAction(7).startAction();
            if (this.isClientSide()) {
               this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EXPLOSION_EVENT.get());
            }
         } else if (this.getMaxHealth() == this.getHealth()) {
            this.setBroken(false);
            this.actionController.getAction(8).startAction();
         }

         if (this.actionController.getAction(7).isInAction() && this.isClientSide()) {
            this.spawnDestructionParticles();
         }
      } else {
         boolean canWork = this.canWork(false);
         if ((!this.prevCanwork || canWork) && (this.prevLocked || !this.isLocked())) {
            if (!this.prevCanwork && canWork || this.prevLocked && !this.isLocked()) {
               this.actionController.getAction(8).startAction();
            }
         } else {
            this.actionController.getAction(7).startAction();
         }

         this.prevCanwork = canWork;
      }

      this.prevBroken = this.isBroken();
      this.prevLocked = this.isLocked();
   }

   public boolean isBoundToRail() {
      return (Boolean)this.entityData.get(IS_BOUND_TO_RAIL);
   }

   public boolean tryBindToRail() {
      BlockPos bp = this.railBindComponent.findRailBlockBelow(this.level(), this.blockPosition());
      this.entityData.set(IS_BOUND_TO_RAIL, bp != null);
      return bp != null;
   }

   public void unbindFromRail() {
      this.entityData.set(IS_BOUND_TO_RAIL, false);
   }

   public void setDeltaMovement(Vec3 movement) {
      if (!Double.isNaN(movement.x) && !Double.isNaN(movement.y) && !Double.isNaN(movement.z)) {
         super.setDeltaMovement(movement);
      } else {
         PomkotsMechs.LOGGER.info(movement + "");
      }
   }

   @Override
   public boolean canWork() {
      return this.canWork(true);
   }

   public boolean canWork(boolean consumeFuel) {
      boolean res = !this.getHeadParts().isEmpty()
         && !this.getBodyParts().isEmpty()
         && !this.getArmParts().isEmpty()
         && !this.getLegsParts().isEmpty()
         && !this.getGenerator().isEmpty();
      if (consumeFuel && this.isServerSide()) {
         res = res && this.consumeFuel();
      } else {
         res = res && this.getFuelNow() != 0;
      }

      if (!res) {
         this.resetExtensionUnitStatus();
         this.setBoost(false);
      }

      return res;
   }

   public void resetExtensionUnitStatus() {
      this.isHovering = false;
      this.unbindFromRail();
      this.setSuperBoost(false);
      this.setGliding(false);
   }

   public boolean shouldShowName() {
      return false;
   }

   public boolean isCustomNameVisible() {
      return false;
   }

   private boolean areWeaponsChanged() {
      boolean b = false;
      b = this.checkWeaponChanged(6, this.getRightArmWeapon().getItem());
      b = b || this.checkWeaponChanged(7, this.getLeftArmWeapon().getItem());
      b = b || this.checkWeaponChanged(8, this.getRightShoulderWeapon().getItem());
      return b || this.checkWeaponChanged(9, this.getLeftShoulderWeapon().getItem());
   }

   private boolean checkWeaponChanged(int num, Item latest) {
      Item old = this.weaponMap.get(num);
      if (old == latest) {
         return false;
      }

      this.weaponMap.put(num, latest);
      return true;
   }

   @Override
   protected void registerActions() {
      this.actionController.registerAction(0, new Action(15, 0, 15), ActionController.ActionType.BASE);
      this.actionController.registerAction(1, new Action(10, 6, 4), ActionController.ActionType.BASE);
      this.actionController.registerAction(2, new ActionWeapon(this, 6), ActionController.ActionType.R_ARM_MAIN);
      this.actionController.registerAction(3, new ActionWeapon(this, 7), ActionController.ActionType.L_ARM_MAIN);
      this.actionController.registerAction(4, new ActionWeapon(this, 8), ActionController.ActionType.R_SHL_MAIN);
      this.actionController.registerAction(5, new ActionWeapon(this, 9), ActionController.ActionType.L_SHL_MAIN);
      this.actionController.registerAction(6, new Action(10, 0, 10), ActionController.ActionType.BASE);
      this.actionController.registerAction(7, new Action(10, 0, 10), ActionController.ActionType.BASE);
      this.actionController.registerAction(8, new Action(10, 0, 10), ActionController.ActionType.BASE);
      this.actionController.registerAction(9, new Action(9, 6, 4), ActionController.ActionType.BASE);
      this.actionController.registerAction(10, new Action(60, 14, 6), ActionController.ActionType.BASE);
      this.actionController.registerAction(11, new Action(21, 15, 5), ActionController.ActionType.BASE);
      this.actionController.registerAction(12, new Action(21, 10, 5), ActionController.ActionType.BASE);
      this.actionController.registerAction(13, new Action(21, 10, 5), ActionController.ActionType.BASE);
      this.actionController.registerAction(14, new Action(21, 10, 5), ActionController.ActionType.BASE);
      this.actionController.registerAction(15, new Action(21, 10, 5), ActionController.ActionType.BASE);
      this.actionController.registerAction(16, new Action(100, 2, 20), ActionController.ActionType.BASE);
      this.actionController.registerAction(99, new Action(60, 5, 60), ActionController.ActionType.BASE);
      this.registerWeapons();
   }

   protected void registerWeapons() {
      this.registerWeapon((ActionWeapon)this.actionController.getAction(2), this.getRightArmWeapon());
      this.registerWeapon((ActionWeapon)this.actionController.getAction(3), this.getLeftArmWeapon());
      this.registerWeapon((ActionWeapon)this.actionController.getAction(4), this.getRightShoulderWeapon());
      this.registerWeapon((ActionWeapon)this.actionController.getAction(5), this.getLeftShoulderWeapon());
   }

   protected void registerWeapon(ActionWeapon act, ItemStack itemStack) {
      BasePartsItem.WeaponInterface wpn = null;
      if (itemStack.getItem() instanceof BasePartsItem.Weapon bw) {
         wpn = bw;
      } else {
         wpn = new ActionWeapon.DefaultDummyWeapon();
      }

      act.setWeapon(wpn, itemStack);
   }

   @Override
   protected void applyPlayerInput(DriverInput driverInput) {
      if (!this.isBroken()) {
         this.getUserIntentionForDirectionFromKey(driverInput);
         this.applyPlayerInputWeapons(driverInput);
         this.applyPlayerInputBoost(driverInput);
         if (driverInput.isLockPressed()
            && !this.hasHardLockCircuit()
            && !this.actionController.getAction(16).isInAction()
            && !this.actionController.getAction(16).isInCooltime()) {
            this.actionController.getAction(16).startAction();
            if (this.isServerSide() && this.getDrivingPassenger() instanceof ServerPlayer serverPlayer) {
               ScanUtils.spawnPersonalScan((ServerLevel)this.level(), serverPlayer, this.position());
            }
         }

         if (!this.isSuperBoost()) {
            this.applyPlayerInputEvasion(driverInput);
            this.applyPlayerInputJump(driverInput);
            this.applyPlayerInputInAirActions(driverInput);
         }
      }
   }

   @Override
   protected void applyPlayerInputWeapons(DriverInput driverInput) {
      if (this.isBuildMode()) {
         if (this.isServerSide() && this.blockPreviewEntity != null) {
            if (!this.actionController.getAction(11).isInAction()) {
               if (driverInput.isWeaponRightShoulderReleased()) {
                  this.blockPreviewEntity.setYRot(this.blockPreviewEntity.getYRot() + 90.0F);
               } else if (driverInput.isWeaponLeftShoulderReleased()) {
                  this.blockPreviewEntity.incrementPatternIndex();
               }
            }

            if (this.actionController.getAction(11).isOnFire()
               && this.level() instanceof ServerLevel l
               && this.getDrivingPassenger() instanceof ServerPlayer p) {
               this.blockPreviewEntity.placeBlocks(l, p);
            }
         }

         if (driverInput.isWeaponRightHandReleased()
            && !this.actionController.getAction(11).isInAction()
            && !this.actionController.getAction(11).isInCooltime()) {
            this.actionController.getAction(11).startAction();
         }
      } else {
         this.applyPlayerInputWeapon(
            (ActionWeapon)this.actionController.getAction(2),
            (ActionWeapon)this.actionController.getAction(3),
            driverInput,
            driverInput.isWeaponRightHandPressed(),
            driverInput.isWeaponRightHandReleased(),
            this.getAmmoManager(6)
         );
         this.applyPlayerInputWeapon(
            (ActionWeapon)this.actionController.getAction(3),
            (ActionWeapon)this.actionController.getAction(2),
            driverInput,
            driverInput.isWeaponLeftHandPressed(),
            driverInput.isWeaponLeftHandReleased(),
            this.getAmmoManager(7)
         );
         this.applyPlayerInputWeapon(
            (ActionWeapon)this.actionController.getAction(4),
            (ActionWeapon)this.actionController.getAction(5),
            driverInput,
            driverInput.isWeaponRightShoulderPressed(),
            driverInput.isWeaponRightShoulderReleased(),
            this.getAmmoManager(8)
         );
         this.applyPlayerInputWeapon(
            (ActionWeapon)this.actionController.getAction(5),
            (ActionWeapon)this.actionController.getAction(4),
            driverInput,
            driverInput.isWeaponLeftShoulderPressed(),
            driverInput.isWeaponLeftShoulderReleased(),
            this.getAmmoManager(9)
         );
      }

      if (driverInput.isExtension1Released()) {
         this.applyPlayerInputExtension(this.getExtension1Weapon());
      }

      if (driverInput.isExtension2Released()) {
         this.applyPlayerInputExtension(this.getExtension2Weapon());
      }

      if (driverInput.isRepairReleased()) {
         this.repair();
      }
   }

   protected void applyPlayerInputWeapon(
      ActionWeapon act, ActionWeapon actLinked, DriverInput driverInput, boolean isPressed, boolean isReleased, Pmvc01Entity.AmmoManager ammoManager
   ) {
      if (this.getVehicle() == null) {
         if (ammoManager.isReloading()) {
            if (act.isInAction()) {
               act.reset();
            }
         } else if (driverInput.isReloadPressed() && isPressed) {
            ammoManager.startReload();
         } else {
            if (act.getMotion().getType().equals(Motion.MotionType.MULTI_LOCK)) {
               if (isReleased) {
                  if (this.shouldLinkWeapon(act.getWeaponItemSlot(), actLinked.getWeaponItemSlot()) && !actLinked.isInAction()) {
                     if (this.isServerSide()) {
                        this.lockTargets.syncTargetMulti(act.getWeaponItemSlot(), actLinked.getWeaponItemSlot());
                     }

                     actLinked.startAction();
                  }

                  act.startAction();
               }
            } else if (act.getMotion().getType().equals(Motion.MotionType.CONTINUOUS)) {
               if (act.canStartAction() && isPressed) {
                  if (act.getMotion().concurrentAvailable() || !this.isUsingUnConcurrentWeapons()) {
                     act.startAction();
                  }
               } else if (act.isInAction() && isReleased) {
                  act.reset();
               }
            } else if (act.getMotion().getType().equals(Motion.MotionType.CHARGE)) {
               if (act.canStartAction() && isPressed) {
                  if (act.getMotion().concurrentAvailable() || !this.isUsingUnConcurrentWeapons()) {
                     act.startAction();
                  }
               } else if (act.isInAction() && isReleased && !act.isInFire()) {
                  act.fireAction();
               }
            } else if (act.getMotion().getType().equals(Motion.MotionType.TOGGLE)) {
               if (act.canStartAction() && isPressed && (act.getMotion().concurrentAvailable() || !this.isUsingUnConcurrentWeapons())) {
                  act.startAction();
               }
            } else if (act.canStartAction() && isPressed && (act.getMotion().concurrentAvailable() || !this.isUsingUnConcurrentWeapons())) {
               act.startAction();
            }
         }
      }
   }

   public boolean shouldLinkWeapon(int srcInvSlot, int dstInvSlot) {
      ItemStack srcW = this.getPart(srcInvSlot);
      ItemStack dstW = this.getPart(dstInvSlot);
      return srcW.is(dstW.getItem())
         && srcW.getItem() instanceof BasePartsItem.Weapon w
         && w.getWeaponCategory() == BasePartsItem.WeaponCategory.MISSILE;
   }

   private ItemStack getPart(int slot) {
      return switch (slot) {
         case 6 -> this.getRightArmWeapon();
         case 7 -> this.getLeftArmWeapon();
         case 8 -> this.getRightShoulderWeapon();
         case 9 -> this.getLeftShoulderWeapon();
         default -> ItemStack.EMPTY;
      };
   }

   private boolean isUsingUnConcurrentWeapons() {
      return this.isUsingUnConcurrentWeapon((ActionWeapon)this.actionController.getAction(2))
         || this.isUsingUnConcurrentWeapon((ActionWeapon)this.actionController.getAction(3))
         || this.isUsingUnConcurrentWeapon((ActionWeapon)this.actionController.getAction(4))
         || this.isUsingUnConcurrentWeapon((ActionWeapon)this.actionController.getAction(5));
   }

   private boolean isUsingUnConcurrentWeapon(ActionWeapon act) {
      return !act.getMotion().concurrentAvailable() && act.isInAction();
   }

   protected void applyPlayerInputExtension(ItemStack extensionStack) {
      if (!extensionStack.isEmpty()) {
         Item extensionItem = extensionStack.getItem();
         if (extensionItem instanceof HoverUnitItem) {
            this.isHovering = !this.isHovering;
         } else if (extensionItem instanceof RailSliderItem) {
            if (this.isBoundToRail()) {
               this.unbindFromRail();
            } else if (this.tryBindToRail()) {
               this.actionController.getAction(6).startAction();
            }
         } else if (extensionItem instanceof SBUnitProtoTypeItem) {
            if (this.isSuperBoost()) {
               if (this.isServerSide()) {
                  this.setSuperBoost(false);
                  this.actionController.setBoost(true);
               }
            } else if (!this.actionController.getAction(10).isInAction() && !this.isGliding()) {
               this.isHovering = false;
               this.actionController.getAction(10).startAction();
               if (this.isServerSide()) {
                  this.unbindFromRail();
                  this.actionController.setBoost(false);
               }
            }
         } else if (extensionItem instanceof GliderUnitItem glider) {
            if (this.isServerSide()) {
               if (this.isGliding()) {
                  this.setGliding(false);
                  glider.endUsing(this.level(), extensionStack, this);
               } else if (!this.onGround()) {
                  this.setSuperBoost(false);
                  this.isHovering = false;
                  this.unbindFromRail();
                  this.setGliding(true);
                  glider.startUsing(this.level(), extensionStack, this);
               }
            }
         } else if (extensionItem instanceof BuilderUnitItem && this.isServerSide()) {
            this.setBuildMode(!this.isBuildMode());
         } else if (extensionItem instanceof CoreDrillItem && !this.actionController.getAction(99).isInAction()) {
            if (this.getVehicle() != null) {
               this.stopRiding();
               this.setNoGravity(false);
            } else {
               this.actionController.getAction(99).startAction();
            }
         }
      }
   }

   @Override
   protected void fireWeapons() {
   }

   public Vec3 getShootingOffset() {
      return this.posHistory.getFirst();
   }

   public Vec3 getPosHistory(int index) {
      if (this.posHistory.isEmpty()) {
         return this.position();
      }

      if (index >= this.posHistory.size()) {
         index = this.posHistory.size() - 1;
      }

      return this.posHistory.get(index);
   }

   public float[] getShootingAngle(Entity bullet, boolean useTarget, boolean useDeviation) {
      Vec3 targetPos = this.getTargetPos(useDeviation);
      Vec3 bulletPos = bullet.position();
      Vec3 bulletDir = targetPos.subtract(bulletPos).normalize();
      float horizontalDist = (float)Math.sqrt(bulletDir.x * bulletDir.x + bulletDir.z * bulletDir.z);
      float pitch = (float)Math.toDegrees(Math.atan2(bulletDir.y, horizontalDist)) * -1.0F;
      float yaw = (float)Math.toDegrees(Math.atan2(-bulletDir.x, bulletDir.z));
      return new float[]{pitch, yaw};
   }

   protected Vec3 getTargetPos(boolean useDeviation) {
      useDeviation = true;
      Entity lockTarget = this.lockTargets.getLockTargetHard();
      if (lockTarget == null) {
         lockTarget = this.lockTargets.getLockTargetSoft();
      }

      Vec3 targetPos;
      if (lockTarget != null) {
         if (useDeviation) {
            targetPos = lockTarget.getBoundingBox()
               .getCenter()
               .add(lockTarget.getDeltaMovement())
               .add(lockTarget.getDeltaMovement())
               .add(lockTarget.getDeltaMovement())
               .add(lockTarget.getDeltaMovement());
         } else {
            targetPos = lockTarget.getBoundingBox().getCenter();
         }
      } else {
         targetPos = this.getCameraTargetPosition(this.getDrivingPassenger());
         targetPos = new Vec3(targetPos.x, targetPos.y, targetPos.z);
      }

      return targetPos;
   }

   protected Vec3 getCameraTargetPosition(Entity cameraEntity) {
      if (cameraEntity == null) {
         cameraEntity = this;
      } else if (cameraEntity instanceof ServerPlayer sp) {
         cameraEntity = sp.getCamera();
      }

      HitResult hitResult = cameraEntity.pick(200.0, 0.0F, true);
      if (hitResult.getType() == Type.MISS) {
         Vec3 cameraPos = cameraEntity.getEyePosition();
         Vec3 cameraDirection = cameraEntity.getLookAngle().normalize();
         return cameraPos.add(cameraDirection.scale(300.0));
      } else {
         return hitResult.getLocation();
      }
   }

   public BlockHitResult performBlockRaycast(LivingEntity player, double maxDistance) {
      Vec3 start = player.getEyePosition();
      Vec3 lookVec = player.getLookAngle();
      Vec3 end = start.add(lookVec.scale(maxDistance));
      return player.level().clip(new ClipContext(start, end, Block.OUTLINE, Fluid.NONE, player));
   }

   public void addHitParticles(Entity target) {
      Vec3 offset = new Vec3(target.position().x, target.getBoundingBox().getCenter().y, target.position().z);

      for (int i = 0; i < 40; i++) {
         double velocityX = this.random.nextDouble() * 4.3 - 1.0;
         double velocityY = this.random.nextDouble() * 4.3 - 1.0;
         double velocityZ = this.random.nextDouble() * 4.3 - 1.0;
         this.level()
            .addAlwaysVisibleParticle((ParticleOptions)PomkotsMechs.SPARK.get(), true, offset.x(), offset.y(), offset.z(), velocityX, velocityY, velocityZ);
      }
   }

   @Override
   protected float getWalkSpeed() {
      return 0.5F * this.getSpeedModifier();
   }

   @Override
   protected float getRunSpeed() {
      return 1.5F * this.getSpeedModifier();
   }

   @Override
   protected float getFlyingSpeed() {
      return this.actionController.isBoost() ? 0.4F * this.getSpeedModifier() : 0.2F * this.getSpeedModifier();
   }

   @Override
   protected float getJumpSpeed() {
      return 1.5F * this.getJumpModifier();
   }

   @Override
   protected float getHorizontalBoostAcceleration() {
      if (this.noHorizontalBoost()) {
         return 5.7F * this.getSpeedModifier() * 1.0F;
      } else if (this.onGround()) {
         return 5.7F * this.getSpeedModifier() * this.getSpeedModifierEvasion();
      } else {
         return this.isHovering()
            ? 4.75F * this.getSpeedModifier() * this.getSpeedModifierEvasion()
            : 3.325F * this.getSpeedModifier() * this.getSpeedModifierEvasion();
      }
   }

   public boolean noHorizontalBoost() {
      return this.getBooster().isEmpty();
   }

   @Override
   protected float getVerticalBoostAcceleration() {
      return 0.3F * this.getJumpModifier() * this.getSpeedModifierVertical();
   }

   @Override
   protected float getVerticalBoostMaxSpeed() {
      return 0.7F;
   }

   @Override
   public double getPassengersRidingOffset() {
      return 3.75;
   }

   @Override
   protected void applyPlayerInputEvasion(DriverInput driverInput) {
      if (driverInput.isEvasionPressed()
         && !this.actionController.getAction(0).isInAction()
         && !this.actionController.getAction(0).isInCooltime()
         && !this.actionController.getAction(9).isInAction()
         && !this.isGliding()) {
         if (this.noHorizontalBoost()) {
            if (this.onGround()) {
               this.actionController.getAction(0).startAction();
               this.startEvasion();
            }
         } else if (this.useEnergy(this.getEnergyConsumeEvasion()) && this.actionController.getAction(0).startAction()) {
            this.startEvasion();
            this.actionController.setBoost(true);
         }
      }
   }

   @Override
   protected void startEvasion() {
      this.startEvasion(1.0F);
   }

   protected void startEvasion(float modifier) {
      if (this.isServerSide()) {
         Vec3 vel;
         if (this.forwardIntention == 0.0F && this.sidewayIntention == 0.0F) {
            vel = new Vec3(0.0, 0.0, 1.0);
         } else {
            vel = new Vec3(this.sidewayIntention, 0.0, this.forwardIntention).normalize();
         }

         vel = vel.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         float distance = this.getHorizontalBoostAcceleration() * modifier;
         vel = vel.scale(distance);
         this.push(vel.x, vel.y, vel.z);
      }
   }

   @Override
   protected void applyPlayerInputInAirActions(DriverInput driverInput) {
      if (this.actionController.getAction(10).isInAction()) {
         if (this.actionController.getAction(10).isOnStart() && this.isClientSide()) {
            Vec3 muzzlPos = new Vec3(0.0, 3.4, -4.0);
            muzzlPos = this.position().add(muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot())));
            int targetId = this.getId();

            for (int i = 0; i < 20; i++) {
               double px = muzzlPos.x + (this.level().random.nextDouble() - 0.5) * 3.0;
               double py = muzzlPos.y + this.level().random.nextDouble() * 3.0;
               double pz = muzzlPos.z + (this.level().random.nextDouble() - 0.5) * 3.0;
               this.level().addParticle((ParticleOptions)PomkotsMechs.SPIRAL.get(), px, py, pz, targetId, 0.0, 0.0);
            }
         }

         if (this.actionController.getAction(10).isOnFire() && this.isServerSide()) {
            this.setSuperBoost(true);
         }
      } else {
         if (!this.onGround()) {
            if (this.actionController.getAction(9).isCharging()) {
               Vec3 delta = this.getDeltaMovement();
               if (this.isServerSide()) {
                  this.setDeltaMovement(delta.x, 0.0, delta.z);
               }
            } else if (this.actionController.getAction(9).isOnFire()) {
               if (driverInput.isEvasionPressed()) {
                  this.actionController.getAction(0).startAction();
                  this.startEvasion(2.0F);
                  if (this.isServerSide()) {
                     this.push(0.0, 1.5, 0.0);
                  }
               } else if (this.isServerSide()) {
                  this.push(0.0, 2.0, 0.0);
               }

               if (this.isClientSide()) {
                  this.spawnJumpParticles();
               }
            } else if (driverInput.isJumpPressed()
               && !this.actionController.getAction(9).isInCooltime()
               && !this.actionController.getAction(1).isInCooltime()
               && this.isTouchingWallSimple()
               && !this.actionController.isInActionAll()) {
               this.actionController.getAction(9).startAction();
            } else if (driverInput.isJumpPressed() && !this.getBooster().isEmpty()) {
               if (!this.tryVerticalBoost() && this.isServerSide()) {
                  Vec3 delta = this.getDeltaMovement();
                  if (this.isGliding() && delta.y < 0.0) {
                     this.setDeltaMovement(delta.x, -0.2, delta.z);
                  } else {
                     this.push(0.0, -0.17640000343322754, 0.0);
                  }
               }
            } else if (this.isServerSide()) {
               if (this.isHovering()) {
                  this.handleHovering();
               } else if (!this.isBoundToRail()) {
                  Vec3 delta = this.getDeltaMovement();
                  if (this.isGliding() && delta.y < 0.0) {
                     this.setDeltaMovement(delta.x, -0.2, delta.z);
                  } else {
                     this.push(0.0, -0.17640000343322754, 0.0);
                  }
               }
            }
         } else {
            if (this.isHovering()) {
               this.handleHovering();
            }

            if (this.isServerSide()) {
               this.setNoGravity(false);
               if (this.isGliding()) {
                  if (this.getExtension1Weapon().getItem() instanceof GliderUnitItem glider1) {
                     glider1.endUsing(this.level(), this.getExtension1Weapon(), this);
                  } else if (this.getExtension2Weapon().getItem() instanceof GliderUnitItem glider2) {
                     glider2.endUsing(this.level(), this.getExtension2Weapon(), this);
                  }

                  this.setGliding(false);
               }
            }
         }
      }
   }

   public void spawnJumpParticles() {
      Vec3 pos = this.position();
      RandomSource random = this.getRandom();
      double baseY = this.getY() - 0.1;

      for (int i = 0; i < 10; i++) {
         double dx = random.nextDouble() - 0.5;
         double dz = random.nextDouble() - 0.5;
         double vy = random.nextDouble() * 0.2 + 0.1;
         this.level().addAlwaysVisibleParticle((ParticleOptions)PomkotsMechs.SPARK.get(), pos.x + dx, baseY, pos.z + dz, 0.0, vy, 0.0);
      }

      BlockState dirt = Blocks.DIRT.defaultBlockState();

      for (int i = 0; i < 30; i++) {
         double dx = random.nextDouble() - 0.5;
         double dz = random.nextDouble() - 0.5;
         double vy = random.nextDouble() * 0.2 + 0.1;
         this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, dirt), pos.x + dx, baseY, pos.z + dz, 0.0, vy, 0.0);
      }
   }

   private boolean isTouchingWallSimple() {
      Level level = this.level();
      AABB box = this.getBoundingBox().inflate(0.3);
      return !level.noCollision(this, box);
   }

   public Pmvc01Entity.WallSide getTouchingWallSide(double checkDistance) {
      Level level = this.level();
      AABB box = this.getBoundingBox();
      float yawRad = (float)Math.toRadians(this.getYRot());
      double forwardX = -Math.sin(yawRad);
      double forwardZ = Math.cos(yawRad);
      double rightX = Math.cos(yawRad);
      double rightZ = Math.sin(yawRad);
      boolean front = !level.noCollision(this, box.move(forwardX * checkDistance, 0.0, forwardZ * checkDistance));
      boolean back = !level.noCollision(this, box.move(-forwardX * checkDistance, 0.0, -forwardZ * checkDistance));
      boolean right = !level.noCollision(this, box.move(rightX * checkDistance, 0.0, rightZ * checkDistance));
      boolean left = !level.noCollision(this, box.move(-rightX * checkDistance, 0.0, -rightZ * checkDistance));
      if (front) {
         return Pmvc01Entity.WallSide.FRONT;
      } else if (right) {
         return Pmvc01Entity.WallSide.RIGHT;
      } else if (left) {
         return Pmvc01Entity.WallSide.LEFT;
      } else {
         return back ? Pmvc01Entity.WallSide.BACK : Pmvc01Entity.WallSide.NONE;
      }
   }

   @Override
   protected boolean tryVerticalBoost() {
      if (this.isGliding()) {
         return false;
      }

      if (this.useEnergy(this.getEnergyConsumeVertical()) && this.isServerSide() && this.getDeltaMovement().y() < this.getVerticalBoostMaxSpeed()) {
         if (this.isHovering) {
            this.push(0.0, this.getVerticalBoostAcceleration() * 2.0F, 0.0);
         } else {
            this.push(0.0, this.getVerticalBoostAcceleration(), 0.0);
         }

         return true;
      } else {
         return false;
      }
   }

   protected boolean isHovering() {
      return this.isHovering;
   }

   private void handleHovering() {
      float distanceToGround = this.getDistanceToGround();
      if (distanceToGround <= 3.0F) {
         Vec3 velocity = this.getDeltaMovement();
         double upwardForce = (3.0F - distanceToGround) * 0.1;
         if (this.isServerSide()) {
            this.setDeltaMovement(velocity.x, velocity.y + upwardForce, velocity.z);
         }
      } else if (distanceToGround > 8.0F && this.isServerSide()) {
         this.push(0.0, -0.17640000343322754, 0.0);
      }
   }

   private float getDistanceToGround() {
      Level world = this.level();
      double entityY = this.getY();

      for (int i = 0; i < 10; i++) {
         BlockPos checkPos = new BlockPos((int)this.getX(), (int)Math.floor(entityY - i), (int)this.getZ());
         BlockState blockState = world.getBlockState(checkPos);
         if (!blockState.isAir()
            && (blockState.isSolid() || blockState.getFluidState().is(FluidTags.WATER) || blockState.getFluidState().is(FluidTags.LAVA))) {
            double groundY = checkPos.getY() + 1.0;
            return (float)(entityY - groundY);
         }
      }

      return Float.MAX_VALUE;
   }

   @Override
   public int getEnergy() {
      return (int)this.energy;
   }

   public void setEnergy(float energy) {
      this.energy = energy;
   }

   public int getOverHeatCooltime() {
      return this.overHeatCooltime;
   }

   @Override
   protected void chargeEnergy() {
      if (this.overHeatCooltime-- == 0) {
         this.setOverHeat(false);
      }

      if (!this.isGliding()) {
         if (this.energy + this.getEnergyChargePerTick() > this.getMaxEnergy()) {
            this.energy = this.getMaxEnergy();
         } else {
            this.energy = this.energy + this.getEnergyChargePerTick();
         }
      }
   }

   @Override
   protected boolean useEnergy(int dec) {
      if (this.isOverHeat()) {
         return false;
      } else if (this.energy - dec < 0.0F) {
         this.energy = 0.0F;
         this.setOverHeat(true);
         this.overHeatCooltime = this.OVER_HEAT_COOL_TIME_MAX;
         return true;
      } else {
         this.energy -= dec;
         return true;
      }
   }

   public boolean consumeEnergy(int dec) {
      return this.useEnergy(dec);
   }

   @Override
   public int getMaxEnergy() {
      return (Integer)this.entityData.get(MAX_ENERGY);
   }

   protected boolean isDriverCreativeMode() {
      return this.getDrivingPassenger() instanceof Player p && p.getAbilities().instabuild;
   }

   protected boolean consumeFuel() {
      if (this.isServerSide()) {
         if (this.isDriverCreativeMode()) {
            return true;
         }

         if (this.fuel > 0) {
            this.fuel--;
            return true;
         }

         ItemStack fuels = this.getFuelFromInventory();
         if (fuels.getCount() > 0) {
            if (this.getGeneratorFromInventory().getItem() instanceof BasePartsItem.Generator gen) {
               this.fuel = (int)this.getWorkSecPerFuel() * 20 * 2;
               fuels.shrink(1);
               return true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   private void syncFuels() {
      int maxFuel = 0;
      int nowFuel = 0;
      if (this.getGeneratorFromInventory().getItem() instanceof BasePartsItem.Generator gen) {
         int tpf = (int)this.getWorkSecPerFuel() * 20 * 2;
         maxFuel = tpf * this.getFuelFromInventory().getMaxStackSize();
         nowFuel = tpf * this.getFuelFromInventory().getCount() + this.fuel;
      }

      this.entityData.set(FUEL_MAX, maxFuel);
      this.entityData.set(FUEL_NOW, nowFuel);
   }

   @Override
   public boolean shouldLockMulti(DriverInput driverInput) {
      if (this.isBuildMode()) {
         return false;
      }

      if (this.isGattai() && this.getVehicle() instanceof Pmb99Entity pmb99) {
         return pmb99.shouldLockMulti(driverInput);
      }

      boolean res = false;
      if (driverInput.isWeaponRightHandPressed()) {
         res |= isMultiLockEnabledWeapon(this.getRightArmWeapon());
      }

      if (driverInput.isWeaponLeftHandPressed()) {
         res |= isMultiLockEnabledWeapon(this.getLeftArmWeapon());
      }

      if (driverInput.isWeaponRightShoulderPressed()) {
         res |= isMultiLockEnabledWeapon(this.getRightShoulderWeapon());
      }

      if (driverInput.isWeaponLeftShoulderPressed()) {
         res |= isMultiLockEnabledWeapon(this.getLeftShoulderWeapon());
      }

      return res;
   }

   public static boolean isMultiLockEnabledWeapon(ItemStack stack) {
      if (stack.isEmpty()) {
         return false;
      } else {
         return stack.getItem() instanceof BasePartsItem.Weapon w ? w.maxMultiLockNum() > 0 : false;
      }
   }

   public static int getMultiLockTargetNum(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      } else {
         return stack.getItem() instanceof BasePartsItem.Weapon w ? w.maxMultiLockNum() : 0;
      }
   }

   @Override
   public boolean shouldLockWeak(DriverInput driverInput) {
      if (this.isBuildMode()) {
         return false;
      }

      boolean hasWeaponInput = driverInput.isWeaponRightHandPressed() || driverInput.isWeaponLeftHandPressed();
      return hasWeaponInput && this.hasLockSoftCircuit();
   }

   private boolean hasSoftLockCircuit() {
      Item i1 = this.getExtension1Weapon().getItem();
      Item i2 = this.getExtension2Weapon().getItem();
      return i1 instanceof CircuitSoftLockItem || i2 instanceof CircuitSoftLockItem;
   }

   @Override
   public boolean shouldLockStrong(DriverInput driverInput) {
      return driverInput.isLockPressed() && this.hasLockHardCircuit();
   }

   private boolean hasHardLockCircuit() {
      Item i1 = this.getExtension1Weapon().getItem();
      Item i2 = this.getExtension2Weapon().getItem();
      return i1 instanceof CircuitHardLockItem || i2 instanceof CircuitHardLockItem;
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
                  this,
                  "controller",
                  0,
                  event -> {
                     if (this.actionController.getAction(99).isInAction()) {
                        if (this.actionController.getAction(99).isOnStart()) {
                           event.getController().forceAnimationReset();
                        }

                        return event.setAndContinue(
                           RawAnimation.begin()
                              .thenPlay("animation." + this.getMechName() + ".z_gattai_01")
                              .thenLoop("animation." + this.getMechName() + ".z_gattai_02")
                        );
                     } else {
                        if (this.getVehicle() instanceof Pmb99Entity) {
                           event.getController().forceAnimationReset();
                           return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".z_gattai_03"));
                        }

                        if (this.actionController.getAction(7).isInAction()) {
                           if (this.actionController.getAction(7).isOnStart()) {
                              event.getController().forceAnimationReset();
                           }

                           return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".deactivate"));
                        } else if (this.actionController.getAction(8).isInAction()) {
                           if (this.actionController.getAction(8).isOnStart()) {
                              event.getController().forceAnimationReset();
                           }

                           return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".inactivate"));
                        } else {
                           if (this.isBroken() && this.prevBroken) {
                              return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".idle_deactive"));
                           }

                           if (this.isLocked() && this.prevLocked) {
                              return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".idle_deactive"));
                           }

                           if (!this.canWork(false)) {
                              return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".idle_deactive"));
                           }

                           if (this.getDrivingPassenger() == null) {
                              return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".idle"));
                           }

                           PlayState res = this.controllAnimationWeapons(event);
                           return res != null ? res : this.controllAnimationBasicMove(event);
                        }
                     }
                  }
               )
               .setSoundKeyframeHandler(soundKeyframeEvent -> this.registerAnimationSoundHandlers(soundKeyframeEvent))
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "fly", 1, event -> this.controllAnimationFlyingMotion(event))
               .setSoundKeyframeHandler(soundKeyframeEvent -> this.registerAnimationSoundHandlers(soundKeyframeEvent))
         }
      );
      controllers.add(new AnimationController[]{new AnimationController(this, "rotation", 1, event -> this.controllAnimationRotation(event))});
      controllers.add(
         new AnimationController[]{
            new AnimationController(
                  this,
                  "boosters",
                  1,
                  event -> {
                     if (this.isGattai()) {
                        return PlayState.STOP;
                     }

                     DriverInput driverInput = this.getDriverInput();
                     if (this.isSuperBoost()) {
                        this.playSoundEffectWithFade("boost", (SoundEvent)PomkotsMechs.SE_BOOST.get(), 0.8F, 20, 10);
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".booster_super"));
                     }

                     if (this.actionController.getAction(0).isInAction()) {
                        if (this.actionController.getAction(0).isOnStart()) {
                           event.getController().forceAnimationReset();
                           if (this.sidewayIntention > 0.0F) {
                              return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".booster_dash_left"));
                           } else {
                              return this.sidewayIntention < 0.0F
                                 ? event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".booster_dash_right"))
                                 : PlayState.CONTINUE;
                           }
                        } else {
                           return PlayState.CONTINUE;
                        }
                     } else {
                        if (driverInput != null && driverInput.isJumpPressed() && this.isInAirInternal() && !this.getBooster().isEmpty()) {
                           this.playSoundEffectWithFade("boost", (SoundEvent)PomkotsMechs.SE_BOOST.get(), 0.6F, 40, 10);
                           return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".booster_vertical"));
                        }

                        if (!this.isBoost()) {
                           this.stopAllSoundEffectWithFade();
                           return PlayState.STOP;
                        }

                        if (!this.onGround() && this.inAirTicks >= 10) {
                           this.stopSoundEffectWithFade("dash");
                        } else {
                           this.playSoundEffectWithFade("dash", (SoundEvent)PomkotsMechs.SE_DASH.get(), 0.6F, 20, 10);
                        }

                        this.stopSoundEffectWithFade("boost");
                        if (this.forwardIntention > 0.0F) {
                           return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".booster_dash_front"));
                        } else if (this.sidewayIntention > 0.0F) {
                           return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".booster_dash_left"));
                        } else {
                           return this.sidewayIntention < 0.0F
                              ? event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".booster_dash_right"))
                              : PlayState.CONTINUE;
                        }
                     }
                  }
               )
               .setSoundKeyframeHandler(soundKeyframeEvent -> this.registerAnimationSoundHandlers(soundKeyframeEvent))
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               "boosters_quick",
               0,
               event -> {
                  if (this.isGattai()) {
                     return PlayState.STOP;
                  }

                  if (this.actionController.getAction(0).isInAction()) {
                     if (this.actionController.getAction(0).isOnStart()) {
                        if (this.getBooster() != null && !this.getBooster().isEmpty()) {
                           event.getController().forceAnimationReset();
                           if (this.forwardIntention > 0.0F) {
                              return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".booster_quick"));
                           } else if (this.sidewayIntention > 0.0F) {
                              return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".booster_quick_left"));
                           } else {
                              return this.sidewayIntention < 0.0F
                                 ? event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".booster_quick_right"))
                                 : PlayState.CONTINUE;
                           }
                        } else {
                           return PlayState.STOP;
                        }
                     } else {
                        return PlayState.CONTINUE;
                     }
                  } else {
                     return PlayState.STOP;
                  }
               }
            )
         }
      );
      this.addExtraAnimationController(controllers);
   }

   public boolean isGattai() {
      return this.getVehicle() instanceof Pmb99Entity;
   }

   @Override
   protected PlayState controllAnimationBasicMove(AnimationState<PomkotsVehicleBase> event) {
      if (this.actionController.getAction(10).isInAction()) {
         if (this.actionController.getAction(10).isOnStart()) {
            event.getController().forceAnimationReset();
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".super_boost"));
         } else {
            return PlayState.CONTINUE;
         }
      } else if (this.actionController.getAction(9).isInAction()) {
         if (this.actionController.getAction(9).isOnStart()) {
            event.getController().forceAnimationReset();
         }

         return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".jump"));
      } else if (this.actionController.getAction(1).isInAction()) {
         if (this.actionController.getAction(1).isOnStart()) {
            event.getController().forceAnimationReset();
         }

         return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".jump"));
      } else if (this.actionController.getAction(6).isInAction()) {
         if (this.actionController.getAction(6).isOnStart()) {
            event.getController().forceAnimationReset();
         }

         return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".bindrail"));
      } else if (event.isMoving()) {
         if (this.isBoundToRail() && this.getDeltaMovement().length() > 2.0) {
            Vec3 legPos1 = new Vec3(-1.0, 0.0, -1.0).yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
            Vec3 legPos2 = new Vec3(1.0, 0.0, -1.0).yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
            ParticleUtil.addSparkParticlesSmall(legPos1, this.level());
            ParticleUtil.addSparkParticlesSmall(legPos2, this.level());
         }

         if (this.actionController.getAction(0).isInAction()) {
            if (this.noHorizontalBoost()) {
               if (this.sidewayIntention > 0.0F) {
                  return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".evasion_left"));
               } else if (this.sidewayIntention < 0.0F) {
                  return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".evasion_right"));
               } else {
                  return this.forwardIntention < 0.0F
                     ? event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".evasion_back"))
                     : event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".evasion_front"));
               }
            } else {
               return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".evasion"));
            }
         } else if (this.actionController.isBoost() || this.isSuperBoost()) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".dash"));
         } else if (this.isBoundToRail()) {
            return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".dash"));
         } else {
            return this.isInAirInternal()
               ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".idle"))
               : event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".walk"));
         }
      } else {
         return this.isBoundToRail()
            ? event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".dash"))
            : event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".idle"));
      }
   }

   @Override
   protected PlayState controllAnimationFlyingMotion(AnimationState<PomkotsVehicleBase> event) {
      QueuedAnimation anim = event.getController().getCurrentAnimation();
      if (anim != null
         && anim.animation() != null
         && ("animation." + this.getMechName() + ".onground").equals(anim.animation().name())
         && !event.getController().hasAnimationFinished()) {
         return PlayState.CONTINUE;
      } else if (this.justLanded((Entity)event.getAnimatable())) {
         event.getController().forceAnimationReset();
         return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".onground"));
      } else if (((PomkotsVehicleBase)event.getAnimatable()).onGround() || this.isBoundToRail() || this.actionController.getAction(9).isInAction()) {
         return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"));
      } else {
         return event.isMoving() && !((PomkotsVehicleBase)event.getAnimatable()).isBoost() && this.inAirTicks < 5 && this.getDeltaMovement().y <= 0.0
            ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"))
            : event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".flylegs"));
      }
   }

   @Override
   protected boolean justLanded(Entity ent) {
      return this.onGround() && !this.onGroundPrev && this.inAirTicks > 5;
   }

   private boolean isInAirInternal() {
      return this.isNoGravity() || !this.onGround() && this.inAirTicks > 5;
   }

   @Override
   protected PlayState controllAnimationRotation(AnimationState<PomkotsVehicleBase> event) {
      if (event.isMoving()) {
         if (this.isInAirInternal() && !this.actionController.isBoost() && !this.actionController.getAction(0).isInAction()) {
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
         } else if (this.actionController.getAction(0).isInAction() && this.noHorizontalBoost()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"));
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

   @Override
   protected PlayState controllAnimationWeapons(AnimationState<PomkotsVehicleBase> event) {
      return null;
   }

   @Override
   protected void addExtraAnimationController(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
                  this,
                  "right_hand",
                  0,
                  event -> {
                     if (this.isBuildMode()) {
                        Action act = this.actionController.getAction(11);
                        if (act.isInAction()) {
                           if (act.isOnStart()) {
                              event.getController().forceAnimationReset();
                           }

                           return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".w_build_place"));
                        } else {
                           return PlayState.STOP;
                        }
                     } else {
                        Action relAct = this.actionController.getAction(12);
                        if (relAct.isInAction()) {
                           if (relAct.isOnStart()) {
                              event.getController().forceAnimationReset();
                           }

                           return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".w_reload_right_hand"));
                        } else {
                           ActionWeapon act = (ActionWeapon)this.actionController.getAction(2);
                           if (act.isInAction()) {
                              if (act.isOnStart()) {
                                 event.getController().forceAnimationReset();
                              }

                              return event.setAndContinue(RawAnimation.begin().thenPlay(act.getMotion().getAnimationName(act, "right")));
                           } else {
                              return act.getMotion().getType() == Motion.MotionType.TOGGLE && act.isToggleOnStart()
                                 ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold(act.getMotion().getAnimationName(act, "right")))
                                 : event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"));
                           }
                        }
                     }
                  }
               )
               .setSoundKeyframeHandler(soundKeyframeEvent -> this.registerAnimationSoundHandlers(soundKeyframeEvent))
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(
                  this,
                  "left_hand",
                  0,
                  event -> {
                     Action relAct = this.actionController.getAction(13);
                     if (relAct.isInAction()) {
                        if (relAct.isOnStart()) {
                           event.getController().forceAnimationReset();
                        }

                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".w_reload_left_hand"));
                     } else {
                        ActionWeapon act = (ActionWeapon)this.actionController.getAction(3);
                        if (act.isInAction()) {
                           if (act.isOnStart()) {
                              event.getController().forceAnimationReset();
                           }

                           return event.setAndContinue(RawAnimation.begin().thenPlay(act.getMotion().getAnimationName(act, "left")));
                        } else {
                           return act.getMotion().getType() == Motion.MotionType.TOGGLE && act.isToggleOn()
                              ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold(act.getMotion().getAnimationName(act, "left")))
                              : event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"));
                        }
                     }
                  }
               )
               .setSoundKeyframeHandler(soundKeyframeEvent -> this.registerAnimationSoundHandlers(soundKeyframeEvent))
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(
                  this,
                  "right_shoulder",
                  1,
                  event -> {
                     Action relAct = this.actionController.getAction(14);
                     if (relAct.isInAction()) {
                        if (relAct.isOnStart()) {
                           event.getController().forceAnimationReset();
                        }

                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".w_reload_right_shoulder"));
                     } else {
                        ActionWeapon act = (ActionWeapon)this.actionController.getAction(4);
                        if (act.isInAction()) {
                           if (act.isOnStart()) {
                              event.getController().forceAnimationReset();
                           }

                           return event.setAndContinue(RawAnimation.begin().thenPlay(act.getMotion().getAnimationName(act, "right")));
                        } else {
                           return act.getMotion().getType() == Motion.MotionType.TOGGLE && act.isToggleOnStart()
                              ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold(act.getMotion().getAnimationName(act, "right")))
                              : event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"));
                        }
                     }
                  }
               )
               .setSoundKeyframeHandler(soundKeyframeEvent -> this.registerAnimationSoundHandlers(soundKeyframeEvent))
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(
                  this,
                  "left_shoulder",
                  1,
                  event -> {
                     Action relAct = this.actionController.getAction(15);
                     if (relAct.isInAction()) {
                        if (relAct.isOnStart()) {
                           event.getController().forceAnimationReset();
                        }

                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".w_reload_left_shoulder"));
                     } else {
                        ActionWeapon act = (ActionWeapon)this.actionController.getAction(5);
                        if (act.isInAction()) {
                           if (act.isOnStart()) {
                              event.getController().forceAnimationReset();
                           }

                           return event.setAndContinue(RawAnimation.begin().thenPlay(act.getMotion().getAnimationName(act, "left")));
                        } else {
                           return act.getMotion().getType() == Motion.MotionType.TOGGLE && act.isToggleOnStart()
                              ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold(act.getMotion().getAnimationName(act, "left")))
                              : event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"));
                        }
                     }
                  }
               )
               .setSoundKeyframeHandler(soundKeyframeEvent -> this.registerAnimationSoundHandlers(soundKeyframeEvent))
         }
      );
   }

   public void spawnReloadParticles(Vec3 spawnLocalPos, boolean isRightArm) {
      Vec3 lookVec = this.getLookAngle();
      Vec3 origin = spawnLocalPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
      origin = this.position().add(origin);
      Vec3 right = lookVec.cross(new Vec3(0.0, 1.0, 0.0)).normalize().scale(1.5);
      if (!isRightArm) {
         right = right.scale(-1.0);
      }

      RandomSource rand = this.getRandom();

      for (int i = 0; i < 6; i++) {
         double vx = (rand.nextDouble() - 0.5) * 0.05;
         double vy = 0.1 + rand.nextDouble() * 0.1;
         double vz = (rand.nextDouble() - 0.5) * 0.05;
         this.level().addAlwaysVisibleParticle(ParticleTypes.SMOKE, true, origin.x(), origin.y(), origin.z(), vx, vy, vz);
      }

      for (int i = 0; i < 4; i++) {
         Vec3 dir = right.scale(0.3 + rand.nextDouble() * 0.2);
         this.level()
            .addAlwaysVisibleParticle(
               (ParticleOptions)PomkotsMechs.SPARK.get(),
               true,
               origin.x(),
               origin.y(),
               origin.z(),
               dir.x,
               0.1 + rand.nextDouble() * 0.1,
               dir.z
            );
      }

      for (int i = 0; i < 1; i++) {
         Vec3 dir = right.scale(0.3 + rand.nextDouble() * 0.2);
         SimpleParticleType partType = (SimpleParticleType)PomkotsMechs.MAGAZINE_LEFT.get();
         if (isRightArm) {
            partType = (SimpleParticleType)PomkotsMechs.MAGAZINE_RIGHT.get();
         }

         this.level()
            .addAlwaysVisibleParticle(partType, true, origin.x(), origin.y(), origin.z(), dir.x * 3.0, 0.1 + rand.nextDouble() * 0.2, dir.z * 3.0);
      }
   }

   @Override
   protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
      String sound = event.getKeyframeData().getSound();
      if ("se_jump".equals(sound)) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_JUMP_EVENT.get());
      } else if ("se_booster".equals(sound)) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BOOSTER_EVENT.get());
      } else if ("se_onground".equals(sound)) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_JUMP_EVENT.get());
      } else if ("se_gashan".equals(sound)) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GASHAN.get());
         Vec3 legPos1 = new Vec3(-1.0, 0.0, -1.0).yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
         Vec3 legPos2 = new Vec3(1.0, 0.0, -1.0).yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
         ParticleUtil.addSparkParticles(legPos1, this.level());
         ParticleUtil.addSparkParticles(legPos2, this.level());
      } else if ("se_step".equals(sound)) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_STEP.get(), 0.1F);
         this.spawnMovingParticles(5, 0.8F, false);
      } else if ("se_machine".equals(sound)) {
         if (!this.isBroken()) {
            this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MACHINE.get());
         }
      } else if ("se_charge".equals(sound)) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BOOST_CHARGE.get(), 1.0F);
      } else if ("se_roller1".equals(sound)) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_ROLLER1.get(), 1.0F);
      } else if ("se_roller2".equals(sound)) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_ROLLER2.get(), 1.0F);
      } else if ("se_lift".equals(sound)) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_LIFT.get(), 1.0F);
      } else if ("se_reload".equals(sound)) {
         if ("right_hand".equals(event.getController().getName())) {
            this.spawnReloadParticles(new Vec3(-2.0, 4.0, 1.5), true);
         } else if ("left_hand".equals(event.getController().getName())) {
            this.spawnReloadParticles(new Vec3(2.0, 4.0, 1.5), false);
         } else if ("right_shoulder".equals(event.getController().getName())) {
            this.spawnReloadParticles(new Vec3(-1.5, 5.0, -2.0), true);
         } else if ("left_shoulder".equals(event.getController().getName())) {
            this.spawnReloadParticles(new Vec3(1.5, 5.0, -2.0), false);
         }

         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_RELOAD.get(), 1.0F);
      }
   }

   protected ItemStack getHeadPartsFromInventory() {
      return this.getItem(0);
   }

   protected ItemStack getBodyPartsFromInventory() {
      return this.getItem(1);
   }

   protected ItemStack getArmPartsFromInventory() {
      return this.getItem(2);
   }

   protected ItemStack getLegsPartsFromInventory() {
      return this.getItem(3);
   }

   protected ItemStack getRightArmWeaponFromInventory() {
      return this.getItem(6);
   }

   protected ItemStack getLeftArmWeaponFromInventory() {
      return this.getItem(7);
   }

   protected ItemStack getRightShoulderWeaponFromInventory() {
      return this.getItem(8);
   }

   protected ItemStack getLeftShoulderWeaponFromInventory() {
      return this.getItem(9);
   }

   protected ItemStack getGeneratorFromInventory() {
      return this.getItem(4);
   }

   protected ItemStack getBoosterFromInventory() {
      return this.getItem(5);
   }

   protected ItemStack getExtension1FromInventory() {
      return this.getItem(10);
   }

   protected ItemStack getExtension2FromInventory() {
      return this.getItem(11);
   }

   protected ItemStack getAmmoRAFromInventory() {
      return this.getItem(12);
   }

   protected ItemStack getAmmoLAFromInventory() {
      return this.getItem(13);
   }

   protected ItemStack getAmmoRSFromInventory() {
      return this.getItem(14);
   }

   protected ItemStack getAmmoLSFromInventory() {
      return this.getItem(15);
   }

   protected ItemStack getFuelFromInventory() {
      return this.getItem(16);
   }

   public boolean isShouldSave() {
      return this.shouldSave;
   }

   public void setShouldSave(boolean shouldSave) {
      this.shouldSave = shouldSave;
   }

   public boolean shouldBeSaved() {
      return this.shouldSave;
   }

   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      this.addChestVehicleSaveData(compound);
      this.addAmmoSaveData(compound);
      compound.putInt(PomkotsMechs.nbtName("TextureColor"), this.getTextureColor());
      compound.putInt(PomkotsMechs.nbtName("FuelNow"), this.fuel);
      compound.putBoolean(PomkotsMechs.nbtName("IsBroken"), this.isBroken());
      compound.putBoolean(PomkotsMechs.nbtName("IsLocked"), this.isLocked());
   }

   private void addChestVehicleSaveData(CompoundTag compoundTag) {
      if (this.getLootTable() != null) {
         compoundTag.putString("LootTable", this.getLootTable().toString());
         if (this.getLootTableSeed() != 0L) {
            compoundTag.putLong("LootTableSeed", this.getLootTableSeed());
         }
      } else {
         ContainerHelper.saveAllItems(compoundTag, this.getItemStacks());
      }
   }

   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.readChestVehicleSaveData(compound);
      this.setTextureColor(compound.getInt(PomkotsMechs.nbtName("TextureColor")));
      this.syncAllParameter2Client(true);
      this.readAmmoSaveData(compound);
      this.fuel = compound.getInt(PomkotsMechs.nbtName("FuelNow"));
      this.setBroken(compound.getBoolean(PomkotsMechs.nbtName("IsBroken")));
      this.setLocked(compound.getBoolean(PomkotsMechs.nbtName("IsLocked")));
   }

   void readChestVehicleSaveData(CompoundTag compoundTag) {
      this.clearItemStacks();
      if (compoundTag.contains("LootTable", 8)) {
         this.setLootTable(new ResourceLocation(compoundTag.getString("LootTable")));
         this.setLootTableSeed(compoundTag.getLong("LootTableSeed"));
      } else {
         ContainerHelper.loadAllItems(compoundTag, this.getItemStacks());
      }
   }

   public void setLevelCallback(EntityInLevelCallback entityInLevelCallback) {
      Pmvc01Entity.EntityInLevelCallback2 hoge = new Pmvc01Entity.EntityInLevelCallback2(entityInLevelCallback, this);
      super.setLevelCallback(hoge);
   }

   public void remove(RemovalReason removalReason) {
      if (!this.level().isClientSide && removalReason.shouldDestroy() && removalReason != RemovalReason.DISCARDED) {
         Containers.dropContents(this.level(), this, this);
      }

      if (this.blockPreviewEntity != null) {
         this.blockPreviewEntity.discard();
      }

      if (this.isServerSide() && this.havingEntity().isPresent()) {
         Entity tgt = ((ServerLevel)this.level()).getEntity(this.havingEntity().get());
         if (tgt != null && tgt.isAlive()) {
            tgt.setNoGravity(false);
         }

         this.setHavingEntity(Optional.empty());
      }

      super.remove(removalReason);
   }

   public boolean isLocked(Player player) {
      if (this.isLocked()) {
         if (this.isServerSide()) {
            player.sendSystemMessage(Utils.string2Component("{text.pomkotsmechs.messages.pmvc01.locked}"));
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public InteractionResult interact(Player player, InteractionHand interactionHand) {
      ItemStack stack = player.getItemInHand(interactionHand);
      Level level = this.level();
      if ((!stack.is((Item)PomkotsMechs.REPAIRKIT_ITEM.get()) || !this.canRepair(this))
         && !stack.is((Item)PomkotsMechs.KEYCARD_ITEM.get())
         && !stack.is((Item)PomkotsMechs.MECH_CAPSULE2_ITEM.get())) {
         if (!this.canAddPassenger(player) || player.isSecondaryUseActive()) {
            return InteractionResult.FAIL;
         }

         if (this.isLocked(player)) {
            return InteractionResult.CONSUME;
         }

         if (this.isBroken()) {
            if (this.isServerSide()) {
               player.sendSystemMessage(Utils.string2Component("{text.pomkotsmechs.messages.pmvc01.broken}"));
            }

            return InteractionResult.CONSUME;
         } else {
            player.setSprinting(false);
            InteractionResult res = super.interact(player, interactionHand);
            this.tickAllParts();
            if (this.isServerSide() && InteractionResult.SUCCESS.equals(res) && this.canWork(true)) {
               Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/ride_the_mech"), player);
            }

            return res;
         }
      } else {
         return InteractionResult.PASS;
      }
   }

   public void healWithEffect(ServerLevel level, float amount) {
      this.heal(amount);
      this.healEffect(level);
   }

   public void healEffect(ServerLevel level) {
      for (int i = 0; i < 5; i++) {
         double dx = this.getX() + (level.random.nextDouble() - 0.5) * this.getBbWidth();
         double dy = this.getY() + level.random.nextDouble() * this.getBbHeight();
         double dz = this.getZ() + (level.random.nextDouble() - 0.5) * this.getBbWidth();
         level.sendParticles(ParticleTypes.HAPPY_VILLAGER, dx, dy, dz, 1, 0.0, 0.1, 0.0, 0.1);
      }
   }

   public void repair() {
      ItemStack repairKitItemStack = this.getItem(17);
      if (this.level() instanceof ServerLevel serverLevel
         && this.canRepair(this)
         && repairKitItemStack.getItem() instanceof RepairKitItem
         && repairKitItemStack.getCount() > 0) {
         float healed = Math.min(this.getMaxHealth(), this.getHealth() + 100.0F);
         this.setHealth(healed);
         repairKitItemStack.shrink(1);
         this.entityData.set(REPAIR_KIT_NUM, this.getItem(17).getCount());
      }

      if (this.isClientSide() && this.canRepair(this) && this.getRepairKitNum() > 0) {
         this.healEffectClient(this.level(), this);
      }
   }

   protected boolean canRepair(LivingEntity target) {
      return target.getMaxHealth() > target.getHealth() && target instanceof Pmvc01Entity;
   }

   public void healEffectClient(Level level, LivingEntity target) {
      for (int i = 0; i < 30; i++) {
         double dx = target.getX() + (level.random.nextDouble() - 0.5) * target.getBbWidth();
         double dy = target.getY() + level.random.nextDouble() * target.getBbHeight();
         double dz = target.getZ() + (level.random.nextDouble() - 0.5) * target.getBbWidth();
         level.addAlwaysVisibleParticle(ParticleTypes.HAPPY_VILLAGER, true, dx, dy, dz, 0.0, 0.1, 0.0);
      }

      this.playSoundPublic(SoundEvents.PLAYER_LEVELUP);
   }

   public void warpEffect(ServerLevel level) {
      for (int i = 0; i < 20; i++) {
         double dx = this.getX() + (level.random.nextDouble() - 0.5) * this.getBbWidth();
         double dy = this.getY() + level.random.nextDouble() * this.getBbHeight();
         double dz = this.getZ() + (level.random.nextDouble() - 0.5) * this.getBbWidth();
         level.sendParticles(ParticleTypes.END_ROD, dx, dy, dz, 1, 0.0, 0.1, 0.0, 0.1);
         level.sendParticles(ParticleTypes.PORTAL, dx, dy, dz, 1, 0.0, 0.1, 0.0, 0.1);
      }

      level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.2F);
   }

   @Override
   public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
      if (passenger instanceof Player) {
         this.resetExtensionUnitStatus();
      }

      return super.getDismountLocationForPassenger(passenger);
   }

   private InteractionResult interactWithContainerVehicle(Player player) {
      player.openMenu(this);
      return !player.level().isClientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
   }

   public void openCustomInventoryScreen(Player player) {
      player.openMenu(this);
   }

   public void setConsoleAccessor(MechWorkbenchBlockEntity ac) {
      this.consoleAccessor = ac;
   }

   @Nullable
   public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
      if (this.lootTable != null && player.isSpectator()) {
         return null;
      }

      if (this.isLocked(player)) {
         return null;
      }

      this.unpackLootTable(inventory.player);
      AbstractContainerMenu menu = null;
      ItemStack dataPad = this.getDatapadOfDriver(player);
      if (dataPad != null) {
         menu = new DataPadMenu(i, inventory, player, dataPad);
      } else {
         short mode = MechWorkbenchMenu.MODE_VIEW;
         if (this.getPassengers().isEmpty()) {
            mode = MechWorkbenchMenu.MODE_ASSEMBLE;
         }

         MechWorkbenchMenu mechMenu = new MechWorkbenchMenu(i, inventory, this, this, mode, this.consoleAccessor);
         mechMenu.setEntityId(this.getUUID().hashCode());
         mechMenu.setTextureColor(this.getTextureColor());
         mechMenu.setMode(mode);
         mechMenu.sendAllDataToRemote();
         menu = mechMenu;
      }

      this.consoleAccessor = null;
      return menu;
   }

   private ItemStack getDatapadOfDriver(Player player) {
      if (player instanceof ServerPlayer serverPlayer && player.equals(this.getDrivingPassenger())) {
         for (ItemStack stack : serverPlayer.getInventory().items) {
            if (stack.getItem() instanceof PomkotsDatapadItem) {
               return stack;
            }
         }
      }

      return null;
   }

   public void setChanged() {
      this.syncAllParameter2Client();
      this.initializeAmmoManager(true);
   }

   public boolean stillValid(Player player) {
      return this.isChestVehicleStillValid(player);
   }

   boolean isChestVehicleStillValid(Player player) {
      return !this.isRemoved() && this.position().closerThan(player.position(), 8.0);
   }

   public void clearContent() {
      this.clearChestVehicleContent();
   }

   void clearChestVehicleContent() {
      this.unpackChestVehicleLootTable((Player)null);
      this.getItemStacks().clear();
   }

   void unpackChestVehicleLootTable(@Nullable Player player) {
      MinecraftServer minecraftServer = this.level().getServer();
      if (this.getLootTable() != null && minecraftServer != null) {
         LootTable lootTable = minecraftServer.getLootData().getLootTable(this.getLootTable());
         if (player != null) {
            CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, this.getLootTable());
         }

         this.setLootTable((ResourceLocation)null);
         net.minecraft.world.level.storage.loot.LootParams.Builder builder = new net.minecraft.world.level.storage.loot.LootParams.Builder(
               (ServerLevel)this.level()
            )
            .withParameter(LootContextParams.ORIGIN, this.position());
         if (player != null) {
            builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
         }

         lootTable.fill(this, builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());
      }
   }

   public int getContainerSize() {
      return 82;
   }

   public boolean isEmpty() {
      return this.isChestVehicleEmpty();
   }

   boolean isChestVehicleEmpty() {
      for (ItemStack itemStack : this.getItemStacks()) {
         if (!itemStack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int i) {
      return this.getChestVehicleItem(i);
   }

   ItemStack getChestVehicleItem(int i) {
      this.unpackChestVehicleLootTable((Player)null);
      return (ItemStack)this.getItemStacks().get(i);
   }

   public ItemStack removeItem(int i, int j) {
      return this.removeChestVehicleItem(i, j);
   }

   ItemStack removeChestVehicleItem(int i, int j) {
      this.unpackChestVehicleLootTable((Player)null);
      return ContainerHelper.removeItem(this.getItemStacks(), i, j);
   }

   public ItemStack removeItemNoUpdate(int i) {
      return this.removeChestVehicleItemNoUpdate(i);
   }

   ItemStack removeChestVehicleItemNoUpdate(int i) {
      this.unpackChestVehicleLootTable((Player)null);
      ItemStack itemStack = (ItemStack)this.getItemStacks().get(i);
      if (itemStack.isEmpty()) {
         return ItemStack.EMPTY;
      }

      this.getItemStacks().set(i, ItemStack.EMPTY);
      return itemStack;
   }

   public void setItem(int i, ItemStack itemStack) {
      this.setChestVehicleItem(i, itemStack);
   }

   public SlotAccess getSlot(int i) {
      return this.getChestVehicleSlot(i);
   }

   SlotAccess getChestVehicleSlot(int i) {
      return i >= 0 && i < this.getContainerSize() ? new SlotAccess() {
         public ItemStack get() {
            return Pmvc01Entity.this.getChestVehicleItem(i);
         }

         public boolean set(ItemStack itemStack) {
            Pmvc01Entity.this.setChestVehicleItem(i, itemStack);
            return true;
         }
      } : SlotAccess.NULL;
   }

   void setChestVehicleItem(int i, ItemStack itemStack) {
      this.unpackChestVehicleLootTable((Player)null);
      this.getItemStacks().set(i, itemStack);
      if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
         itemStack.setCount(this.getMaxStackSize());
      }
   }

   public void unpackLootTable(Player player) {
      this.unpackChestVehicleLootTable(player);
   }

   public ResourceLocation getLootTable() {
      return this.lootTable;
   }

   public long getLootTableSeed() {
      return this.lootTableSeed;
   }

   public void setLootTable(ResourceLocation resourceLocation) {
      this.lootTable = resourceLocation;
   }

   public void setLootTableSeed(long l) {
      this.lootTableSeed = l;
   }

   public NonNullList<ItemStack> getItemStacks() {
      return this.itemStacks;
   }

   public void clearItemStacks() {
      this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
   }

   public void stopOpen(Player player) {
      this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), Context.of(player));
   }

   protected void tickAmmos() {
      for (Pmvc01Entity.AmmoManager ammoData : this.ammoManagers) {
         ammoData.tick();
      }
   }

   protected void addAmmoSaveData(CompoundTag compound) {
      compound.putInt(PomkotsMechs.nbtName("BulletNum1"), this.ammoManagers.get(0).bulletNum);
      compound.putInt(PomkotsMechs.nbtName("BulletNum2"), this.ammoManagers.get(1).bulletNum);
      compound.putInt(PomkotsMechs.nbtName("BulletNum3"), this.ammoManagers.get(2).bulletNum);
      compound.putInt(PomkotsMechs.nbtName("BulletNum4"), this.ammoManagers.get(3).bulletNum);
   }

   protected void readAmmoSaveData(CompoundTag compound) {
      this.ammoManagers.get(0).bulletNum = compound.getInt(PomkotsMechs.nbtName("BulletNum1"));
      this.ammoManagers.get(1).bulletNum = compound.getInt(PomkotsMechs.nbtName("BulletNum2"));
      this.ammoManagers.get(2).bulletNum = compound.getInt(PomkotsMechs.nbtName("BulletNum3"));
      this.ammoManagers.get(3).bulletNum = compound.getInt(PomkotsMechs.nbtName("BulletNum4"));
      this.initializeAmmoManager(false);
   }

   protected void initializeAmmoManager(boolean resetBulletNum) {
      if (this.isServerSide()) {
         for (Pmvc01Entity.AmmoManager ammoData : this.ammoManagers) {
            ammoData.initialize(resetBulletNum);
         }
      }
   }

   public boolean consumeBullet(int num, ItemStack weaponStack, int weaponItemSlot) {
      Pmvc01Entity.AmmoManager ammoManager = this.getAmmoManager(weaponItemSlot);
      return ammoManager.consumeBullet(num);
   }

   public boolean consumeBulletFromServerSide(int num, ItemStack weaponStack, int weaponItemSlot) {
      Pmvc01Entity.AmmoManager ammoManager = this.getAmmoManager(weaponItemSlot);
      return ammoManager.consumeBulletFromServerSide(num);
   }

   public Pmvc01Entity.AmmoManager getAmmoManager(int weaponItemSlot) {
      return this.ammoManagers.get(weaponItemSlot - 6);
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(P_HEAD, ItemStack.EMPTY);
      this.entityData.define(P_ARMS, ItemStack.EMPTY);
      this.entityData.define(P_BODY, ItemStack.EMPTY);
      this.entityData.define(P_LEGS, ItemStack.EMPTY);
      this.entityData.define(P_GENERATOR, ItemStack.EMPTY);
      this.entityData.define(P_BOOSTER, ItemStack.EMPTY);
      this.entityData.define(W_RIGHT_ARM, ItemStack.EMPTY);
      this.entityData.define(W_LEFT_ARM, ItemStack.EMPTY);
      this.entityData.define(W_RIGHT_SHOULDER, ItemStack.EMPTY);
      this.entityData.define(W_LEFT_SHOULDER, ItemStack.EMPTY);
      this.entityData.define(W_EXTENSION1, ItemStack.EMPTY);
      this.entityData.define(W_EXTENSION2, ItemStack.EMPTY);
      this.entityData.define(DURABILITY, 0);
      this.entityData.define(WEIGHT, 0);
      this.entityData.define(MAX_WEIGHT, 0);
      this.entityData.define(SPEED, 0.0F);
      this.entityData.define(JUMP, 0.0F);
      this.entityData.define(MAX_ENERGY, 0);
      this.entityData.define(ENERGY_CHARGE_PER_TICK, 0.0F);
      this.entityData.define(WORK_SEC_PER_FUEL, 0);
      this.entityData.define(SPEED_MODIFIER_EVASION, 0.0F);
      this.entityData.define(SPEED_MODIFIER_VERTICAL, 0.0F);
      this.entityData.define(ENERGY_CONSUME_EVASION, 0);
      this.entityData.define(ENERGY_CONSUME_VERTICAL, 0);
      this.entityData.define(HAS_LOCK_SOFT, false);
      this.entityData.define(HAS_LOCK_HARD, false);
      this.entityData.define(RELOAD_RA, 0);
      this.entityData.define(RELOAD_LA, 0);
      this.entityData.define(RELOAD_RS, 0);
      this.entityData.define(RELOAD_LS, 0);
      this.entityData.define(FUEL_MAX, 0);
      this.entityData.define(FUEL_NOW, 0);
      this.entityData.define(REPAIR_KIT_NUM, 0);
      this.entityData.define(IS_BOUND_TO_RAIL, false);
      this.entityData.define(TEXTURE_COLOR, 0);
      this.entityData.define(IS_BROKEN, false);
      this.entityData.define(IS_LOCKED, false);
      this.entityData.define(IS_OVER_HEAT, false);
      this.entityData.define(IS_SUPER_BOOST, false);
      this.entityData.define(SHOW_PLAYER_INV_SLOT, false);
      this.entityData.define(BUILD_MODE, false);
      this.entityData.define(IS_GLIDING, false);
      this.entityData.define(HAVING_ENTITY, Optional.empty());
   }

   protected void syncAllParameter2Client() {
      this.syncAllParameter2Client(true);
   }

   protected void syncAllParameter2Client(boolean updateHealth) {
      this.entityData.set(P_HEAD, this.getHeadPartsFromInventory());
      this.entityData.set(P_ARMS, this.getArmPartsFromInventory());
      this.entityData.set(P_BODY, this.getBodyPartsFromInventory());
      this.entityData.set(P_LEGS, this.getLegsPartsFromInventory());
      this.entityData.set(P_GENERATOR, this.getGeneratorFromInventory());
      this.entityData.set(P_BOOSTER, this.getBoosterFromInventory());
      this.entityData.set(W_RIGHT_ARM, this.getRightArmWeaponFromInventory());
      this.entityData.set(W_LEFT_ARM, this.getLeftArmWeaponFromInventory());
      this.entityData.set(W_RIGHT_SHOULDER, this.getRightShoulderWeaponFromInventory());
      this.entityData.set(W_LEFT_SHOULDER, this.getLeftShoulderWeaponFromInventory());
      this.entityData.set(W_EXTENSION1, this.getExtension1FromInventory());
      this.entityData.set(W_EXTENSION2, this.getExtension2FromInventory());
      this.entityData.set(REPAIR_KIT_NUM, this.getItem(17).getCount());
      this.updateShowPlaeyrInv();
      this.updateMechParams(updateHealth);
      this.registerWeapons();
   }

   private void updateShowPlaeyrInv() {
      if (!(this.getRightArmWeaponFromInventory().getItem() instanceof AmagiItem)
         && !(this.getLeftArmWeaponFromInventory().getItem() instanceof AmagiItem)
         && !(this.getRightArmWeaponFromInventory().getItem() instanceof ShoutouItem)
         && !(this.getLeftArmWeaponFromInventory().getItem() instanceof ShoutouItem)) {
         this.entityData.set(SHOW_PLAYER_INV_SLOT, false);
      } else {
         this.entityData.set(SHOW_PLAYER_INV_SLOT, true);
      }
   }

   private void updateMechParams(boolean updateHealth) {
      Pmvc01Entity.MechParam param = new Pmvc01Entity.MechParam();
      this.sumPartsParameter(this.getHeadPartsFromInventory(), param);
      this.sumPartsParameter(this.getArmPartsFromInventory(), param);
      this.sumPartsParameter(this.getBodyPartsFromInventory(), param);
      this.sumPartsParameter(this.getLegsPartsFromInventory(), param);
      this.sumPartsParameter(this.getRightArmWeaponFromInventory(), param);
      this.sumPartsParameter(this.getLeftArmWeaponFromInventory(), param);
      this.sumPartsParameter(this.getRightShoulderWeaponFromInventory(), param);
      this.sumPartsParameter(this.getLeftShoulderWeaponFromInventory(), param);
      this.sumPartsParameter(this.getGeneratorFromInventory(), param);
      this.sumPartsParameter(this.getBoosterFromInventory(), param);
      this.sumPartsParameter(this.getExtension1FromInventory(), param);
      this.sumPartsParameter(this.getExtension2FromInventory(), param);
      param.durability = param.durability + (int)BattleBalance.MECH_HEALTH / 2;
      param.speed = this.calculateSpeedModifier(param.maxWeight, param.weight) * param.speed;
      param.jump = this.calculateSpeedModifier(param.maxWeight, param.weight) * param.jump;
      MechStatus mechStatus = this.applySkillEffects(param);
      MechStats mechStats = mechStatus.stats();
      this.entityData.set(DURABILITY, (int)mechStats.health());
      this.entityData.set(WEIGHT, param.weight);
      this.entityData.set(MAX_WEIGHT, param.maxWeight);
      this.entityData.set(SPEED, (float)mechStats.moveSpeed());
      this.entityData.set(JUMP, (float)mechStats.jumpPower());
      this.entityData.set(MAX_ENERGY, (int)mechStats.energyCapacity());
      this.entityData.set(ENERGY_CHARGE_PER_TICK, (float)mechStats.energyRecovery());
      this.entityData.set(WORK_SEC_PER_FUEL, param.workSecPerFuel);
      this.entityData.set(SPEED_MODIFIER_EVASION, (float)mechStats.dashSpeed());
      this.entityData.set(SPEED_MODIFIER_VERTICAL, (float)mechStats.verticalSpeed());
      this.entityData.set(ENERGY_CONSUME_EVASION, (int)(mechStats.energyCost() * param.energyConsumeEvasion));
      this.entityData.set(ENERGY_CONSUME_VERTICAL, (int)(mechStats.energyCost() * param.energyConsumeVertical));
      this.entityData.set(HAS_LOCK_SOFT, this.hasSoftLockCircuit() || mechStatus.features().isEnabled(MechFeatureType.TARGET_ASSIST_SOFT));
      this.entityData.set(HAS_LOCK_HARD, this.hasHardLockCircuit() || mechStatus.features().isEnabled(MechFeatureType.TARGET_ASSIST_HARD));
      this.syncFuels();
      if (updateHealth) {
         this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(mechStats.health());
         this.setHealth(Math.min(this.getHealth(), (float)mechStats.health()));
      }

      this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(mechStats.knockBackResistance());
   }

   private MechStatus applySkillEffects(Pmvc01Entity.MechParam param) {
      this.modifiedMechStatus = new MechStatus();
      this.modifiedMechStatus.setupBaseMechParams(param);
      SkillEffectApplicator.Accumulator accumulator = SkillEffectApplicator.createAccumulator();

      for (int i = 63; i < 81; i++) {
         ItemStack item = this.getItem(i);
         if (item.getItem() instanceof CircuitItem) {
            CircuitInstance circuit = CircuitItemStackHelper.getCircuit(item);
            if (circuit != null) {
               accumulator.add(circuit);
            }
         }
      }

      accumulator.applyTo(this.modifiedMechStatus);
      return this.modifiedMechStatus;
   }

   private void sumPartsParameter(ItemStack stack, Pmvc01Entity.MechParam param) {
      if (stack != null && !stack.isEmpty()) {
         if (stack.getItem() instanceof BasePartsItem partsItem) {
            param.durability = param.durability + partsItem.getDurability(stack);
            param.weight = param.weight + partsItem.getWeight(stack);
            if (partsItem instanceof BasePartsItem.Legs legs) {
               param.maxWeight = param.maxWeight + legs.getMaxWeight(stack);
               param.speed = param.speed + legs.getSpeedModifier(stack);
               param.jump = param.jump + legs.getJumpModifier(stack);
            } else if (partsItem instanceof BasePartsItem.Generator g) {
               param.maxEnergy = param.maxEnergy + g.getMaxEnergy(stack);
               param.energyChargePerTick = param.energyChargePerTick + g.getEnergyChargePerTick(stack);
               param.workSecPerFuel = param.workSecPerFuel + g.getWorkSecPerFuel(stack);
            } else if (partsItem instanceof BasePartsItem.Booster b) {
               param.speedModifierEvasion = param.speedModifierEvasion + b.getSpeedModifierEvasion(stack);
               param.speedModifierVertical = param.speedModifierVertical + b.getSpeedModifierVertical(stack);
               param.energyConsumeEvasion = param.energyConsumeEvasion + b.getEnergyConsumeEvasion(stack);
               param.energyConsumeVertical = param.energyConsumeVertical + b.getEnergyConsumeVertical(stack);
            }
         }
      }
   }

   public float calculateSpeedModifier(float maxLoad, float currentLoad) {
      double ratio = currentLoad / maxLoad;
      if (ratio <= 1.0) {
         return (float)(1.0 - 0.4 * Math.max(0.0, ratio - 0.5));
      }

      double excess = ratio - 1.0;
      return (float)(0.2 / (1.0 + excess * excess * excess * excess));
   }

   public int getDurability() {
      return (Integer)this.entityData.get(DURABILITY);
   }

   public int getWeight() {
      return (Integer)this.entityData.get(WEIGHT);
   }

   public int getMaxWeight() {
      return (Integer)this.entityData.get(MAX_WEIGHT);
   }

   public float getSpeedModifier() {
      return (Float)this.entityData.get(SPEED);
   }

   public float getJumpModifier() {
      return (Float)this.entityData.get(JUMP);
   }

   public float getEnergyChargePerTick() {
      return (Float)this.entityData.get(ENERGY_CHARGE_PER_TICK);
   }

   public float getWorkSecPerFuel() {
      return ((Integer)this.entityData.get(WORK_SEC_PER_FUEL)).intValue();
   }

   public float getSpeedModifierEvasion() {
      return (Float)this.entityData.get(SPEED_MODIFIER_EVASION);
   }

   public float getSpeedModifierVertical() {
      return (Float)this.entityData.get(SPEED_MODIFIER_VERTICAL);
   }

   public int getEnergyConsumeEvasion() {
      return (Integer)this.entityData.get(ENERGY_CONSUME_EVASION);
   }

   public int getEnergyConsumeVertical() {
      return (Integer)this.entityData.get(ENERGY_CONSUME_VERTICAL);
   }

   public boolean hasLockSoftCircuit() {
      return (Boolean)this.entityData.get(HAS_LOCK_SOFT);
   }

   public boolean hasLockHardCircuit() {
      return (Boolean)this.entityData.get(HAS_LOCK_HARD);
   }

   public ItemStack getHeadParts() {
      return (ItemStack)this.entityData.get(P_HEAD);
   }

   public ItemStack getBodyParts() {
      return (ItemStack)this.entityData.get(P_BODY);
   }

   public ItemStack getArmParts() {
      return (ItemStack)this.entityData.get(P_ARMS);
   }

   public ItemStack getLegsParts() {
      return (ItemStack)this.entityData.get(P_LEGS);
   }

   public ItemStack getBooster() {
      return (ItemStack)this.entityData.get(P_BOOSTER);
   }

   public ItemStack getGenerator() {
      return (ItemStack)this.entityData.get(P_GENERATOR);
   }

   public ItemStack getRightArmWeapon() {
      return (ItemStack)this.entityData.get(W_RIGHT_ARM);
   }

   public ItemStack getLeftArmWeapon() {
      return (ItemStack)this.entityData.get(W_LEFT_ARM);
   }

   public ItemStack getRightShoulderWeapon() {
      return (ItemStack)this.entityData.get(W_RIGHT_SHOULDER);
   }

   public ItemStack getLeftShoulderWeapon() {
      return (ItemStack)this.entityData.get(W_LEFT_SHOULDER);
   }

   public ItemStack getExtension1Weapon() {
      return (ItemStack)this.entityData.get(W_EXTENSION1);
   }

   public ItemStack getExtension2Weapon() {
      return (ItemStack)this.entityData.get(W_EXTENSION2);
   }

   public void setTextureColor(int i) {
      this.entityData.set(TEXTURE_COLOR, i);
   }

   public int getTextureColor() {
      return (Integer)this.entityData.get(TEXTURE_COLOR);
   }

   public int getMaxFuel() {
      return (Integer)this.entityData.get(FUEL_MAX);
   }

   public int getFuelNow() {
      return (Integer)this.entityData.get(FUEL_NOW);
   }

   public int getRepairKitNum() {
      return (Integer)this.entityData.get(REPAIR_KIT_NUM);
   }

   public boolean isBroken() {
      return (Boolean)this.entityData.get(IS_BROKEN);
   }

   public void setBroken(boolean broken) {
      this.entityData.set(IS_BROKEN, broken);
   }

   public boolean isLocked() {
      return (Boolean)this.entityData.get(IS_LOCKED);
   }

   public void setLocked(boolean broken) {
      this.entityData.set(IS_LOCKED, broken);
   }

   public boolean isOverHeat() {
      return (Boolean)this.entityData.get(IS_OVER_HEAT);
   }

   public void setOverHeat(boolean broken) {
      this.entityData.set(IS_OVER_HEAT, broken);
   }

   public boolean isSuperBoost() {
      return (Boolean)this.entityData.get(IS_SUPER_BOOST);
   }

   public void setSuperBoost(boolean flag) {
      if (this.isServerSide()) {
         this.entityData.set(IS_SUPER_BOOST, flag);
         this.setNoGravity(flag);
      }
   }

   public boolean isShowPlayerInventory() {
      return (Boolean)this.entityData.get(SHOW_PLAYER_INV_SLOT);
   }

   public boolean isBuildMode() {
      return (Boolean)this.entityData.get(BUILD_MODE);
   }

   public void setBuildMode(boolean b) {
      this.entityData.set(BUILD_MODE, b);
      if (b) {
         if (this.blockPreviewEntity == null || this.blockPreviewEntity.isRemoved()) {
            this.blockPreviewEntity = new BlockPlacementPreviewEntity(this.level(), this.position());
            this.level().addFreshEntity(this.blockPreviewEntity);
         }
      } else if (this.blockPreviewEntity != null) {
         this.blockPreviewEntity.discard();
         this.blockPreviewEntity = null;
      }
   }

   public Optional<UUID> havingEntity() {
      return (Optional<UUID>)this.entityData.get(HAVING_ENTITY);
   }

   public void setHavingEntity(Optional<UUID> b) {
      this.entityData.set(HAVING_ENTITY, b);
   }

   protected boolean isGliding() {
      return (Boolean)this.entityData.get(IS_GLIDING);
   }

   protected void setGliding(boolean b) {
      this.entityData.set(IS_GLIDING, b);
   }

   public Vec3 getMuzzlePosLA() {
      return this.muzzlePosLA;
   }

   public void setMuzzlePosLA(Vec3 muzzlePosLA) {
      this.muzzlePosLA = muzzlePosLA;
   }

   public Vec3 getMuzzlePosLS() {
      return this.muzzlePosLS;
   }

   public void setMuzzlePosLS(Vec3 muzzlePosLS) {
      this.muzzlePosLS = muzzlePosLS;
   }

   public Vec3 getMuzzlePosRA() {
      return this.muzzlePosRA;
   }

   public void setMuzzlePosRA(Vec3 muzzlePosRA) {
      this.muzzlePosRA = muzzlePosRA;
   }

   public Vec3 getMuzzlePosRS() {
      return this.muzzlePosRS;
   }

   public void setMuzzlePosRS(Vec3 muzzlePosRS) {
      this.muzzlePosRS = muzzlePosRS;
   }

   public boolean isUsingWeapon(BasePartsItem.Weapon w) {
      BasePartsItem.WeaponInterface.WeaponAttachPoint attchPoint = w.getWeaponAttachPoint();
      BasePartsItem.AttachSide side = w.getSide();
      if (BasePartsItem.AttachSide.RIGHT == side) {
         if (BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_ARM == attchPoint
            || BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_HAND == attchPoint) {
            return this.actionController.getAction(2).isInAction();
         }

         if (BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_SHOULDER == attchPoint) {
            return this.actionController.getAction(4).isInAction();
         }
      } else if (BasePartsItem.AttachSide.LEFT == side) {
         if (BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_ARM == attchPoint
            || BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_HAND == attchPoint) {
            return this.actionController.getAction(3).isInAction();
         }

         if (BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_SHOULDER == attchPoint) {
            return this.actionController.getAction(5).isInAction();
         }
      }

      return false;
   }

   public boolean isSelfPublic(Entity ent) {
      return this.isSelf(ent);
   }

   public Vec3 getMainCameraPosition() {
      return this.mainCameraPosition == null ? Vec3.ZERO : this.mainCameraPosition;
   }

   public void setMainCameraPosition(Vec3 pos) {
      this.mainCameraPosition = pos;
   }

   @Override
   public void travel(Vec3 pos) {
      if (!this.isBroken()) {
         if (this.isSuperBoost()) {
            ItemStack sb = this.getSuperBoostUnit();
            if (!(sb.getItem() instanceof BasePartsItem p && this.useEnergy(p.getEnergyConsumePerTick(sb))) && this.isServerSide()) {
               this.setSuperBoost(false);
            }

            if (this.isServerSide()) {
               Player owner = this.getDrivingPassenger() instanceof Player p ? p : null;
               if (owner == null) {
                  return;
               }

               Vec3 look = owner.getLookAngle();
               Vec3 velocity = look.normalize().scale(4.0);
               this.setDeltaMovement(velocity);
               this.setYRot(owner.getYRot());
               this.setXRot(owner.getXRot());
               this.yRotO = this.getYRot();
               this.xRotO = this.getXRot();
               this.yBodyRot = this.getYRot();
               this.yHeadRot = this.getYRot();
               this.setNoGravity(true);
               this.hasImpulse = true;
               super.travelBypass(this.position().add(this.getDeltaMovement()));
            }
         } else {
            this.travelInternal(pos);
         }
      } else {
         super.travelBypass(pos);
      }
   }

   public void travelInternal(Vec3 pos) {
      if (this.isAlive() && this.isVehicle() && this.canWork()) {
         LivingEntity pilot = this.getDrivingPassenger();
         if (pilot != null) {
            if (this.getVehicle() instanceof ElevatorEntity elv) {
               this.setYRot(elv.getFixedYaw());
               this.yRotO = elv.getFixedYaw();
               this.setYBodyRot(elv.getFixedYaw());
               this.yBodyRotO = elv.getFixedYaw();
               this.setYHeadRot(elv.getFixedYaw());
            } else {
               this.setYRot(pilot.getYRot());
               this.setXRot(pilot.getXRot() * 0.5F);
               this.setYBodyRot(this.getYRot());
               this.setYHeadRot(this.getYRot());
            }

            float strafe = pilot.xxa * 0.5F;
            float forward = pilot.zza * 0.5F;
            if (!this.level().isClientSide) {
               if (this.actionController.isBoost()) {
                  this.setSpeed(this.getRunSpeed());
               } else {
                  this.setSpeed(this.getWalkSpeed());
               }

               this.hasImpulse = true;
            }

            super.travelBypass(new Vec3(strafe, pos.y, forward));
            return;
         }
      }

      super.travelBypass(pos);
   }

   private ItemStack getSuperBoostUnit() {
      if (this.getExtension1Weapon().getItem() instanceof SBUnitProtoTypeItem bst) {
         return this.getExtension1Weapon();
      } else {
         return this.getExtension2Weapon().getItem() instanceof SBUnitProtoTypeItem bst ? this.getExtension2Weapon() : ItemStack.EMPTY;
      }
   }

   private boolean consumeWasHurtClient() {
      if (!this.wasHurtClient) {
         return false;
      }

      this.wasHurtClient = false;
      return true;
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (this.isClientSide()) {
         this.wasHurtClient = true;
      } else if (this.isServerSide() && this.mechAutoController != null) {
         this.mechAutoController.handleHurt(source, amount);
      }

      if (this.isBroken()) {
         return this.isForceKillSource(source) ? super.hurt(source, amount) : false;
      } else {
         return super.hurt(source, amount);
      }
   }

   private boolean isForceKillSource(DamageSource source) {
      if (source != this.damageSources().genericKill() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
         String msgId = source.getMsgId();
         return msgId.equals("kill") || msgId.equals("out_of_world");
      } else {
         return true;
      }
   }

   protected void actuallyHurt(DamageSource damageSource, float f) {
      if (this.breakCompatible()) {
         super.actuallyHurt(damageSource, f);
      } else {
         f = this.calcActualDamage(damageSource, f);
         if (!this.isForceKillSource(damageSource) && f > this.getHealth()) {
            this.setHealth(1.0F);
            this.setBroken(true);

            for (Entity passenger : this.getPassengers()) {
               passenger.stopRiding();
               if (passenger instanceof Mob mob) {
                  mob.setNoAi(false);
               }
            }
         } else {
            super.actuallyHurt(damageSource, f);
         }
      }
   }

   protected float calcActualDamage(DamageSource damageSource, float f) {
      if (!this.isInvulnerableTo(damageSource)) {
         f = this.getDamageAfterArmorAbsorb(damageSource, f);
         f = this.getDamageAfterMagicAbsorb(damageSource, f);
         return Math.max(f - this.getAbsorptionAmount(), 0.0F);
      } else {
         return 0.0F;
      }
   }

   protected boolean breakCompatible() {
      return false;
   }

   protected void tickDeath() {
      super.tickDeath();
      if (this.deathTime == 10 && this.isServerSide()) {
         Level level = this.level();
         ExplosionEntity e = new ExplosionEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOSION.get(), level);
         e.setPos(this.position());
         level.addFreshEntity(e);
      }
   }

   protected void checkInsideBlocks() {
   }

   public boolean ignoreExplosion() {
      return this.getDrivingPassenger() == null;
   }

   private void spawnDestructionParticles() {
      for (int i = 0; i < 5; i++) {
         double offsetX = (this.random.nextDouble() - 0.5) * this.getBbWidth();
         double offsetY = this.random.nextDouble() * this.getBbHeight();
         double offsetZ = (this.random.nextDouble() - 0.5) * this.getBbWidth();
         double x = this.getX() + offsetX;
         double y = this.getY() + offsetY;
         double z = this.getZ() + offsetZ;

         ParticleOptions particle = switch (this.random.nextInt(4)) {
            case 0 -> ParticleTypes.SMOKE;
            case 1 -> ParticleTypes.SMALL_FLAME;
            case 2 -> ParticleTypes.ELECTRIC_SPARK;
            default -> ParticleTypes.EXPLOSION;
         };
         this.level().addParticle(particle, x, y, z, 0.0, 0.01, 0.0);
      }

      if (this.random.nextFloat() < 1.0F) {
         this.playSoundEffect(SoundEvents.GENERIC_EXPLODE);
      }
   }

   public Action getActionEvasion() {
      return this.actionController.getAction(0);
   }

   protected void playSoundEffectWithFade(String key, SoundEvent event, float volume, int fadein, int fadeout) {
      key = this.getId() + ":" + key;
      PomkotsSoundManager.playSound(
         key, event, SoundConfig.loopFadeSE().followEntity(this).source(SoundSource.PLAYERS).relative(false).volume(volume).fadeIn(fadein).fadeOut(fadeout)
      );
      this.hasLoopSound = true;
   }

   protected void stopSoundEffectWithFade(String key) {
      key = this.getId() + ":" + key;
      PomkotsSoundManager.stopSound(key, true);
   }

   protected void stopAllSoundEffectWithFade() {
      if (this.hasLoopSound) {
         PomkotsSoundManager.stopAllSounds(this.getId(), true);
         this.hasLoopSound = false;
      }
   }

   public void onClientRemoval() {
      super.onClientRemoval();
      PomkotsSoundManager.stopAllSounds(this.getId(), true);
   }

   @Override
   public boolean shouldRenderDefaultHud(String hudName) {
      return "renderHotbar".equals(hudName) && (this.isShowPlayerInventory() || this.isBuildMode());
   }

   public boolean showCustomHealthBar() {
      return this.showCustomHealthBar;
   }

   public void setShowCustomHealthBar(boolean b) {
      this.showCustomHealthBar = b;
   }

   public static class AmmoManager {
      public int RELOAD_TICKS = 60;
      private int bulletNum;
      private int bulletNumPerMagazine;
      private int magazineNum;
      private final Pmvc01Entity owner;
      private int prevState;
      private int reloadTicks;
      private Action reloadAction;
      private final Supplier<ItemStack> ammoStackSupplier;
      private final Supplier<ItemStack> weaponStackSupplier;
      private final EntityDataAccessor<Integer> reloadStateAccessor;

      AmmoManager(
         Supplier<ItemStack> ammoStackSupplier, Supplier<ItemStack> weaponStackSupplier, EntityDataAccessor<Integer> r, Pmvc01Entity owner, Action reloadAction
      ) {
         this.ammoStackSupplier = ammoStackSupplier;
         this.weaponStackSupplier = weaponStackSupplier;
         this.reloadStateAccessor = r;
         this.owner = owner;
         this.reloadAction = reloadAction;
      }

      private void initialize(boolean resetBulletNum) {
         if (this.owner.isServerSide()) {
            int tmpBN = this.bulletNum;
            int tmpBNPM = this.bulletNumPerMagazine;
            this.bulletNum = 0;
            this.bulletNumPerMagazine = 0;
            this.magazineNum = 0;
            ItemStack ammoStack = this.ammoStackSupplier.get();
            ItemStack weaponStack = this.weaponStackSupplier.get();
            if (ammoStack != null
               && weaponStack != null
               && ammoStack.getItem() instanceof BasePartsItem.Magazine mag
               && weaponStack.getItem() instanceof BasePartsItem.Weapon weapon
               && weapon.isMatchAmmo(mag)) {
               this.bulletNumPerMagazine = mag.getBulletsPerMagazine(ammoStack);
               this.magazineNum = ammoStack.getCount();
               if (resetBulletNum) {
                  if (this.bulletNumPerMagazine != tmpBNPM) {
                     this.bulletNum = Math.min(this.bulletNumPerMagazine, tmpBN);
                  } else {
                     this.bulletNum = tmpBN;
                  }
               } else {
                  this.bulletNum = tmpBN;
               }
            }

            this.syncClient();
         }
      }

      public boolean isReloading() {
         return this.reloadTicks > 0;
      }

      protected void tick() {
         if (this.owner.isClientSide()) {
            int state = (Integer)this.owner.entityData.get(this.reloadStateAccessor);
            if (this.prevState != state) {
               this.deserialize(state);
               this.prevState = state;
            }
         }

         if (this.reloadTicks > 0) {
            if (this.reloadTicks == this.RELOAD_TICKS && !this.reloadAction.isInAction()) {
               this.reloadAction.startAction();
            }

            this.reloadTicks--;
            if (this.owner.isServerSide() && this.reloadTicks == 0) {
               this.doActualReload(true);
            }
         }
      }

      private boolean doActualReload(boolean doSync) {
         ItemStack ammoStack = this.ammoStackSupplier.get();
         if (ammoStack.getCount() > 0 && ammoStack.getItem() instanceof BasePartsItem.Magazine mag) {
            ammoStack.shrink(1);
            this.bulletNum = this.bulletNumPerMagazine = mag.getBulletsPerMagazine(ammoStack);
            this.magazineNum = ammoStack.getCount();
            if (doSync) {
               this.syncClient();
            }

            return true;
         } else {
            return false;
         }
      }

      protected void syncClient() {
         this.owner.entityData.set(this.reloadStateAccessor, this.serialize());
      }

      protected boolean consumeBullet(int num) {
         if (this.bulletNumPerMagazine > 0) {
            if (this.bulletNum == 0 && this.reloadTicks > 0) {
               return false;
            } else {
               this.bulletNum = Math.max(0, this.bulletNum - num);
               if (this.bulletNum > 0) {
                  return true;
               } else if (!(
                  this.weaponStackSupplier.get().getItem() instanceof BasePartsItem.Weapon w
                     && w.getWeaponCategory() == BasePartsItem.WeaponCategory.MISSILE
                     && this.owner.isServerSide()
               )) {
                  this.startReload();
                  return false;
               } else if (this.doActualReload(false)) {
                  this.bulletNum -= num;
                  this.syncClient();
                  return true;
               } else {
                  return false;
               }
            }
         } else {
            return false;
         }
      }

      public void startReload() {
         if (this.owner.isServerSide() && this.reloadTicks == 0) {
            this.reloadTicks = this.RELOAD_TICKS;
            this.syncClient();
         }
      }

      protected boolean consumeBulletFromServerSide(int num) {
         boolean b = this.consumeBullet(num);
         this.syncClient();
         return b;
      }

      public int getBulletNum() {
         return this.bulletNum;
      }

      public int getBulletNumPerMagazine() {
         return this.bulletNumPerMagazine;
      }

      public int getMagazineNum() {
         return this.magazineNum;
      }

      public int getReloadTicks() {
         return this.reloadTicks;
      }

      private int serialize() {
         int result = 0;
         result |= (this.bulletNumPerMagazine & 1023) << 22;
         result |= (this.bulletNum & 1023) << 12;
         result |= (this.magazineNum & 63) << 6;
         return result | this.reloadTicks & 63;
      }

      private void deserialize(int serialized) {
         this.bulletNumPerMagazine = serialized >> 22 & 1023;
         this.bulletNum = serialized >> 12 & 1023;
         this.magazineNum = serialized >> 6 & 63;
         this.reloadTicks = serialized & 63;
      }
   }

   public static class EntityInLevelCallback2 implements EntityInLevelCallback {
      EntityInLevelCallback del;
      Pmvc01Entity ent;

      EntityInLevelCallback2(EntityInLevelCallback delegate, Pmvc01Entity ent) {
         this.del = delegate;
         this.ent = ent;
      }

      public void onMove() {
         this.del.onMove();
      }

      public void onRemove(RemovalReason removalReason) {
         this.del.onRemove(removalReason);
         if (this.ent.level() instanceof ServerLevel serverLevel
            && (removalReason == RemovalReason.UNLOADED_TO_CHUNK || removalReason == RemovalReason.UNLOADED_WITH_PLAYER)) {
            PomkotsMechsSaveData.get(serverLevel).updateMech(this.ent.getUUID(), serverLevel.dimension(), new ChunkPos(this.ent.blockPosition()));
         }
      }
   }

   public static class MechParam {
      public int durability;
      public int weight;
      public int maxWeight;
      public float speed;
      public float jump;
      public int maxEnergy;
      public float energyChargePerTick;
      public int workSecPerFuel;
      public int energyConsumeEvasion;
      public int energyConsumeVertical;
      public float speedModifierEvasion;
      public float speedModifierVertical;
   }

   public enum WallSide {
      FRONT,
      BACK,
      LEFT,
      RIGHT,
      NONE;
   }
}
