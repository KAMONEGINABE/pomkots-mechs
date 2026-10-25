package grcmcs.minecraft.mods.pomkotsmechs.client.sound;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.BGMState;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

public class PomkotsBGMManager {
   private static BGMState currentState = BGMState.NONE;
   private static PomkotsSoundInstance currentSound;
   private static boolean suppressingVanilla = false;

   public static boolean isSuppressingVanilla() {
      return suppressingVanilla;
   }

   public static void setState(BGMState newState) {
      if (PomkotsMechs.CONFIG.survivalModeEnabled) {
         if (newState != currentState) {
            fadeOutCurrent();
            if (newState == BGMState.NONE) {
               resumeVanillaMusic();
               currentState = newState;
            } else {
               stopVanillaMusic();
               currentState = newState;
               playNew(newState);
            }
         }
      }
   }

   private static void resumeVanillaMusic() {
      suppressingVanilla = false;
      if (currentSound != null) {
         currentSound.startFadeOut();
         currentSound = null;
      }
   }

   private static void stopVanillaMusic() {
      suppressingVanilla = true;
      Minecraft.getInstance().getMusicManager().stopPlaying();
   }

   private static void fadeOutCurrent() {
      if (currentSound != null) {
         currentSound.startFadeOut();
      }
   }

   private static void playNew(BGMState state) {
      SoundEvent sound = switch (state) {
         case RUIN_CITY -> (SoundEvent)PomkotsMechs.BGM_RUIN_CITY.get();
         case RUIN_SCATTERED -> (SoundEvent)PomkotsMechs.BGM_RUIN_SCATTERED.get();
         case BATTLE_BOSS -> (SoundEvent)PomkotsMechs.BGM_BATTLE_BOSS.get();
         case BATTLE_RAID -> (SoundEvent)PomkotsMechs.BGM_BATTLE_RAID.get();
         case OPENING -> (SoundEvent)PomkotsMechs.BGM_OPENING.get();
         default -> null;
      };
      if (sound != null) {
         currentSound = new PomkotsSoundInstance(sound, SoundConfig.loopFadeSE().source(SoundSource.MUSIC).relative(false).volume(0.5F).fadeIn(40).fadeOut(40));
         Minecraft.getInstance().getSoundManager().play(currentSound);
      }
   }
}
