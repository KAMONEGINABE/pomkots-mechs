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
      this.lifetime = 20;
      this.gravity = 0.0F;
      this.quadSize *= 6.5F;
      this.setSpriteFromAge(spriteSet);
      this.hasPhysics = false;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      super.tick();
      if (!this.removed) {
         this.setSprite(this.spriteSet.get(this.age / 1 % 1 + 1, 1));
      }

      float lifeRatio = (float)this.age / this.lifetime;
      this.alpha = 1.0F - lifeRatio;
   }

   public int getLightColor(float f) {
      float g = this.lifetime / (this.age + f);
      g = Mth.clamp(g, 0.0F, 1.0F);
      int i = super.getLightColor(f);
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
         p.setLifetime(40);
         return p;
      }
   }
}
