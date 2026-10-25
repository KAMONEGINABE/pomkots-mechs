package grcmcs.minecraft.mods.pomkotsmechs.block;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class EntitySpawnerBlockEntity extends BlockEntity {
   private String targetEntityTypeID = "";
   private String nbt = "";
   private float healthModifier = 1.0F;
   private float attackModifier = 1.0F;
   private int spawnIntervalDays = 1;
   private int maxDistance = 400;
   private boolean activateBoss = false;
   private long lastSpawnDay = -1L;
   private UUID spawnedEntityUUID = null;
   private RegistrySupplier<EntityType<?>> cachedEntityType = null;
   private Entity cachedEntity = null;
   private boolean hasCheckedInitialSpawn = false;

   public EntitySpawnerBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.ENTITY_SPAWNER_BLOCK_ENTITY.get(), pos, state);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, EntitySpawnerBlockEntity be) {
      if (!level.f_46443_) {
         if (!Utils.isMapEditingMode(level)) {
            if (!be.targetEntityTypeID.isEmpty()) {
               ServerLevel serverLevel = (ServerLevel)level;
               if (!be.hasCheckedInitialSpawn) {
                  be.hasCheckedInitialSpawn = true;
                  if (be.lastSpawnDay == -1L) {
                     be.trySpawn(serverLevel);
                     return;
                  }
               }

               if (level.m_46467_() % 200L == 0L) {
                  be.trySpawn(serverLevel);
               }
            }
         }
      }
   }

   private void trySpawn(ServerLevel level) {
      if (this.cachedEntityType == null) {
         this.cachedEntityType = this.getEntityType(this.targetEntityTypeID);
         if (this.cachedEntityType == null) {
            PomkotsMechs.LOGGER.warn("Unknown entity type: {}", this.targetEntityTypeID);
            return;
         }
      }

      long currentDay = level.m_46468_() / 24000L;
      if (this.lastSpawnDay < 0L || currentDay >= this.lastSpawnDay + this.spawnIntervalDays) {
         if (!this.isSpawnedEntityValid(level)) {
            this.spawnEntity(level, currentDay);
         }
      }
   }

   private boolean isSpawnedEntityValid(ServerLevel level) {
      if (this.spawnedEntityUUID == null) {
         this.cachedEntity = null;
         return false;
      }

      if (this.cachedEntity == null || this.cachedEntity.m_213877_()) {
         this.cachedEntity = level.m_8791_(this.spawnedEntityUUID);
      }

      if (this.cachedEntity == null) {
         this.spawnedEntityUUID = null;
         this.cachedEntity = null;
         return false;
      } else if (!this.cachedEntity.m_6084_()) {
         this.spawnedEntityUUID = null;
         this.cachedEntity = null;
         return false;
      } else {
         double distSqr = this.cachedEntity.m_20275_(this.f_58858_.m_123341_() + 0.5, this.f_58858_.m_123342_() + 0.5, this.f_58858_.m_123343_() + 0.5);
         if (distSqr > this.maxDistance * this.maxDistance) {
            this.spawnedEntityUUID = null;
            this.cachedEntity = null;
            return false;
         } else {
            return true;
         }
      }
   }

   private void spawnEntity(ServerLevel level, long currentDay) {
      Entity entity = ((EntityType)this.cachedEntityType.get()).m_20615_(level);
      if (entity == null) {
         PomkotsMechs.LOGGER.error("Failed to create entity: {}", this.targetEntityTypeID);
      } else {
         entity.m_7678_(
            this.f_58858_.m_123341_() + 0.5, this.f_58858_.m_123342_() + 1.0, this.f_58858_.m_123343_() + 0.5, level.f_46441_.m_188501_() * 360.0F, 0.0F
         );
         level.m_7967_(entity);
         if (entity instanceof GenericPomkotsMonster monster) {
            if (this.nbt != null && !this.nbt.isEmpty()) {
               try {
                  CompoundTag nbtTag = TagParser.m_129359_(this.nbt);
                  monster.m_7378_(nbtTag);
               } catch (CommandSyntaxException e) {
                  PomkotsMechs.LOGGER.error("Failed to parse NBT: {}", this.nbt, e);
               }
            }

            monster.setModifiers(this.healthModifier, this.attackModifier);
            if (entity instanceof BaseBossEntity boss) {
               if (!this.activateBoss) {
                  boss.setActivated(false);
                  boss.setAiMode(-2);
               }
            } else if (entity instanceof BaseSmallMonsterEntity small) {
               small.m_21530_();
            }
         }

         this.spawnedEntityUUID = entity.m_20148_();
         this.cachedEntity = entity;
         this.lastSpawnDay = currentDay;
         this.m_6596_();
         PomkotsMechs.LOGGER.info("Spawned entity {} at {} (day: {})", new Object[]{this.targetEntityTypeID, this.f_58858_, currentDay});
      }
   }

   private RegistrySupplier<EntityType<?>> getEntityType(String typeId) {
      try {
         ResourceLocation rl = new ResourceLocation(typeId);

         for (RegistrySupplier<EntityType<?>> entityType : PomkotsMechs.ENTITIES) {
            if (entityType.getId().equals(rl)) {
               return entityType;
            }
         }
      } catch (Exception e) {
         PomkotsMechs.LOGGER.error("Invalid entity type ID: {}", typeId, e);
      }

      return null;
   }

   public void m_142466_(CompoundTag tag) {
      super.m_142466_(tag);
      this.targetEntityTypeID = tag.m_128461_(PomkotsMechs.nbtName("TargetEntityTypeID"));
      this.nbt = tag.m_128461_(PomkotsMechs.nbtName("EntityTag"));
      this.healthModifier = tag.m_128457_(PomkotsMechs.nbtName("HealthModifier"));
      this.attackModifier = tag.m_128457_(PomkotsMechs.nbtName("AttackModifier"));
      this.spawnIntervalDays = tag.m_128451_(PomkotsMechs.nbtName("SpawnIntervalDays"));
      this.maxDistance = tag.m_128451_(PomkotsMechs.nbtName("MaxDistance"));
      this.lastSpawnDay = tag.m_128454_(PomkotsMechs.nbtName("LastSpawnDay"));
      if (tag.m_128441_(PomkotsMechs.nbtName("ActivateBoss"))) {
         this.activateBoss = tag.m_128471_(PomkotsMechs.nbtName("ActivateBoss"));
      } else {
         this.activateBoss = false;
      }

      if (tag.m_128403_(PomkotsMechs.nbtName("SpawnedEntityUUID"))) {
         this.spawnedEntityUUID = tag.m_128342_(PomkotsMechs.nbtName("SpawnedEntityUUID"));
      }

      if (this.spawnIntervalDays <= 0) {
         this.spawnIntervalDays = 1;
      }

      if (this.maxDistance <= 0) {
         this.maxDistance = 400;
      }

      if (this.healthModifier == 0.0F) {
         this.healthModifier = 1.0F;
      }

      if (this.attackModifier == 0.0F) {
         this.attackModifier = 1.0F;
      }

      this.cachedEntityType = null;
      this.cachedEntity = null;
      this.hasCheckedInitialSpawn = false;
   }

   protected void m_183515_(CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128359_(PomkotsMechs.nbtName("TargetEntityTypeID"), this.targetEntityTypeID);
      tag.m_128359_(PomkotsMechs.nbtName("EntityTag"), this.nbt);
      tag.m_128350_(PomkotsMechs.nbtName("HealthModifier"), this.healthModifier);
      tag.m_128350_(PomkotsMechs.nbtName("AttackModifier"), this.attackModifier);
      tag.m_128405_(PomkotsMechs.nbtName("SpawnIntervalDays"), this.spawnIntervalDays);
      tag.m_128405_(PomkotsMechs.nbtName("MaxDistance"), this.maxDistance);
      tag.m_128356_(PomkotsMechs.nbtName("LastSpawnDay"), this.lastSpawnDay);
      tag.m_128379_(PomkotsMechs.nbtName("ActivateBoss"), this.activateBoss);
      if (this.spawnedEntityUUID != null) {
         tag.m_128362_(PomkotsMechs.nbtName("SpawnedEntityUUID"), this.spawnedEntityUUID);
      }
   }

   public void setTargetEntityTypeID(String targetEntityTypeID) {
      this.targetEntityTypeID = targetEntityTypeID;
      this.cachedEntityType = null;
      this.m_6596_();
   }

   public void setNbt(String nbt) {
      this.nbt = nbt;
      this.m_6596_();
   }

   public void setHealthModifier(float healthModifier) {
      this.healthModifier = healthModifier;
      this.m_6596_();
   }

   public void setAttackModifier(float attackModifier) {
      this.attackModifier = attackModifier;
      this.m_6596_();
   }

   public void setSpawnIntervalDays(int spawnIntervalDays) {
      this.spawnIntervalDays = Math.max(1, spawnIntervalDays);
      this.m_6596_();
   }

   public void setMaxDistance(int maxDistance) {
      this.maxDistance = Math.max(1, maxDistance);
      this.m_6596_();
   }

   public void resetSpawnState() {
      this.lastSpawnDay = -1L;
      this.spawnedEntityUUID = null;
      this.cachedEntity = null;
      this.hasCheckedInitialSpawn = false;
      this.m_6596_();
   }

   public String getTargetEntityTypeID() {
      return this.targetEntityTypeID;
   }

   public String getNbt() {
      return this.nbt;
   }

   public float getHealthModifier() {
      return this.healthModifier;
   }

   public float getAttackModifier() {
      return this.attackModifier;
   }

   public int getSpawnIntervalDays() {
      return this.spawnIntervalDays;
   }

   public int getMaxDistance() {
      return this.maxDistance;
   }

   public long getLastSpawnDay() {
      return this.lastSpawnDay;
   }

   public Entity getSpawnedEntity() {
      return this.cachedEntity;
   }
}
