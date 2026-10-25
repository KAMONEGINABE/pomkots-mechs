package grcmcs.minecraft.mods.pomkotsmechs.client.misc;

import net.minecraft.client.Minecraft;

public class ClientHudShake {
   public static float shake = 0.0F;

   public static void addShake(float amount) {
      shake = Math.max(shake, amount);
   }

   public static void tick(Minecraft mc) {
      shake *= 0.9F;
      if (shake < 0.05F) {
         shake = 0.0F;
      }
   }
}
