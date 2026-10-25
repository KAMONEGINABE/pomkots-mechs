package grcmcs.minecraft.mods.pomkotsmechs.entity.event;

import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.EventDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.RaidDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.SpawnTarget;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.WaveDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.Pmc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.BGMState;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.ServerBGMTracker;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.NotNull;

public class RaidControllerEntity extends LivingEntity {
   private static final int MAX_LIVING_TICK = 120000;
   private static final String NBT_START_TARGET_RAID_NAME = "StartTargetRaidName";
   private static final String NBT_RAID_OWNER_UUID = "RaidOwnerUUID";
   private static final String NBT_RAID_FINALIZE_CMD_SUCCESS = "RaidFinalizeCommandSuccess";
   private static final String NBT_RAID_FINALIZE_CMD_FAIL = "RaidFinalizeCommandFail";
   private static final String NBT_CURRENT_RAID_NAME = "CurrentRaidName";
   private static final String NBT_RAID_TARGET_ENTITY_UUID = "RaidTargetEntityUUID";
   private static final String NBT_CURRENT_WAVE_INDEX = "CurrentWaveIndex";
   private static final String NBT_TICK_COUNTER = "TickCounter";
   private static final String NBT_END_EVENT_WHEN_KILLED_ALL = "EndEventWhenKilledAllMobs";
   private static final String NBT_SPAWNED_MOBS = "SpawnedMobs";
   private static final String NBT_SPAWNED_MOBS_NUM = "SpawnedMobsNum";
   private String startTargetRaidName = null;
   private UUID raidOwnerUUID = null;
   private String raidFinalizeCommandSuccess = null;
   private String raidFinalizeCommandFail = null;
   private String currentRaidName = null;
   private UUID raidTargetEntityUUID = null;
   private int currentWaveIndex = -1;
   private int tickCounter = -1;
   private boolean endEventWhenKilledAllMobs = false;
   private int spawnedMobsNum = -1;
   private RaidDefinition raidDefinition = null;
   private LivingEntity raidTargetEntity = null;
   private Entity raidOwner = null;
   private List<Entity> spawnedMobs = new ArrayList<>();
   private ListTag spawnedMobsTag = null;
   private final ServerBossEvent timeBar;
   private final ServerBossEvent baseHpBar;
   private int prevRemaining = 0;

   public static Builder createMobAttributes() {
      return LivingEntity.m_21183_()
         .m_22266_(Attributes.f_22282_)
         .m_22268_(Attributes.f_22278_, 1.0)
         .m_22268_(Attributes.f_22276_, 1000.0)
         .m_22268_(Attributes.f_22284_, 20.0)
         .m_22268_(Attributes.f_22285_, 1.0);
   }

   @NotNull
   public static CompoundTag buildCompoundTag(String startTargetRaidName, UUID raidOwnerUUID, String raidFinalizeCommandSuccess, String raidFinalizeCommandFail) {
      CompoundTag tag = new CompoundTag();
      tag.m_128359_(nbt("StartTargetRaidName"), startTargetRaidName);
      if (raidOwnerUUID != null) {
         tag.m_128362_(nbt("RaidOwnerUUID"), raidOwnerUUID);
      }

      if (raidFinalizeCommandSuccess != null) {
         tag.m_128359_(nbt("RaidFinalizeCommandSuccess"), raidFinalizeCommandSuccess);
      }

      if (raidFinalizeCommandFail != null) {
         tag.m_128359_(nbt("RaidFinalizeCommandFail"), raidFinalizeCommandFail);
      }

      return tag;
   }

   public static List<BaseBossEntity> getInactiveBossesAroundPos(Level level, BlockPos pos) {
      AABB area = AABB.m_82333_(pos.m_252807_()).m_82400_(40.0);
      return level.m_45976_(BaseBossEntity.class, area).stream().filter(b -> b.getAiMode() == -2).toList();
   }

   public static List<Player> getPlayersAroundPos(Level level, BlockPos pos) {
      AABB area = AABB.m_82333_(pos.m_252807_()).m_82400_(100.0);
      return level.m_45976_(Player.class, area);
   }

   public static Optional<SoundEvent> getSoundEvent(ResourceLocation id) {
      return BuiltInRegistries.f_256894_.m_6612_(id);
   }

