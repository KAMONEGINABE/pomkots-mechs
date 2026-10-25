package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import net.minecraft.client.Minecraft;

public class ScreenFadeManager {
   private static boolean active = false;
   private static long startTime;
   private static int fadeInTicks;
   private static int holdTicks;
   private static int fadeOutTicks;

   public static void start(int fadeIn, int hold, int fadeOut) {
      active = true;
      startTime = Minecraft.getInstance().level.getGameTime();
      fadeInTicks = fadeIn;
      holdTicks = hold;
      fadeOutTicks = fadeOut;
   }

   public static boolean isActive() {
      return active;
   }

   public static float getAlpha() {
      if (!active) {
         return 0.0F;
      }

      Minecraft mc = Minecraft.getInstance();
      long elapsed = mc.level.getGameTime() - startTime;
      if (elapsed < fadeInTicks) {
         return (float)elapsed / fadeInTicks;
      }

      elapsed -= fadeInTicks;
      if (elapsed < holdTicks) {
         return 1.0F;
      }

      elapsed -= holdTicks;
      if (elapsed < fadeOutTicks) {
         return 1.0F - (float)elapsed / fadeOutTicks;
      }

      active = false;
      return 0.0F;
   }
}
