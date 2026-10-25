package grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm;

import com.mojang.text2speech.Narrator;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import io.netty.buffer.Unpooled;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ServerBGMTracker {
   private static final Map<UUID, BGMState> LAST = new HashMap<>();
   private static final Map<UUID, BGMState> FORCE = new HashMap<>();
   public static LostCitiesCompatible lostCitiesAccessor = new LostCitiesCompatible();

   public static void tick(ServerPlayer player) {
      if (PomkotsMechs.CONFIG.survivalModeEnabled) {
         BGMState oldState = LAST.get(player.m_20148_());
         BGMState newState = computeState(player, oldState);
         if (newState != oldState) {
            LAST.put(player.m_20148_(), newState);
            sendDataPack2Player(player, newState);
         }
      }
   }

   public static void forceSetBGMState(ServerPlayer player, BGMState state) {
      FORCE.put(player.m_20148_(), state);
   }

   public static void forceUnsetBGMState(ServerPlayer player) {
      FORCE.remove(player.m_20148_());
   }

   public static void sendDataPack2Player(ServerPlayer player, BGMState newState) {
      try {
         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
         buf.m_130068_(newState);
         NetworkManager.sendToPlayer(player, PomkotsMechs.id("bs"), buf);
      } catch (Exception e) {
         Narrator.LOGGER.error("Failed to send bgm event to client", e);
      }
   }

   private static BGMState computeState(ServerPlayer player, BGMState oldState) {
      BGMState forceSound = FORCE.get(player.m_20148_());
      if (forceSound != null) {
         return forceSound;
      }

      BGMState lostCitiesSound = lostCitiesAccessor.getBGMState(player, oldState);
      return lostCitiesSound != null ? lostCitiesSound : BGMState.NONE;
   }

   private static boolean isBossNearby(Player player) {
      return !player.m_9236_().m_45976_(BaseBossEntity.class, player.m_20191_().m_82400_(50.0)).isEmpty();
   }
}
