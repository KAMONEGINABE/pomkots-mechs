package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class BulletBeamEntity extends PomkotsCustomDecayedThrowableProjectile {

    private float explosionScale = 5;

    public BulletBeamEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        this(entityType, level, null, 20, BattleBalance.MECH_BEAM_DAMAGE, 10);
    }

    public BulletBeamEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, float damage) {
        this(entityType, level, shooter, 20, damage, 10);
    }

    public BulletBeamEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, int maxLifeTicks, float damage, int stun) {
        super(entityType, level, shooter,
                maxLifeTicks, damage,stun);

        this.setNoGravity(true);
        this.noCulling = true;
        this.noPhysics = true;
        this.shooter = shooter;
        this.damage = damage;
    }

    @Override
    public boolean shouldRender(double a, double b, double c) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double e) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.hasImpulse = true;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        this.discard();
    }

    @Override
    protected void defineSynchedData() {
        defineLinearPredictionData();
    }

    @Override
    protected boolean usesClientLinearPrediction() {
        return true;
    }

    @Override
    public RangeCategory calcRangeCategory(int distance) {
        return calcRangeCategoryStatic(distance);
    }

    @Override
    protected float getDamageModifier(RangeCategory range) {
        return switch (range) {
            case OPTIMAL -> 1.0F;
            case EFFECTIVE -> 0.8F;
            case MAXIMUM -> 0.5F;
            case OUT -> 0.1F;
            default -> 1F;
        };
    }

    public static RangeCategory calcRangeCategoryStatic(int distance) {
        if (distance < 80) {
            return RangeCategory.OPTIMAL;
        } else if (distance < 150) {
            return RangeCategory.EFFECTIVE;
        } else if (distance < 200){
            return RangeCategory.MAXIMUM;
        } else {
            return RangeCategory.OUT;
        }
    }
}
