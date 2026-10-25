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
         Scoreboard board = server.m_129896_();
         Objective obj = board.m_83477_("pomkots.mp");
         if (obj == null) {
            PomkotsMechs.LOGGER.info("Creating Score Board Objective for Progress Management: pomkots.mp");
            board.m_83436_("pomkots.mp", ObjectiveCriteria.f_83588_, Component.m_237113_("Main Progress"), RenderType.INTEGER);
         }
      }
   }

   public static void onPlayerJoin(ServerPlayer player) {
      if (PomkotsMechs.CONFIG.survivalModeEnabled) {
         MinecraftServer server = player.m_20194_();
         if (server == null) {
            return;
         }

         Scoreboard board = server.m_129896_();
         Objective obj = board.m_83477_("pomkots.mp");
         if (obj == null) {
            PomkotsMechs.LOGGER.info("No Score Board Objective for Progress Management:pomkots.mp");
            return;
         }

         if (!board.m_83461_(player.m_6302_(), obj)) {
            board.m_83471_(player.m_6302_(), obj).m_83402_(0);
            PomkotsMechs.LOGGER.info("Initialized score for " + player.m_7755_().getString());
         } else if (board.m_83471_(player.m_6302_(), obj).m_83400_() == 0) {
            sendStartOpening(player);
         }
      }
   }

   public static void sendStartOpening(ServerPlayer player) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.EMPTY_BUFFER);
      NetworkManager.sendToPlayer(player, PomkotsMechs.id("so"), buf);
   }
}
