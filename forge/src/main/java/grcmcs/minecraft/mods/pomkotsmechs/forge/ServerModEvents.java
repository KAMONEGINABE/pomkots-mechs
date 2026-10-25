package grcmcs.minecraft.mods.pomkotsmechs.forge;

import grcmcs.minecraft.mods.pomkotsmechs.survival.SurvivalInitActions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "pomkotsmechs", bus = Bus.FORGE)
public class ServerModEvents {
   @SubscribeEvent
   public static void onPlayerJoin(PlayerLoggedInEvent event) {
      if (event.getEntity() instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null) {
         SurvivalInitActions.onServerStarted(serverPlayer.getServer());
         SurvivalInitActions.onPlayerJoin(serverPlayer);
      }
   }
}
