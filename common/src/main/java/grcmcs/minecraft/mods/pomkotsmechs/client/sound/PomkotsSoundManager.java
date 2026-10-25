package grcmcs.minecraft.mods.pomkotsmechs.client.sound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;

public class PomkotsSoundManager {
   private static final Map<String, PomkotsSoundInstance> activeSounds = new HashMap<>();

   public static String playSound(String id, SoundEvent soundEvent, SoundConfig config) {
      if (activeSounds.get(id) != null) {
         return id;
      }

      PomkotsSoundInstance sound = new PomkotsSoundInstance(soundEvent, config);
      activeSounds.put(id, sound);
      Minecraft.m_91087_().m_91106_().m_120367_(sound);
      return id;
   }

   public static String playSound(SoundEvent soundEvent, SoundConfig config) {
      return playSound(UUID.randomUUID().toString(), soundEvent, config);
   }

   public static void stopSound(String id, boolean fadeOut) {
      PomkotsSoundInstance sound = activeSounds.get(id);
      if (sound != null && !sound.m_7801_()) {
         if (fadeOut) {
            sound.startFadeOut();
         } else {
            sound.stopImmediately();
            activeSounds.remove(id);
         }
      }
   }

   public static void stopSound(String id) {
      stopSound(id, true);
   }

   public static void stopAllSounds(int entId, boolean fadeOut) {
      for (String id : activeSounds.keySet().toArray(new String[0])) {
         if (id.startsWith(entId + "")) {
            stopSound(id, fadeOut);
         }
      }
   }

   public static boolean isPlaying(String id) {
      PomkotsSoundInstance sound = activeSounds.get(id);
      return sound != null && !sound.m_7801_();
   }

   public static void cleanup() {
      activeSounds.entrySet().removeIf(entry -> entry.getValue().m_7801_());
   }

   public static void cleanup(Minecraft minecraft) {
      cleanup();
   }
}
