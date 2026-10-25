package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import grcmcs.minecraft.mods.pomkotsmechs.util.WeightedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class CustomSpawnerBlockEntity extends BlockEntity {
   private int maxMobs = 4;
   private int spawnInterval = 600;
   private int maxHops = 10;
   private AABB spawnSpace = new AABB(0.0, 0.0, 0.0, 2.0, 2.0, 2.0);
   private WeightedList<ResourceLocation> mobTypes = new WeightedList<>();
   private final Map<UUID, Entity> spawnedMobs = new HashMap<>();

   public CustomSpawnerBlockEntity(BlockPos blockPos, BlockState blockState) {
      super((BlockEntityType)PomkotsMechs.CUSTOM_SPAWNER_BLOCK_ENTITY.get(), blockPos, blockState);
   }

   public void saveAdditional(CompoundTag tag) {
      tag.putInt(PomkotsMechs.nbtName("MaxMobs"), this.maxMobs);
      tag.putInt(PomkotsMechs.nbtName("SpawnInterval"), this.spawnInterval);
      tag.putInt(PomkotsMechs.nbtName("MaxHops"), this.maxHops);
      tag.putDouble(PomkotsMechs.nbtName("SpawnSpaceX"), this.spawnSpace.maxX - this.spawnSpace.minX);
      tag.putDouble(PomkotsMechs.nbtName("SpawnSpaceY"), this.spawnSpace.maxY - this.spawnSpace.minY);
      tag.putDouble(PomkotsMechs.nbtName("SpawnSpaceZ"), this.spawnSpace.maxZ - this.spawnSpace.minZ);
      ListTag mobList = new ListTag();

      for (WeightedList.Entry<ResourceLocation> entry : this.mobTypes.getEntries()) {
         CompoundTag mobTag = new CompoundTag();
         mobTag.putString("type", entry.instance().toString());
         mobTag.putInt("weight", entry.weight());
         mobList.add(mobTag);
      }

      tag.put(PomkotsMechs.nbtName("MobTypes"), mobList);
   }

   public void load(CompoundTag tag) {
      this.maxMobs = tag.getInt(PomkotsMechs.nbtName("MaxMobs"));
      this.spawnInterval = tag.getInt(PomkotsMechs.nbtName("SpawnInterval"));
      this.maxHops = tag.getInt(PomkotsMechs.nbtName("MaxHops"));
      double sx = tag.getDouble(PomkotsMechs.nbtName("SpawnSpaceX"));
      double sy = tag.getDouble(PomkotsMechs.nbtName("SpawnSpaceY"));
      double sz = tag.getDouble(PomkotsMechs.nbtName("SpawnSpaceZ"));
      this.spawnSpace = new AABB(0.0, 0.0, 0.0, sx, sy, sz);
      this.mobTypes = new WeightedList<>();

      for (Tag t : tag.getList(PomkotsMechs.nbtName("MobTypes"), 10)) {
         CompoundTag mobTag = (CompoundTag)t;
         ResourceLocation rl = new ResourceLocation(mobTag.getString("type"));
         int weight = mobTag.getInt("weight");
         this.mobTypes.addObject(weight, rl);
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, CustomSpawnerBlockEntity be) {
      if (level instanceof ServerLevel serverLevel) {
         if (!Utils.isMapEditingMode(level)) {
            be.spawnedMobs.entrySet().removeIf(e -> e.getValue() == null || !e.getValue().isAlive());
            if (be.spawnedMobs.size() < be.maxMobs) {
               if (level.getGameTime() % be.spawnInterval == 0L) {
                  if (!be.mobTypes.isEmpty()) {
                     be.findSpawnPos(serverLevel, pos, RandomSource.create()).ifPresent(spawnPos -> {
                        ResourceLocation mobType = be.mobTypes.getRandomObject(serverLevel.getRandom());
                        EntityType<?> entityType = (EntityType<?>)BuiltInRegistries.ENTITY_TYPE.getOptional(mobType).orElse(null);
                        if (entityType == null) {
                           PomkotsMechs.LOGGER.error("Unknown entity type: {}", mobType);
                        } else {
                           Entity mob = entityType.create(serverLevel);
                           if (mob != null) {
                              mob.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                              serverLevel.addFreshEntity(mob);
                              be.spawnedMobs.put(mob.getUUID(), mob);
                           }
                        }
                     });
                  }
               }
            }
         }
      }
   }

   private Optional<BlockPos> findSpawnPos(ServerLevel level, BlockPos origin, RandomSource random) {
      for (int attempt = 0; attempt < 20; attempt++) {
         double angle = random.nextDouble() * Math.PI * 2.0;
         double dist = random.nextDouble() * this.maxHops;
         double dx = Math.cos(angle) * dist;
         double dz = Math.sin(angle) * dist;
         double dy = random.nextDouble() * 8.0;
         BlockPos candidate = origin.offset((int)dx, (int)dy, (int)dz);
         if (candidate.getY() >= origin.getY()) {
            BlockHitResult hit = level.isBlockInLine(
               new ClipBlockStateContext(
                  Vec3.atCenterOf(candidate), Vec3.atCenterOf(origin), blockState -> !blockState.isAir() && !blockState.equals(level.getBlockState(origin))
               )
            );
            if (hit.getType().equals(Type.MISS)) {
               AABB checkBox = this.spawnSpace
                  .move(
                     candidate.getX() - this.spawnSpace.minX,
                     candidate.getY() - this.spawnSpace.minY,
                     candidate.getZ() - this.spawnSpace.minZ
                  );
               if (level.noCollision(checkBox)) {
                  return Optional.of(candidate);
               }
            }
         }
      }

      return Optional.of(origin);
   }
}