   public RaidControllerEntity(EntityType<? extends LivingEntity> entityType, Level world) {
      this(entityType, world, null);
   }

   public RaidControllerEntity(EntityType<? extends LivingEntity> entityType, Level world, BaseBossEntity parent) {
      super(entityType, world);
      this.f_19794_ = true;
      this.m_20242_(true);
      this.timeBar = new ServerBossEvent(Component.m_237113_("Raid Time"), BossBarColor.BLUE, BossBarOverlay.PROGRESS);
      this.baseHpBar = new ServerBossEvent(Component.m_237113_("Target HP"), BossBarColor.GREEN, BossBarOverlay.PROGRESS);
      this.timeBar.m_142711_(1.0F);
      this.baseHpBar.m_142711_(1.0F);
      this.setBossBarVisibility(false);
   }

   private static String nbt(String key) {
      return PomkotsMechs.nbtName(key);
   }

   public void m_7378_(CompoundTag tag) {
      super.m_7378_(tag);
      this.startTargetRaidName = readString(tag, "StartTargetRaidName");
      this.raidOwnerUUID = readUUID(tag, "RaidOwnerUUID");
      this.raidFinalizeCommandSuccess = readString(tag, "RaidFinalizeCommandSuccess");
      this.raidFinalizeCommandFail = readString(tag, "RaidFinalizeCommandFail");
      this.currentRaidName = readString(tag, "CurrentRaidName");
      this.raidTargetEntityUUID = readUUID(tag, "RaidTargetEntityUUID");
      this.currentWaveIndex = tag.m_128441_(nbt("CurrentWaveIndex")) ? tag.m_128451_(nbt("CurrentWaveIndex")) : -1;
      this.tickCounter = tag.m_128441_(nbt("TickCounter")) ? tag.m_128451_(nbt("TickCounter")) : -1;
      this.spawnedMobsNum = tag.m_128441_(nbt("SpawnedMobsNum")) ? tag.m_128451_(nbt("SpawnedMobsNum")) : -1;
      this.endEventWhenKilledAllMobs = tag.m_128441_(nbt("EndEventWhenKilledAllMobs")) && tag.m_128471_(nbt("EndEventWhenKilledAllMobs"));
      this.spawnedMobsTag = (ListTag)tag.m_128423_(nbt("SpawnedMobs"));
   }

   public void m_7380_(CompoundTag tag) {
      super.m_7380_(tag);
      writeString(tag, "StartTargetRaidName", this.startTargetRaidName);
      writeUUID(tag, "RaidOwnerUUID", this.raidOwnerUUID);
      writeString(tag, "RaidFinalizeCommandSuccess", this.raidFinalizeCommandSuccess);
      writeString(tag, "RaidFinalizeCommandFail", this.raidFinalizeCommandFail);
      writeString(tag, "CurrentRaidName", this.currentRaidName);
      writeUUID(tag, "RaidTargetEntityUUID", this.raidTargetEntityUUID);
      writeIntIfSet(tag, "CurrentWaveIndex", this.currentWaveIndex);
      writeIntIfSet(tag, "TickCounter", this.tickCounter);
      writeIntIfSet(tag, "SpawnedMobsNum", this.spawnedMobsNum);
      if (this.endEventWhenKilledAllMobs) {
         tag.m_128379_(nbt("EndEventWhenKilledAllMobs"), true);
      } else {
         tag.m_128473_(nbt("EndEventWhenKilledAllMobs"));
      }

      ListTag list = new ListTag();

      for (Entity ent : this.spawnedMobs) {
         list.add(NbtUtils.m_129226_(ent.m_20148_()));
      }

      tag.m_128365_(nbt("SpawnedMobs"), list);
   }

   private static String readString(CompoundTag tag, String key) {
      return tag.m_128441_(nbt(key)) ? tag.m_128461_(nbt(key)) : null;
   }

   private static UUID readUUID(CompoundTag tag, String key) {
      return tag.m_128441_(nbt(key)) ? tag.m_128342_(nbt(key)) : null;
   }

   private static void writeString(CompoundTag tag, String key, String value) {
      if (value != null) {
         tag.m_128359_(nbt(key), value);
      } else {
         tag.m_128473_(nbt(key));
      }
   }

   private static void writeUUID(CompoundTag tag, String key, UUID value) {
      if (value != null) {
         tag.m_128362_(nbt(key), value);
      } else {
         tag.m_128473_(nbt(key));
      }
   }

