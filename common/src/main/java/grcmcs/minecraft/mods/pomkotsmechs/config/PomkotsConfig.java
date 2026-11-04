package grcmcs.minecraft.mods.pomkotsmechs.config;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = PomkotsMechs.MODID)
public class PomkotsConfig implements ConfigData {
    public boolean enableEntityBlockDestruction = false;
    public boolean enablePlayerVehicleBlockDestruction = true;
    public String nonDestructiveBlocks = "minecraft:bedrock,minecraft:structure_void,minecraft:structure_block";
    public String nonDropBlocks = "minecraft:stone,minecraft:grass_block,minecraft:dirt,minecraft:gravel";
    public boolean enableHudHealthBar = true;
    public boolean consumeBlocksWhenPlacing = true;
    public boolean dropItemsWhenDestroyBlock = false;
    public boolean enablePartsLevelCompatibility = true;
    @ConfigEntry.Gui.Excluded
    public boolean survivalModeEnabled = false;
    public boolean debugModeEnabled = false;
    public boolean forceThirdPersonViewWhenRidingMech = true;
    public boolean targetLockNonPomkotsMobs = true;
    public boolean targetLockPlayers = true;
    public boolean targetLockPomkotsVehicles = true;

    public float lostCityMechSpawnChance = 0.008F;

    // ==================== Enemy Mech Spawning ====================
    // --- Wilderness Spawning (Night / Surface) ---
    public boolean enableWildMechSpawn = true;
    public int wildSmallMechSpawnWeight = 5;
    public int wildMediumMechSpawnWeight = 1;
    public float wildMediumMechPassRatio = 0.1F;

    // --- City Spawning (Lost Cities) ---
    public boolean enableLostCityEnemySpawn = true;
    public float lostCitySmallMechSpawnChance = 0.024F;
    public float lostCityMediumMechSpawnChance = 0.006F;
    public float lostCityLargeMechSpawnChance = 0.00133F;
    public boolean lostCityMediumMechPersistent = true;
    public boolean lostCityLargeMechPersistent = true;

    @ConfigEntry.Gui.Excluded
    public String flightControlType = "SUPER_BOOST_STYLE";

    @ConfigEntry.Gui.Excluded
    public int missionMaxForcedChunksPerInstance = 1024;
    @ConfigEntry.Gui.Excluded
    public int missionMaxForcedChunksTotal = 2048;
}
