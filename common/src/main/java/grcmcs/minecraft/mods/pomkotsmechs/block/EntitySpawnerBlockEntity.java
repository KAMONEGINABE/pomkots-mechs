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
      if (!level.isClientSide) {
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

               if (level.getGameTime() % 200L == 0L) {
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

      long currentDay = level.getDayTime() / 24000L;
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

      if (this.cachedEntity == null || this.cachedEntity.isRemoved()) {
         this.cachedEntity = level.getEntity(this.spawnedEntityUUID);
      }

      if (this.cachedEntity == null) {
         this.spawnedEntityUUID = null;
         this.cachedEntity = null;
         return false;
      } else if (!this.cachedEntity.isAlive()) {
         this.spawnedEntityUUID = null;
         this.cachedEntity = null;
         return false;
      } else {
         double distSqr = this.cachedEntity.distanceToSqr(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5, this.worldPosition.getZ() + 0.5);
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
      Entity entity = ((EntityType)this.cachedEntityType.get()).create(level);
      if (entity == null) {
         PomkotsMechs.LOGGER.error("Failed to create entity: {}", this.targetEntityTypeID);
      } else {
         entity.moveTo(
            this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0, this.worldPosition.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F
         );
         level.addFreshEntity(entity);
         if (entity instanceof GenericPomkotsMonster monster) {
            if (this.nbt != null && !this.nbt.isEmpty()) {
               try {
                  CompoundTag nbtTag = TagParser.parseTag(this.nbt);
                  monster.readAdditionalSaveData(nbtTag);
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
               small.setPersistenceRequired();
            }
         }

         this.spawnedEntityUUID = entity.getUUID();
         this.cachedEntity = entity;
         this.lastSpawnDay = currentDay;
         this.setChanged();
         PomkotsMechs.LOGGER.info("Spawned entity {} at {} (day: {})", new Object[]{this.targetEntityTypeID, this.worldPosition, currentDay});
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

   public void load(CompoundTag tag) {
      super.load(tag);
      this.targetEntityTypeID = tag.getString(PomkotsMechs.nbtName("TargetEntityTypeID"));
      this.nbt = tag.getString(PomkotsMechs.nbtName("EntityTag"));
      this.healthModifier = tag.getFloat(PomkotsMechs.nbtName("HealthModifier"));
      this.attackModifier = tag.getFloat(PomkotsMechs.nbtName("AttackModifier"));
      this.spawnIntervalDays = tag.getInt(PomkotsMechs.nbtName("SpawnIntervalDays"));
      this.maxDistance = tag.getInt(PomkotsMechs.nbtName("MaxDistance"));
      this.lastSpawnDay = tag.getLong(PomkotsMechs.nbtName("LastSpawnDay"));
      if (tag.contains(PomkotsMechs.nbtName("ActivateBoss"))) {
         this.activateBoss = tag.getBoolean(PomkotsMechs.nbtName("ActivateBoss"));
      } else {
         this.activateBoss = false;
      }

      if (tag.hasUUID(PomkotsMechs.nbtName("SpawnedEntityUUID"))) {
         this.spawnedEntityUUID = tag.getUUID(PomkotsMechs.nbtName("SpawnedEntityUUID"));
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

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putString(PomkotsMechs.nbtName("TargetEntityTypeID"), this.targetEntityTypeID);
      tag.putString(PomkotsMechs.nbtName("EntityTag"), this.nbt);
      tag.putFloat(PomkotsMechs.nbtName("HealthModifier"), this.healthModifier);
      tag.putFloat(PomkotsMechs.nbtName("AttackModifier"), this.attackModifier);
      tag.putInt(PomkotsMechs.nbtName("SpawnIntervalDays"), this.spawnIntervalDays);
      tag.putInt(PomkotsMechs.nbtName("MaxDistance"), this.maxDistance);
      tag.putLong(PomkotsMechs.nbtName("LastSpawnDay"), this.lastSpawnDay);
      tag.putBoolean(PomkotsMechs.nbtName("ActivateBoss"), this.activateBoss);
      if (this.spawnedEntityUUID != null) {
         tag.putUUID(PomkotsMechs.nbtName("SpawnedEntityUUID"), this.spawnedEntityUUID);
      }
   }

   public void setTargetEntityTypeID(String targetEntityTypeID) {
      this.targetEntityTypeID = targetEntityTypeID;
      this.cachedEntityType = null;
      this.setChanged();
   }

   public void setNbt(String nbt) {
      this.nbt = nbt;
      this.setChanged();
   }

   public void setHealthModifier(float healthModifier) {
      this.healthModifier = healthModifier;
      this.setChanged();
   }

   public void setAttackModifier(float attackModifier) {
      this.attackModifier = attackModifier;
      this.setChanged();
   }

   public void setSpawnIntervalDays(int spawnIntervalDays) {
      this.spawnIntervalDays = Math.max(1, spawnIntervalDays);
      this.setChanged();
   }

   public void setMaxDistance(int maxDistance) {
      this.maxDistance = Math.max(1, maxDistance);
      this.setChanged();
   }

   public void resetSpawnState() {
      this.lastSpawnDay = -1L;
      this.spawnedEntityUUID = null;
      this.cachedEntity = null;
      this.hasCheckedInitialSpawn = false;
      this.setChanged();
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
