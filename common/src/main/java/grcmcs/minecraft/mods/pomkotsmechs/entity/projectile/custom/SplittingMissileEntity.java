package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Large parent missile that opens into a nine-missile homing flower. */
public class SplittingMissileEntity extends MissileGenericLargeEntity {
    private static final int SPLIT_AFTER_TICKS = 24;
    private static final double SPLIT_TARGET_DISTANCE = 40.0D;
    private static final int CHILD_COUNT = 9;
    private boolean split;
    private final float childSpeed;

    public SplittingMissileEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        this(type, level, null, null, BattleBalance.MECH_MISSILE_DAMAGE,
                BattleBalance.MECH_MISSILE_GENERIC_SPEED);
    }

    public SplittingMissileEntity(EntityType<? extends ThrowableProjectile> type, Level level,
                                  LivingEntity shooter, LivingEntity target,
                                  float damage, float speed) {
        this(type, level, shooter, target, damage, speed, speed);
    }

    public SplittingMissileEntity(EntityType<? extends ThrowableProjectile> type, Level level,
                                  LivingEntity shooter, LivingEntity target,
                                  float damage, float parentSpeed, float childSpeed) {
        super(type, level, shooter, target, damage, parentSpeed);
        this.childSpeed = childSpeed;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide || split || isRemoved()) {
            return;
        }

        boolean nearTarget = target != null && target.isAlive()
                && distanceToSqr(target) <= SPLIT_TARGET_DISTANCE * SPLIT_TARGET_DISTANCE;
        if (tickCount >= SPLIT_AFTER_TICKS || nearTarget) {
            splitIntoChildren();
        }
    }

    private void splitIntoChildren() {
        split = true;
        Vec3 forward = getDeltaMovement().lengthSqr() > 1.0E-6D
                ? getDeltaMovement().normalize()
                : Vec3.directionFromRotation(getXRot(), getYRot());
        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-6D) {
            right = forward.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }
        right = right.normalize();
        Vec3 up = right.cross(forward).normalize();
        double childLaunchSpeed = Math.max(0.1D, childSpeed);

        for (int i = 0; i < CHILD_COUNT; i++) {
            double radial = i == 0 ? 0.0D : 0.72D;
            double angle = i == 0 ? 0.0D : Math.PI * 2.0D * (i - 1) / (CHILD_COUNT - 1);
            Vec3 direction = forward.scale(0.72D)
                    .add(right.scale(Math.cos(angle) * radial))
                    .add(up.scale(Math.sin(angle) * radial))
                    .normalize();

            MissileGenericEntity child = new MissileGenericEntity(
                    PomkotsMechs.MISSILE_GENERIC.get(), level(), shooter, target,
                    damage, (float) childLaunchSpeed);
            child.setPos(position().add(direction.scale(1.5D)));
            child.setMaxRotationAnglePerTick(7.0F);
            child.shoot(direction.x, direction.y, direction.z, (float) childLaunchSpeed, 0.0F);
            level().addFreshEntity(child);
        }

        discard();
    }
}
