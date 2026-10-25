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
   private Vec3 previousPos = Vec3.ZERO;

   private MuzzleFlashParticle(ClientLevel level, int parentEntityId, int weaponAttachPoint, float size, Vec3 localOffset, SpriteSet sprites) {
      super(level, 0.0, 0.0, 0.0);
      this.sprites = sprites;
      this.initialSize = size;
      this.quadSize = size;
      this.parentEntityId = parentEntityId;
      this.weaponAttachPoint = weaponAttachPoint;
      Vec3 randomOffset = new Vec3(
         (level.random.nextDouble() - 0.5) * 2.0 * 0.025, (level.random.nextDouble() - 0.5) * 2.0 * 0.025, (level.random.nextDouble() - 0.5) * 2.0 * 0.025
      );
      this.localOffset = localOffset.add(randomOffset);
      this.hasPhysics = false;
      this.gravity = 0.0F;
      this.lifetime = 6;
      this.pickSprite(sprites);
      Entity parent = level.getEntity(parentEntityId);
      if (parent == null) {
         this.remove();
      } else {
         Vec3 initialPos = this.calculatePosition(parent, localOffset, false);
         this.setPos(initialPos.x, initialPos.y, initialPos.z);
         this.xo = initialPos.x;
         this.yo = initialPos.y;
         this.zo = initialPos.z;
         this.rCol = 1.0F;
         this.gCol = 1.0F;
         this.bCol = 1.0F;
         this.alpha = 1.0F;
         this.roll = level.random.nextFloat() * (float) (Math.PI * 2);
         this.oRoll = this.roll;
      }
   }

   public void tick() {
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         Entity parent = this.level.getEntity(this.parentEntityId);
         if (parent != null && !parent.isRemoved()) {
            Vec3 previousPos = this.calculatePosition(parent, this.localOffset, true);
            Vec3 currentPos = this.calculatePosition(parent, this.localOffset, false);
            this.xo = previousPos.x;
            this.yo = previousPos.y;
            this.zo = previousPos.z;
            this.x = currentPos.x;
            this.y = currentPos.y;
            this.z = currentPos.z;
            float progress = (float)this.age / this.lifetime;
            float scale = 1.0F - progress;
            if (progress > 0.6F) {
               this.alpha = 1.0F - (progress - 0.6F) / 0.4F;
            } else {
               this.alpha = 1.0F;
            }

            this.previousPos = currentPos;
         } else {
            this.remove();
         }
      }
   }

   private Vec3 calculatePosition(Entity parent, Vec3 localOffset, boolean previousTick) {
      Vec3 parentPos;
      float yaw;
      float pitch;
      if (previousTick) {
         parentPos = new Vec3(parent.xo, parent.yo, parent.zo);
         yaw = parent.yRotO;
         pitch = parent.xRotO;
      } else {
         parentPos = parent.position();
         yaw = parent.getYRot();
         pitch = parent.getXRot();
      }

      Vec3 rotatedOffset = localOffset.xRot((float)Math.toRadians(-pitch)).yRot((float)Math.toRadians(-yaw));
      return parentPos.add(rotatedOffset);
   }

   public float getQuadSize(float partialTick) {
      float progress = (this.age + partialTick) / this.lifetime;
      progress = Mth.clamp(progress, 0.0F, 1.0F);
      float scale = 1.0F - progress;
      scale *= scale;
      return this.initialSize * scale;
   }

   protected int getLightColor(float partialTick) {
      return 15728880;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
