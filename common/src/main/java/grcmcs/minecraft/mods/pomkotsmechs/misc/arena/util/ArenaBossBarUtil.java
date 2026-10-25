package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util;

import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;

public final class ArenaBossBarUtil {
   private ArenaBossBarUtil() {
   }

   public static void createCountdownBar(ArenaMatchContext context, String title, long durationMs) {
      removeBar(context);
      ServerBossEvent bossBar = new ServerBossEvent(Component.m_237113_(title), BossBarColor.BLUE, BossBarOverlay.PROGRESS);
      bossBar.m_142711_(1.0F);
      if (context.getChallengerEntity() instanceof ServerPlayer player) {
         bossBar.m_6543_(player);
      }

      if (context.getOpponentEntity() instanceof ServerPlayer player) {
         bossBar.m_6543_(player);
      }

      context.runtime().setBossBar(bossBar);
   }

   public static void updateCountdownBar(ArenaMatchContext context, String title, long elapsedMs, long durationMs) {
      ServerBossEvent bossBar = context.runtime().getBossBar();
      if (bossBar != null) {
         long remainMs = Math.max(0L, durationMs - elapsedMs);
         long remainSec = remainMs / 1000L;
         bossBar.m_6456_(Component.m_237113_(title + " : " + remainSec + "s"));
         float progress = Math.max(0.0F, Math.min(1.0F, 1.0F - (float)elapsedMs / (float)durationMs));
         bossBar.m_142711_(progress);
      }
   }

   public static void removeBar(ArenaMatchContext context) {
      ServerBossEvent bossBar = context.runtime().getBossBar();
      if (bossBar != null) {
         bossBar.m_7706_();
         context.runtime().setBossBar(null);
      }
   }
}