   private static void writeIntIfSet(CompoundTag tag, String key, int value) {
      if (value != -1) {
         tag.m_128405_(nbt(key), value);
      } else {
         tag.m_128473_(nbt(key));
      }
   }

   public void m_8119_() {
      if (this.f_19803_ && !this.m_9236_().f_46443_) {
         this.loadSpawnedMobs();
      }

      super.m_8119_();
      if (!this.m_9236_().f_46443_) {
         if (this.f_19797_ > 120000) {
            PomkotsMechs.LOGGER.info("MAX Tick Count exceeded: {}", this);
            this.m_146870_();
         } else {
            ServerLevel serverLevel = (ServerLevel)this.m_9236_();
            this.spawnedMobs.removeIf(e -> e == null || !e.m_6084_());
            if (this.startTargetRaidName == null || this.initializeRaid(serverLevel)) {
               this.raidOwner = this.raidOwnerUUID != null ? serverLevel.m_8791_(this.raidOwnerUUID) : null;
               if (this.currentRaidName != null) {
                  if (this.raidTargetEntity == null) {
                     this.raidTargetEntity = (LivingEntity)serverLevel.m_8791_(this.raidTargetEntityUUID);
                     this.raidDefinition = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(this.currentRaidName);
                  }

                  if (this.raidDefinition != null && this.raidTargetEntity != null) {
                     this.tickRaid(serverLevel);
                  }
               }
            }
         }
      }
   }

   private boolean initializeRaid(ServerLevel serverLevel) {
      this.raidDefinition = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(this.startTargetRaidName);
      if (this.raidDefinition == null) {
         PomkotsMechs.LOGGER.error("Raid def not found: {}", this.startTargetRaidName);
         this.m_146870_();
         return false;
      }

      this.currentRaidName = this.startTargetRaidName;
      this.tickCounter = 0;
      this.currentWaveIndex = 0;

      for (ServerPlayer player : this.baseHpBar.m_8324_()) {
         this.startBGMforPlayer(player, this.raidDefinition);
      }

      this.setupRaidTarget(serverLevel);
      this.startTargetRaidName = null;
      return true;
   }

   private void setupRaidTarget(ServerLevel serverLevel) {
      switch (RaidControllerEntity.RaidType.of(this.raidDefinition.type)) {
         case DEFENSE:
            RaidObjectiveEntity roe = (RaidObjectiveEntity)((EntityType)PomkotsMechs.RAID_OBJECTIVE.get()).m_20615_(serverLevel);
            if (roe != null) {
               BlockPos pos = this.m_20183_();
               roe.m_6034_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
               this.m_9236_().m_7967_(roe);
               this.raidTargetEntity = roe;
               this.raidTargetEntityUUID = roe.m_20148_();
            }
            break;
         case SURVIVE:
            this.raidTargetEntity = (LivingEntity)serverLevel.m_8791_(this.raidOwnerUUID);
            this.raidTargetEntityUUID = this.raidOwnerUUID;
            break;
         case SWEEP:
         case SWEEP_BOSS_BOX:
         case ACTIVATE:
            this.raidTargetEntity = this;
            this.raidTargetEntityUUID = this.m_20148_();
            if (RaidControllerEntity.RaidType.of(this.raidDefinition.type) == RaidControllerEntity.RaidType.ACTIVATE) {
               List<BaseBossEntity> bosses = getInactiveBossesAroundPos(this.m_9236_(), this.m_20183_());
               List<Player> players = getPlayersAroundPos(this.m_9236_(), this.m_20183_());

               for (BaseBossEntity boss : bosses) {
                  boss.boot();
                  players.forEach(p -> boss.addHateToEntity(p, 50.0F));
                  this.spawnedMobs.add(boss);
               }
            }
      }
   }

