package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class ReusableLaserEmitter {
    public static final int HALO_RED = 0xFF140C;
    public static final int HALO_BLUE = 0x0860E8;

    private ReusableLaserEmitter() { }

    public static ReusableLaserEntity pulse(ServerLevel level, Entity owner, Vec3 start, Vec3 target,
                                             float width, float maxRange, float damage,
                                             int visibleTicks, int explosionScale) {
        return spawn(level, owner, start, target, ReusableLaserEntity.Mode.PULSE,
                width, maxRange, damage, visibleTicks, 1, explosionScale, HALO_RED);
    }

    public static ReusableLaserEntity pulse(ServerLevel level, Entity owner, Vec3 start, Vec3 target,
                                             float width, float maxRange, float damage,
                                             int visibleTicks, int explosionScale, int haloColor) {
        return spawn(level, owner, start, target, ReusableLaserEntity.Mode.PULSE,
                width, maxRange, damage, visibleTicks, 1, explosionScale, haloColor);
    }

    public static ReusableLaserEntity sustained(ServerLevel level, Entity owner, Vec3 start, Vec3 target,
                                                 float width, float maxRange, float damage,
                                                 int durationTicks, int damageIntervalTicks,
                                                 int explosionScale) {
        return spawn(level, owner, start, target, ReusableLaserEntity.Mode.SUSTAINED,
                width, maxRange, damage, durationTicks, damageIntervalTicks, explosionScale, HALO_RED);
    }

    public static ReusableLaserEntity sustained(ServerLevel level, Entity owner, Vec3 start, Vec3 target,
                                                 float width, float maxRange, float damage,
                                                 int durationTicks, int damageIntervalTicks,
                                                 int explosionScale, int haloColor) {
        return spawn(level, owner, start, target, ReusableLaserEntity.Mode.SUSTAINED,
                width, maxRange, damage, durationTicks, damageIntervalTicks, explosionScale, haloColor);
    }

    private static ReusableLaserEntity spawn(ServerLevel level, Entity owner, Vec3 start, Vec3 target,
                                              ReusableLaserEntity.Mode mode, float width, float maxRange,
                                              float damage, int durationTicks, int damageIntervalTicks,
                                              int explosionScale, int haloColor) {
        ReusableLaserEntity laser = PomkotsMechs.REUSABLE_LASER.get().create(level);
        if (laser == null) return null;
        laser.configure(owner, start, target, mode, width, maxRange, damage,
                durationTicks, damageIntervalTicks, explosionScale, haloColor);
        return level.addFreshEntity(laser) ? laser : null;
    }
}
