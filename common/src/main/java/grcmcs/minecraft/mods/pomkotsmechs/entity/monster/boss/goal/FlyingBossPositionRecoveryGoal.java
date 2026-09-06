package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Restores PMB02's combat spacing and altitude after close-range attacks. */
public class FlyingBossPositionRecoveryGoal extends Goal {
    private static final double RETREAT_START_DISTANCE = 40.0D;
    private static final double RETREAT_END_DISTANCE = 46.0D;
    private static final int CLOSE_GRACE_TICKS = 6;
    private static final int MAX_RECOVERY_TICKS = 50;
    private static final int RECOVERY_COOLDOWN_TICKS = 20;
    private final BaseBossEntity mob;
    private LivingEntity target;
    private int closeTicks;
    private int recoveryTicks;
    private int cooldown;
    private double orbitSign = 1.0D;

    public FlyingBossPositionRecoveryGoal(BaseBossEntity mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        target = mob.getTarget();
        if (target == null || !target.isAlive() || mob.getActionController().isInActionAll()) {
            closeTicks = 0;
            return false;
        }
        double horizontalDistance = horizontalDistanceTo(target);
        closeTicks = horizontalDistance < RETREAT_START_DISTANCE ? closeTicks + 1 : 0;
        return closeTicks >= CLOSE_GRACE_TICKS || mob.getY() < desiredY() - 3.0D;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || !target.isAlive() || mob.getActionController().isInActionAll()) return false;
        boolean spacingRecovered = horizontalDistanceTo(target) >= RETREAT_END_DISTANCE;
        boolean altitudeRecovered = mob.getY() >= desiredY() - 1.0D;
        return recoveryTicks < MAX_RECOVERY_TICKS && (!spacingRecovered || !altitudeRecovered);
    }

    @Override
    public void start() {
        recoveryTicks = 0;
        closeTicks = 0;
        orbitSign = mob.getRandom().nextBoolean() ? 1.0D : -1.0D;
    }

    @Override
    public void stop() {
        cooldown = RECOVERY_COOLDOWN_TICKS;
        recoveryTicks = 0;
        target = null;
    }

    @Override
    public void tick() {
        recoveryTicks++;
        mob.rotateToTarget(target);
        Vec3 away = new Vec3(mob.getX() - target.getX(), 0.0D, mob.getZ() - target.getZ());
        if (away.lengthSqr() < 1.0E-6D) {
            away = Vec3.directionFromRotation(0.0F, mob.getYRot()).multiply(-1.0D, 0.0D, -1.0D);
        }
        away = away.normalize();
        Vec3 tangent = new Vec3(-away.z, 0.0D, away.x).scale(orbitSign);
        double horizontalDistance = horizontalDistanceTo(target);
        double strength = Mth.clamp((RETREAT_END_DISTANCE - horizontalDistance)
                / RETREAT_END_DISTANCE, 0.0D, 1.0D);
        Vec3 horizontal = away.scale(0.70D + strength * 0.60D).add(tangent.scale(0.24D));
        if (horizontalDistance >= RETREAT_END_DISTANCE) {
            horizontal = new Vec3(mob.getDeltaMovement().x, 0.0D, mob.getDeltaMovement().z).scale(0.8D);
        }
        double vertical = Mth.clamp((desiredY() - mob.getY()) * 0.16D, -0.40D, 0.90D);
        mob.setDeltaMovement(mob.getDeltaMovement().lerp(
                new Vec3(horizontal.x, vertical, horizontal.z), 0.28D));
        mob.hurtMarked = true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private double horizontalDistanceTo(LivingEntity entity) {
        double dx = mob.getX() - entity.getX();
        double dz = mob.getZ() - entity.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private double desiredY() {
        BlockPos ground = mob.level().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, mob.blockPosition());
        return Math.max(ground.getY() + 12.0D, target.getY() + 12.0D);
    }
}
