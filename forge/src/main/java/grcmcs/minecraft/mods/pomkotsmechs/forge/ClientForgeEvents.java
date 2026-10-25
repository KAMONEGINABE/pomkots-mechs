package grcmcs.minecraft.mods.pomkotsmechs.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = "pomkotsmechs", bus = Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {
   @SubscribeEvent
   public static void onClientPlayerJoin(LoggingIn event) {
   }
}
