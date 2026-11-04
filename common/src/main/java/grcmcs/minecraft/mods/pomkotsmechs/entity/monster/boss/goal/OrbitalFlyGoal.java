package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Smooth inertial orbit used by PMB02 between attacks. */
public class OrbitalFlyGoal extends BaseBossGoal {
    private LivingEntity target;
    private float angle;
    private boolean clockwise = true;
    private int switchCooldown;
    private final double minRadius;
    private final double maxRadius;
    private double currentRadius;
    private int radiusCooldown;
    private final double heightMin;
    private final double heightMax;
    private double currentHeight;
    private final double speed;
    private int ticksUntilNextSwitch;
    private int burstTicks;
    private int burstCooldown = 20;
    private double burstDirection = 1.0D;

    public OrbitalFlyGoal(BaseBossEntity mob, double speed, double minRadius, double maxRadius,
                          double heightMin, double heightMax) {
        super(mob);
        this.speed = speed;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.heightMin = heightMin;
        this.heightMax = heightMax;
        this.currentRadius = randomRadius();
        this.currentHeight = randomHeight();
        setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    private double randomRadius() {
        return minRadius + mob.getRandom().nextDouble() * (maxRadius - minRadius);
    }

    private double randomHeight() {
        return heightMin + mob.getRandom().nextDouble() * (heightMax - heightMin);
    }

    @Override
    public boolean canUseInternal() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return --ticksUntilNextSwitch > 0;
    }

    @Override
    public void start() {
        target = mob.getTarget();
        ticksUntilNextSwitch = mob.getRandom().nextInt(30) + 30;
        if (target != null) {
            Vec3 relative = mob.position().subtract(target.position());
            angle = (float)Math.toDegrees(Math.atan2(relative.z, relative.x));
        }
    }

    @Override
    public void stop() {
        target = null;
    }

    @Override
    public void tick() {
        if (target == null) return;
        mob.rotateToTarget(target);
        if (--switchCooldown <= 0) {
            clockwise = mob.getRandom().nextBoolean();
            switchCooldown = 100 + mob.getRandom().nextInt(60);
        }
        if (--radiusCooldown <= 0) {
            currentRadius = randomRadius();
            currentHeight = randomHeight();
            radiusCooldown = 80 + mob.getRandom().nextInt(60);
        }

        if (burstTicks > 0) {
            burstTicks--;
        } else if (--burstCooldown <= 0) {
            burstTicks = 12 + mob.getRandom().nextInt(7);
            burstCooldown = 25 + mob.getRandom().nextInt(31);
            burstDirection = mob.getRandom().nextBoolean() ? 1.0D : -1.0D;
        }

        angle += (clockwise ? 1.0F : -1.0F) * 2.0F;
        double radians = Math.toRadians(angle);
        double x = target.getX() + currentRadius * Math.cos(radians);
        double z = target.getZ() + currentRadius * Math.sin(radians);
        BlockPos column = BlockPos.containing(x, mob.getY(), z);
        BlockPos ground = mob.level().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, column);
        double y = Math.max(ground.getY() + currentHeight, target.getY() + 8.0D);

        Vec3 error = new Vec3(x, y, z).subtract(mob.position());
        Vec3 desired = error.lengthSqr() > 1.0E-6D
                ? error.normalize().scale(Math.min(speed, error.length() * 0.08D))
                : Vec3.ZERO;
        double acceleration = 0.24D;
        if (burstTicks > 0) {
            Vec3 radial = new Vec3(mob.getX() - target.getX(), 0.0D,
                    mob.getZ() - target.getZ());
            if (radial.lengthSqr() > 1.0E-6D) {
                radial = radial.normalize();
                Vec3 tangent = new Vec3(-radial.z, 0.0D, radial.x).scale(burstDirection);
                desired = desired.add(tangent.scale(speed * 1.35D));
                double burstMaxSpeed = speed * 1.6D;
                if (desired.lengthSqr() > burstMaxSpeed * burstMaxSpeed) {
                    desired = desired.normalize().scale(burstMaxSpeed);
                }
                acceleration = 0.48D;
            }
        }
        mob.setDeltaMovement(mob.getDeltaMovement().lerp(desired, acceleration));
        mob.hurtMarked = true;
    }
}
