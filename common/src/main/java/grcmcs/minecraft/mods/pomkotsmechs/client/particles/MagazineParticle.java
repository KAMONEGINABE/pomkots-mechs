package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MagazineParticle extends TextureSheetParticle {
   private final SpriteSet spriteSet;
   private float rollSpeed = 0.0F;

   protected MagazineParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet, float rollSpeed) {
      super(level, x, y, z, vx, vy, vz);
      this.spriteSet = spriteSet;
      this.lifetime = 20;
      this.gravity = 0.9F;
      this.setSpriteFromAge(spriteSet);
      this.rollSpeed = rollSpeed;
      this.hasPhysics = false;
   }

   public void tick() {
      if (this.rollSpeed != 0.0F) {
         this.oRoll = this.roll;
         this.roll = this.roll + this.rollSpeed;
      }

      super.tick();
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public Provider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         Particle p = new MagazineParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet, 0.0F);
         p.scale(2.5F);
         p.setLifetime(20);
         return p;
      }
   }

   public static class ProviderL implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public ProviderL(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         Particle p = new MagazineParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet, 0.1F);
         p.scale(2.5F);
         p.setLifetime(20);
         return p;
      }
   }

   public static class ProviderR implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public ProviderR(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         Particle p = new MagazineParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet, -0.1F);
         p.scale(2.5F);
         p.setLifetime(20);
         return p;
      }
   }
}