   private void tickRaid(ServerLevel serverLevel) {
      WaveDefinition wave = this.raidDefinition.waves.get(this.currentWaveIndex);
      RaidControllerEntity.RaidType type = RaidControllerEntity.RaidType.of(this.raidDefinition.type);
      if (this.raidTargetEntity instanceof RaidObjectiveEntity) {
         BlockPos pos = this.m_20183_();
         this.raidTargetEntity.m_6034_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
      }

      if (this.tickCounter == 5) {
         this.showTitlesForPlayers(wave.title, wave.sub_title, this.baseHpBar.m_8324_());
      }

      this.tickCounter++;
      if (!this.baseHpBar.m_8323_()) {
         this.setBossBarVisibility(true);
      }

      int[] remainMaxMobs = this.remainingSpawnTargets(this.raidDefinition, this.currentWaveIndex, this.tickCounter);
      this.updateBossBars(wave, remainMaxMobs);
      if (this.raidOwner == null || !this.raidOwner.m_6084_()) {
         this.endRaid(false, "レイドオーナーが消失");
      } else if (!this.checkDefeat(type, remainMaxMobs)) {
         if (type == RaidControllerEntity.RaidType.SURVIVE) {
            this.m_146884_(this.raidTargetEntity.m_20182_());
         }

         if (this.endEventWhenKilledAllMobs && this.areAllMobsDefeated(remainMaxMobs)) {
            this.tickCounter = wave.duration_ticks;
         }

         this.fireEvent(wave);
         this.checkWaveEnd(wave, type);
      }
   }

   private boolean checkDefeat(RaidControllerEntity.RaidType type, int[] remainMaxMobs) {
      switch (type) {
         case DEFENSE:
         case SURVIVE:
            if (this.raidTargetEntity == null || !this.raidTargetEntity.m_6084_()) {
               this.endRaid(false);
               return true;
            }
            break;
         case SWEEP:
         case SWEEP_BOSS_BOX:
            if (remainMaxMobs == null) {
               this.endRaid(false);
               return true;
            }

            if (this.spawnedMobs.isEmpty() && remainMaxMobs[0] <= 0) {
               if (this.isFinalWave(this.raidDefinition, this.currentWaveIndex)) {
                  this.endRaid(true);
               } else {
                  this.proceedWave();
               }

               return true;
            }
            break;
         case ACTIVATE:
            if (this.spawnedMobs.isEmpty()) {
               this.endRaid(true);
               return true;
            }
      }

      return false;
   }

   private void proceedWave() {
      this.currentWaveIndex++;
      this.endEventWhenKilledAllMobs = false;
      this.tickCounter = 0;
   }

   private boolean areAllMobsDefeated(int[] remainMaxMobs) {
      if (this.spawnedMobs.isEmpty() && remainMaxMobs[0] <= 0) {
         return true;
      }

      for (Entity ent : this.spawnedMobs) {
         if (!(ent instanceof Pmc01Entity pmc01)) {
            return false;
         }

         if (!"".equals(pmc01.containerType)) {
            return false;
         }
      }

      return true;
   }

   private void checkWaveEnd(WaveDefinition wave, RaidControllerEntity.RaidType type) {
      if (this.tickCounter >= wave.duration_ticks) {
         switch (type) {
            case DEFENSE:
            case SURVIVE:
               this.killAllMobs();
               this.proceedWave();
               if (this.currentWaveIndex >= this.raidDefinition.waves.size()) {
                  this.endRaid(true);
               }
               break;
            case SWEEP:
            case SWEEP_BOSS_BOX:
            case ACTIVATE:
               this.killAllMobs();
               this.endRaid(false);
         }
      }
   }

   private void setBossBarVisibility(boolean visible) {
      this.baseHpBar.m_8321_(visible);
      this.timeBar.m_8321_(visible);
   }

   private void updateBossBars(WaveDefinition wave, int[] remainingMobs) {
      if (this.raidTargetEntity != null) {
         int remaining = wave.duration_ticks - this.tickCounter;
         if (remaining != this.prevRemaining) {
            float timeProgress = (float)remaining / wave.duration_ticks;
            this.timeBar.m_6456_(Component.m_237113_(Utils.string2Component(wave.title).getString() + " | Time: " + remaining / 20 + "sec"));
            this.timeBar.m_142711_(Mth.m_14036_(timeProgress, 0.0F, 1.0F));
            RaidControllerEntity.RaidType type = RaidControllerEntity.RaidType.of(this.raidDefinition.type);
            if ((
                  type == RaidControllerEntity.RaidType.SWEEP
                     || type == RaidControllerEntity.RaidType.SWEEP_BOSS_BOX
                     || type == RaidControllerEntity.RaidType.ACTIVATE
               )
               && remainingMobs != null) {
               int total = remainingMobs[0] + this.spawnedMobs.size();
               float prog = (float)total / remainingMobs[1];
               this.baseHpBar.m_6456_(Component.m_237113_("Target Monsters: " + total + "/" + remainingMobs[1]));
               this.baseHpBar.m_142711_(Mth.m_14036_(prog, 0.0F, 1.0F));
            } else if (type != RaidControllerEntity.RaidType.ACTIVATE) {
               float hp = this.raidTargetEntity.m_21223_() / this.raidTargetEntity.m_21233_();
               this.baseHpBar.m_6456_(Component.m_237113_("Target HP: " + (int)this.raidTargetEntity.m_21223_() + "/" + (int)this.raidTargetEntity.m_21233_()));
               this.baseHpBar.m_142711_(Mth.m_14036_(hp, 0.0F, 1.0F));
            }

            this.prevRemaining = remaining;
         }
      }
   }

