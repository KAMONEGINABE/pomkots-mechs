package grcmcs.minecraft.mods.pomkotsmechs.misc.migration;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.migration.AssetAnchorBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public final class AssetMigrationManager {
   private static final Queue<AssetMigrationManager.MigrationTask> QUEUE = new ArrayDeque<>();

   private AssetMigrationManager() {
   }

   public static void queueCheck(AssetAnchorBlockEntity be) {
      if (be.getLevel() instanceof ServerLevel level) {
         PomkotsDataPack.AssetDefinition def = PomkotsDataPackManager.getInstance().getDataPack().getAssetDefinitions().get(be.getAssetId());
         if (def != null && def.getLatestVersion() > be.getAssetVersion()) {
            QUEUE.add(new AssetMigrationManager.MigrationTask(level, be.getBlockPos()));
         }
      }
   }

   public static void tick(MinecraftServer server) {
      while (!QUEUE.isEmpty()) {
         AssetMigrationManager.MigrationTask task = QUEUE.poll();
         if (task.level().getBlockEntity(task.pos()) instanceof AssetAnchorBlockEntity anchor) {
            checkMigration(anchor);
         }
      }
   }

   private static void checkMigration(AssetAnchorBlockEntity anchor) {
      String assetId = anchor.getAssetId();
      int currentVersion = anchor.getAssetVersion();
      PomkotsMechs.LOGGER.info("[Asset] Check {} v{}", assetId, currentVersion);
      PomkotsDataPack.AssetDefinition def = PomkotsDataPackManager.getInstance().getDataPack().getAssetDefinitions().get(assetId);
      if (def != null) {
         switch (def.getAssetType()) {
            case "customnpcs":
               migrateCnpc(anchor, def);
               break;
            case "small_structure":
               migrateSmallStructure(anchor, def);
               break;
            default:
               migrateNone(anchor, def);
         }
      }
   }

   private static void migrateCnpc(AssetAnchorBlockEntity anchor, PomkotsDataPack.AssetDefinition definition) {
      if (anchor.getAssetVersion() < definition.getLatestVersion() && definition.getNpcId() != null && !definition.getNpcId().isEmpty()) {
         String spawnName = definition.getNpcId();
         String[] parts = spawnName.split("/", 2);
         if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid spawn_name: " + spawnName);
         }

         int tabId = Integer.parseInt(parts[0]);
         String npcName = parts[1];
         AssetMigrationCommands.runCommand(anchor, "noppes npc " + npcName + " delete");
         if (AssetMigrationCommands.runCommand(anchor, "noppes clone spawn " + npcName + " " + tabId + " ~ ~2 ~")) {
            anchor.setAssetVersion(definition.getLatestVersion());
         }
      }
   }

   private static void migrateSmallStructure(AssetAnchorBlockEntity anchor, PomkotsDataPack.AssetDefinition definition) {
      if (anchor.getAssetVersion() < definition.getLatestVersion() && definition.getStructureId() != null && !definition.getStructureId().isEmpty()) {
         ServerLevel serverLevel = (ServerLevel)anchor.getLevel();
         StructureTemplateManager manager = serverLevel.getStructureManager();
         Optional<StructureTemplate> templateOpt = manager.get(new ResourceLocation(definition.getStructureId()));
         if (templateOpt.isPresent()) {
            StructureTemplate template = templateOpt.get();
            StructurePlaceSettings settings = new StructurePlaceSettings().setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(false);
            if (template.placeInWorld(serverLevel, anchor.getBlockPos().above(), anchor.getBlockPos().above(), settings, serverLevel.random, 2)) {
               anchor.setAssetVersion(definition.getLatestVersion());
            }
         }
      }
   }

   private static void migrateNone(AssetAnchorBlockEntity anchor, PomkotsDataPack.AssetDefinition def) {
      int currentVersion = anchor.getAssetVersion();
      if (currentVersion < def.getLatestVersion()) {
         anchor.setAssetVersion(currentVersion);
      }
   }

   private static void migrateGeneral(AssetAnchorBlockEntity anchor, PomkotsDataPack.AssetDefinition def) {
      int currentVersion = anchor.getAssetVersion();

      while (currentVersion < def.getLatestVersion()) {
         int nextVersion = currentVersion + 1;
         String functionId = def.getFunctions().get(nextVersion);
         if (functionId == null || functionId.isEmpty()) {
            PomkotsMechs.LOGGER.error("[Asset] Migration stopped at v{}, no functionId", currentVersion);
            break;
         }

         PomkotsMechs.LOGGER.info("[Asset] {} v{} -> v{}", new Object[]{anchor.getAssetId(), currentVersion, nextVersion});
         if (!AssetMigrationCommands.runFunction(anchor, functionId)) {
            PomkotsMechs.LOGGER.error("[Asset] Migration stopped at v{}", currentVersion);
            break;
         }

         currentVersion = nextVersion;
         anchor.setAssetVersion(currentVersion);
      }
   }

   private record MigrationTask(ServerLevel level, BlockPos pos) {
   }
}
