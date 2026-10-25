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

   public void m_183515_(CompoundTag tag) {
      tag.m_128405_(PomkotsMechs.nbtName("MaxMobs"), this.maxMobs);
      tag.m_128405_(PomkotsMechs.nbtName("SpawnInterval"), this.spawnInterval);
      tag.m_128405_(PomkotsMechs.nbtName("MaxHops"), this.maxHops);
      tag.m_128347_(PomkotsMechs.nbtName("SpawnSpaceX"), this.spawnSpace.f_82291_ - this.spawnSpace.f_82288_);
      tag.m_128347_(PomkotsMechs.nbtName("SpawnSpaceY"), this.spawnSpace.f_82292_ - this.spawnSpace.f_82289_);
      tag.m_128347_(PomkotsMechs.nbtName("SpawnSpaceZ"), this.spawnSpace.f_82293_ - this.spawnSpace.f_82290_);
      ListTag mobList = new ListTag();

      for (WeightedList.Entry<ResourceLocation> entry : this.mobTypes.getEntries()) {
         CompoundTag mobTag = new CompoundTag();
         mobTag.m_128359_("type", entry.instance().toString());
         mobTag.m_128405_("weight", entry.weight());
         mobList.add(mobTag);
      }

      tag.m_128365_(PomkotsMechs.nbtName("MobTypes"), mobList);
   }

   public void m_142466_(CompoundTag tag) {
      this.maxMobs = tag.m_128451_(PomkotsMechs.nbtName("MaxMobs"));
      this.spawnInterval = tag.m_128451_(PomkotsMechs.nbtName("SpawnInterval"));
      this.maxHops = tag.m_128451_(PomkotsMechs.nbtName("MaxHops"));
      double sx = tag.m_128459_(PomkotsMechs.nbtName("SpawnSpaceX"));
      double sy = tag.m_128459_(PomkotsMechs.nbtName("SpawnSpaceY"));
      double sz = tag.m_128459_(PomkotsMechs.nbtName("SpawnSpaceZ"));
      this.spawnSpace = new AABB(0.0, 0.0, 0.0, sx, sy, sz);
      this.mobTypes = new WeightedList<>();

      for (Tag t : tag.m_128437_(PomkotsMechs.nbtName("MobTypes"), 10)) {
         CompoundTag mobTag = (CompoundTag)t;
         ResourceLocation rl = new ResourceLocation(mobTag.m_128461_("type"));
         int weight = mobTag.m_128451_("weight");
         this.mobTypes.addObject(weight, rl);
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, CustomSpawnerBlockEntity be) {
      if (level instanceof ServerLevel serverLevel) {
         if (!Utils.isMapEditingMode(level)) {
            be.spawnedMobs.entrySet().removeIf(e -> e.getValue() == null || !e.getValue().m_6084_());
            if (be.spawnedMobs.size() < be.maxMobs) {
               if (level.m_46467_() % be.spawnInterval == 0L) {
                  if (!be.mobTypes.isEmpty()) {
                     be.findSpawnPos(serverLevel, pos, RandomSource.m_216327_()).ifPresent(spawnPos -> {
                        ResourceLocation mobType = be.mobTypes.getRandomObject(serverLevel.m_213780_());
                        EntityType<?> entityType = (EntityType<?>)BuiltInRegistries.f_256780_.m_6612_(mobType).orElse(null);
                        if (entityType == null) {
                           PomkotsMechs.LOGGER.error("Unknown entity type: {}", mobType);
                        } else {
                           Entity mob = entityType.m_20615_(serverLevel);
                           if (mob != null) {
                              mob.m_6027_(spawnPos.m_123341_() + 0.5, spawnPos.m_123342_(), spawnPos.m_123343_() + 0.5);
                              serverLevel.m_7967_(mob);
                              be.spawnedMobs.put(mob.m_20148_(), mob);
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
         double angle = random.m_188500_() * Math.PI * 2.0;
         double dist = random.m_188500_() * this.maxHops;
         double dx = Math.cos(angle) * dist;
         double dz = Math.sin(angle) * dist;
         double dy = random.m_188500_() * 8.0;
         BlockPos candidate = origin.m_7918_((int)dx, (int)dy, (int)dz);
         if (candidate.m_123342_() >= origin.m_123342_()) {
            BlockHitResult hit = level.m_151353_(
               new ClipBlockStateContext(
                  Vec3.m_82512_(candidate), Vec3.m_82512_(origin), blockState -> !blockState.m_60795_() && !blockState.equals(level.m_8055_(origin))
               )
            );
            if (hit.m_6662_().equals(Type.MISS)) {
               AABB checkBox = this.spawnSpace
                  .m_82386_(
                     candidate.m_123341_() - this.spawnSpace.f_82288_,
                     candidate.m_123342_() - this.spawnSpace.f_82289_,
                     candidate.m_123343_() - this.spawnSpace.f_82290_
                  );
               if (level.m_45772_(checkBox)) {
                  return Optional.of(candidate);
               }
            }
         }
      }

      return Optional.of(origin);
   }
}
