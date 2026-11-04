package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ProjectileUtil {
    public static boolean isDestructionAllowed(Entity ent) {
        return Utils.isBlockDestructionAllowed(ent);
    }

    public static HitResult raycastBoundingCheck(Projectile entity) {
        // 弾速が早すぎると、ティック間にすり抜けちゃうのでレイキャスティングで補完
        Vec3 currentPosition = entity.position();
        Vec3 nextPosition = currentPosition.add(entity.getDeltaMovement());

        HitResult hitResult = entity.level().clip(new ClipContext(
                currentPosition,
                nextPosition,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        ));

        return hitResult;
    }

    public static boolean isSuperbWarfareVehicle(Entity target) {
        if (target == null) {
            return false;
        }
        return target.getClass().getName().contains("superbwarfare.entity.vehicle");
    }

    public static void hurt(Entity target, PomkotsThrowableProjectile prj, Entity cause, float damage) {
        hurt(target, (Entity) prj, cause, damage);
    }

    public static void hurt(Entity target, Entity projectileOrNull, Entity cause, float damage) {
        if (target instanceof PomkotsVehicleBase v1 && cause instanceof PomkotsVehicleBase v2) {
            var d1 = v1.getDrivingPassenger();
            var d2 = v2.getDrivingPassenger();

            if (d1 != null && d2 != null && d1.isAlliedTo(d2)) {
                if (d1.getTeam() != null && !d1.getTeam().isAllowFriendlyFire()) {
                    return;
                }
            }
        }

        Entity rootCause = null;
        if (cause instanceof PomkotsVehicleBase vehicle) {
            var driver = vehicle.getDrivingPassenger();
            if (driver != null) {
                rootCause = driver;
            } else {
                rootCause = cause;
            }
        } else {
            rootCause = cause;
        }

        boolean isVehicle = isSuperbWarfareVehicle(target);
        float vehicleMultiplier = 1.0F;
        DamageSource ds;

        if (projectileOrNull instanceof BulletBeamEntity || projectileOrNull instanceof ReusableLaserEntity) {
            // ビーム・レーザー兵器: 対車両30%
            vehicleMultiplier = 0.3F;
            if (rootCause != null) {
                ds = target.damageSources().source(PomkotsMechs.ENERGY_BEAM_DAMAGE_TYPE, projectileOrNull, rootCause);
            } else {
                ds = target.damageSources().source(PomkotsMechs.ENERGY_BEAM_DAMAGE_TYPE, projectileOrNull);
            }
        } else if (projectileOrNull instanceof GiantTomahawkEntity) {
            // 投擲斧: 対車両30%
            vehicleMultiplier = 0.3F;
            if (rootCause != null) {
                ds = target.damageSources().source(PomkotsMechs.ANTI_ARMOR_DAMAGE_TYPE, projectileOrNull, rootCause);
            } else {
                ds = target.damageSources().source(PomkotsMechs.ANTI_ARMOR_DAMAGE_TYPE, projectileOrNull);
            }
        } else if (projectileOrNull instanceof BulletRifleEntity) {
            // スナイパーライフル: 対車両30%
            vehicleMultiplier = 0.3F;
            if (rootCause != null) {
                ds = target.damageSources().source(PomkotsMechs.ANTI_ARMOR_DAMAGE_TYPE, projectileOrNull, rootCause);
            } else {
                ds = target.damageSources().source(PomkotsMechs.ANTI_ARMOR_DAMAGE_TYPE, projectileOrNull);
            }
        } else if (projectileOrNull instanceof BulletGrenadeEntity) {
            // グレネード弾: 対車両100%
            vehicleMultiplier = 1.0F;
            if (rootCause != null) {
                ds = target.damageSources().source(PomkotsMechs.GRENADE_DAMAGE_TYPE, projectileOrNull, rootCause);
            } else {
                ds = target.damageSources().source(PomkotsMechs.GRENADE_DAMAGE_TYPE, projectileOrNull);
            }
        } else if (projectileOrNull instanceof MissileGenericLargeEntity) {
            // 大型ミサイル: 対車両100%
            vehicleMultiplier = 1.0F;
            if (rootCause != null) {
                ds = target.damageSources().source(PomkotsMechs.HEAVY_MISSILE_DAMAGE_TYPE, projectileOrNull, rootCause);
            } else {
                ds = target.damageSources().source(PomkotsMechs.HEAVY_MISSILE_DAMAGE_TYPE, projectileOrNull);
            }
        } else if (projectileOrNull == null) {
            if (rootCause instanceof Player p) {
                ds = cause.damageSources().playerAttack(p);
            } else {
                ds = cause.damageSources().generic();
            }
        } else {
            if (rootCause == null) {
                rootCause = projectileOrNull;
            }
            ds = target.damageSources().thrown(projectileOrNull, rootCause);
        }

        float finalDamage = isVehicle ? (damage * vehicleMultiplier) : damage;
        if (finalDamage > 0.0F) {
            target.hurt(ds, finalDamage);
        }
    }
}
