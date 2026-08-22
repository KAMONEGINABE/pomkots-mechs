package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class ReusableLaserEmitter {
    private ReusableLaserEmitter() { }

    public static ReusableLaserEntity pulse(ServerLevel level, Entity owner, Vec3 start, Vec3 target,
                                             float width, float maxRange, float damage,
                                             int visibleTicks, int explosionScale) {
        return spawn(level, owner, start, target, ReusableLaserEntity.Mode.PULSE,
                width, maxRange, damage, visibleTicks, 1, explosionScale);
    }

    public static ReusableLaserEntity sustained(ServerLevel level, Entity owner, Vec3 start, Vec3 target,
                                                 float width, float maxRange, float damage,
                                                 int durationTicks, int damageIntervalTicks,
                                                 int explosionScale) {
        return spawn(level, owner, start, target, ReusableLaserEntity.Mode.SUSTAINED,
                width, maxRange, damage, durationTicks, damageIntervalTicks, explosionScale);
    }

    private static ReusableLaserEntity spawn(ServerLevel level, Entity owner, Vec3 start, Vec3 target,
                                              ReusableLaserEntity.Mode mode, float width, float maxRange,
                                              float damage, int durationTicks, int damageIntervalTicks,
                                              int explosionScale) {
        ReusableLaserEntity laser = PomkotsMechs.REUSABLE_LASER.get().create(level);
        if (laser == null) return null;
        laser.configure(owner, start, target, mode, width, maxRange, damage,
                durationTicks, damageIntervalTicks, explosionScale);
        return level.addFreshEntity(laser) ? laser : null;
    }
}
