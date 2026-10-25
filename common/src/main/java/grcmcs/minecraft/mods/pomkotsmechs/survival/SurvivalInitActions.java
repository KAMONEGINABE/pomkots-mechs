package grcmcs.minecraft.mods.pomkotsmechs.survival;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;

public class SurvivalInitActions {
   public static void onServerStarted(MinecraftServer server) {
      if (PomkotsMechs.CONFIG.survivalModeEnabled) {
         Scoreboard board = server.getScoreboard();
         Objective obj = board.getObjective("pomkots.mp");
         if (obj == null) {
            PomkotsMechs.LOGGER.info("Creating Score Board Objective for Progress Management: pomkots.mp");
            board.addObjective("pomkots.mp", ObjectiveCriteria.DUMMY, Component.literal("Main Progress"), RenderType.INTEGER);
         }
      }
   }

   public static void onPlayerJoin(ServerPlayer player) {
      if (PomkotsMechs.CONFIG.survivalModeEnabled) {
         MinecraftServer server = player.getServer();
         if (server == null) {
            return;
         }

         Scoreboard board = server.getScoreboard();
         Objective obj = board.getObjective("pomkots.mp");
         if (obj == null) {
            PomkotsMechs.LOGGER.info("No Score Board Objective for Progress Management:pomkots.mp");
            return;
         }

         if (!board.hasPlayerScore(player.getScoreboardName(), obj)) {
            board.getOrCreatePlayerScore(player.getScoreboardName(), obj).setScore(0);
            PomkotsMechs.LOGGER.info("Initialized score for " + player.getName().getString());
         } else if (board.getOrCreatePlayerScore(player.getScoreboardName(), obj).getScore() == 0) {
            sendStartOpening(player);
         }
      }
   }

   public static void sendStartOpening(ServerPlayer player) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.EMPTY_BUFFER);
      NetworkManager.sendToPlayer(player, PomkotsMechs.id("so"), buf);
   }
}
