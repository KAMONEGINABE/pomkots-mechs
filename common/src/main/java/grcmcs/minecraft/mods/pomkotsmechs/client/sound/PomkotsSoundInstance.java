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
      super(soundEvent, config.soundSource, RandomSource.m_216327_());
      this.config = config;
      this.targetVolume = config.volume;
      this.f_119573_ = config.fadeInTicks > 0 ? 0.0F : config.volume;
      this.currentVolume = this.f_119573_;
      this.f_119574_ = config.pitch;
      this.f_119578_ = config.loopCount != 0;
      if (config.positionSupplier != null) {
         this.positionSupplier = config.positionSupplier;
         this.f_119580_ = config.attenuation;
         Vec3 pos = config.positionSupplier.get();
         this.f_119575_ = pos.f_82479_;
         this.f_119576_ = pos.f_82480_;
         this.f_119577_ = pos.f_82481_;
      } else if (config.position != null) {
         this.positionSupplier = () -> config.position;
         this.f_119580_ = config.attenuation;
         this.f_119575_ = config.position.f_82479_;
         this.f_119576_ = config.position.f_82480_;
         this.f_119577_ = config.position.f_82481_;
      } else {
         this.positionSupplier = null;
         this.f_119580_ = Attenuation.NONE;
         this.f_119575_ = 0.0;
         this.f_119576_ = 0.0;
         this.f_119577_ = 0.0;
      }

      this.f_119582_ = config.relative;
   }

   public boolean m_7784_() {
      return true;
   }

   public void m_7788_() {
      if (!this.m_7801_()) {
         this.tickCount++;
         if (this.positionSupplier != null && !this.config.relative) {
            Vec3 pos = this.positionSupplier.get();
            this.f_119575_ = pos.f_82479_;
            this.f_119576_ = pos.f_82480_;
            this.f_119577_ = pos.f_82481_;
         }

         if (this.isFadingOut) {
            if (this.config.fadeOutTicks > 0) {
               this.currentVolume = this.currentVolume - this.targetVolume / this.config.fadeOutTicks;
               if (this.currentVolume <= 0.0F) {
                  this.currentVolume = 0.0F;
                  this.m_119609_();
               }
            } else {
               this.m_119609_();
            }

            this.f_119573_ = this.currentVolume;
         } else {
            if (this.tickCount <= this.config.fadeInTicks) {
               this.currentVolume = this.targetVolume * this.tickCount / this.config.fadeInTicks;
               this.f_119573_ = this.currentVolume;
            } else {
               this.f_119573_ = this.targetVolume;
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
      this.f_119578_ = false;
   }

   public void stopImmediately() {
      this.m_119609_();
   }
}
