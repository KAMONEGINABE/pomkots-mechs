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
      this.f_107225_ = 20;
      this.f_107226_ = 0.9F;
      this.m_108339_(spriteSet);
      this.rollSpeed = rollSpeed;
      this.f_107219_ = false;
   }

   public void m_5989_() {
      if (this.rollSpeed != 0.0F) {
         this.f_107204_ = this.f_107231_;
         this.f_107231_ = this.f_107231_ + this.rollSpeed;
      }

      super.m_5989_();
   }

   public ParticleRenderType m_7556_() {
      return ParticleRenderType.f_107430_;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public Provider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         Particle p = new MagazineParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet, 0.0F);
         p.m_6569_(2.5F);
         p.m_107257_(20);
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
         p.m_6569_(2.5F);
         p.m_107257_(20);
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
         p.m_6569_(2.5F);
         p.m_107257_(20);
         return p;
      }
   }
}
