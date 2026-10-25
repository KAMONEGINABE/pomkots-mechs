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
      if (hitTarget.m_9236_().f_46443_) {
         Vec3 offset = hitTarget.m_20191_().m_82399_();
         addSparkParticles(offset, hitTarget.m_9236_());
      }
   }

   public static void addParticles(DamageSource ds, Entity hitTarget) {
      if (hitTarget.m_9236_().f_46443_) {
         Entity srcEnt = ds.m_7640_();
         if (srcEnt != null) {
            Vec3 offset;
            if (srcEnt.m_20191_().m_82309_() < 8.0 && srcEnt instanceof ThrowableProjectile) {
               offset = srcEnt.m_20191_().m_82399_();
            } else {
               offset = hitTarget.m_20191_().m_82399_();
            }

            addSparkParticles(offset, hitTarget.m_9236_());
         }
      }
   }

   public static void addSparkParticles(Vec3 offset, Level level) {
      RandomSource random = level.m_213780_();

      for (int i = 0; i < 10; i++) {
         double velocityX = random.m_188500_() * 2.0 - 1.0;
         double velocityY = random.m_188500_() * 2.0 - 1.0;
         double velocityZ = random.m_188500_() * 2.0 - 1.0;
         level.m_6485_((ParticleOptions)PomkotsMechs.SPARK.get(), true, offset.m_7096_(), offset.m_7098_(), offset.m_7094_(), velocityX, velocityY, velocityZ);
      }
   }

   public static void addSparkParticles(Vec3 offset, Level level, int num, ParticleOptions particle) {
      RandomSource random = level.m_213780_();

      for (int i = 0; i < num; i++) {
         double velocityX = random.m_188500_() * 2.0 - 1.0;
         double velocityY = random.m_188500_() * 2.0 - 1.0;
         double velocityZ = random.m_188500_() * 2.0 - 1.0;
         level.m_6485_(particle, true, offset.m_7096_(), offset.m_7098_(), offset.m_7094_(), velocityX, velocityY, velocityZ);
      }
   }

   public static void addSparkParticlesMedium(Vec3 offset, Level level) {
      RandomSource random = level.m_213780_();

      for (int i = 0; i < 20; i++) {
         double velocityX = random.m_188500_() * 2.0 - 1.0;
         double velocityY = random.m_188500_() * 2.0 - 1.0;
         double velocityZ = random.m_188500_() * 2.0 - 1.0;
         level.m_6485_((ParticleOptions)PomkotsMechs.SPARK.get(), true, offset.m_7096_(), offset.m_7098_(), offset.m_7094_(), velocityX, velocityY, velocityZ);
      }
   }

   public static void addSparkParticlesSmall(Vec3 offset, Level level) {
      RandomSource random = level.m_213780_();

      for (int i = 0; i < 5; i++) {
         double velocityX = random.m_188500_() * 2.0 - 1.0;
         double velocityY = random.m_188500_() * 2.0 - 1.0;
         double velocityZ = random.m_188500_() * 2.0 - 1.0;
         level.m_6485_((ParticleOptions)PomkotsMechs.SPARK.get(), true, offset.m_7096_(), offset.m_7098_(), offset.m_7094_(), velocityX, velocityY, velocityZ);
      }
   }

   public static void spawnAttachedMuzzleFlash(ClientLevel level, Entity mech, int weaponAttachPoint, double size, Vec3 localOffset) {
      AttachedMuzzleFlashOptions options = new AttachedMuzzleFlashOptions(
         mech.m_19879_(), weaponAttachPoint, (float)size, (float)localOffset.f_82479_, (float)localOffset.f_82480_, (float)localOffset.f_82481_
      );
      level.m_7106_(options, localOffset.f_82479_, localOffset.f_82480_, localOffset.f_82481_, 0.0, 0.0, 0.0);
   }

   public static void spawnAttachedMuzzleFlash(ServerLevel level, Entity mech, double size, Vec3 localOffset) {
      AttachedMuzzleFlashOptions options = new AttachedMuzzleFlashOptions(
         mech.m_19879_(), 0, (float)size, (float)localOffset.f_82479_, (float)localOffset.f_82480_, (float)localOffset.f_82481_
      );

      for (ServerPlayer player : level.m_6907_()) {
         if (!(player.m_20280_(mech) > 40000.0)) {
            level.m_8624_(player, options, true, mech.m_20185_(), mech.m_20186_(), mech.m_20189_(), 1, 0.0, 0.0, 0.0, 0.0);
         }
      }
   }
}
