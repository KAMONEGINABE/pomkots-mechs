package grcmcs.minecraft.mods.pomkotsmechs.forge;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientRenderEvents {
   public static void init() {
      MinecraftForge.EVENT_BUS.register(ClientRenderEvents.class);
   }

   @SubscribeEvent
   public static void onRenderGuiOverlay(Pre event) {
      Minecraft client = Minecraft.m_91087_();
      LocalPlayer p = client.f_91074_;
      if (p != null && p.m_20202_() instanceof PomkotsVehicle) {
         String id = event.getOverlay().id().toString();
         if ("minecraft:player_health".equals(id) || "minecraft:mount_health".equals(id) || "minecraft:armor_level".equals(id)) {
            event.setCanceled(true);
         }
      }
   }

   private static boolean shouldCancel(Object v) {
      Minecraft client = Minecraft.m_91087_();
      LocalPlayer p = client.f_91074_;
      return p != null && p.m_20202_() instanceof PomkotsVehicle;
   }
}
