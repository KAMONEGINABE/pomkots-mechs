package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class SparkParticle extends TextureSheetParticle {
   private final SpriteSet spriteSet;
   public static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");

   protected SparkParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet spriteSet) {
      super(level, x, y, z, vx, vy, vz);
      this.spriteSet = spriteSet;
      this.f_107225_ = 20;
      this.f_107226_ = 0.9F;
      this.f_107663_ *= 2.0F;
      this.m_108339_(spriteSet);
      this.f_107219_ = false;
   }

   public ParticleRenderType m_7556_() {
      return ParticleRenderType.f_107430_;
   }

   public void m_5989_() {
      super.m_5989_();
   }

   public int m_6355_(float f) {
      return 15728880;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public Provider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
         Particle p = new SparkParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
         if (typeIn == PomkotsMechs.SPARK_RED.get()) {
            p.m_107253_(1.0F, 0.1F, 0.0F);
         } else if (typeIn == PomkotsMechs.SPARK_ORANGE.get()) {
            p.m_107253_(1.0F, 0.5F, 0.0F);
         } else {
            p.m_107253_(1.0F, 1.0F, 0.0F);
         }

         p.m_107257_(20);
         return p;
      }
   }
}
