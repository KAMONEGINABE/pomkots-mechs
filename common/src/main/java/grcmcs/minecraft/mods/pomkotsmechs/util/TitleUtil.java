package grcmcs.minecraft.mods.pomkotsmechs.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class TitleUtil {
   private TitleUtil() {
   }

   public static void showArenaMessage(Entity entity, String title, String subtitle) {
      showArenaMessage(entity, title, subtitle, 10, 60, 20);
   }

   public static void showArenaMessage(Entity entity, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
      if (entity instanceof ServerPlayer player) {
         showTitle(player, Component.m_237113_(title), Component.m_237113_(subtitle), fadeIn, stay, fadeOut);
      }
   }

   public static void showTitle(ServerPlayer player, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
      player.f_8906_.m_9829_(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
      if (title != null) {
         player.f_8906_.m_9829_(new ClientboundSetTitleTextPacket(title));
      }

      if (subtitle != null) {
         player.f_8906_.m_9829_(new ClientboundSetSubtitleTextPacket(subtitle));
      }
   }

   public static void clear(ServerPlayer player) {
      player.f_8906_.m_9829_(new ClientboundClearTitlesPacket(true));
   }
}
