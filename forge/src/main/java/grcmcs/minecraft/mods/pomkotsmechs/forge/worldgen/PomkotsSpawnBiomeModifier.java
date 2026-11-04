package grcmcs.minecraft.mods.pomkotsmechs.forge.worldgen;

import com.mojang.serialization.Codec;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public class PomkotsSpawnBiomeModifier implements BiomeModifier {
    public static final Codec<PomkotsSpawnBiomeModifier> CODEC = Codec.unit(PomkotsSpawnBiomeModifier::new);

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD) {
            if (!PomkotsMechs.CONFIG.enableWildMechSpawn) {
                return;
            }

            // Overworld バイオームを対象とする
            if (biome.is(BiomeTags.IS_OVERWORLD)) {
                int smallWeight = PomkotsMechs.CONFIG.wildSmallMechSpawnWeight;
                if (smallWeight > 0) {
                    // PMSS01~03 (小型機ペア: 2〜4機)
                    int w1 = Math.max(1, (smallWeight + 2) / 3);
                    int w2 = Math.max(1, (smallWeight + 1) / 3);
                    int w3 = Math.max(1, smallWeight / 3);

                    builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PomkotsMechs.PMSS01.get(), w1, 2, 4));
                    builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PomkotsMechs.PMSS02.get(), w2, 2, 4));
                    builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PomkotsMechs.PMSS03.get(), w3, 2, 4));
                }

                int mediumWeight = PomkotsMechs.CONFIG.wildMediumMechSpawnWeight;
                if (mediumWeight > 0) {
                    // PMS01~10 (中型機: 1機)
                    // canSpawnCommon 側で wildMediumMechPassRatio による間引きを行い、実効ウィッチの1/5を実現
                    builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PomkotsMechs.PMS01.get(), mediumWeight, 1, 1));
                    builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PomkotsMechs.PMS02.get(), mediumWeight, 1, 1));
                    builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PomkotsMechs.PMS03.get(), mediumWeight, 1, 1));
                    builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PomkotsMechs.PMS04.get(), mediumWeight, 1, 1));
                    builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PomkotsMechs.PMS05.get(), mediumWeight, 1, 1));
                    builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PomkotsMechs.PMS06.get(), mediumWeight, 1, 1));
                    builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PomkotsMechs.PMS07.get(), mediumWeight, 1, 1));
                    builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PomkotsMechs.PMS08.get(), mediumWeight, 1, 1));
                    builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PomkotsMechs.PMS09.get(), mediumWeight, 1, 1));
                    builder.getMobSpawnSettings().addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(PomkotsMechs.PMS10.get(), mediumWeight, 1, 1));
                }
            }
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
