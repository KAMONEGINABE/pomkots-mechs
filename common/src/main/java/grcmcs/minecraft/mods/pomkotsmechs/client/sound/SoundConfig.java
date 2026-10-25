package grcmcs.minecraft.mods.pomkotsmechs.client.sound;

import java.util.function.Supplier;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class SoundConfig {
   float volume = 1.0F;
   float pitch = 1.0F;
   int fadeInTicks = 0;
   int fadeOutTicks = 0;
   int loopCount = 0;
   int estimatedDurationTicks = 0;
   int maxDurationTicks = 0;
   SoundSource soundSource = SoundSource.MASTER;
   Vec3 position = null;
   Supplier<Vec3> positionSupplier = null;
   Attenuation attenuation = Attenuation.LINEAR;
   boolean relative = false;

   @Override
   public String toString() {
      return this.volume
         + ","
         + this.pitch
         + ","
         + this.fadeInTicks
         + ","
         + this.loopCount
         + ","
         + (this.positionSupplier == null ? "null" : this.positionSupplier.get())
         + ","
         + this.relative;
   }

   public SoundConfig volume(float volume) {
      this.volume = Math.max(0.0F, Math.min(1.0F, volume));
      return this;
   }

   public SoundConfig pitch(float pitch) {
      this.pitch = pitch;
      return this;
   }

   public SoundConfig fadeIn(int ticks) {
      this.fadeInTicks = Math.max(0, ticks);
      return this;
   }

   public SoundConfig fadeOut(int ticks) {
      this.fadeOutTicks = Math.max(0, ticks);
      return this;
   }

   public SoundConfig loop(int count, int estimatedDurationTicks) {
      this.loopCount = count;
      this.estimatedDurationTicks = estimatedDurationTicks;
      return this;
   }

   public SoundConfig loopIndefinitely() {
      this.loopCount = -1;
      return this;
   }

   public SoundConfig maxDuration(int ticks) {
      this.maxDurationTicks = ticks;
      return this;
   }

   public SoundConfig source(SoundSource source) {
      this.soundSource = source;
      return this;
   }

   public SoundConfig at(Vec3 position) {
      this.position = position;
      this.positionSupplier = null;
      return this;
   }

   public SoundConfig at(double x, double y, double z) {
      return this.at(new Vec3(x, y, z));
   }

   public SoundConfig followEntity(Entity entity) {
      this.positionSupplier = entity::m_20182_;
      this.position = null;
      return this;
   }

   public SoundConfig followPosition(Supplier<Vec3> positionSupplier) {
      this.positionSupplier = positionSupplier;
      this.position = null;
      return this;
   }

   public SoundConfig attenuation(Attenuation attenuation) {
      this.attenuation = attenuation;
      return this;
   }

   public SoundConfig relative(boolean relative) {
      this.relative = relative;
      return this;
   }

   public static SoundConfig shortSound() {
      return new SoundConfig().volume(0.8F);
   }

   public static SoundConfig loopFadeSE() {
      return new SoundConfig().volume(1.0F).fadeIn(20).fadeOut(10).loopIndefinitely();
   }

   public static SoundConfig ambient() {
      return new SoundConfig().volume(0.3F).fadeIn(60).fadeOut(60).loopIndefinitely();
   }
}
