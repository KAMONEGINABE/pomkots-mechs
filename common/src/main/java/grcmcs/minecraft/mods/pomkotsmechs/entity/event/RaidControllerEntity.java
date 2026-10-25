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
      return LivingEntity.createLivingAttributes()
         .add(Attributes.ATTACK_KNOCKBACK)
         .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
         .add(Attributes.MAX_HEALTH, 1000.0)
         .add(Attributes.ARMOR, 20.0)
         .add(Attributes.ARMOR_TOUGHNESS, 1.0);
   }

   @NotNull
   public static CompoundTag buildCompoundTag(String startTargetRaidName, UUID raidOwnerUUID, String raidFinalizeCommandSuccess, String raidFinalizeCommandFail) {
      CompoundTag tag = new CompoundTag();
      tag.putString(nbt("StartTargetRaidName"), startTargetRaidName);
      if (raidOwnerUUID != null) {
         tag.putUUID(nbt("RaidOwnerUUID"), raidOwnerUUID);
      }

      if (raidFinalizeCommandSuccess != null) {
         tag.putString(nbt("RaidFinalizeCommandSuccess"), raidFinalizeCommandSuccess);
      }

      if (raidFinalizeCommandFail != null) {
         tag.putString(nbt("RaidFinalizeCommandFail"), raidFinalizeCommandFail);
      }

      return tag;
   }

   public static List<BaseBossEntity> getInactiveBossesAroundPos(Level level, BlockPos pos) {
      AABB area = AABB.unitCubeFromLowerCorner(pos.getCenter()).inflate(40.0);
      return level.getEntitiesOfClass(BaseBossEntity.class, area).stream().filter(b -> b.getAiMode() == -2).toList();
   }

   public static List<Player> getPlayersAroundPos(Level level, BlockPos pos) {
      AABB area = AABB.unitCubeFromLowerCorner(pos.getCenter()).inflate(100.0);
      return level.getEntitiesOfClass(Player.class, area);
   }

   public static Optional<SoundEvent> getSoundEvent(ResourceLocation id) {
      return BuiltInRegistries.SOUND_EVENT.getOptional(id);
   }

   public RaidControllerEntity(EntityType<? extends LivingEntity> entityType, Level world) {
      this(entityType, world, null);
   }

   public RaidControllerEntity(EntityType<? extends LivingEntity> entityType, Level world, BaseBossEntity parent) {
      super(entityType, world);
      this.noPhysics = true;
      this.setNoGravity(true);
      this.timeBar = new ServerBossEvent(Component.literal("Raid Time"), BossBarColor.BLUE, BossBarOverlay.PROGRESS);
      this.baseHpBar = new ServerBossEvent(Component.literal("Target HP"), BossBarColor.GREEN, BossBarOverlay.PROGRESS);
      this.timeBar.setProgress(1.0F);
      this.baseHpBar.setProgress(1.0F);
      this.setBossBarVisibility(false);
   }

   private static String nbt(String key) {
      return PomkotsMechs.nbtName(key);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.startTargetRaidName = readString(tag, "StartTargetRaidName");
      this.raidOwnerUUID = readUUID(tag, "RaidOwnerUUID");
      this.raidFinalizeCommandSuccess = readString(tag, "RaidFinalizeCommandSuccess");
      this.raidFinalizeCommandFail = readString(tag, "RaidFinalizeCommandFail");
      this.currentRaidName = readString(tag, "CurrentRaidName");
      this.raidTargetEntityUUID = readUUID(tag, "RaidTargetEntityUUID");
      this.currentWaveIndex = tag.contains(nbt("CurrentWaveIndex")) ? tag.getInt(nbt("CurrentWaveIndex")) : -1;
      this.tickCounter = tag.contains(nbt("TickCounter")) ? tag.getInt(nbt("TickCounter")) : -1;
      this.spawnedMobsNum = tag.contains(nbt("SpawnedMobsNum")) ? tag.getInt(nbt("SpawnedMobsNum")) : -1;
      this.endEventWhenKilledAllMobs = tag.contains(nbt("EndEventWhenKilledAllMobs")) && tag.getBoolean(nbt("EndEventWhenKilledAllMobs"));
      this.spawnedMobsTag = (ListTag)tag.get(nbt("SpawnedMobs"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
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
         tag.putBoolean(nbt("EndEventWhenKilledAllMobs"), true);
      } else {
         tag.remove(nbt("EndEventWhenKilledAllMobs"));
      }

      ListTag list = new ListTag();

      for (Entity ent : this.spawnedMobs) {
         list.add(NbtUtils.createUUID(ent.getUUID()));
      }

      tag.put(nbt("SpawnedMobs"), list);
   }

   private static String readString(CompoundTag tag, String key) {
      return tag.contains(nbt(key)) ? tag.getString(nbt(key)) : null;
   }

   private static UUID readUUID(CompoundTag tag, String key) {
      return tag.contains(nbt(key)) ? tag.getUUID(nbt(key)) : null;
   }

   private static void writeString(CompoundTag tag, String key, String value) {
      if (value != null) {
         tag.putString(nbt(key), value);
      } else {
         tag.remove(nbt(key));
      }
   }

   private static void writeUUID(CompoundTag tag, String key, UUID value) {
      if (value != null) {
         tag.putUUID(nbt(key), value);
      } else {
         tag.remove(nbt(key));
      }
   }

   private static void writeIntIfSet(CompoundTag tag, String key, int value) {
      if (value != -1) {
         tag.putInt(nbt(key), value);
      } else {
         tag.remove(nbt(key));
      }
   }

   public void tick() {
      if (this.firstTick && !this.level().isClientSide) {
         this.loadSpawnedMobs();
      }

      super.tick();
      if (!this.level().isClientSide) {
         if (this.tickCount > 120000) {
            PomkotsMechs.LOGGER.info("MAX Tick Count exceeded: {}", this);
            this.discard();
         } else {
            ServerLevel serverLevel = (ServerLevel)this.level();
            this.spawnedMobs.removeIf(e -> e == null || !e.isAlive());
            if (this.startTargetRaidName == null || this.initializeRaid(serverLevel)) {
               this.raidOwner = this.raidOwnerUUID != null ? serverLevel.getEntity(this.raidOwnerUUID) : null;
               if (this.currentRaidName != null) {
                  if (this.raidTargetEntity == null) {
                     this.raidTargetEntity = (LivingEntity)serverLevel.getEntity(this.raidTargetEntityUUID);
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
         this.discard();
         return false;
      }

      this.currentRaidName = this.startTargetRaidName;
      this.tickCounter = 0;
      this.currentWaveIndex = 0;

      for (ServerPlayer player : this.baseHpBar.getPlayers()) {
         this.startBGMforPlayer(player, this.raidDefinition);
      }

      this.setupRaidTarget(serverLevel);
      this.startTargetRaidName = null;
      return true;
   }

   private void setupRaidTarget(ServerLevel serverLevel) {
      switch (RaidControllerEntity.RaidType.of(this.raidDefinition.type)) {
         case DEFENSE:
            RaidObjectiveEntity roe = (RaidObjectiveEntity)((EntityType)PomkotsMechs.RAID_OBJECTIVE.get()).create(serverLevel);
            if (roe != null) {
               BlockPos pos = this.blockPosition();
               roe.setPos(pos.getX(), pos.getY(), pos.getZ());
               this.level().addFreshEntity(roe);
               this.raidTargetEntity = roe;
               this.raidTargetEntityUUID = roe.getUUID();
            }
            break;
         case SURVIVE:
            this.raidTargetEntity = (LivingEntity)serverLevel.getEntity(this.raidOwnerUUID);
            this.raidTargetEntityUUID = this.raidOwnerUUID;
            break;
         case SWEEP:
         case SWEEP_BOSS_BOX:
         case ACTIVATE:
            this.raidTargetEntity = this;
            this.raidTargetEntityUUID = this.getUUID();
            if (RaidControllerEntity.RaidType.of(this.raidDefinition.type) == RaidControllerEntity.RaidType.ACTIVATE) {
               List<BaseBossEntity> bosses = getInactiveBossesAroundPos(this.level(), this.blockPosition());
               List<Player> players = getPlayersAroundPos(this.level(), this.blockPosition());

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
         BlockPos pos = this.blockPosition();
         this.raidTargetEntity.setPos(pos.getX(), pos.getY(), pos.getZ());
      }

      if (this.tickCounter == 5) {
         this.showTitlesForPlayers(wave.title, wave.sub_title, this.baseHpBar.getPlayers());
      }

      this.tickCounter++;
      if (!this.baseHpBar.isVisible()) {
         this.setBossBarVisibility(true);
      }

      int[] remainMaxMobs = this.remainingSpawnTargets(this.raidDefinition, this.currentWaveIndex, this.tickCounter);
      this.updateBossBars(wave, remainMaxMobs);
      if (this.raidOwner == null || !this.raidOwner.isAlive()) {
         this.endRaid(false, "レイドオーナーが消失");
      } else if (!this.checkDefeat(type, remainMaxMobs)) {
         if (type == RaidControllerEntity.RaidType.SURVIVE) {
            this.setPos(this.raidTargetEntity.position());
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
            if (this.raidTargetEntity == null || !this.raidTargetEntity.isAlive()) {
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
      this.baseHpBar.setVisible(visible);
      this.timeBar.setVisible(visible);
   }

   private void updateBossBars(WaveDefinition wave, int[] remainingMobs) {
      if (this.raidTargetEntity != null) {
         int remaining = wave.duration_ticks - this.tickCounter;
         if (remaining != this.prevRemaining) {
            float timeProgress = (float)remaining / wave.duration_ticks;
            this.timeBar.setName(Component.literal(Utils.string2Component(wave.title).getString() + " | Time: " + remaining / 20 + "sec"));
            this.timeBar.setProgress(Mth.clamp(timeProgress, 0.0F, 1.0F));
            RaidControllerEntity.RaidType type = RaidControllerEntity.RaidType.of(this.raidDefinition.type);
            if ((
                  type == RaidControllerEntity.RaidType.SWEEP
                     || type == RaidControllerEntity.RaidType.SWEEP_BOSS_BOX
                     || type == RaidControllerEntity.RaidType.ACTIVATE
               )
               && remainingMobs != null) {
               int total = remainingMobs[0] + this.spawnedMobs.size();
               float prog = (float)total / remainingMobs[1];
               this.baseHpBar.setName(Component.literal("Target Monsters: " + total + "/" + remainingMobs[1]));
               this.baseHpBar.setProgress(Mth.clamp(prog, 0.0F, 1.0F));
            } else if (type != RaidControllerEntity.RaidType.ACTIVATE) {
               float hp = this.raidTargetEntity.getHealth() / this.raidTargetEntity.getMaxHealth();
               this.baseHpBar.setName(Component.literal("Target HP: " + (int)this.raidTargetEntity.getHealth() + "/" + (int)this.raidTargetEntity.getMaxHealth()));
               this.baseHpBar.setProgress(Mth.clamp(hp, 0.0F, 1.0F));
            }

            this.prevRemaining = remaining;
         }
      }
   }

   public void startSeenByPlayer(ServerPlayer player) {
      this.baseHpBar.addPlayer(player);
      this.timeBar.addPlayer(player);
      if (!this.firstTick) {
         this.startBGMforPlayer(player, this.raidDefinition);
      }
   }

   public void stopSeenByPlayer(ServerPlayer player) {
      this.baseHpBar.removePlayer(player);
      this.timeBar.removePlayer(player);
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
               this.sendMessage(event.message, this.baseHpBar.getPlayers());
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
                  if (pos != null && this.level() != null) {
                     Entity entity = ((EntityType)type.get()).create(this.level());
                     if (entity != null) {
                        BlockPos offset = this.blockPosition();
                        entity.setPos(offset.getX() + pos.getX(), offset.getY() + pos.getY(), offset.getZ() + pos.getZ());
                        this.applySnbt(entity, st.snbt);
                        this.setupSpawnedMob(entity);
                        this.spawnedMobs.add(entity);
                        this.level().addFreshEntity(entity);
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
         Level level = this.level();
         EntityType[] mobTypeList = this.getMobList(event);
         int count = event.amount;
         BlockPos center = this.blockPosition();
         LivingEntity opener = this.getTargetCandidate();
         AABB spawnSpace = new AABB(0.0, 0.0, 0.0, 2.0, 2.0, 2.0);
         int maxHops = (event.range_x + event.range_z) / 2;
         int spawned = 0;

         for (int i = 0; i < 100 && spawned < count; i++) {
            Optional<BlockPos> pos = this.findSpawnPos(maxHops, spawnSpace, (ServerLevel)level, center, this.random, event);
            if (pos.isPresent()) {
               Entity mob = this.getRandomMobType(rand, mobTypeList).create(level);
               if (mob != null) {
                  mob.moveTo(pos.get().getX() + 0.5, pos.get().getY(), pos.get().getZ() + 0.5, rand.nextFloat() * 360.0F, 0.0F);
                  if (mob instanceof Mob m) {
                     if (opener != null) {
                        m.setTarget(opener);
                     }

                     if (mob instanceof BaseSmallMonsterEntity small) {
                        small.setPersistenceRequired();
                        small.setInEvent(true);
                     }
                  }

                  level.addFreshEntity(mob);
                  this.spawnedMobs.add(mob);
                  spawned++;
               }
            }
         }
      }
   }

   private Optional<BlockPos> findSpawnPos(int maxHops, AABB spawnSpace, ServerLevel level, BlockPos origin, RandomSource random, EventDefinition event) {
      for (int attempt = 0; attempt < 20; attempt++) {
         double angle = random.nextDouble() * Math.PI * 2.0;
         double dist = random.nextDouble() * maxHops;
         double dx = Math.cos(angle) * dist;
         double dz = Math.sin(angle) * dist;
         double dy = random.nextDouble() * 8.0;
         BlockPos candidate = origin.offset((int)dx, (int)dy, (int)dz);
         if (!event.spawn_mobs_in_closed_area) {
            return Optional.of(candidate);
         }

         if (candidate.getY() >= origin.getY()) {
            BlockHitResult hit = level.isBlockInLine(
               new ClipBlockStateContext(
                  Vec3.atCenterOf(candidate), Vec3.atCenterOf(origin), blockState -> !blockState.isAir() && !blockState.equals(level.getBlockState(origin))
               )
            );
            if (hit.getType().equals(Type.MISS)) {
               AABB checkBox = spawnSpace.move(
                  candidate.getX() - spawnSpace.minX, candidate.getY() - spawnSpace.minY, candidate.getZ() - spawnSpace.minZ
               );
               if (level.noCollision(checkBox)) {
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
               mob.setTarget(this.raidTargetEntity);
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

               if (((ServerLevel)this.level()).getEntity(this.raidOwnerUUID) instanceof LivingEntity le) {
                  mob.setTarget(le);
               }
            }
      }

      if (entity instanceof Pmc01Entity pmc01) {
         pmc01.raidControllerEntity = this;
         pmc01.raidControllerEntityUUID = this.getUUID();
      } else if (entity instanceof Mob mob) {
         mob.setPersistenceRequired();
      }
   }

   private void applySnbt(Entity entity, String snbt) {
      if (snbt != null && entity instanceof LivingEntity le) {
         try {
            le.readAdditionalSaveData(TagParser.parseTag(snbt));
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
      this.showTitlesForPlayers(title, subTitle, this.baseHpBar.getPlayers());
      String finalizeCommand = victory ? this.raidFinalizeCommandSuccess : this.raidFinalizeCommandFail;
      if (this.level() instanceof ServerLevel serverLevel
         && this.raidOwnerUUID != null
         && finalizeCommand != null
         && serverLevel.getPlayerByUUID(this.raidOwnerUUID) instanceof ServerPlayer serverPlayer) {
         this.runCommandAsServer(finalizeCommand, serverPlayer, serverLevel);
      }

      this.resetRaid();
      this.discard();
   }

   private void resetRaid() {
      if (this.raidTargetEntity instanceof RaidObjectiveEntity) {
         this.raidTargetEntity.discard();
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
            if (ent != null && ent.isAlive()) {
               ent.kill();
            }
         }
      }

      this.spawnedMobs.clear();
   }

   public void remove(RemovalReason reason) {
      if (!this.level().isClientSide && (reason == RemovalReason.DISCARDED || reason == RemovalReason.KILLED)) {
         this.setBossBarVisibility(false);
         if (this.raidTargetEntity instanceof RaidObjectiveEntity && this.raidTargetEntity.isAlive()) {
            this.raidTargetEntity.discard();
         }

         this.killAllMobs();
      }

      super.remove(reason);
   }

   private void loadSpawnedMobs() {
      if (this.spawnedMobsTag != null) {
         this.spawnedMobs.clear();

         for (Tag t : this.spawnedMobsTag) {
            UUID uuid = NbtUtils.loadUUID(t);
            Entity ent = ((ServerLevel)this.level()).getEntity(uuid);
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
         return ((ServerLevel)this.level()).getEntity(this.raidOwnerUUID) instanceof LivingEntity le ? le : null;
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
         AABB checkBox = spaceSize.move(x - spaceSize.minX, y - spaceSize.minY, z - spaceSize.minZ);
         if (level.noCollision(checkBox)) {
            found = y;
            break;
         }
      }

      return found != Integer.MIN_VALUE ? found : level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
   }

   private void runCommandAsOwner(String command, ServerPlayer player, ServerLevel serverLevel) {
      MinecraftServer server = serverLevel.getServer();
      CommandSourceStack source = player.createCommandSourceStack().withPermission(4);
      server.getCommands().performPrefixedCommand(source, command);
   }

   private void runCommandAsServer(String command, ServerPlayer player, ServerLevel serverLevel) {
      MinecraftServer server = serverLevel.getServer();
      CommandSourceStack source = server.createCommandSourceStack().withPermission(4).withSuppressedOutput().withEntity(player).withPosition(player.position()).withLevel(player.serverLevel());
      server.getCommands().performPrefixedCommand(source, command);
   }

   private String replaceSelectorWithUUID(String command, ServerPlayer player) {
      String selector = "@e[uuid=" + player.getUUID() + "]";
      return command.replace("@s", selector);
   }

   private void sendMessage(String message, Collection<ServerPlayer> players) {
      Component msg = Utils.string2Component(message);
      players.forEach(p -> p.sendSystemMessage(msg));
   }

   private void showTitlesForPlayers(String title, String subTitle, Collection<ServerPlayer> players) {
      ClientboundSetTitlesAnimationPacket timesPacket = new ClientboundSetTitlesAnimationPacket(20, 60, 20);
      ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(Utils.string2Component(title));
      ClientboundSetSubtitleTextPacket subtitlePacket = new ClientboundSetSubtitleTextPacket(Utils.string2Component(subTitle));

      for (ServerPlayer player : players) {
         player.connection.send(timesPacket);
         player.connection.send(titlePacket);
         player.connection.send(subtitlePacket);
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

   public boolean hurt(DamageSource source, float amount) {
      return Utils.isSystemicDamage(source) && super.hurt(source, amount);
   }

   public boolean displayFireAnimation() {
      return false;
   }

   public boolean ignoreExplosion() {
      return true;
   }

   public HumanoidArm getMainArm() {
      return null;
   }

   public Iterable<ItemStack> getArmorSlots() {
      return NonNullList.withSize(4, ItemStack.EMPTY);
   }

   public ItemStack getItemBySlot(EquipmentSlot slot) {
      return ItemStack.EMPTY;
   }

   public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
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
