package grcmcs.minecraft.mods.pomkotsmechs.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Excluded;

@Config(name = "pomkotsmechs")
public class PomkotsConfig implements ConfigData {
   public boolean enableEntityBlockDestruction = false;
   public boolean enablePlayerVehicleBlockDestruction = true;
   public String nonDestructiveBlocks = "minecraft:bedrock,minecraft:structure_void,minecraft:structure_block";
   public String nonDropBlocks = "minecraft:stone,minecraft:grass_block,minecraft:dirt,minecraft:gravel";
   public boolean enableHudHealthBar = true;
   public boolean consumeBlocksWhenPlacing = true;
   public boolean dropItemsWhenDestroyBlock = false;
   public boolean enablePartsLevelCompatibility = true;
   @Excluded
   public boolean survivalModeEnabled = false;
   public boolean debugModeEnabled = false;
   public boolean forceThirdPersonViewWhenRidingMech = true;
   public boolean targetLockNonPomkotsMobs = true;
   public boolean targetLockPlayers = true;
   public boolean targetLockPomkotsVehicles = true;
}
