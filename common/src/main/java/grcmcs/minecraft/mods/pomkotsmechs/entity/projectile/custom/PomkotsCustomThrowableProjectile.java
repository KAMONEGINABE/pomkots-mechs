package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.SmallMobHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ProjectileUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class PomkotsCustomThrowableProjectile extends PomkotsThrowableProjectile {
    private static final double WATER_IMPACT_VIEW_DISTANCE = 200.0D;
    private static final EntityDataAccessor<Float> PREDICTION_VELOCITY_X =
            SynchedEntityData.defineId(PomkotsCustomThrowableProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PREDICTION_VELOCITY_Y =
            SynchedEntityData.defineId(PomkotsCustomThrowableProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PREDICTION_VELOCITY_Z =
            SynchedEntityData.defineId(PomkotsCustomThrowableProjectile.class, EntityDataSerializers.FLOAT);

    public enum RangeCategory {
        OPTIMAL            ((short)0),
        EFFECTIVE            ((short)1),
        MAXIMUM            ((short)2),
        OUT            ((short)3),
        SUPER            ((short)4);

        private final short category;

        RangeCategory(final short category) {
            this.category = category;
        }

        public short category() {
            return this.category;
        }
    }

    protected int maxLifeTicks = 20;
    protected int lifeTicks = 0;
    protected float damage;
    protected int stun;
    protected double initialSpeed = 0;
    protected boolean toDiscard = false;

    private static final int SUBSTEPS = 1;
    private static final double CLIENT_REMOVAL_LOOKAHEAD_TICKS = 3.0D;
    private boolean waterImpactHandled;

    public PomkotsCustomThrowableProjectile(
            EntityType<? extends ThrowableProjectile> entityType, Level world,
            LivingEntity shooter,
            int maxLifeTicks, float damage, int stun) {
        super(entityType, shooter, world);
        this.maxLifeTicks = maxLifeTicks;
        this.damage = damage;
        this.stun = stun;

        if (shooter instanceof Pmvc01Entity pmvc01 && pmvc01.getDrivingPassenger() != null) {
            this.setOwner(pmvc01.getDrivingPassenger());
        }
    }

    @Override
    public void tick() {
        if (toDiscard) {
            this.discard();
        }

        this.setOldPosAndRot();

        if (this.level().isClientSide) {
            // Collision and damage remain server-authoritative, but advance the visual
            // entity locally between position packets. LevelRenderer then interpolates
            // old -> current position with partialTick instead of displaying a projectile
            // that waits at each synchronized position.
            Vec3 movement = usesClientLinearPrediction()
                    ? getPredictionVelocity()
                    : this.getDeltaMovement();
            if (movement.lengthSqr() > 1.0E-10D) {
                this.setDeltaMovement(movement);
                this.setPos(this.position().add(movement));
            }
            return;
        }

        if (this.lifeTicks++ >= this.maxLifeTicks) {
            this.discard();
            return;
        }

        this.checkInsideBlocks();

        if (!performSubStepMovement()) {
            return;
        }
    }

    /** Whether this short-lived projectile can be predicted exactly from its velocity. */
    protected boolean usesClientLinearPrediction() {
        return false;
    }

    protected final void defineLinearPredictionData() {
        this.entityData.define(PREDICTION_VELOCITY_X, 0.0F);
        this.entityData.define(PREDICTION_VELOCITY_Y, 0.0F);
        this.entityData.define(PREDICTION_VELOCITY_Z, 0.0F);
    }

    private Vec3 getPredictionVelocity() {
        return new Vec3(
                this.entityData.get(PREDICTION_VELOCITY_X),
                this.entityData.get(PREDICTION_VELOCITY_Y),
                this.entityData.get(PREDICTION_VELOCITY_Z));
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        syncPredictionVelocity();
    }

    @Override
    public void shootFromRotation(Entity shooter, float xRot, float yRot, float zRotOffset,
                                  float velocity, float inaccuracy) {
        super.shootFromRotation(shooter, xRot, yRot, zRotOffset, velocity, inaccuracy);
        // Most existing callers pass the projectile itself as `shooter`. Vanilla then
        // adds that entity's movement after shoot(), so capture the true final velocity
        // here rather than leaving the pre-addition value synchronized.
        syncPredictionVelocity();
    }

    private void syncPredictionVelocity() {
        if (!this.level().isClientSide && usesClientLinearPrediction()) {
            Vec3 movement = this.getDeltaMovement();
            this.entityData.set(PREDICTION_VELOCITY_X, (float) movement.x);
            this.entityData.set(PREDICTION_VELOCITY_Y, (float) movement.y);
            this.entityData.set(PREDICTION_VELOCITY_Z, (float) movement.z);
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot,
                       int lerpSteps, boolean teleport) {
        if (this.level().isClientSide && this.tickCount > 0 && usesClientLinearPrediction()) {
            // Do not let delayed position packets fight the client-side visual prediction.
            // Server-authoritative impact and removal packets are still handled normally.
            this.setYRot(yRot);
            this.setXRot(xRot);
            return;
        }
        super.lerpTo(x, y, z, yRot, xRot, lerpSteps, teleport);
    }

    protected boolean performSubStepMovement() {
        if (firstTick) {
            initialSpeed = this.getDeltaMovement().distanceTo(Vec3.ZERO);
        }

        Vec3 totalMovement = this.getDeltaMovement();
        Vec3 subStepMovement = totalMovement;

        for (int step = 0; step < SUBSTEPS; step++) {
            Vec3 start = this.position();
            Vec3 end = start.add(subStepMovement);

            if (!waterImpactHandled && isWaterAt(start)) {
                waterImpactHandled = true;
            }

            // ブロックへのレイキャスト
            BlockHitResult blockHit = this.level().clip(new ClipContext(
                    start, end,
                    ClipContext.Block.COLLIDER,
                    waterImpactHandled ? ClipContext.Fluid.NONE : ClipContext.Fluid.ANY,
                    this
            ));
            if (!waterImpactHandled && isWaterHit(blockHit)) {
                spawnWaterImpact(blockHit.getLocation());
                waterImpactHandled = true;
                // Water only produces a visual effect. Solid collision behind it
                // must still be evaluated by the original block-only ray cast.
                blockHit = this.level().clip(new ClipContext(
                        start, end,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        this
                ));
            }

            // エンティティへのレイキャスト（射出者を除外）
            AABB searchBox = new AABB(start, end).inflate(0.5);
            EntityHitResult entityHit = getEntityHitResult(
                    this.level(),
                    this,
                    start,
                    end,
                    searchBox,
                    entity -> entity != this.getShooter()
                            && entity != this
                            && entity.isAlive()
                            && entity.isPickable()
                            && !entity.isSpectator()
                            && !(entity instanceof BossHitBoxEntity hb && hb.isRelatedShooter(this))
                            && !(entity instanceof SmallMobHitBoxEntity hb && hb.isAttachedTo(this.getShooter()))
            );

            HitResult hit = selectCloserHit(start, blockHit, entityHit);

            if (hit != null && hit.getType() != HitResult.Type.MISS) {
                this.setPosInSubStep(hit.getLocation());
                this.onHit(hit);
                return false;
            }

            this.setPosInSubStep(end);
        }

        return true; // 生存
    }

    private boolean isWaterHit(BlockHitResult hit) {
        return hit.getType() != HitResult.Type.MISS
                && this.level().getFluidState(hit.getBlockPos()).is(FluidTags.WATER);
    }

    private boolean isWaterAt(Vec3 position) {
        return this.level().getFluidState(BlockPos.containing(position)).is(FluidTags.WATER);
    }

    private void spawnWaterImpact(Vec3 position) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        double intensity = Math.max(1.0D, getWaterImpactIntensity());
        Vec3 movement = getDeltaMovement();
        double horizontalSpread = Math.min(
                0.38D * Math.sqrt(intensity),
                (movement.horizontalDistance() * 0.045D + 0.14D) * Math.sqrt(intensity));
        double verticalSpread = 0.18D * Math.sqrt(intensity);
        double launchSpeed = 0.32D * (1.0D + (intensity - 1.0D) * 0.28D);
        int particleCount = (int) Math.round(18.0D * intensity);
        double viewDistanceSqr = WATER_IMPACT_VIEW_DISTANCE * WATER_IMPACT_VIEW_DISTANCE;
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(position) > viewDistanceSqr) {
                continue;
            }
            serverLevel.sendParticles(
                    player,
                    PomkotsMechs.WATER_SPLASH.get(),
                    true,
                    position.x, position.y + 0.12D, position.z,
                    particleCount,
                    horizontalSpread, verticalSpread, horizontalSpread,
                    launchSpeed
            );
        }
    }

    protected double getWaterImpactIntensity() {
        return 1.0D;
    }

    protected void setPosInSubStep(Vec3 pos) {
        this.setPos(pos);
    }

    protected EntityHitResult getEntityHitResult(
            Level level,
            Entity projectile,
            Vec3 start,
            Vec3 end,
            AABB searchBox,
            Predicate<Entity> filter) {

        Entity closestEntity = null;
        Vec3 closestHitPos = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity candidate : level.getEntities(projectile, searchBox, filter)) {

            // 当たり判定をわずかに膨らませる（掠り当たり対応）
            AABB candidateBox = candidate.getBoundingBox().inflate(candidate.getPickRadius());

            Optional<Vec3> hitPos = candidateBox.clip(start, end);

            if (hitPos.isPresent()) {
                double dist = start.distanceToSqr(hitPos.get());

                if (dist < closestDist) {
                    closestDist = dist;
                    closestEntity = candidate;
                    closestHitPos = hitPos.get();
                }
            }
        }

        return closestEntity != null
                ? new EntityHitResult(closestEntity, closestHitPos)
//                ? new EntityHitResult(closestEntity, closestEntity.position())
                : null;
    }

    /**
     * ブロックとエンティティのヒット結果から近い方を返す
     */
    protected HitResult selectCloserHit(Vec3 origin,
                                      BlockHitResult blockHit,
                                      EntityHitResult entityHit) {
        boolean blockMiss = blockHit.getType() == HitResult.Type.MISS;

        if (entityHit == null && blockMiss) {
            return null; // 両方ミス
        }
        if (entityHit == null) {
            return blockHit;
        }
        if (blockMiss) {
            return entityHit;
        }

        // 両方ヒット → 近い方を返す
        double blockDist = origin.distanceToSqr(blockHit.getLocation());
        double entityDist = origin.distanceToSqr(entityHit.getLocation());
        return entityDist <= blockDist ? entityHit : blockHit;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();

        if (entity instanceof BossHitBoxEntity hb && hb.isRelatedShooter(this)) {
            return;
        }

        // ダメージ計算（射出直後はボーナスダメージ）
        float finalDamage = calcFinalDamage(damage);
//        if (lifeTicks < 10) {
//            finalDamage += (10 - (float) lifeTicks) * damage / 20;
//        }

        // スタン付与
        if (entity instanceof BaseBossEntity boss) {
            boss.addStunPoint(stun);
        } else if (entity instanceof BossHitBoxEntity hitbox) {
            hitbox.addStunPoint(stun);
        }

        // ダメージ付与
        ProjectileUtil.hurt(entity, this, this.getOwner(), finalDamage);
        entity.invulnerableTime = 0;

        toDiscard = true;
//        this.discard();
    }

    protected float calcFinalDamage(float baseDamage) {
        return baseDamage;
    }

    public int getStunPoint() {
        return this.stun;
    }

    public void setStunPoint(int stunPoint) {
        this.stun = stunPoint;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {

        toDiscard = true;
//        this.discard();
    }

    @Override
    public void onClientRemoval() {
        if (this.level().isClientSide) {
            ParticleUtil.addSparkParticles(findClientRemovalEffectPosition(), this.level());
        }
    }

    /**
     * Reconstructs a likely impact point from the client's last synchronized position.
     * This runs once, only when the projectile is removed, so active projectile cost is unchanged.
     */
    private Vec3 findClientRemovalEffectPosition() {
        Vec3 start = position();
        Vec3 movement = getDeltaMovement().scale(CLIENT_REMOVAL_LOOKAHEAD_TICKS);
        if (movement.lengthSqr() < 1.0E-7D) {
            return start;
        }

        Vec3 end = start.add(movement);
        BlockHitResult blockHit = level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
        ));

        AABB searchBox = new AABB(start, end).inflate(0.5D);
        EntityHitResult entityHit = getEntityHitResult(
                level(),
                this,
                start,
                end,
                searchBox,
                entity -> entity != this
                        && entity != getShooter()
                        && entity != getOwner()
                        && entity.isAlive()
                        && entity.isPickable()
                        && !entity.isSpectator()
                        && !(entity instanceof BossHitBoxEntity hitBox && hitBox.isRelatedShooter(this))
                        && !(entity instanceof SmallMobHitBoxEntity hitBox
                            && (hitBox.isAttachedTo(getShooter()) || hitBox.isAttachedTo(getOwner())))
        );

        HitResult hit = selectCloserHit(start, blockHit, entityHit);
        return hit != null && hit.getType() != HitResult.Type.MISS
                ? hit.getLocation()
                : start;
    }

    // 重力を無効化（ThrowableProjectileの挙動を上書き）
    @Override
    protected float getGravity() {
        return 0.0F;
    }
}
