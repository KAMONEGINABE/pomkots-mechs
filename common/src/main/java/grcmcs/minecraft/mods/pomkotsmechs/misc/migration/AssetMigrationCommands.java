package grcmcs.minecraft.mods.pomkotsmechs.misc.migration;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.migration.AssetAnchorBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class AssetMigrationCommands {
   private AssetMigrationCommands() {
   }

   private static CommandSourceStack createCommandSource(AssetAnchorBlockEntity anchor) {
      if (anchor.m_58904_() instanceof ServerLevel level) {
         MinecraftServer server = level.m_7654_();
         return server.m_129893_().m_81327_(level).m_81348_(Vec3.m_82512_(anchor.m_58899_())).m_81325_(4).m_81324_();
      } else {
         return null;
      }
   }

   public static int executeCommand(AssetAnchorBlockEntity anchor, String command) {
      if (anchor.m_58904_() instanceof ServerLevel level) {
         CommandSourceStack source = createCommandSource(anchor);
         if (source == null) {
            return -1;
         }

         try {
            int result = level.m_7654_().m_129892_().m_230957_(source, command);
            PomkotsMechs.LOGGER.info("[Asset] Executed: {} (result={})", command, result);
            return result;
         } catch (Exception e) {
            PomkotsMechs.LOGGER.error("[Asset] Command exception: {}", command, e);
            return -1;
         }
      } else {
         return -1;
      }
   }

   public static boolean runCommand(AssetAnchorBlockEntity anchor, String command) {
      int result = executeCommand(anchor, command);
      if (result <= 0) {
         PomkotsMechs.LOGGER.error("[Asset] Command failed: {}", command);
         return false;
      } else {
         return true;
      }
   }

   public static boolean runFunction(AssetAnchorBlockEntity anchor, String functionId) {
      return runCommand(anchor, "function " + functionId);
   }
}
