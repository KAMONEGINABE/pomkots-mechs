package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ProjectileUtil {
   public static boolean isDestructionAllowed(Entity ent) {
      return Utils.isBlockDestructionAllowed(ent);
   }

   public static HitResult raycastBoundingCheck(Projectile entity) {
      Vec3 currentPosition = entity.m_20182_();
      Vec3 nextPosition = currentPosition.m_82549_(entity.m_20184_());
      HitResult hitResult = entity.m_9236_().m_45547_(new ClipContext(currentPosition, nextPosition, Block.COLLIDER, Fluid.NONE, entity));
      return hitResult;
   }

   public static void hurt(Entity target, PomkotsThrowableProjectile prj, Entity cause, float damage) {
      if (target instanceof PomkotsVehicleBase v1 && cause instanceof PomkotsVehicleBase v2) {
         LivingEntity d1 = v1.getDrivingPassenger();
         LivingEntity d2 = v2.getDrivingPassenger();
         if (d1 != null && d2 != null && d1.m_7307_(d2) && d1.m_5647_() != null && !d1.m_5647_().m_6260_()) {
            return;
         }
      }

      Entity rootCause = null;
      if (cause instanceof PomkotsVehicleBase vehicle) {
         LivingEntity driver = vehicle.getDrivingPassenger();
         if (driver != null) {
            rootCause = driver;
         } else {
            rootCause = cause;
         }
      } else {
         rootCause = cause;
      }

      DamageSource ds;
      if (prj == null) {
         if (rootCause instanceof Player p) {
            ds = cause.m_269291_().m_269075_(p);
         } else {
            ds = cause.m_269291_().m_269264_();
         }
      } else {
         if (rootCause == null) {
            rootCause = prj;
         }

         ds = target.m_269291_().m_269390_(prj, rootCause);
      }

      target.m_6469_(ds, damage);
   }
}
