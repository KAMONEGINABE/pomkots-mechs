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
      tag.m_128359_(PomkotsMechs.nbtName("StartTargetRaidName"), startTargetRaidName);
      if (raidOwnerUUID != null) {
         tag.m_128362_(PomkotsMechs.nbtName("RaidOwnerUUID"), raidOwnerUUID);
      }

      if (raidFinalizeCommandSuccess != null) {
         tag.m_128359_(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"), raidFinalizeCommandSuccess);
      }

      if (raidFinalizeCommandFail != null) {
         tag.m_128359_(PomkotsMechs.nbtName("RaidFinalizeCommandFail"), raidFinalizeCommandFail);
      }

      return tag;
   }

   public static List<BaseBossEntity> getInactiveBossesAroundPos(Level level, BlockPos bPos) {
      return level.m_45976_(
            BaseBossEntity.class,
            new AABB(bPos.m_123341_(), bPos.m_123342_(), bPos.m_123343_(), bPos.m_123341_() + 1, bPos.m_123342_() + 1, bPos.m_123343_() + 1).m_82400_(40.0)
         )
         .stream()
         .filter(b -> b.getAiMode() == -2)
         .toList();
   }

   public static List<Player> getPlayersAroundPos(Level level, BlockPos bPos) {
      return level.m_45976_(
         Player.class,
         new AABB(bPos.m_123341_(), bPos.m_123342_(), bPos.m_123343_(), bPos.m_123341_() + 1, bPos.m_123342_() + 1, bPos.m_123343_() + 1).m_82400_(100.0)
      );
   }

   public RaidControllerEntityBack(EntityType<? extends LivingEntity> entityType, Level world) {
      this(entityType, world, null);
   }

   public RaidControllerEntityBack(EntityType<? extends LivingEntity> entityType, Level world, BaseBossEntity parent) {
      super(entityType, world);
      this.f_19794_ = true;
      this.m_20242_(true);
      this.raidDefinition = null;
      this.raidTargetEntity = null;
      this.timeBar = new ServerBossEvent(Component.m_237113_("Raid Time"), BossBarColor.BLUE, BossBarOverlay.PROGRESS);
      this.baseHpBar = new ServerBossEvent(Component.m_237113_("Target HP"), BossBarColor.GREEN, BossBarOverlay.PROGRESS);
      this.initBossBars();
   }

   private void initBossBars() {
      this.timeBar.m_142711_(1.0F);
      this.baseHpBar.m_142711_(1.0F);
      this.setBossBarVisibility(false);
   }

   private void setBossBarVisibility(boolean bl) {
      this.baseHpBar.m_8321_(bl);
      this.timeBar.m_8321_(bl);
   }

   public void m_7378_(CompoundTag tag) {
      super.m_7378_(tag);
      if (tag.m_128441_(PomkotsMechs.nbtName("StartTargetRaidName"))) {
         this.startTargetRaidName = tag.m_128461_(PomkotsMechs.nbtName("StartTargetRaidName"));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("RaidOwnerUUID"))) {
         this.raidOwnerUUID = tag.m_128342_(PomkotsMechs.nbtName("RaidOwnerUUID"));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"))) {
         this.raidFinalizeCommandSuccess = tag.m_128461_(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("RaidFinalizeCommandFail"))) {
         this.raidFinalizeCommandFail = tag.m_128461_(PomkotsMechs.nbtName("RaidFinalizeCommandFail"));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("CurrentRaidName"))) {
         this.currentRaidName = tag.m_128461_(PomkotsMechs.nbtName("CurrentRaidName"));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("RaidTargetEntityUUID"))) {
         this.raidTargetEntityUUID = tag.m_128342_(PomkotsMechs.nbtName("RaidTargetEntityUUID"));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("CurrentWaveIndex"))) {
         this.currentWaveIndex = tag.m_128451_(PomkotsMechs.nbtName("CurrentWaveIndex"));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("TickCounter"))) {
         this.tickCounter = tag.m_128451_(PomkotsMechs.nbtName("TickCounter"));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("EndEventWhenKilledAllMobs"))) {
         this.endEventWhenKilledAllMobs = tag.m_128471_(PomkotsMechs.nbtName("EndEventWhenKilledAllMobs"));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("SpawnedMobsNum"))) {
         this.spawnedMobsNum = tag.m_128451_(PomkotsMechs.nbtName("SpawnedMobsNum"));
      }

      this.spawnedMobsTag = (ListTag)tag.m_128423_(PomkotsMechs.nbtName("SpawnedMobs"));
   }

   public void m_7380_(CompoundTag tag) {
      super.m_7380_(tag);
      if (this.startTargetRaidName != null) {
         tag.m_128359_(PomkotsMechs.nbtName("StartTargetRaidName"), this.startTargetRaidName);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("StartTargetRaidName"));
      }

      if (this.raidOwnerUUID != null) {
         tag.m_128362_(PomkotsMechs.nbtName("RaidOwnerUUID"), this.raidOwnerUUID);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("RaidOwnerUUID"));
      }

      if (this.raidFinalizeCommandSuccess != null) {
         tag.m_128359_(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"), this.raidFinalizeCommandSuccess);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"));
      }

      if (this.raidFinalizeCommandFail != null) {
         tag.m_128359_(PomkotsMechs.nbtName("RaidFinalizeCommandFail"), this.raidFinalizeCommandFail);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("RaidFinalizeCommandFail"));
      }

      if (this.currentRaidName != null) {
         tag.m_128359_(PomkotsMechs.nbtName("CurrentRaidName"), this.currentRaidName);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("CurrentRaidName"));
      }

      if (this.raidTargetEntityUUID != null) {
         tag.m_128362_(PomkotsMechs.nbtName("RaidTargetEntityUUID"), this.raidTargetEntityUUID);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("RaidTargetEntityUUID"));
      }

      if (this.currentWaveIndex != -1) {
         tag.m_128405_(PomkotsMechs.nbtName("CurrentWaveIndex"), this.currentWaveIndex);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("CurrentWaveIndex"));
      }

      if (this.tickCounter != -1) {
         tag.m_128405_(PomkotsMechs.nbtName("TickCounter"), this.tickCounter);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("TickCounter"));
      }

      if (this.endEventWhenKilledAllMobs) {
         tag.m_128379_(PomkotsMechs.nbtName("EndEventWhenKilledAllMobs"), this.endEventWhenKilledAllMobs);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("EndEventWhenKilledAllMobs"));
      }

      ListTag list = new ListTag();

      for (Entity ent : this.spawnedMobs) {
         list.add(NbtUtils.m_129226_(ent.m_20148_()));
      }

      tag.m_128365_(PomkotsMechs.nbtName("SpawnedMobs"), list);
      if (this.spawnedMobsNum != -1) {
         tag.m_128405_(PomkotsMechs.nbtName("SpawnedMobsNum"), this.spawnedMobsNum);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("SpawnedMobsNum"));
      }
   }

   private void loadSpawnedMobs() {
      if (this.spawnedMobsTag != null) {
         this.spawnedMobs.clear();
         if (this.spawnedMobsTag != null) {
            for (Tag t : this.spawnedMobsTag) {
               UUID uuid = NbtUtils.m_129233_(t);
               Entity ent = ((ServerLevel)this.m_9236_()).m_8791_(uuid);
               if (ent != null) {
                  this.spawnedMobs.add(ent);
               }
            }
         }
      }
   }

   public void m_8119_() {
      if (this.f_19803_ && !this.m_9236_().f_46443_) {
         this.loadSpawnedMobs();
      }

      super.m_8119_();
      if (!this.m_9236_().f_46443_) {
         if (this.f_19797_ > 120000) {
            PomkotsMechs.LOGGER.info("MAX Tick Count exceeded:" + this);
            this.m_146870_();
         }

         ServerLevel serverLevel = (ServerLevel)this.m_9236_();
         if (!this.spawnedMobs.isEmpty()) {
            this.spawnedMobs.removeIf(e -> e == null || !e.m_6084_());
         }

         if (this.startTargetRaidName != null) {
            this.raidDefinition = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(this.startTargetRaidName);
            if (this.raidDefinition == null) {
               PomkotsMechs.LOGGER.error("Raid def not found:" + this.startTargetRaidName);
               this.m_146870_();
               return;
            }

            this.currentRaidName = this.startTargetRaidName;
            this.tickCounter = 0;
            this.currentWaveIndex = 0;
            if (!this.baseHpBar.m_8324_().isEmpty()) {
               for (ServerPlayer player : this.baseHpBar.m_8324_()) {
                  this.startBGMforPlayer(player, this.raidDefinition);
               }
            }

            if ("defense".equals(this.raidDefinition.type)) {
               RaidObjectiveEntity roe = (RaidObjectiveEntity)((EntityType)PomkotsMechs.RAID_OBJECTIVE.get()).m_20615_(serverLevel);
               if (roe != null) {
                  BlockPos pos = this.m_20183_();
                  roe.m_6034_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
                  this.m_9236_().m_7967_(roe);
                  this.raidTargetEntity = roe;
                  this.raidTargetEntityUUID = this.raidTargetEntity.m_20148_();
               }
            } else if ("survive".equals(this.raidDefinition.type)) {
               this.raidTargetEntity = (LivingEntity)serverLevel.m_8791_(this.raidOwnerUUID);
               this.raidTargetEntityUUID = this.raidOwnerUUID;
            } else if ("sweep".equals(this.raidDefinition.type)) {
               this.raidTargetEntity = this;
               this.raidTargetEntityUUID = this.m_20148_();
            } else if ("activate".equals(this.raidDefinition.type)) {
               this.raidTargetEntity = this;
               this.raidTargetEntityUUID = this.m_20148_();
               List<BaseBossEntity> bosses = getInactiveBossesAroundPos(this.m_9236_(), this.m_20183_());
               List<Player> players = getPlayersAroundPos(this.m_9236_(), this.m_20183_());

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
            this.raidOwner = serverLevel.m_8791_(this.raidOwnerUUID);
         }

         if (this.currentRaidName != null) {
            if (this.raidTargetEntity == null) {
               this.raidTargetEntity = (LivingEntity)serverLevel.m_8791_(this.raidTargetEntityUUID);
               this.raidDefinition = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(this.currentRaidName);
            }

            if (this.raidDefinition != null && this.raidTargetEntity != null) {
               WaveDefinition wave = this.raidDefinition.waves.get(this.currentWaveIndex);
               if (this.tickCounter == 5) {
                  this.showTitlesForPlayers(wave.title, wave.sub_title, this.baseHpBar.m_8324_());
               }

               this.tickCounter++;
               if (!this.baseHpBar.m_8323_()) {
                  this.setBossBarVisibility(true);
               }

               int[] remainMaxMobs = null;
               if ("sweep".equals(this.raidDefinition.type)) {
                  remainMaxMobs = this.remainingSpawnTargets(this.raidDefinition, this.currentWaveIndex, this.tickCounter);
               }

               this.updateBossBars(wave, remainMaxMobs);
               if (this.raidOwner != null && this.raidOwner.m_6084_()) {
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
                  } else if (this.raidTargetEntity == null || !this.raidTargetEntity.m_6084_()) {
                     this.endRaid(false);
                     return;
                  }

                  if (this.raidDefinition != null && "survive".equals(this.raidDefinition.type)) {
                     this.m_146884_(this.raidTargetEntity.m_20182_());
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

   public void m_142687_(RemovalReason removalReason) {
      if (!this.m_9236_().f_46443_ && (removalReason == RemovalReason.DISCARDED || removalReason == RemovalReason.KILLED)) {
         this.setBossBarVisibility(false);
         if (this.raidTargetEntity != null && this.raidTargetEntity.m_6084_() && this.raidTargetEntity instanceof RaidObjectiveEntity) {
            this.raidTargetEntity.m_146870_();
         }

         this.killAllMobs();
      }

      super.m_142687_(removalReason);
   }

   private void killAllMobs() {
      if (this.m_9236_() instanceof ServerLevel sl) {
         for (Entity ent : this.spawnedMobs) {
            if (ent != null && ent.m_6084_()) {
               ent.m_6074_();
            }
         }

         this.spawnedMobs.clear();
      }
   }

   public void m_6457_(ServerPlayer player) {
      this.baseHpBar.m_6543_(player);
      this.timeBar.m_6543_(player);
      if (!this.f_19803_) {
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
      return BuiltInRegistries.f_256894_.m_6612_(id);
   }

   public void m_6452_(ServerPlayer player) {
      this.baseHpBar.m_6539_(player);
      this.timeBar.m_6539_(player);
      ServerBGMTracker.forceUnsetBGMState(player);
   }

   private void sendMessage(String message, Collection<ServerPlayer> players) {
      for (ServerPlayer player : players) {
         player.m_213846_(Component.m_237113_(message));
      }
   }

   private void showTitlesForPlayers(String title, String subTitle, Collection<ServerPlayer> players) {
      ClientboundSetTitlesAnimationPacket timesPacket = new ClientboundSetTitlesAnimationPacket(20, 60, 20);
      ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(Component.m_237113_(title));
      ClientboundSetSubtitleTextPacket subtitlePacket = new ClientboundSetSubtitleTextPacket(Component.m_237113_(subTitle));

      for (ServerPlayer player : players) {
         player.f_8906_.m_9829_(timesPacket);
         player.f_8906_.m_9829_(titlePacket);
         player.f_8906_.m_9829_(subtitlePacket);
      }
   }

   private void updateBossBars(WaveDefinition wave, int[] remainingMobs) {
      if (this.raidTargetEntity != null) {
         int remaining = wave.duration_ticks - this.tickCounter;
         if (remaining != this.prevRemaining) {
            float timeProgress = (float)remaining / wave.duration_ticks;
            this.timeBar.m_6456_(Component.m_237113_(wave.title + " | Time: " + remaining / 20 + "sec"));
            this.timeBar.m_142711_(Math.max(0.0F, Math.min(1.0F, timeProgress)));
            if ("sweep".equals(this.raidDefinition.type) && remainingMobs != null) {
               float remainProgress = (float)(remainingMobs[0] + this.spawnedMobs.size()) / remainingMobs[1];
               this.baseHpBar.m_6456_(Component.m_237113_("Target Monsters: " + (remainingMobs[0] + this.spawnedMobs.size()) + "/" + remainingMobs[1]));
               this.baseHpBar.m_142711_(Math.max(0.0F, Math.min(1.0F, remainProgress)));
            } else if (!"activate".equals(this.raidDefinition.type) || this.spawnedMobs == null) {
               float healthProgress = this.raidTargetEntity.m_21223_() / this.raidTargetEntity.m_21233_();
               this.baseHpBar.m_6456_(Component.m_237113_("Target HP: " + (int)this.raidTargetEntity.m_21223_() + "/" + (int)this.raidTargetEntity.m_21233_()));
               this.baseHpBar.m_142711_(Math.max(0.0F, Math.min(1.0F, healthProgress)));
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
               this.sendMessage(event.message, this.baseHpBar.m_8324_());
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
               if (pos != null && this.m_9236_() != null) {
                  Entity entity = ((EntityType)type.get()).m_20615_(this.m_9236_());
                  if (entity != null) {
                     BlockPos offset = this.m_20183_();
                     entity.m_6034_(offset.m_123341_() + pos.m_123341_(), offset.m_123342_() + pos.m_123342_(), offset.m_123343_() + pos.m_123343_());
                     if (st.snbt != null && entity instanceof LivingEntity le) {
                        try {
                           le.m_7378_(TagParser.m_129359_(st.snbt));
                        } catch (Exception e) {
                           PomkotsMechs.LOGGER.error("failed to parse snbt.", e);
                        }
                     }

                     if ("defense".equals(this.raidDefinition.type)) {
                        if (entity instanceof Mob mons) {
                           mons.m_6710_(this.raidTargetEntity);
                        }

                        if (entity instanceof GenericPomkotsMonster pomkots) {
                           pomkots.setInRaid(true);
                           pomkots.setInEvent(true);
                        }
                     } else if (("survive".equals(this.raidDefinition.type) || "sweep".equals(this.raidDefinition.type)) && entity instanceof Mob mons) {
                        if (entity instanceof GenericPomkotsMonster pomkots) {
                           pomkots.setInEvent(true);
                        }

                        if (((ServerLevel)this.m_9236_()).m_8791_(this.raidOwnerUUID) instanceof LivingEntity le) {
                           mons.m_6710_(le);
                        }
                     }

                     if (entity instanceof Pmc01Entity pmc01) {
                        pmc01.raidControllerEntityUUID = this.m_20148_();
                     } else if (entity instanceof Mob mob) {
                        mob.m_21530_();
                     }

                     this.spawnedMobs.add(entity);
                     this.m_9236_().m_7967_(entity);
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
         int rangex = event.range_x;
         int rangez = event.range_z;
         BlockPos centerPos = this.m_20183_();
         LivingEntity opener = this.getTargetCandidate();
         int spawned = 0;

         for (int i = 0; i < 100 && spawned < count; i++) {
            int x = centerPos.m_123341_() + rand.nextInt(rangex) - rangex / 2;
            int z = centerPos.m_123343_() + rand.nextInt(rangez) - rangez / 2;
            int y = this.findSpawnY(level, x, centerPos.m_123342_(), z, new AABB(0.0, 0.0, 0.0, 10.0, 10.0, 10.0));
            Entity mob = this.getRandomMobType(rand, mobTypeList).m_20615_(level);
            if (mob != null) {
               mob.m_7678_(x + 0.5, y, z + 0.5, rand.nextFloat() * 360.0F, 0.0F);
               if (mob instanceof Mob m) {
                  if (opener != null) {
                     m.m_6710_(opener);
                  }

                  if (m instanceof BaseSmallMonsterEntity small) {
                     small.m_21530_();
                     small.setInEvent(true);
                  }
               }

               level.m_7967_(mob);
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
         AABB checkBox = spaceSize.m_82386_(x - spaceSize.f_82288_, y - spaceSize.f_82289_, z - spaceSize.f_82290_);
         if (level.m_45772_(checkBox)) {
            res = y;
         }
      }

      return res == Integer.MIN_VALUE ? level.m_6924_(Types.MOTION_BLOCKING_NO_LEAVES, x, z) : res;
   }

   private LivingEntity getTargetCandidate() {
      if (this.raidOwnerUUID == null) {
         return null;
      } else {
         return ((ServerLevel)this.m_9236_()).m_8791_(this.raidOwnerUUID) instanceof LivingEntity r ? r : null;
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
         this.showTitlesForPlayers("Mission Complete!!", subTitle, this.baseHpBar.m_8324_());
         finalizeCommand = this.raidFinalizeCommandSuccess;
      } else {
         this.showTitlesForPlayers("Mission Failed...", subTitle, this.baseHpBar.m_8324_());
         finalizeCommand = this.raidFinalizeCommandFail;
      }

      if (this.m_9236_() instanceof ServerLevel serverLevel
         && this.raidOwnerUUID != null
         && finalizeCommand != null
         && serverLevel.m_46003_(this.raidOwnerUUID) instanceof ServerPlayer serverPlayer) {
         this.runCommandAsOwner(finalizeCommand, serverPlayer, serverLevel);
      }

      this.resetRaid();
      this.m_146870_();
   }

   private void runCommandAsOwner(String command, ServerPlayer serverPlayer, ServerLevel serverLevel) {
      MinecraftServer server = serverLevel.m_7654_();
      CommandSourceStack source = serverPlayer.m_20203_();
      server.m_129892_().m_230957_(source, command);
   }

   private void resetRaid() {
      this.raidDefinition = null;
      if (this.raidTargetEntity != null && this.raidTargetEntity instanceof RaidObjectiveEntity) {
         this.raidTargetEntity.m_146870_();
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

   public boolean m_6469_(DamageSource source, float amount) {
      return Utils.isSystemicDamage(source) ? super.m_6469_(source, amount) : false;
   }

   public boolean m_6051_() {
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

   public boolean m_6128_() {
      return true;
   }

   public HumanoidArm m_5737_() {
      return null;
   }
}
