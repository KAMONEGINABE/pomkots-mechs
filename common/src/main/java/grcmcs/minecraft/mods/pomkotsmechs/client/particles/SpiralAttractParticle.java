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
      this.baseRadius = 1.5 + this.f_107223_.m_188500_() * 0.5;
      this.spiralSpeed = 0.3 + this.f_107223_.m_188500_() * 0.2;
      this.attractionStrength = 0.05;
      this.angle = this.f_107223_.m_188501_() * (float) Math.PI * 2.0F;
      this.f_107225_ = 30 + this.f_107223_.m_188503_(10);
      this.f_107226_ = 0.0F;
      this.m_108339_(spriteSet);
      this.f_107663_ *= 2.0F;
   }

   public ParticleRenderType m_7556_() {
      return ParticleRenderType.f_107430_;
   }

   public void m_5989_() {
      super.m_5989_();
      if (this.f_107224_++ >= this.f_107225_) {
         this.m_107274_();
      } else if (this.target == null) {
         this.m_107274_();
      } else {
         Vec3 targetPos = new Vec3(0.0, 3.4, -1.5);
         targetPos = this.target.m_20182_().m_82549_(targetPos.m_82524_((float)Math.toRadians(-1.0 * this.target.m_146908_())));
         this.angle = (float)(this.angle + this.spiralSpeed);
         double radius = this.baseRadius * (1.0 - (double)this.f_107224_ / this.f_107225_);
         double offsetX = Math.cos(this.angle) * radius;
         double offsetZ = Math.sin(this.angle) * radius;
         double offsetY = Math.sin(this.angle * 0.3) * 0.5 * radius;
         Vec3 currentPos = new Vec3(this.f_107212_, this.f_107213_, this.f_107214_);
         Vec3 attractDir = targetPos.m_82546_(currentPos).m_82541_().m_82490_(this.attractionStrength);
         this.f_107215_ = attractDir.f_82479_ + (offsetX - (this.f_107212_ - targetPos.f_82479_)) * 0.05;
         this.f_107216_ = attractDir.f_82480_ + (offsetY - (this.f_107213_ - targetPos.f_82480_)) * 0.05;
         this.f_107217_ = attractDir.f_82481_ + (offsetZ - (this.f_107214_ - targetPos.f_82481_)) * 0.05;
         this.m_6257_(this.f_107215_, this.f_107216_, this.f_107217_);
      }
   }

   public int m_6355_(float f) {
      return 15728880;
   }

   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet spriteSet;

      public Provider(SpriteSet spriteSet) {
         this.spriteSet = spriteSet;
      }

      public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
         Entity target = Minecraft.m_91087_().f_91073_.m_6815_((int)dx);
         return new SpiralAttractParticle(level, x, y, z, target, this.spriteSet);
      }
   }
}
