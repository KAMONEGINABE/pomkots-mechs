package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public abstract class PomkotsThrowableProjectile extends ThrowableProjectile {
   protected LivingEntity shooter;
   private float soundEffectVolume = -1.0F;

   public PomkotsThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      this(entityType, null, world);
   }

   public PomkotsThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, LivingEntity shooter, Level world) {
      super(entityType, world);
      this.shooter = shooter;
   }

   public void tick() {
      this.soundEffectVolume = -1.0F;
      super.tick();
   }

   public void onHitEntityPublic(Entity entity) {
      this.onHitEntity(new EntityHitResult(entity));
   }

   public float getHitDamage() {
      return 0.0F;
   }

   public boolean shouldBeSaved() {
      return false;
   }

   public boolean ignoreExplosion() {
      return true;
   }

   public LivingEntity getShooter() {
      return this.shooter;
   }

   protected void playSoundEffect(SoundEvent event) {
      this.playSoundEffect(event, 1.0F);
   }

   protected void playSoundEffect(SoundEvent event, float volume) {
      this.playSoundEffect(event, volume, 64.0);
   }

   protected void playSoundEffect(SoundEvent event, float volume, double maxDistance) {
      if (this.soundEffectVolume < 0.0F) {
         this.soundEffectVolume = this.computeVolume(maxDistance);
      }

      if (this.soundEffectVolume > 0.0F) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, volume * this.soundEffectVolume, 1.0F, false);
      }
   }

   private float computeVolume(double maxDistance) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player == null) {
         return 0.0F;
      }

      double distance = player.distanceTo(this);
      if (distance > maxDistance) {
         return 0.0F;
      }

      float volume = 1.0F - (float)(distance / maxDistance);
      return Mth.clamp(volume, 0.0F, 1.0F);
   }
}
