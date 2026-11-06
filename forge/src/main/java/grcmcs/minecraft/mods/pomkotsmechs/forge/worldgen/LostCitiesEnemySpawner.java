package grcmcs.minecraft.mods.pomkotsmechs.forge.worldgen;

import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import mcjty.lostcities.api.LostCityEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
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

    private static void logTrace(String message, Object... args) {
        if (PomkotsMechs.CONFIG.debugModeEnabled) {
            PomkotsMechs.LOGGER.info("[CitySpawn] " + message, args);
        } else {
            PomkotsMechs.LOGGER.debug("[CitySpawn] " + message, args);
        }
    }

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

        logTrace("Processing PostGenCityChunkEvent for chunk [{}, {}]", chunkX, chunkZ);

        // 1. 大型機（PMB）の抽選 (都市3つに1体 ≒ 750チャンクに1体)
        float largeChance = PomkotsMechs.CONFIG.lostCityLargeMechSpawnChance;
        if (largeChance > 0.0F && random.nextFloat() < largeChance) {
            logTrace("Large mech roll PASSED in chunk [{}, {}] (chance: {})", chunkX, chunkZ, largeChance);
            if (trySpawnLargeMech(level, chunkX, chunkZ, random, false)) {
                return;
            }
        }

        // 2. 中型機（PMS）の抽選 (都市1つに1〜2体 ≒ 250チャンクに1.5体)
        float mediumChance = PomkotsMechs.CONFIG.lostCityMediumMechSpawnChance;
        if (mediumChance > 0.0F && random.nextFloat() < mediumChance) {
            logTrace("Medium mech roll PASSED in chunk [{}, {}] (chance: {})", chunkX, chunkZ, mediumChance);
            if (trySpawnMediumMech(level, chunkX, chunkZ, random, false)) {
                return;
            }
        }

        // 3. 小型機（PMSS）ペアの抽選 (都市1つに4〜8ペア ≒ 250チャンクに6ペア)
        float smallChance = PomkotsMechs.CONFIG.lostCitySmallMechSpawnChance;
        if (smallChance > 0.0F && random.nextFloat() < smallChance) {
            logTrace("Small mech squad roll PASSED in chunk [{}, {}] (chance: {})", chunkX, chunkZ, smallChance);
            trySpawnSmallMechSquad(level, chunkX, chunkZ, random, false);
        }
    }

    public static boolean trySpawnLargeMech(ServerLevelAccessor level, int chunkX, int chunkZ, RandomSource random, boolean force) {
        // 障害物回避のため、チャンク中央付近 (4〜11) から最大8回サンプリング
        int maxAttempts = 8;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            int x = (chunkX << 4) + random.nextInt(8) + 4;
            int z = (chunkZ << 4) + random.nextInt(8) + 4;

            BlockPos surfacePos = findOutdoorSurface(level, x, z);
            if (surfacePos == null) {
                logTrace("Large mech attempt {}/{}: no valid surface at ({}, {}) in chunk [{}, {}]", attempt, maxAttempts, x, z, chunkX, chunkZ);
                continue;
            }

            // 大型機用クリアスペースチェック: 半径1（3x3）、高さ14ブロック
            SpaceCheckResult spaceResult = checkClearSpace(level, surfacePos, 1, 14);
            if (!spaceResult.isClear) {
                logTrace("Large mech attempt {}/{}: space blocked at ({}, {}, {}) by {} in chunk [{}, {}]",
                        attempt, maxAttempts, spaceResult.blockPos.getX(), spaceResult.blockPos.getY(), spaceResult.blockPos.getZ(),
                        spaceResult.blockName, chunkX, chunkZ);
                continue;
            }

            RegistrySupplier<?> supplier = LARGE_MECHS[random.nextInt(LARGE_MECHS.length)];
            @SuppressWarnings("unchecked")
            EntityType<? extends Mob> entityType = (EntityType<? extends Mob>) supplier.get();
            Mob entity = entityType.create(level.getLevel());
            if (entity == null) {
                return false;
            }

            entity.moveTo(x + 0.5, surfacePos.getY(), z + 0.5, random.nextFloat() * 360F, 0.0F);
            entity.finalizeSpawn(level.getLevel(), level.getLevel().getCurrentDifficultyAt(surfacePos), MobSpawnType.STRUCTURE, null, null);

            if (PomkotsMechs.CONFIG.lostCityLargeMechPersistent) {
                entity.setPersistenceRequired();
            }
            entity.addTag("pomkots_city_spawn");

            level.addFreshEntity(entity);
            PomkotsMechs.LOGGER.info("*** [CitySpawn] SPAWNED LARGE BOSS {} at [{}, {}, {}] in chunk [{}, {}] ***",
                    BuiltInRegistries.ENTITY_TYPE.getKey(entityType), surfacePos.getX(), surfacePos.getY(), surfacePos.getZ(), chunkX, chunkZ);
            return true;
        }

        logTrace("Large mech spawn failed after {} attempts in chunk [{}, {}]", maxAttempts, chunkX, chunkZ);
        return false;
    }

    public static boolean trySpawnMediumMech(ServerLevelAccessor level, int chunkX, int chunkZ, RandomSource random, boolean force) {
        int maxAttempts = 6;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            int x = (chunkX << 4) + random.nextInt(10) + 3;
            int z = (chunkZ << 4) + random.nextInt(10) + 3;

            BlockPos surfacePos = findOutdoorSurface(level, x, z);
            if (surfacePos == null) {
                continue;
            }

            // 中型機用クリアスペースチェック: 半径0（1x1）、高さ4ブロック
            SpaceCheckResult spaceResult = checkClearSpace(level, surfacePos, 0, 4);
            if (!spaceResult.isClear) {
                continue;
            }

            RegistrySupplier<?> supplier = MEDIUM_MECHS[random.nextInt(MEDIUM_MECHS.length)];
            @SuppressWarnings("unchecked")
            EntityType<? extends Mob> entityType = (EntityType<? extends Mob>) supplier.get();
            Mob entity = entityType.create(level.getLevel());
            if (entity == null) {
                return false;
            }

            entity.moveTo(x + 0.5, surfacePos.getY(), z + 0.5, random.nextFloat() * 360F, 0.0F);
            entity.finalizeSpawn(level.getLevel(), level.getLevel().getCurrentDifficultyAt(surfacePos), MobSpawnType.STRUCTURE, null, null);

            if (PomkotsMechs.CONFIG.lostCityMediumMechPersistent) {
                entity.setPersistenceRequired();
            }
            entity.addTag("pomkots_city_spawn");

            level.addFreshEntity(entity);
            PomkotsMechs.LOGGER.info("*** [CitySpawn] SPAWNED MEDIUM MECH {} at [{}, {}, {}] in chunk [{}, {}] ***",
                    BuiltInRegistries.ENTITY_TYPE.getKey(entityType), surfacePos.getX(), surfacePos.getY(), surfacePos.getZ(), chunkX, chunkZ);
            return true;
        }
        return false;
    }

    public static boolean trySpawnSmallMechSquad(ServerLevelAccessor level, int chunkX, int chunkZ, RandomSource random, boolean force) {
        int maxAttempts = 6;
        BlockPos baseSurfacePos = null;
        int baseX = 0;
        int baseZ = 0;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            baseX = (chunkX << 4) + random.nextInt(10) + 3;
            baseZ = (chunkZ << 4) + random.nextInt(10) + 3;
            baseSurfacePos = findOutdoorSurface(level, baseX, baseZ);
            if (baseSurfacePos != null && checkClearSpace(level, baseSurfacePos, 0, 2).isClear) {
                break;
            }
            baseSurfacePos = null;
        }

        if (baseSurfacePos == null) {
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
                spawnX = baseX;
                spawnZ = baseZ;
            }

            if (!checkClearSpace(level, spawnPos, 0, 2).isClear) {
                continue;
            }

            RegistrySupplier<?> supplier = SMALL_MECHS[random.nextInt(SMALL_MECHS.length)];
            @SuppressWarnings("unchecked")
            EntityType<? extends Mob> entityType = (EntityType<? extends Mob>) supplier.get();
            Mob entity = entityType.create(level.getLevel());
            if (entity != null) {
                entity.moveTo(spawnX + 0.5, spawnPos.getY(), spawnZ + 0.5, random.nextFloat() * 360F, 0.0F);
                entity.finalizeSpawn(level.getLevel(), level.getLevel().getCurrentDifficultyAt(spawnPos), MobSpawnType.STRUCTURE, null, null);
                entity.addTag("pomkots_city_spawn");
                level.addFreshEntity(entity);
                anySpawned = true;
            }
        }

        if (anySpawned) {
            PomkotsMechs.LOGGER.info("*** [CitySpawn] SPAWNED SMALL MECH SQUAD (count: {}) around [{}, {}, {}] in chunk [{}, {}] ***",
                    count, baseSurfacePos.getX(), baseSurfacePos.getY(), baseSurfacePos.getZ(), chunkX, chunkZ);
        }
        return anySpawned;
    }

    /**
     * Y=250 から下降スキャンし、最初に見つかった「上面が頑丈で非流体」のブロックの直上を屋外表面（Surface）とする。
     * ワールド生成時（PostGenCityChunkEvent）はスカイライトがベイクされていないため、level.canSeeSky() を呼ぶと
     * 常に false になって全滅してしまうため、canSeeSky は使用せず下降スキャンの最上部を信頼する。
     */
    public static BlockPos findOutdoorSurface(ServerLevelAccessor level, int x, int z) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, 250, z);
        while (cursor.getY() > 60) {
            BlockState state = level.getBlockState(cursor);
            if (state.isFaceSturdy(level, cursor, Direction.UP) && state.getFluidState().isEmpty()) {
                BlockPos surfacePos = cursor.above();
                BlockState aboveState = level.getBlockState(surfacePos);
                // 足場直上が空気または置換可能であること
                if (aboveState.isAir() || aboveState.canBeReplaced()) {
                    return surfacePos.immutable();
                }
            }
            cursor.move(Direction.DOWN);
        }
        return null;
    }

    public static class SpaceCheckResult {
        public final boolean isClear;
        public final BlockPos blockPos;
        public final String blockName;

        public SpaceCheckResult(boolean isClear, BlockPos blockPos, String blockName) {
            this.isClear = isClear;
            this.blockPos = blockPos;
            this.blockName = blockName;
        }

        public static SpaceCheckResult clear() {
            return new SpaceCheckResult(true, null, null);
        }

        public static SpaceCheckResult blocked(BlockPos pos, BlockState state) {
            String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            return new SpaceCheckResult(false, pos, name);
        }
    }

    public static SpaceCheckResult checkClearSpace(ServerLevelAccessor level, BlockPos pos, int radius, int height) {
        for (int dy = 0; dy < height; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos p = pos.offset(dx, dy, dz);
                    BlockState s = level.getBlockState(p);
                    if (!s.isAir() && !s.canBeReplaced()) {
                        return SpaceCheckResult.blocked(p, s);
                    }
                }
            }
        }
        return SpaceCheckResult.clear();
    }
}
