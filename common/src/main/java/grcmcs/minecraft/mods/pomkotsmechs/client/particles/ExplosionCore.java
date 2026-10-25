package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class ExplosionCore extends TextureSheetParticle {
   private final SpriteSet spriteSet;

   public ExplosionCore(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteSet) {
      super(level, x, y, z);
      this.spriteSet = spriteSet;
      this.f_107225_ = 20;
      this.f_107226_ = 0.0F;
      this.f_107663_ = 10.0F;
      this.f_107215_ = 0.0;
      this.f_107216_ = 0.0;
      this.f_107217_ = 0.0;
      this.m_108339_(spriteSet);
      this.f_107219_ = false;
      this.m_107253_(1.0F, 1.0F, 0.0F);
   }

   public void m_5989_() {
      super.m_5989_();
      this.m_108339_(this.spriteSet);
      float lifeRatio = (float)this.f_107224_ / this.f_107225_;
      this.f_107230_ = 1.0F - lifeRatio;
      float red = Mth.m_14179_(lifeRatio, 1.0F, 1.0F);
      float green = Mth.m_14179_(lifeRatio, 1.0F, 0.5F);
      float blue = Mth.m_14179_(lifeRatio, 0.0F, 0.0F);
      this.m_107253_(red, green, blue);
   }

   public int m_6355_(float f) {
      return 15728880;
   }

   public ParticleRenderType m_7556_() {
      return ParticleRenderType.f_107431_;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public Provider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(
         SimpleParticleType type, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ
      ) {
         return new ExplosionCore(world, x, y, z, velocityX, velocityY, velocityZ, this.spriteSet);
      }
   }
}
