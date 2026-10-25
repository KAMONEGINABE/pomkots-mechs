package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class DustHeavyParticle extends TextureSheetParticle {
   private final SpriteSet spriteSet;

   public DustHeavyParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
      super(level, x, y, z, vx, vy, vz);
      this.f_107227_ = 0.1F;
      this.f_107228_ = 0.1F;
      this.f_107229_ = 0.1F;
      this.f_107230_ = 1.0F;
      this.f_107225_ = 10;
      this.f_107219_ = false;
      this.f_107226_ = 0.5F;
      this.spriteSet = spriteSet;
      this.m_108339_(spriteSet);
   }

   public ParticleRenderType m_7556_() {
      return ParticleRenderType.f_107431_;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public Provider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         Particle p = new DustHeavyParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
         p.m_107257_((int)(10.0 * Math.random()) + 10);
         p.m_6569_(0.5F);
         return p;
      }
   }
}