   public void m_6457_(ServerPlayer player) {
      this.baseHpBar.m_6543_(player);
      this.timeBar.m_6543_(player);
      if (!this.f_19803_) {
         this.startBGMforPlayer(player, this.raidDefinition);
      }
   }

   public void m_6452_(ServerPlayer player) {
      this.baseHpBar.m_6539_(player);
      this.timeBar.m_6539_(player);
      ServerBGMTracker.forceUnsetBGMState(player);
   }

   private void startBGMforPlayer(ServerPlayer player, RaidDefinition raidDef) {
      if (raidDef != null) {
         BGMState state = "defense".equals(raidDef.type) ? BGMState.BATTLE_RAID : BGMState.BATTLE_BOSS;
         ServerBGMTracker.forceSetBGMState(player, state);
      }
   }

   private void fireEvent(WaveDefinition wave) {
      if (wave.timeline != null) {
         EventDefinition event = wave.timeline.get(this.tickCounter);
         if (event != null) {
            switch (event.type) {
               case "spawn":
                  this.fireSpawnEvent(event);
                  break;
               case "spawn_random":
                  this.fireSpawnRandomEvent(event);
            }

            if (event.message != null) {
               this.sendMessage(event.message, this.baseHpBar.m_8324_());
            }

            this.endEventWhenKilledAllMobs = event.force_end_wave_when_killed_all;
         }
      }
   }

   private void fireSpawnEvent(EventDefinition event) {
      if (event.spawn_targets != null) {
         for (SpawnTarget st : event.spawn_targets) {
            if (st.position != null) {
               RegistrySupplier<EntityType<?>> type = this.getEntityType(st.mob_type);
               if (type != null) {
                  BlockPos pos = this.parseBlockPos(st.position);
                  if (pos != null && this.m_9236_() != null) {
                     Entity entity = ((EntityType)type.get()).m_20615_(this.m_9236_());
                     if (entity != null) {
                        BlockPos offset = this.m_20183_();
                        entity.m_6034_(offset.m_123341_() + pos.m_123341_(), offset.m_123342_() + pos.m_123342_(), offset.m_123343_() + pos.m_123343_());
                        this.applySnbt(entity, st.snbt);
                        this.setupSpawnedMob(entity);
                        this.spawnedMobs.add(entity);
                        this.m_9236_().m_7967_(entity);
                     }
                  }
               }
            }
         }
      }
   }

   private void fireSpawnRandomEvent(EventDefinition event) {
      if (event.spawn_targets != null) {
         Random rand = new Random();
         Level level = this.m_9236_();
         EntityType[] mobTypeList = this.getMobList(event);
         int count = event.amount;
         BlockPos center = this.m_20183_();
         LivingEntity opener = this.getTargetCandidate();
         AABB spawnSpace = new AABB(0.0, 0.0, 0.0, 2.0, 2.0, 2.0);
         int maxHops = (event.range_x + event.range_z) / 2;
         int spawned = 0;

         for (int i = 0; i < 100 && spawned < count; i++) {
            Optional<BlockPos> pos = this.findSpawnPos(maxHops, spawnSpace, (ServerLevel)level, center, this.f_19796_, event);
            if (pos.isPresent()) {
               Entity mob = this.getRandomMobType(rand, mobTypeList).m_20615_(level);
               if (mob != null) {
                  mob.m_7678_(pos.get().m_123341_() + 0.5, pos.get().m_123342_(), pos.get().m_123343_() + 0.5, rand.nextFloat() * 360.0F, 0.0F);
                  if (mob instanceof Mob m) {
                     if (opener != null) {
                        m.m_6710_(opener);
                     }

                     if (mob instanceof BaseSmallMonsterEntity small) {
                        small.m_21530_();
                        small.setInEvent(true);
                     }
                  }

                  level.m_7967_(mob);
                  this.spawnedMobs.add(mob);
                  spawned++;
               }
            }
         }
      }
   }

