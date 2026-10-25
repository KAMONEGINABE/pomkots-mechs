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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class RaidControllerEntityBack extends LivingEntity {
   private static final int MAX_LIVING_TICK = 120000;
   private RaidDefinition raidDefinition;
   private LivingEntity raidTargetEntity;
   private String startTargetRaidName = null;
   private UUID raidOwnerUUID = null;
   private Entity raidOwner = null;
   private String raidFinalizeCommandSuccess = null;
   private String raidFinalizeCommandFail = null;
   private String currentRaidName = null;
   private UUID raidTargetEntityUUID = null;
   private int currentWaveIndex = -1;
   private int tickCounter = -1;
   private List<Entity> spawnedMobs = new ArrayList<>();
   private ListTag spawnedMobsTag;
   private int spawnedMobsNum = -1;
   private boolean endEventWhenKilledAllMobs = false;
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
      tag.putString(PomkotsMechs.nbtName("StartTargetRaidName"), startTargetRaidName);
      if (raidOwnerUUID != null) {
         tag.putUUID(PomkotsMechs.nbtName("RaidOwnerUUID"), raidOwnerUUID);
      }

      if (raidFinalizeCommandSuccess != null) {
         tag.putString(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"), raidFinalizeCommandSuccess);
      }

      if (raidFinalizeCommandFail != null) {
         tag.putString(PomkotsMechs.nbtName("RaidFinalizeCommandFail"), raidFinalizeCommandFail);
      }

      return tag;
   }

   public static List<BaseBossEntity> getInactiveBossesAroundPos(Level level, BlockPos bPos) {
      return level.getEntitiesOfClass(
            BaseBossEntity.class,
            new AABB(bPos.getX(), bPos.getY(), bPos.getZ(), bPos.getX() + 1, bPos.getY() + 1, bPos.getZ() + 1).inflate(40.0)
         )
         .stream()
         .filter(b -> b.getAiMode() == -2)
         .toList();
   }

   public static List<Player> getPlayersAroundPos(Level level, BlockPos bPos) {
      return level.getEntitiesOfClass(
         Player.class,
         new AABB(bPos.getX(), bPos.getY(), bPos.getZ(), bPos.getX() + 1, bPos.getY() + 1, bPos.getZ() + 1).inflate(100.0)
      );
   }

   public RaidControllerEntityBack(EntityType<? extends LivingEntity> entityType, Level world) {
      this(entityType, world, null);
   }

   public RaidControllerEntityBack(EntityType<? extends LivingEntity> entityType, Level world, BaseBossEntity parent) {
      super(entityType, world);
      this.noPhysics = true;
      this.setNoGravity(true);
      this.raidDefinition = null;
      this.raidTargetEntity = null;
      this.timeBar = new ServerBossEvent(Component.literal("Raid Time"), BossBarColor.BLUE, BossBarOverlay.PROGRESS);
      this.baseHpBar = new ServerBossEvent(Component.literal("Target HP"), BossBarColor.GREEN, BossBarOverlay.PROGRESS);
      this.initBossBars();
   }

   private void initBossBars() {
      this.timeBar.setProgress(1.0F);
      this.baseHpBar.setProgress(1.0F);
      this.setBossBarVisibility(false);
   }

   private void setBossBarVisibility(boolean bl) {
      this.baseHpBar.setVisible(bl);
      this.timeBar.setVisible(bl);
   }

   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      if (tag.contains(PomkotsMechs.nbtName("StartTargetRaidName"))) {
         this.startTargetRaidName = tag.getString(PomkotsMechs.nbtName("StartTargetRaidName"));
      }

      if (tag.contains(PomkotsMechs.nbtName("RaidOwnerUUID"))) {
         this.raidOwnerUUID = tag.getUUID(PomkotsMechs.nbtName("RaidOwnerUUID"));
      }

      if (tag.contains(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"))) {
         this.raidFinalizeCommandSuccess = tag.getString(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"));
      }

      if (tag.contains(PomkotsMechs.nbtName("RaidFinalizeCommandFail"))) {
         this.raidFinalizeCommandFail = tag.getString(PomkotsMechs.nbtName("RaidFinalizeCommandFail"));
      }

      if (tag.contains(PomkotsMechs.nbtName("CurrentRaidName"))) {
         this.currentRaidName = tag.getString(PomkotsMechs.nbtName("CurrentRaidName"));
      }

      if (tag.contains(PomkotsMechs.nbtName("RaidTargetEntityUUID"))) {
         this.raidTargetEntityUUID = tag.getUUID(PomkotsMechs.nbtName("RaidTargetEntityUUID"));
      }

      if (tag.contains(PomkotsMechs.nbtName("CurrentWaveIndex"))) {
         this.currentWaveIndex = tag.getInt(PomkotsMechs.nbtName("CurrentWaveIndex"));
      }

      if (tag.contains(PomkotsMechs.nbtName("TickCounter"))) {
         this.tickCounter = tag.getInt(PomkotsMechs.nbtName("TickCounter"));
      }

      if (tag.contains(PomkotsMechs.nbtName("EndEventWhenKilledAllMobs"))) {
         this.endEventWhenKilledAllMobs = tag.getBoolean(PomkotsMechs.nbtName("EndEventWhenKilledAllMobs"));
      }

      if (tag.contains(PomkotsMechs.nbtName("SpawnedMobsNum"))) {
         this.spawnedMobsNum = tag.getInt(PomkotsMechs.nbtName("SpawnedMobsNum"));
      }

      this.spawnedMobsTag = (ListTag)tag.get(PomkotsMechs.nbtName("SpawnedMobs"));
   }

   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      if (this.startTargetRaidName != null) {
         tag.putString(PomkotsMechs.nbtName("StartTargetRaidName"), this.startTargetRaidName);
      } else {
         tag.remove(PomkotsMechs.nbtName("StartTargetRaidName"));
      }

      if (this.raidOwnerUUID != null) {
         tag.putUUID(PomkotsMechs.nbtName("RaidOwnerUUID"), this.raidOwnerUUID);
      } else {
         tag.remove(PomkotsMechs.nbtName("RaidOwnerUUID"));
      }

      if (this.raidFinalizeCommandSuccess != null) {
         tag.putString(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"), this.raidFinalizeCommandSuccess);
      } else {
         tag.remove(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"));
      }

      if (this.raidFinalizeCommandFail != null) {
         tag.putString(PomkotsMechs.nbtName("RaidFinalizeCommandFail"), this.raidFinalizeCommandFail);
      } else {
         tag.remove(PomkotsMechs.nbtName("RaidFinalizeCommandFail"));
      }

      if (this.currentRaidName != null) {
         tag.putString(PomkotsMechs.nbtName("CurrentRaidName"), this.currentRaidName);
      } else {
         tag.remove(PomkotsMechs.nbtName("CurrentRaidName"));
      }

      if (this.raidTargetEntityUUID != null) {
         tag.putUUID(PomkotsMechs.nbtName("RaidTargetEntityUUID"), this.raidTargetEntityUUID);
      } else {
         tag.remove(PomkotsMechs.nbtName("RaidTargetEntityUUID"));
      }

      if (this.currentWaveIndex != -1) {
         tag.putInt(PomkotsMechs.nbtName("CurrentWaveIndex"), this.currentWaveIndex);
      } else {
         tag.remove(PomkotsMechs.nbtName("CurrentWaveIndex"));
      }

      if (this.tickCounter != -1) {
         tag.putInt(PomkotsMechs.nbtName("TickCounter"), this.tickCounter);
      } else {
         tag.remove(PomkotsMechs.nbtName("TickCounter"));
      }

      if (this.endEventWhenKilledAllMobs) {
         tag.putBoolean(PomkotsMechs.nbtName("EndEventWhenKilledAllMobs"), this.endEventWhenKilledAllMobs);
      } else {
         tag.remove(PomkotsMechs.nbtName("EndEventWhenKilledAllMobs"));
      }

      ListTag list = new ListTag();

      for (Entity ent : this.spawnedMobs) {
         list.add(NbtUtils.createUUID(ent.getUUID()));
      }

      tag.put(PomkotsMechs.nbtName("SpawnedMobs"), list);
      if (this.spawnedMobsNum != -1) {
         tag.putInt(PomkotsMechs.nbtName("SpawnedMobsNum"), this.spawnedMobsNum);
      } else {
         tag.remove(PomkotsMechs.nbtName("SpawnedMobsNum"));
      }
   }

   private void loadSpawnedMobs() {
      if (this.spawnedMobsTag != null) {
         this.spawnedMobs.clear();
         if (this.spawnedMobsTag != null) {
            for (Tag t : this.spawnedMobsTag) {
               UUID uuid = NbtUtils.loadUUID(t);
               Entity ent = ((ServerLevel)this.level()).getEntity(uuid);
               if (ent != null) {
                  this.spawnedMobs.add(ent);
               }
            }
         }
      }
   }

   public void tick() {
      if (this.firstTick && !this.level().isClientSide) {
         this.loadSpawnedMobs();
      }

      super.tick();
      if (!this.level().isClientSide) {
         if (this.tickCount > 120000) {
            PomkotsMechs.LOGGER.info("MAX Tick Count exceeded:" + this);
            this.discard();
         }

         ServerLevel serverLevel = (ServerLevel)this.level();
         if (!this.spawnedMobs.isEmpty()) {
            this.spawnedMobs.removeIf(e -> e == null || !e.isAlive());
         }

         if (this.startTargetRaidName != null) {
            this.raidDefinition = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(this.startTargetRaidName);
            if (this.raidDefinition == null) {
               PomkotsMechs.LOGGER.error("Raid def not found:" + this.startTargetRaidName);
               this.discard();
               return;
            }

            this.currentRaidName = this.startTargetRaidName;
            this.tickCounter = 0;
            this.currentWaveIndex = 0;
            if (!this.baseHpBar.getPlayers().isEmpty()) {
               for (ServerPlayer player : this.baseHpBar.getPlayers()) {
                  this.startBGMforPlayer(player, this.raidDefinition);
               }
            }

            if ("defense".equals(this.raidDefinition.type)) {
               RaidObjectiveEntity roe = (RaidObjectiveEntity)((EntityType)PomkotsMechs.RAID_OBJECTIVE.get()).create(serverLevel);
               if (roe != null) {
                  BlockPos pos = this.blockPosition();
                  roe.setPos(pos.getX(), pos.getY(), pos.getZ());
                  this.level().addFreshEntity(roe);
                  this.raidTargetEntity = roe;
                  this.raidTargetEntityUUID = this.raidTargetEntity.getUUID();
               }
            } else if ("survive".equals(this.raidDefinition.type)) {
               this.raidTargetEntity = (LivingEntity)serverLevel.getEntity(this.raidOwnerUUID);
               this.raidTargetEntityUUID = this.raidOwnerUUID;
            } else if ("sweep".equals(this.raidDefinition.type)) {
               this.raidTargetEntity = this;
               this.raidTargetEntityUUID = this.getUUID();
            } else if ("activate".equals(this.raidDefinition.type)) {
               this.raidTargetEntity = this;
               this.raidTargetEntityUUID = this.getUUID();
               List<BaseBossEntity> bosses = getInactiveBossesAroundPos(this.level(), this.blockPosition());
               List<Player> players = getPlayersAroundPos(this.level(), this.blockPosition());

               for (BaseBossEntity boss : bosses) {
                  boss.boot();

                  for (Player player : players) {
                     boss.addHateToEntity(player, 50.0F);
                  }

                  this.spawnedMobs.add(boss);
               }
            }

            this.startTargetRaidName = null;
         }

         if (this.raidOwnerUUID == null) {
            this.raidOwner = null;
         } else {
            this.raidOwner = serverLevel.getEntity(this.raidOwnerUUID);
         }

         if (this.currentRaidName != null) {
            if (this.raidTargetEntity == null) {
               this.raidTargetEntity = (LivingEntity)serverLevel.getEntity(this.raidTargetEntityUUID);
               this.raidDefinition = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(this.currentRaidName);
            }

            if (this.raidDefinition != null && this.raidTargetEntity != null) {
               WaveDefinition wave = this.raidDefinition.waves.get(this.currentWaveIndex);
               if (this.tickCounter == 5) {
                  this.showTitlesForPlayers(wave.title, wave.sub_title, this.baseHpBar.getPlayers());
               }

               this.tickCounter++;
               if (!this.baseHpBar.isVisible()) {
                  this.setBossBarVisibility(true);
               }

               int[] remainMaxMobs = null;
               if ("sweep".equals(this.raidDefinition.type)) {
                  remainMaxMobs = this.remainingSpawnTargets(this.raidDefinition, this.currentWaveIndex, this.tickCounter);
               }

               this.updateBossBars(wave, remainMaxMobs);
               if (this.raidOwner != null && this.raidOwner.isAlive()) {
                  if (!"defense".equals(this.raidDefinition.type) && !"survive".equals(this.raidDefinition.type)) {
                     if ("sweep".equals(this.raidDefinition.type)) {
                        if (remainMaxMobs == null) {
                           this.endRaid(false);
                        } else if (this.spawnedMobs.isEmpty() && remainMaxMobs[0] <= 0) {
                           if (this.isFinalWave(this.raidDefinition, this.currentWaveIndex)) {
                              this.endRaid(true);
                           } else {
                              this.currentWaveIndex++;
                              this.tickCounter = 0;
                           }

                           return;
                        }
                     } else if ("activate".equals(this.raidDefinition.type) && this.spawnedMobs.isEmpty()) {
                        this.endRaid(true);
                     }
                  } else if (this.raidTargetEntity == null || !this.raidTargetEntity.isAlive()) {
                     this.endRaid(false);
                     return;
                  }

                  if (this.raidDefinition != null && "survive".equals(this.raidDefinition.type)) {
                     this.setPos(this.raidTargetEntity.position());
                  }

                  if (this.endEventWhenKilledAllMobs) {
                     boolean end = true;

                     for (Entity ent : this.spawnedMobs) {
                        if (!(ent instanceof Pmc01Entity pmc01)) {
                           end = false;
                           break;
                        }

                        if (!"".equals(pmc01.containerType)) {
                           end = false;
                           break;
                        }
                     }

                     if (end) {
                        this.tickCounter = wave.duration_ticks;
                     }
                  }

                  this.fireEvent(wave);
                  if (this.tickCounter >= wave.duration_ticks) {
                     if ("defense".equals(this.raidDefinition.type) || "survive".equals(this.raidDefinition.type)) {
                        this.currentWaveIndex++;
                        this.tickCounter = 0;
                        this.killAllMobs();
                        if (this.currentWaveIndex >= this.raidDefinition.waves.size()) {
                           this.endRaid(true);
                        }
                     } else if ("sweep".equals(this.raidDefinition.type) || "activate".equals(this.raidDefinition.type)) {
                        this.killAllMobs();
                        this.endRaid(false);
                     }
                  }
               } else {
                  this.endRaid(false, "レイドオーナーが消失");
               }
            }
         }
      }
   }

   private boolean isFinalWave(RaidDefinition raidDef, int currentWaveIndex) {
      return raidDef.waves == null ? true : raidDef.waves.size() == currentWaveIndex + 1;
   }

   private int[] remainingSpawnTargets(RaidDefinition raidDef, int currentWaveIndex, int tickCounter) {
      if (raidDef.waves != null && raidDef.waves.size() > currentWaveIndex) {
         WaveDefinition eventDef = raidDef.waves.get(currentWaveIndex);
         int[] res = new int[]{0, 0};
         if (eventDef.events != null) {
            for (EventDefinition event : eventDef.events) {
               if (event.trigger_tick >= tickCounter) {
                  if ("spawn".equals(event.type)) {
                     res[0] += event.spawn_targets.size();
                  } else if ("spawn_random".equals(event.type)) {
                     res[0] += event.amount;
                  }
               }

               if ("spawn".equals(event.type)) {
                  res[1] += event.spawn_targets.size();
               } else if ("spawn_random".equals(event.type)) {
                  res[1] += event.amount;
               }
            }
         }

         return res;
      } else {
         return null;
      }
   }

   public void remove(RemovalReason removalReason) {
      if (!this.level().isClientSide && (removalReason == RemovalReason.DISCARDED || removalReason == RemovalReason.KILLED)) {
         this.setBossBarVisibility(false);
         if (this.raidTargetEntity != null && this.raidTargetEntity.isAlive() && this.raidTargetEntity instanceof RaidObjectiveEntity) {
            this.raidTargetEntity.discard();
         }

         this.killAllMobs();
      }

      super.remove(removalReason);
   }

   private void killAllMobs() {
      if (this.level() instanceof ServerLevel sl) {
         for (Entity ent : this.spawnedMobs) {
            if (ent != null && ent.isAlive()) {
               ent.kill();
            }
         }

         this.spawnedMobs.clear();
      }
   }

   public void startSeenByPlayer(ServerPlayer player) {
      this.baseHpBar.addPlayer(player);
      this.timeBar.addPlayer(player);
      if (!this.firstTick) {
         this.startBGMforPlayer(player, this.raidDefinition);
      }
   }

   private void startBGMforPlayer(ServerPlayer player, RaidDefinition raidDefinition) {
      if (raidDefinition != null) {
         if ("defense".equals(raidDefinition.type)) {
            ServerBGMTracker.forceSetBGMState(player, BGMState.BATTLE_RAID);
         } else {
            ServerBGMTracker.forceSetBGMState(player, BGMState.BATTLE_BOSS);
         }
      }
   }

   public static Optional<SoundEvent> getSoundEvent(ResourceLocation id) {
      return BuiltInRegistries.SOUND_EVENT.getOptional(id);
   }

   public void stopSeenByPlayer(ServerPlayer player) {
      this.baseHpBar.removePlayer(player);
      this.timeBar.removePlayer(player);
      ServerBGMTracker.forceUnsetBGMState(player);
   }

   private void sendMessage(String message, Collection<ServerPlayer> players) {
      for (ServerPlayer player : players) {
         player.sendSystemMessage(Component.literal(message));
      }
   }

   private void showTitlesForPlayers(String title, String subTitle, Collection<ServerPlayer> players) {
      ClientboundSetTitlesAnimationPacket timesPacket = new ClientboundSetTitlesAnimationPacket(20, 60, 20);
      ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(Component.literal(title));
      ClientboundSetSubtitleTextPacket subtitlePacket = new ClientboundSetSubtitleTextPacket(Component.literal(subTitle));

      for (ServerPlayer player : players) {
         player.connection.send(timesPacket);
         player.connection.send(titlePacket);
         player.connection.send(subtitlePacket);
      }
   }

   private void updateBossBars(WaveDefinition wave, int[] remainingMobs) {
      if (this.raidTargetEntity != null) {
         int remaining = wave.duration_ticks - this.tickCounter;
         if (remaining != this.prevRemaining) {
            float timeProgress = (float)remaining / wave.duration_ticks;
            this.timeBar.setName(Component.literal(wave.title + " | Time: " + remaining / 20 + "sec"));
            this.timeBar.setProgress(Math.max(0.0F, Math.min(1.0F, timeProgress)));
            if ("sweep".equals(this.raidDefinition.type) && remainingMobs != null) {
               float remainProgress = (float)(remainingMobs[0] + this.spawnedMobs.size()) / remainingMobs[1];
               this.baseHpBar.setName(Component.literal("Target Monsters: " + (remainingMobs[0] + this.spawnedMobs.size()) + "/" + remainingMobs[1]));
               this.baseHpBar.setProgress(Math.max(0.0F, Math.min(1.0F, remainProgress)));
            } else if (!"activate".equals(this.raidDefinition.type) || this.spawnedMobs == null) {
               float healthProgress = this.raidTargetEntity.getHealth() / this.raidTargetEntity.getMaxHealth();
               this.baseHpBar.setName(Component.literal("Target HP: " + (int)this.raidTargetEntity.getHealth() + "/" + (int)this.raidTargetEntity.getMaxHealth()));
               this.baseHpBar.setProgress(Math.max(0.0F, Math.min(1.0F, healthProgress)));
            }

            this.prevRemaining = remaining;
         }
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
            RegistrySupplier<EntityType<?>> type = this.getEntityType(st.mob_type);
            if (type != null && st.position != null) {
               BlockPos pos = this.parseBlockPos(st.position);
               if (pos != null && this.level() != null) {
                  Entity entity = ((EntityType)type.get()).create(this.level());
                  if (entity != null) {
                     BlockPos offset = this.blockPosition();
                     entity.setPos(offset.getX() + pos.getX(), offset.getY() + pos.getY(), offset.getZ() + pos.getZ());
                     if (st.snbt != null && entity instanceof LivingEntity le) {
                        try {
                           le.readAdditionalSaveData(TagParser.parseTag(st.snbt));
                        } catch (Exception e) {
                           PomkotsMechs.LOGGER.error("failed to parse snbt.", e);
                        }
                     }

                     if ("defense".equals(this.raidDefinition.type)) {
                        if (entity instanceof Mob mons) {
                           mons.setTarget(this.raidTargetEntity);
                        }

                        if (entity instanceof GenericPomkotsMonster pomkots) {
                           pomkots.setInRaid(true);
                           pomkots.setInEvent(true);
                        }
                     } else if (("survive".equals(this.raidDefinition.type) || "sweep".equals(this.raidDefinition.type)) && entity instanceof Mob mons) {
                        if (entity instanceof GenericPomkotsMonster pomkots) {
                           pomkots.setInEvent(true);
                        }

                        if (((ServerLevel)this.level()).getEntity(this.raidOwnerUUID) instanceof LivingEntity le) {
                           mons.setTarget(le);
                        }
                     }

                     if (entity instanceof Pmc01Entity pmc01) {
                        pmc01.raidControllerEntityUUID = this.getUUID();
                     } else if (entity instanceof Mob mob) {
                        mob.setPersistenceRequired();
                     }

                     this.spawnedMobs.add(entity);
                     this.level().addFreshEntity(entity);
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
         int rangex = event.range_x;
         int rangez = event.range_z;
         BlockPos centerPos = this.blockPosition();
         LivingEntity opener = this.getTargetCandidate();
         int spawned = 0;

         for (int i = 0; i < 100 && spawned < count; i++) {
            int x = centerPos.getX() + rand.nextInt(rangex) - rangex / 2;
            int z = centerPos.getZ() + rand.nextInt(rangez) - rangez / 2;
            int y = this.findSpawnY(level, x, centerPos.getY(), z, new AABB(0.0, 0.0, 0.0, 10.0, 10.0, 10.0));
            Entity mob = this.getRandomMobType(rand, mobTypeList).create(level);
            if (mob != null) {
               mob.moveTo(x + 0.5, y, z + 0.5, rand.nextFloat() * 360.0F, 0.0F);
               if (mob instanceof Mob m) {
                  if (opener != null) {
                     m.setTarget(opener);
                  }

                  if (m instanceof BaseSmallMonsterEntity small) {
                     small.setPersistenceRequired();
                     small.setInEvent(true);
                  }
               }

               level.addFreshEntity(mob);
               spawned++;
               this.spawnedMobs.add(mob);
            }
         }
      }
   }

   private int findSpawnY(Level level, int x, int startY, int z, AABB spaceSize) {
      int maxY = startY + 50;
      int res = Integer.MIN_VALUE;

      for (int y = startY; y < maxY; y++) {
         AABB checkBox = spaceSize.move(x - spaceSize.minX, y - spaceSize.minY, z - spaceSize.minZ);
         if (level.noCollision(checkBox)) {
            res = y;
         }
      }

      return res == Integer.MIN_VALUE ? level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z) : res;
   }

   private LivingEntity getTargetCandidate() {
      if (this.raidOwnerUUID == null) {
         return null;
      } else {
         return ((ServerLevel)this.level()).getEntity(this.raidOwnerUUID) instanceof LivingEntity r ? r : null;
      }
   }

   private EntityType[] getMobList(EventDefinition event) {
      List<EntityType<? extends Entity>> list = new ArrayList<>();

      for (SpawnTarget ent : event.spawn_targets) {
         RegistrySupplier<EntityType<?>> s = Utils.getEntityType(ent.mob_type);
         if (s != null) {
            list.add((EntityType<? extends Entity>)s.get());
         }
      }

      return list.toArray(new EntityType[0]);
   }

   private EntityType<? extends Entity> getRandomMobType(Random rand, EntityType<? extends Entity>[] mobList) {
      int max = mobList.length;
      return mobList[Math.abs(rand.nextInt()) % max];
   }

   public void addSpawnedEntity(Entity ent) {
      this.spawnedMobs.add(ent);
   }

   public BlockPos parseBlockPos(String posString) {
      if (posString != null && !posString.isEmpty()) {
         try {
            String[] parts = posString.trim().split("[,\\s]+");
            if (parts.length != 3) {
               return null;
            }

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            return new BlockPos(x, y, z);
         } catch (Exception e) {
            return null;
         }
      } else {
         return null;
      }
   }

   private RegistrySupplier<EntityType<?>> getEntityType(String typeId) {
      ResourceLocation rl = new ResourceLocation(typeId);
      RegistrySupplier<EntityType<?>> et = null;

      for (RegistrySupplier<EntityType<?>> entityType : PomkotsMechs.ENTITIES) {
         if (entityType.getId().equals(rl)) {
            et = entityType;
         }
      }

      return et;
   }

   private void endRaid(boolean victory) {
      this.endRaid(victory, "");
   }

   private void endRaid(boolean victory, String subTitle) {
      String finalizeCommand = null;
      if (victory) {
         this.showTitlesForPlayers("Mission Complete!!", subTitle, this.baseHpBar.getPlayers());
         finalizeCommand = this.raidFinalizeCommandSuccess;
      } else {
         this.showTitlesForPlayers("Mission Failed...", subTitle, this.baseHpBar.getPlayers());
         finalizeCommand = this.raidFinalizeCommandFail;
      }

      if (this.level() instanceof ServerLevel serverLevel
         && this.raidOwnerUUID != null
         && finalizeCommand != null
         && serverLevel.getPlayerByUUID(this.raidOwnerUUID) instanceof ServerPlayer serverPlayer) {
         this.runCommandAsOwner(finalizeCommand, serverPlayer, serverLevel);
      }

      this.resetRaid();
      this.discard();
   }

   private void runCommandAsOwner(String command, ServerPlayer serverPlayer, ServerLevel serverLevel) {
      MinecraftServer server = serverLevel.getServer();
      CommandSourceStack source = serverPlayer.createCommandSourceStack();
      server.getCommands().performPrefixedCommand(source, command);
   }

   private void resetRaid() {
      this.raidDefinition = null;
      if (this.raidTargetEntity != null && this.raidTargetEntity instanceof RaidObjectiveEntity) {
         this.raidTargetEntity.discard();
      }

      this.raidTargetEntity = null;
      this.startTargetRaidName = null;
      this.raidOwnerUUID = null;
      this.raidOwner = null;
      this.raidFinalizeCommandSuccess = null;
      this.raidFinalizeCommandFail = null;
      this.currentRaidName = null;
      this.raidTargetEntityUUID = null;
      this.currentWaveIndex = -1;
      this.tickCounter = -1;
      this.endEventWhenKilledAllMobs = false;
      this.setBossBarVisibility(false);
   }

   public int getTickCounter() {
      return this.tickCounter;
   }

   public void setTickCounter(int tickCounter) {
      this.tickCounter = tickCounter;
   }

   public int getCurrentWaveIndex() {
      return this.currentWaveIndex;
   }

   public void setCurrentWaveIndex(int currentWaveIndex) {
      this.currentWaveIndex = currentWaveIndex;
   }

   public boolean hurt(DamageSource source, float amount) {
      return Utils.isSystemicDamage(source) ? super.hurt(source, amount) : false;
   }

   public boolean displayFireAnimation() {
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

   public boolean ignoreExplosion() {
      return true;
   }

   public HumanoidArm getMainArm() {
      return null;
   }
}
