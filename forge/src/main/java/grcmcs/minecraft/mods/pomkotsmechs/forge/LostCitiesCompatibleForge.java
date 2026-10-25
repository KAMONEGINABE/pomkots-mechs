package grcmcs.minecraft.mods.pomkotsmechs.forge;

import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.BGMState;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.LostCitiesCompatible;
import mcjty.lostcities.LostCities;
import mcjty.lostcities.LostCitiesImp;
import mcjty.lostcities.api.ILostChunkInfo;
import mcjty.lostcities.api.ILostCityInformation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public class LostCitiesCompatibleForge extends LostCitiesCompatible {
   @Override
   public BGMState getBGMState(ServerPlayer serverPlayer, BGMState oldState) {
      LostCitiesImp lostCitiesImp = LostCities.lostCitiesImp;
      ILostCityInformation lostCitiesInfo = lostCitiesImp.getLostInfo(serverPlayer.level());
      if (lostCitiesInfo == null) {
         return null;
      } else {
         ChunkPos chunkPos = serverPlayer.chunkPosition();
         ILostChunkInfo chunkInfo = lostCitiesInfo.getChunkInfo(chunkPos.x, chunkPos.z);
         if (chunkInfo != null) {
            return chunkInfo.isCity() ? BGMState.RUIN_CITY : null;
         } else {
            return null;
         }
      }
   }

   private void showTitlesForPlayers(String title, ServerPlayer player) {
      ClientboundSetTitlesAnimationPacket timesPacket = new ClientboundSetTitlesAnimationPacket(20, 60, 20);
      ClientboundSetTitleTextPacket titlePacket = new ClientboundSetTitleTextPacket(Component.literal(title));
      player.connection.send(timesPacket);
      player.connection.send(titlePacket);
   }
}
