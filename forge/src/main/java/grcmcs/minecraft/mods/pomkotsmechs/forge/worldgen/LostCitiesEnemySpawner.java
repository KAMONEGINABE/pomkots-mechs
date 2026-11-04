package grcmcs.minecraft.mods.pomkotsmechs.forge.worldgen;

import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import mcjty.lostcities.api.LostCityEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LostCitiesEnemySpawner {

    private static final RegistrySupplier<?>[] LARGE_MECHS = new RegistrySupplier<?>[]{
            PomkotsMechs.PMB01,
            PomkotsMechs.PMB01MK2,
            PomkotsMechs.PMB02,
            PomkotsMechs.PMB03,
            PomkotsMechs.PMB04,
            PomkotsMechs.PMB05,
            PomkotsMechs.PMB06,
            PomkotsMechs.PMB07,
            PomkotsMechs.PMB08
    };

    private static final RegistrySupplier<?>[] MEDIUM_MECHS = new RegistrySupplier<?>[]{
            PomkotsMechs.PMS01,
            PomkotsMechs.PMS02,
            PomkotsMechs.PMS03,
            PomkotsMechs.PMS04,
            PomkotsMechs.PMS05,
            PomkotsMechs.PMS06,
            PomkotsMechs.PMS07,
            PomkotsMechs.PMS08,
            PomkotsMechs.PMS09,
            PomkotsMechs.PMS10
    };

    private static final RegistrySupplier<?>[] SMALL_MECHS = new RegistrySupplier<?>[]{
            PomkotsMechs.PMSS01,
            PomkotsMechs.PMSS02,
            PomkotsMechs.PMSS03
    };

    @SubscribeEvent
    public static void onPostGenCityChunk(LostCityEvent.PostGenCityChunkEvent event) {
        WorldGenLevel level = event.getWorld();
        if (level == null || level.isClientSide()) {
            return;
        }

        if (!PomkotsMechs.CONFIG.enableLostCityEnemySpawn) {
            return;
        }

        RandomSource random = level.getRandom();
        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();

        // 1. 大型機（PMB）の抽選 (都市3つに1体 ≒ 750チャンクに1体)
        float largeChance = PomkotsMechs.CONFIG.lostCityLargeMechSpawnChance;
        if (largeChance > 0.0F && random.nextFloat() < largeChance) {
            if (trySpawnLargeMech(level, chunkX, chunkZ, random)) {
                return;
            }
        }

        // 2. 中型機（PMS）の抽選 (都市1つに1〜2体 ≒ 250チャンクに1.5体)
        float mediumChance = PomkotsMechs.CONFIG.lostCityMediumMechSpawnChance;
        if (mediumChance > 0.0F && random.nextFloat() < mediumChance) {
            if (trySpawnMediumMech(level, chunkX, chunkZ, random)) {
                return;
            }
        }

        // 3. 小型機（PMSS）ペアの抽選 (都市1つに4〜8ペア ≒ 250チャンクに6ペア)
        float smallChance = PomkotsMechs.CONFIG.lostCitySmallMechSpawnChance;
        if (smallChance > 0.0F && random.nextFloat() < smallChance) {
            trySpawnSmallMechSquad(level, chunkX, chunkZ, random);
        }
    }

    private static boolean trySpawnLargeMech(WorldGenLevel level, int chunkX, int chunkZ, RandomSource random) {
        // チャンク中央付近 (4〜11)
        int x = (chunkX << 4) + random.nextInt(8) + 4;
        int z = (chunkZ << 4) + random.nextInt(8) + 4;

        BlockPos surfacePos = findOutdoorSurface(level, x, z);
        if (surfacePos == null) {
            return false;
        }

        // 大型機用クリアスペースチェック: 半径1（3x3）、高さ20ブロック
        if (!checkClearSpace(level, surfacePos, 1, 20)) {
            return false;
        }

        RegistrySupplier<?> supplier = LARGE_MECHS[random.nextInt(LARGE_MECHS.length)];
        @SuppressWarnings("unchecked")
        EntityType<? extends Mob> entityType = (EntityType<? extends Mob>) supplier.get();
        Mob entity = entityType.create(level.getLevel());
        if (entity == null) {
            return false;
        }

        entity.moveTo(x + 0.5, surfacePos.getY(), z + 0.5, random.nextFloat() * 360F, 0.0F);
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(surfacePos), MobSpawnType.STRUCTURE, null, null);

        if (PomkotsMechs.CONFIG.lostCityLargeMechPersistent) {
            entity.setPersistenceRequired();
        }
        entity.addTag("pomkots_city_spawn");

        level.addFreshEntity(entity);
        PomkotsMechs.LOGGER.info("Spawned city large boss mech {} at {}", entityType.getDescriptionId(), surfacePos);
        return true;
    }

    private static boolean trySpawnMediumMech(WorldGenLevel level, int chunkX, int chunkZ, RandomSource random) {
        int x = (chunkX << 4) + random.nextInt(10) + 3;
        int z = (chunkZ << 4) + random.nextInt(10) + 3;

        BlockPos surfacePos = findOutdoorSurface(level, x, z);
        if (surfacePos == null) {
            return false;
        }

        // 中型機用クリアスペースチェック: 高さ4ブロック
        if (!checkClearSpace(level, surfacePos, 0, 4)) {
            return false;
        }

        RegistrySupplier<?> supplier = MEDIUM_MECHS[random.nextInt(MEDIUM_MECHS.length)];
        @SuppressWarnings("unchecked")
        EntityType<? extends Mob> entityType = (EntityType<? extends Mob>) supplier.get();
        Mob entity = entityType.create(level.getLevel());
        if (entity == null) {
            return false;
        }

        entity.moveTo(x + 0.5, surfacePos.getY(), z + 0.5, random.nextFloat() * 360F, 0.0F);
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(surfacePos), MobSpawnType.STRUCTURE, null, null);

        if (PomkotsMechs.CONFIG.lostCityMediumMechPersistent) {
            entity.setPersistenceRequired();
        }
        entity.addTag("pomkots_city_spawn");

        level.addFreshEntity(entity);
        PomkotsMechs.LOGGER.debug("Spawned city medium mech {} at {}", entityType.getDescriptionId(), surfacePos);
        return true;
    }

    private static boolean trySpawnSmallMechSquad(WorldGenLevel level, int chunkX, int chunkZ, RandomSource random) {
        int baseX = (chunkX << 4) + random.nextInt(10) + 3;
        int baseZ = (chunkZ << 4) + random.nextInt(10) + 3;

        BlockPos baseSurfacePos = findOutdoorSurface(level, baseX, baseZ);
        if (baseSurfacePos == null) {
            return false;
        }

        if (!checkClearSpace(level, baseSurfacePos, 0, 2)) {
            return false;
        }

        // 1ペアあたり 2〜4 機
        int count = random.nextInt(3) + 2;
        boolean anySpawned = false;

        for (int i = 0; i < count; i++) {
            int offsetX = (i == 0) ? 0 : random.nextInt(5) - 2;
            int offsetZ = (i == 0) ? 0 : random.nextInt(5) - 2;
            int spawnX = baseX + offsetX;
            int spawnZ = baseZ + offsetZ;

            BlockPos spawnPos = findOutdoorSurface(level, spawnX, spawnZ);
            if (spawnPos == null || Math.abs(spawnPos.getY() - baseSurfacePos.getY()) > 3) {
                spawnPos = baseSurfacePos;
            }

            if (!checkClearSpace(level, spawnPos, 0, 2)) {
                continue;
            }

            RegistrySupplier<?> supplier = SMALL_MECHS[random.nextInt(SMALL_MECHS.length)];
            @SuppressWarnings("unchecked")
            EntityType<? extends Mob> entityType = (EntityType<? extends Mob>) supplier.get();
            Mob entity = entityType.create(level.getLevel());
            if (entity != null) {
                entity.moveTo(spawnX + 0.5, spawnPos.getY(), spawnZ + 0.5, random.nextFloat() * 360F, 0.0F);
                entity.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.STRUCTURE, null, null);
                // 小型機は自然デスポーン許可 (setPersistenceRequiredは呼ばない)
                entity.addTag("pomkots_city_spawn");
                level.addFreshEntity(entity);
                anySpawned = true;
            }
        }

        if (anySpawned) {
            PomkotsMechs.LOGGER.debug("Spawned city small mech squad around {}", baseSurfacePos);
        }
        return anySpawned;
    }

    private static BlockPos findOutdoorSurface(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, 250, z);
        while (cursor.getY() > 60) {
            BlockState state = level.getBlockState(cursor);
            if (state.isFaceSturdy(level, cursor, Direction.UP) && state.getFluidState().isEmpty()) {
                BlockPos surfacePos = cursor.above();
                // 空が見える屋外（Surface）であることを確認
                if (level.canSeeSky(surfacePos)) {
                    return surfacePos.immutable();
                }
            }
            cursor.move(Direction.DOWN);
        }
        return null;
    }

    private static boolean checkClearSpace(WorldGenLevel level, BlockPos pos, int radius, int height) {
        for (int dy = 0; dy < height; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = pos.offset(dx, dy, dz);
                    BlockState s = level.getBlockState(p);
                    if (!s.isAir() && !s.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
