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
      this.lifetime = 20;
      this.gravity = 0.0F;
      this.quadSize = 10.0F;
      this.xd = 0.0;
      this.yd = 0.0;
      this.zd = 0.0;
      this.setSpriteFromAge(spriteSet);
      this.hasPhysics = false;
      this.setColor(1.0F, 1.0F, 0.0F);
   }

   public void tick() {
      super.tick();
      this.setSpriteFromAge(this.spriteSet);
      float lifeRatio = (float)this.age / this.lifetime;
      this.alpha = 1.0F - lifeRatio;
      float red = Mth.lerp(lifeRatio, 1.0F, 1.0F);
      float green = Mth.lerp(lifeRatio, 1.0F, 0.5F);
      float blue = Mth.lerp(lifeRatio, 0.0F, 0.0F);
      this.setColor(red, green, blue);
   }

   public int getLightColor(float f) {
      return 15728880;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
