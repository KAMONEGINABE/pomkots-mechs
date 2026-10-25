package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class FireParticle extends TextureSheetParticle {
   private final SpriteSet spriteSet;
   public static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");

   protected FireParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
      super(level, x, y, z, vx, vy, vz);
      this.spriteSet = spriteSet;
      this.f_107225_ = 20;
      this.f_107226_ = 0.0F;
      this.f_107663_ *= 6.5F;
      this.m_108339_(spriteSet);
      this.f_107219_ = false;
   }

   public ParticleRenderType m_7556_() {
      return ParticleRenderType.f_107430_;
   }

   public void m_5989_() {
      super.m_5989_();
      if (!this.f_107220_) {
         this.m_108337_(this.spriteSet.m_5819_(this.f_107224_ / 1 % 1 + 1, 1));
      }

      float lifeRatio = (float)this.f_107224_ / this.f_107225_;
      this.f_107230_ = 1.0F - lifeRatio;
   }

   public int m_6355_(float f) {
      float g = this.f_107225_ / (this.f_107224_ + f);
      g = Mth.m_14036_(g, 0.0F, 1.0F);
      int i = super.m_6355_(f);
      int j = i & 0xFF;
      int k = i >> 16 & 0xFF;
      j += (int)(g * 15.0F * 16.0F);
      if (j > 240) {
         j = 240;
      }

      return j | k << 16;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public Provider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         Particle p = new FireParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
         p.m_107257_(40);
         return p;
      }
   }
}