   private Optional<BlockPos> findSpawnPos(int maxHops, AABB spawnSpace, ServerLevel level, BlockPos origin, RandomSource random, EventDefinition event) {
      for (int attempt = 0; attempt < 20; attempt++) {
         double angle = random.m_188500_() * Math.PI * 2.0;
         double dist = random.m_188500_() * maxHops;
         double dx = Math.cos(angle) * dist;
         double dz = Math.sin(angle) * dist;
         double dy = random.m_188500_() * 8.0;
         BlockPos candidate = origin.m_7918_((int)dx, (int)dy, (int)dz);
         if (!event.spawn_mobs_in_closed_area) {
            return Optional.of(candidate);
         }

         if (candidate.m_123342_() >= origin.m_123342_()) {
            BlockHitResult hit = level.m_151353_(
               new ClipBlockStateContext(
                  Vec3.m_82512_(candidate), Vec3.m_82512_(origin), blockState -> !blockState.m_60795_() && !blockState.equals(level.m_8055_(origin))
               )
            );
            if (hit.m_6662_().equals(Type.MISS)) {
               AABB checkBox = spawnSpace.m_82386_(
                  candidate.m_123341_() - spawnSpace.f_82288_, candidate.m_123342_() - spawnSpace.f_82289_, candidate.m_123343_() - spawnSpace.f_82290_
               );
               if (level.m_45772_(checkBox)) {
                  return Optional.of(candidate);
               }
            }
         }
      }

      return Optional.of(origin);
   }

   private void setupSpawnedMob(Entity entity) {
      RaidControllerEntity.RaidType type = RaidControllerEntity.RaidType.of(this.raidDefinition.type);
      switch (type) {
         case DEFENSE:
            if (entity instanceof Mob mob) {
               mob.m_6710_(this.raidTargetEntity);
            }

            if (entity instanceof GenericPomkotsMonster p) {
               p.setInRaid(true);
               p.setInEvent(true);
            }
            break;
         case SURVIVE:
         case SWEEP:
         case SWEEP_BOSS_BOX:
            if (entity instanceof Mob mob) {
               if (entity instanceof GenericPomkotsMonster p) {
                  p.setInEvent(true);
               }

               if (((ServerLevel)this.m_9236_()).m_8791_(this.raidOwnerUUID) instanceof LivingEntity le) {
                  mob.m_6710_(le);
               }
            }
      }

      if (entity instanceof Pmc01Entity pmc01) {
         pmc01.raidControllerEntity = this;
         pmc01.raidControllerEntityUUID = this.m_20148_();
      } else if (entity instanceof Mob mob) {
         mob.m_21530_();
      }
   }

   private void applySnbt(Entity entity, String snbt) {
      if (snbt != null && entity instanceof LivingEntity le) {
         try {
            le.m_7378_(TagParser.m_129359_(snbt));
         } catch (Exception e) {
            PomkotsMechs.LOGGER.error("failed to parse snbt.", e);
         }
      }
   }

   private void endRaid(boolean victory) {
      if (victory
         && this.raidDefinition != null
         && this.raidDefinition.advancement != null
         && !this.raidDefinition.advancement.isEmpty()
         && this.raidOwner instanceof Player p) {
         Utils.completeAdvancement(new ResourceLocation(this.raidDefinition.advancement), p);
      }

      this.endRaid(victory, "");
   }

   private void endRaid(boolean victory, String subTitle) {
      String title = victory ? "Mission Complete!!" : "Mission Failed...";
      this.showTitlesForPlayers(title, subTitle, this.baseHpBar.m_8324_());
      String finalizeCommand = victory ? this.raidFinalizeCommandSuccess : this.raidFinalizeCommandFail;
      if (this.m_9236_() instanceof ServerLevel serverLevel
         && this.raidOwnerUUID != null
         && finalizeCommand != null
         && serverLevel.m_46003_(this.raidOwnerUUID) instanceof ServerPlayer serverPlayer) {
         this.runCommandAsServer(finalizeCommand, serverPlayer, serverLevel);
      }

      this.resetRaid();
      this.m_146870_();
   }

