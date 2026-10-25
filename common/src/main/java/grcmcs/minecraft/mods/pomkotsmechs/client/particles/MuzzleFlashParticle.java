package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class MuzzleFlashParticle extends TextureSheetParticle {
   private static final int LIFETIME = 5;
   private static final double POSITION_JITTER = 0.025;
   private final SpriteSet sprites;
   private final float initialSize;
   private final int parentEntityId;
   private final int weaponAttachPoint;
   private final Vec3 localOffset;
   private Vec3 previousPos = Vec3.f_82478_;

   private MuzzleFlashParticle(ClientLevel level, int parentEntityId, int weaponAttachPoint, float size, Vec3 localOffset, SpriteSet sprites) {
      super(level, 0.0, 0.0, 0.0);
      this.sprites = sprites;
      this.initialSize = size;
      this.f_107663_ = size;
      this.parentEntityId = parentEntityId;
      this.weaponAttachPoint = weaponAttachPoint;
      Vec3 randomOffset = new Vec3(
         (level.f_46441_.m_188500_() - 0.5) * 2.0 * 0.025, (level.f_46441_.m_188500_() - 0.5) * 2.0 * 0.025, (level.f_46441_.m_188500_() - 0.5) * 2.0 * 0.025
      );
      this.localOffset = localOffset.m_82549_(randomOffset);
      this.f_107219_ = false;
      this.f_107226_ = 0.0F;
      this.f_107225_ = 6;
      this.m_108335_(sprites);
      Entity parent = level.m_6815_(parentEntityId);
      if (parent == null) {
         this.m_107274_();
      } else {
         Vec3 initialPos = this.calculatePosition(parent, localOffset, false);
         this.m_107264_(initialPos.f_82479_, initialPos.f_82480_, initialPos.f_82481_);
         this.f_107209_ = initialPos.f_82479_;
         this.f_107210_ = initialPos.f_82480_;
         this.f_107211_ = initialPos.f_82481_;
         this.f_107227_ = 1.0F;
         this.f_107228_ = 1.0F;
         this.f_107229_ = 1.0F;
         this.f_107230_ = 1.0F;
         this.f_107231_ = level.f_46441_.m_188501_() * (float) (Math.PI * 2);
         this.f_107204_ = this.f_107231_;
      }
   }

   public void m_5989_() {
      if (this.f_107224_++ >= this.f_107225_) {
         this.m_107274_();
      } else {
         Entity parent = this.f_107208_.m_6815_(this.parentEntityId);
         if (parent != null && !parent.m_213877_()) {
            Vec3 previousPos = this.calculatePosition(parent, this.localOffset, true);
            Vec3 currentPos = this.calculatePosition(parent, this.localOffset, false);
            this.f_107209_ = previousPos.f_82479_;
            this.f_107210_ = previousPos.f_82480_;
            this.f_107211_ = previousPos.f_82481_;
            this.f_107212_ = currentPos.f_82479_;
            this.f_107213_ = currentPos.f_82480_;
            this.f_107214_ = currentPos.f_82481_;
            float progress = (float)this.f_107224_ / this.f_107225_;
            float scale = 1.0F - progress;
            if (progress > 0.6F) {
               this.f_107230_ = 1.0F - (progress - 0.6F) / 0.4F;
            } else {
               this.f_107230_ = 1.0F;
            }

            this.previousPos = currentPos;
         } else {
            this.m_107274_();
         }
      }
   }

   private Vec3 calculatePosition(Entity parent, Vec3 localOffset, boolean previousTick) {
      Vec3 parentPos;
      float yaw;
      float pitch;
      if (previousTick) {
         parentPos = new Vec3(parent.f_19854_, parent.f_19855_, parent.f_19856_);
         yaw = parent.f_19859_;
         pitch = parent.f_19860_;
      } else {
         parentPos = parent.m_20182_();
         yaw = parent.m_146908_();
         pitch = parent.m_146909_();
      }

      Vec3 rotatedOffset = localOffset.m_82496_((float)Math.toRadians(-pitch)).m_82524_((float)Math.toRadians(-yaw));
      return parentPos.m_82549_(rotatedOffset);
   }

   public float m_5902_(float partialTick) {
      float progress = (this.f_107224_ + partialTick) / this.f_107225_;
      progress = Mth.m_14036_(progress, 0.0F, 1.0F);
      float scale = 1.0F - progress;
      scale *= scale;
      return this.initialSize * scale;
   }

   protected int m_6355_(float partialTick) {
      return 15728880;
   }

   public ParticleRenderType m_7556_() {
      return ParticleRenderType.f_107431_;
   }

   public static final class Provider implements ParticleProvider<AttachedMuzzleFlashOptions> {
      private final SpriteSet sprites;

      public Provider(SpriteSet sprites) {
         this.sprites = sprites;
      }

      public Particle createParticle(
         AttachedMuzzleFlashOptions options, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ
      ) {
         return new MuzzleFlashParticle(
            level,
            options.parentEntityId(),
            options.weaponPoint(),
            options.size(),
            new Vec3(options.localX(), options.localY(), options.localZ()),
            this.sprites
         );
      }
   }
}
