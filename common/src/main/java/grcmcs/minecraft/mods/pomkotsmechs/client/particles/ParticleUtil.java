package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticleUtil {
   private static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");

   public static void addParticles(Entity hitTarget) {
      if (hitTarget.level().isClientSide) {
         Vec3 offset = hitTarget.getBoundingBox().getCenter();
         addSparkParticles(offset, hitTarget.level());
      }
   }

   public static void addParticles(DamageSource ds, Entity hitTarget) {
      if (hitTarget.level().isClientSide) {
         Entity srcEnt = ds.getDirectEntity();
         if (srcEnt != null) {
            Vec3 offset;
            if (srcEnt.getBoundingBox().getSize() < 8.0 && srcEnt instanceof ThrowableProjectile) {
               offset = srcEnt.getBoundingBox().getCenter();
            } else {
               offset = hitTarget.getBoundingBox().getCenter();
            }

            addSparkParticles(offset, hitTarget.level());
         }
      }
   }

   public static void addSparkParticles(Vec3 offset, Level level) {
      RandomSource random = level.getRandom();

      for (int i = 0; i < 10; i++) {
         double velocityX = random.nextDouble() * 2.0 - 1.0;
         double velocityY = random.nextDouble() * 2.0 - 1.0;
         double velocityZ = random.nextDouble() * 2.0 - 1.0;
         level.addAlwaysVisibleParticle((ParticleOptions)PomkotsMechs.SPARK.get(), true, offset.x(), offset.y(), offset.z(), velocityX, velocityY, velocityZ);
      }
   }

   public static void addSparkParticles(Vec3 offset, Level level, int num, ParticleOptions particle) {
      RandomSource random = level.getRandom();

      for (int i = 0; i < num; i++) {
         double velocityX = random.nextDouble() * 2.0 - 1.0;
         double velocityY = random.nextDouble() * 2.0 - 1.0;
         double velocityZ = random.nextDouble() * 2.0 - 1.0;
         level.addAlwaysVisibleParticle(particle, true, offset.x(), offset.y(), offset.z(), velocityX, velocityY, velocityZ);
      }
   }

   public static void addSparkParticlesMedium(Vec3 offset, Level level) {
      RandomSource random = level.getRandom();

      for (int i = 0; i < 20; i++) {
         double velocityX = random.nextDouble() * 2.0 - 1.0;
         double velocityY = random.nextDouble() * 2.0 - 1.0;
         double velocityZ = random.nextDouble() * 2.0 - 1.0;
         level.addAlwaysVisibleParticle((ParticleOptions)PomkotsMechs.SPARK.get(), true, offset.x(), offset.y(), offset.z(), velocityX, velocityY, velocityZ);
      }
   }

   public static void addSparkParticlesSmall(Vec3 offset, Level level) {
      RandomSource random = level.getRandom();

      for (int i = 0; i < 5; i++) {
         double velocityX = random.nextDouble() * 2.0 - 1.0;
         double velocityY = random.nextDouble() * 2.0 - 1.0;
         double velocityZ = random.nextDouble() * 2.0 - 1.0;
         level.addAlwaysVisibleParticle((ParticleOptions)PomkotsMechs.SPARK.get(), true, offset.x(), offset.y(), offset.z(), velocityX, velocityY, velocityZ);
      }
   }

   public static void spawnAttachedMuzzleFlash(ClientLevel level, Entity mech, int weaponAttachPoint, double size, Vec3 localOffset) {
      AttachedMuzzleFlashOptions options = new AttachedMuzzleFlashOptions(
         mech.getId(), weaponAttachPoint, (float)size, (float)localOffset.x, (float)localOffset.y, (float)localOffset.z
      );
      level.addParticle(options, localOffset.x, localOffset.y, localOffset.z, 0.0, 0.0, 0.0);
   }

   public static void spawnAttachedMuzzleFlash(ServerLevel level, Entity mech, double size, Vec3 localOffset) {
      AttachedMuzzleFlashOptions options = new AttachedMuzzleFlashOptions(
         mech.getId(), 0, (float)size, (float)localOffset.x, (float)localOffset.y, (float)localOffset.z
      );

      for (ServerPlayer player : level.players()) {
         if (!(player.distanceToSqr(mech) > 40000.0)) {
            level.sendParticles(player, options, true, mech.getX(), mech.getY(), mech.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }
}