   private void resetRaid() {
      if (this.raidTargetEntity instanceof RaidObjectiveEntity) {
         this.raidTargetEntity.m_146870_();
      }

      this.raidTargetEntity = null;
      this.raidOwner = null;
      this.startTargetRaidName = null;
      this.raidOwnerUUID = null;
      this.raidFinalizeCommandSuccess = null;
      this.raidFinalizeCommandFail = null;
      this.currentRaidName = null;
      this.raidTargetEntityUUID = null;
      this.currentWaveIndex = -1;
      this.tickCounter = -1;
      this.endEventWhenKilledAllMobs = false;
      this.setBossBarVisibility(false);
   }

   private void killAllMobs() {
      if (this.raidDefinition == null || !this.raidDefinition.type.equals(RaidControllerEntity.RaidType.ACTIVATE.getId())) {
         for (Entity ent : this.spawnedMobs) {
            if (ent != null && ent.m_6084_()) {
               ent.m_6074_();
            }
         }
      }

      this.spawnedMobs.clear();
   }

   public void m_142687_(RemovalReason reason) {
      if (!this.m_9236_().f_46443_ && (reason == RemovalReason.DISCARDED || reason == RemovalReason.KILLED)) {
         this.setBossBarVisibility(false);
         if (this.raidTargetEntity instanceof RaidObjectiveEntity && this.raidTargetEntity.m_6084_()) {
            this.raidTargetEntity.m_146870_();
         }

         this.killAllMobs();
      }

      super.m_142687_(reason);
   }

   private void loadSpawnedMobs() {
      if (this.spawnedMobsTag != null) {
         this.spawnedMobs.clear();

         for (Tag t : this.spawnedMobsTag) {
            UUID uuid = NbtUtils.m_129233_(t);
            Entity ent = ((ServerLevel)this.m_9236_()).m_8791_(uuid);
            if (ent != null) {
               this.spawnedMobs.add(ent);
            }
         }
      }
   }

   private LivingEntity getTargetCandidate() {
      if (this.raidOwnerUUID == null) {
         return null;
      } else {
         return ((ServerLevel)this.m_9236_()).m_8791_(this.raidOwnerUUID) instanceof LivingEntity le ? le : null;
      }
   }

   private EntityType[] getMobList(EventDefinition event) {
      List<EntityType<? extends Entity>> list = new ArrayList<>();

      for (SpawnTarget st : event.spawn_targets) {
         RegistrySupplier<EntityType<?>> s = Utils.getEntityType(st.mob_type);
         if (s != null) {
            list.add((EntityType<? extends Entity>)s.get());
         }
      }

      return list.toArray(new EntityType[0]);
   }

   private EntityType<? extends Entity> getRandomMobType(Random rand, EntityType<? extends Entity>[] mobList) {
      return mobList[rand.nextInt(mobList.length)];
   }

   private RegistrySupplier<EntityType<?>> getEntityType(String typeId) {
      ResourceLocation rl = new ResourceLocation(typeId);

      for (RegistrySupplier<EntityType<?>> entityType : PomkotsMechs.ENTITIES) {
         if (entityType.getId().equals(rl)) {
            return entityType;
         }
      }

      return null;
   }

   private boolean isFinalWave(RaidDefinition raidDef, int waveIndex) {
      return raidDef.waves == null || raidDef.waves.size() == waveIndex + 1;
   }

   private int[] remainingSpawnTargets(RaidDefinition raidDef, int waveIndex, int tick) {
      if (raidDef.waves != null && raidDef.waves.size() > waveIndex) {
         WaveDefinition waveDef = raidDef.waves.get(waveIndex);
         if (waveDef.events == null) {
            return new int[]{0, 0};
         }

         int[] res = new int[]{0, 0};

         for (EventDefinition event : waveDef.events) {
            int count = "spawn".equals(event.type) ? event.spawn_targets.size() : ("spawn_random".equals(event.type) ? event.amount : 0);
            res[1] += count;
            if (event.trigger_tick >= tick) {
               res[0] += count;
            }
         }

         return res;
      } else {
         return null;
      }
   }

