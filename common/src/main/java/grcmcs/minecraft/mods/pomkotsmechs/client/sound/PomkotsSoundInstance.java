package grcmcs.minecraft.mods.pomkotsmechs.client.sound;

import java.util.function.Supplier;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class PomkotsSoundInstance extends AbstractTickableSoundInstance {
   private final SoundConfig config;
   private int tickCount = 0;
   private float currentVolume = 0.0F;
   private boolean isFadingOut = false;
   private int loopCount = 0;
   private float targetVolume;
   private final Supplier<Vec3> positionSupplier;

   protected PomkotsSoundInstance(SoundEvent soundEvent, SoundConfig config) {
      super(soundEvent, config.soundSource, RandomSource.create());
      this.config = config;
      this.targetVolume = config.volume;
      this.volume = config.fadeInTicks > 0 ? 0.0F : config.volume;
      this.currentVolume = this.volume;
      this.pitch = config.pitch;
      this.looping = config.loopCount != 0;
      if (config.positionSupplier != null) {
         this.positionSupplier = config.positionSupplier;
         this.attenuation = config.attenuation;
         Vec3 pos = config.positionSupplier.get();
         this.x = pos.x;
         this.y = pos.y;
         this.z = pos.z;
      } else if (config.position != null) {
         this.positionSupplier = () -> config.position;
         this.attenuation = config.attenuation;
         this.x = config.position.x;
         this.y = config.position.y;
         this.z = config.position.z;
      } else {
         this.positionSupplier = null;
         this.attenuation = Attenuation.NONE;
         this.x = 0.0;
         this.y = 0.0;
         this.z = 0.0;
      }

      this.relative = config.relative;
   }

   public boolean canStartSilent() {
      return true;
   }

   public void tick() {
      if (!this.isStopped()) {
         this.tickCount++;
         if (this.positionSupplier != null && !this.config.relative) {
            Vec3 pos = this.positionSupplier.get();
            this.x = pos.x;
            this.y = pos.y;
            this.z = pos.z;
         }

         if (this.isFadingOut) {
            if (this.config.fadeOutTicks > 0) {
               this.currentVolume = this.currentVolume - this.targetVolume / this.config.fadeOutTicks;
               if (this.currentVolume <= 0.0F) {
                  this.currentVolume = 0.0F;
                  this.stop();
               }
            } else {
               this.stop();
            }

            this.volume = this.currentVolume;
         } else {
            if (this.tickCount <= this.config.fadeInTicks) {
               this.currentVolume = this.targetVolume * this.tickCount / this.config.fadeInTicks;
               this.volume = this.currentVolume;
            } else {
               this.volume = this.targetVolume;
               this.currentVolume = this.targetVolume;
            }

            if (this.config.loopCount > 0) {
               int estimatedDuration = this.config.estimatedDurationTicks;
               if (estimatedDuration > 0 && this.tickCount >= estimatedDuration * (this.loopCount + 1)) {
                  this.loopCount++;
                  if (this.loopCount >= this.config.loopCount) {
                     this.startFadeOut();
                  }
               }
            }

            if (this.config.maxDurationTicks > 0 && this.tickCount >= this.config.maxDurationTicks) {
               this.startFadeOut();
            }
         }
      }
   }

   public void startFadeOut() {
      this.isFadingOut = true;
      this.looping = false;
   }

   public void stopImmediately() {
      this.stop();
   }
}
