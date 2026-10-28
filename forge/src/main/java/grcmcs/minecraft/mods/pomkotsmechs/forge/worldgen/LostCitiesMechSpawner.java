package grcmcs.minecraft.mods.pomkotsmechs.forge.worldgen;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.worldgen.WreckedMechFeature;
import mcjty.lostcities.api.LostCityEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LostCitiesMechSpawner {

    @SubscribeEvent
    public static void onPostGenCityChunk(LostCityEvent.PostGenCityChunkEvent event) {
        WorldGenLevel level = event.getWorld();
        if (level == null || level.isClientSide()) {
            return;
        }

        RandomSource random = level.getRandom();
        float spawnChance = PomkotsMechs.CONFIG.lostCityMechSpawnChance;
        if (spawnChance <= 0.0F || random.nextFloat() >= spawnChance) {
            return;
        }

        int chunkX = event.getChunkX();
        int chunkZ = event.getChunkZ();

        // チャンク境界から内側（3〜12）に配置し、機体の幅によるチャンクはみ出しを防止
        int x = (chunkX << 4) + random.nextInt(10) + 3;
        int z = (chunkZ << 4) + random.nextInt(10) + 3;

        // Y=250 から Y=60 へ向けて下降スキャン
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, 250, z);
        while (cursor.getY() > 60) {
            BlockState groundState = level.getBlockState(cursor);

            // 上面が頑丈で、流体（水・溶岩）ではないブロックを検出
            if (groundState.isFaceSturdy(level, cursor, Direction.UP) && groundState.getFluidState().isEmpty()) {
                BlockPos surfacePos = cursor.above();
                BlockState atSurface = level.getBlockState(surfacePos);
                BlockState aboveSurface = level.getBlockState(surfacePos.above());
                BlockState headSurface = level.getBlockState(surfacePos.above(2));

                // 機体の高さ（3ブロック分）の空間が確保されているか確認
                boolean atOk = atSurface.isAir() || atSurface.canBeReplaced();
                boolean aboveOk = aboveSurface.isAir() || aboveSurface.canBeReplaced();
                boolean headOk = headSurface.isAir() || headSurface.canBeReplaced();

                if (atOk && aboveOk && headOk && atSurface.getFluidState().isEmpty()) {
                    // 平坦な屋根・タンク上・敷地地面のいずれかに機体をスポーン
                    boolean success = WreckedMechFeature.spawnWreckedMech(level, surfacePos, random);
                    if (success) {
                        PomkotsMechs.LOGGER.debug("Spawned city mech at {}", surfacePos);
                    }
                    break;
                }
            }
            cursor.move(Direction.DOWN);
        }
    }
}
