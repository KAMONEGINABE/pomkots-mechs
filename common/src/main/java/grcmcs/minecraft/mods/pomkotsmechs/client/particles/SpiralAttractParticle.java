package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class SpiralAttractParticle extends TextureSheetParticle {
   private final double baseRadius;
   private final double spiralSpeed;
   private final double attractionStrength;
   private float angle;
   private Entity target = null;

   protected SpiralAttractParticle(ClientLevel level, double x, double y, double z, Entity target, SpriteSet spriteSet) {
      super(level, x, y, z, 0.0, 0.0, 0.0);
      this.target = target;
      this.baseRadius = 1.5 + this.random.nextDouble() * 0.5;
      this.spiralSpeed = 0.3 + this.random.nextDouble() * 0.2;
      this.attractionStrength = 0.05;
      this.angle = this.random.nextFloat() * (float) Math.PI * 2.0F;
      this.lifetime = 30 + this.random.nextInt(10);
      this.gravity = 0.0F;
      this.setSpriteFromAge(spriteSet);
      this.quadSize *= 2.0F;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      super.tick();
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else if (this.target == null) {
         this.remove();
      } else {
         Vec3 targetPos = new Vec3(0.0, 3.4, -1.5);
         targetPos = this.target.position().add(targetPos.yRot((float)Math.toRadians(-1.0 * this.target.getYRot())));
         this.angle = (float)(this.angle + this.spiralSpeed);
         double radius = this.baseRadius * (1.0 - (double)this.age / this.lifetime);
         double offsetX = Math.cos(this.angle) * radius;
         double offsetZ = Math.sin(this.angle) * radius;
         double offsetY = Math.sin(this.angle * 0.3) * 0.5 * radius;
         Vec3 currentPos = new Vec3(this.x, this.y, this.z);
         Vec3 attractDir = targetPos.subtract(currentPos).normalize().scale(this.attractionStrength);
         this.xd = attractDir.x + (offsetX - (this.x - targetPos.x)) * 0.05;
         this.yd = attractDir.y + (offsetY - (this.y - targetPos.y)) * 0.05;
         this.zd = attractDir.z + (offsetZ - (this.z - targetPos.z)) * 0.05;
         this.move(this.xd, this.yd, this.zd);
      }
   }

   public int getLightColor(float f) {
      return 15728880;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public Provider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
         Entity target = Minecraft.getInstance().level.getEntity((int)dx);
         return new SpiralAttractParticle(level, x, y, z, target, this.spriteSet);
      }
   }
}