   private int findSpawnY(Level level, int x, int startY, int z, AABB spaceSize) {
      int maxY = startY + 50;
      int found = Integer.MIN_VALUE;

      for (int y = startY; y < maxY; y++) {
         AABB checkBox = spaceSize.m_82386_(x - spaceSize.f_82288_, y - spaceSize.f_82289_, z - spaceSize.f_82290_);
         if (level.m_45772_(checkBox)) {
            found = y;
            break;
         }
      }

      return found != Integer.MIN_VALUE ? found : level.m_6924_(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
   }

   private void runCommandAsOwner(String command, ServerPlayer player, ServerLevel serverLevel) {
      MinecraftServer server = serverLevel.m_7654_();
      CommandSourceStack source = player.m_20203_().m_81325_(4);
      server.m_129892_().m_230957_(source, command);
   }

   private void runCommandAsServer(String command, ServerPlayer player, ServerLevel serverLevel) {
      MinecraftServer server = serverLevel.m_7654_();
      CommandSourceStack source = server.m_129893_().m_81325_(4).m_81324_().m_81329_(player).m_81348_(player.m_20182_()).m_81327_(player.m_284548_());
      server.m_129892_().m_230957_(source, command);
   }

   private String replaceSelectorWithUUID(String command, ServerPlayer player) {
      String selector = "@e[uuid=" + player.m_20148_() + "]";
      return command.replace("@s", selector);
   }

   private void sendMessage(String message, Collection<ServerPlayer> players) {
      Component msg = Utils.string2Component(message);
      players.forEach(p -> p.m_213846_(msg));
   }

   private void showTitlesForPlayers(String title, String subTitle, Collection<ServerPlayer> players) {
      ClientboundSetTitlesAnimationPacket timesPacket = new ClientboundSetTitlesAnimationPacket(20, 60, 20);
      ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(Utils.string2Component(title));
      ClientboundSetSubtitleTextPacket subtitlePacket = new ClientboundSetSubtitleTextPacket(Utils.string2Component(subTitle));

      for (ServerPlayer player : players) {
         player.f_8906_.m_9829_(timesPacket);
         player.f_8906_.m_9829_(titlePacket);
         player.f_8906_.m_9829_(subtitlePacket);
      }
   }

   public BlockPos parseBlockPos(String posString) {
      if (posString != null && !posString.isEmpty()) {
         try {
            String[] parts = posString.trim().split("[,\\s]+");
            return parts.length != 3 ? null : new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
         } catch (Exception e) {
            return null;
         }
      } else {
         return null;
      }
   }

   public void addSpawnedEntity(Entity ent) {
      this.spawnedMobs.add(ent);
   }

   public int getTickCounter() {
      return this.tickCounter;
   }

   public void setTickCounter(int v) {
      this.tickCounter = v;
   }

   public int getCurrentWaveIndex() {
      return this.currentWaveIndex;
   }

   public void setCurrentWaveIndex(int v) {
      this.currentWaveIndex = v;
   }

   public boolean m_6469_(DamageSource source, float amount) {
      return Utils.isSystemicDamage(source) && super.m_6469_(source, amount);
   }

   public boolean m_6051_() {
      return false;
   }

   public boolean m_6128_() {
      return true;
   }

   public HumanoidArm m_5737_() {
      return null;
   }

   public Iterable<ItemStack> m_6168_() {
      return NonNullList.m_122780_(4, ItemStack.f_41583_);
   }

   public ItemStack m_6844_(EquipmentSlot slot) {
      return ItemStack.f_41583_;
   }

   public void m_8061_(EquipmentSlot slot, ItemStack stack) {
   }

   public enum RaidType {
      DEFENSE("defense"),
      SURVIVE("survive"),
      SWEEP("sweep"),
      SWEEP_BOSS_BOX("sweep_boss_box"),
      ACTIVATE("activate"),
      UNKNOWN("");

      private final String id;

      RaidType(String id) {
         this.id = id;
      }

      public static RaidControllerEntity.RaidType of(String id) {
         if (id == null) {
            return UNKNOWN;
         }

         for (RaidControllerEntity.RaidType type : values()) {
            if (type.id.equals(id)) {
               return type;
            }
         }

         return UNKNOWN;
      }

      public String getId() {
         return this.id;
      }
   }
}
