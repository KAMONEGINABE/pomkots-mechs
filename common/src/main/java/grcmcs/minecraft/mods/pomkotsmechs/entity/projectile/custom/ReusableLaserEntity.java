package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.SmallMobHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ProjectileUtil;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Server-authoritative, reusable hitscan laser with pulse and sustained modes. */
public final class ReusableLaserEntity extends Entity implements GeoEntity {
    public enum Mode { PULSE, SUSTAINED }

    private static final int GROW_TICKS = 4;
    private static final int SHRINK_TICKS = 5;
    private static final double DIRECTION_FOLLOW_FACTOR = 0.35D;

    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(
            ReusableLaserEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> BEAM_LENGTH = SynchedEntityData.defineId(
            ReusableLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> BEAM_WIDTH = SynchedEntityData.defineId(
            ReusableLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(
            ReusableLaserEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> TARGET_X = SynchedEntityData.defineId(
            ReusableLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TARGET_Y = SynchedEntityData.defineId(
            ReusableLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TARGET_Z = SynchedEntityData.defineId(
            ReusableLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIRECTION_X = SynchedEntityData.defineId(
            ReusableLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIRECTION_Y = SynchedEntityData.defineId(
            ReusableLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIRECTION_Z = SynchedEntityData.defineId(
            ReusableLaserEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(
            ReusableLaserEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(
            ReusableLaserEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HALO_COLOR = SynchedEntityData.defineId(
            ReusableLaserEntity.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private float damage;
    private float maxRange = 200.0F;
    private int durationTicks = 1;
    private int damageIntervalTicks = 1;
    private int explosionScale;
    private boolean pulseDamageApplied;
    private Vec3 impactPosition;
    private Vec3 previousRenderDirection = Vec3.ZERO;
    private Vec3 renderDirection = Vec3.ZERO;
    private Vec3 previousRenderPosition = Vec3.ZERO;
    private Vec3 renderPosition = Vec3.ZERO;
    private boolean pulseImpactParticlesSpawned;

    public ReusableLaserEntity(EntityType<?> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    public void configure(Entity owner, Vec3 start, Vec3 target, Mode mode, float width,
                          float maxRange, float damage, int durationTicks,
                          int damageIntervalTicks, int explosionScale, int haloColor) {
        setPos(start);
        setTargetPosition(target);
        setMode(mode);
        entityData.set(BEAM_WIDTH, Math.max(0.01F, width));
        entityData.set(OWNER_UUID, owner == null ? Optional.empty() : Optional.of(owner.getUUID()));
        entityData.set(OWNER_ID, owner == null ? -1 : owner.getId());
        this.maxRange = Math.max(0.1F, maxRange);
        this.damage = Math.max(0.0F, damage);
        this.durationTicks = Math.max(GROW_TICKS + SHRINK_TICKS + 1, durationTicks);
        entityData.set(DURATION, this.durationTicks);
        this.damageIntervalTicks = Math.max(1, damageIntervalTicks);
        this.explosionScale = Math.max(0, explosionScale);
        entityData.set(HALO_COLOR, haloColor & 0xFFFFFF);
        updateRotation();
        updateRaycastAndLength();
    }

    public void setEndpoints(Vec3 start, Vec3 target) {
        setPos(start);
        setTargetPosition(target);
    }

    public void setTargetPosition(Vec3 target) {
        entityData.set(TARGET_X, (float) target.x);
        entityData.set(TARGET_Y, (float) target.y);
        entityData.set(TARGET_Z, (float) target.z);
        Vec3 direction = target.subtract(position());
        if (direction.lengthSqr() >= 1.0E-8D) {
            direction = direction.normalize();
            entityData.set(DIRECTION_X, (float) direction.x);
            entityData.set(DIRECTION_Y, (float) direction.y);
            entityData.set(DIRECTION_Z, (float) direction.z);
        }
    }

    public Vec3 getTargetPosition() {
        return new Vec3(entityData.get(TARGET_X), entityData.get(TARGET_Y), entityData.get(TARGET_Z));
    }

    public Vec3 getBeamDirection() {
        return new Vec3(entityData.get(DIRECTION_X), entityData.get(DIRECTION_Y), entityData.get(DIRECTION_Z));
    }

    public Mode getMode() {
        return entityData.get(MODE) == Mode.SUSTAINED.ordinal() ? Mode.SUSTAINED : Mode.PULSE;
    }

    private void setMode(Mode mode) {
        entityData.set(MODE, mode.ordinal());
    }

    public float getBeamLength() { return entityData.get(BEAM_LENGTH); }
    public float getBeamWidth() { return entityData.get(BEAM_WIDTH); }
    public int getHaloColor() { return entityData.get(HALO_COLOR); }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        updateRotation();
        if (level().isClientSide) {
            updateRenderDirection();
            updateRenderPosition();
            spawnClientImpactParticles();
            return;
        }
        updateRaycastAndLength();

        boolean fullWidth = tickCount >= GROW_TICKS && durationTicks - tickCount >= SHRINK_TICKS;
        boolean shouldDamage = fullWidth && (getMode() == Mode.PULSE
                ? !pulseDamageApplied
                : (tickCount - GROW_TICKS) % damageIntervalTicks == 0);
        if (shouldDamage) {
            applyLaserDamage();
            applyImpactEffect();
            pulseDamageApplied = true;
        }
        if (tickCount >= durationTicks) discard();
    }

    private void updateRenderPosition() {
        Vec3 syncedPosition = position();
        if (renderPosition.lengthSqr() < 1.0E-8D) {
            previousRenderPosition = syncedPosition;
            renderPosition = syncedPosition;
            return;
        }
        previousRenderPosition = renderPosition;
        renderPosition = renderPosition.lerp(syncedPosition, 0.5D);
    }

    public Vec3 getInterpolatedRenderPosition(float partialTick) {
        if (renderPosition.lengthSqr() < 1.0E-8D) return getPosition(partialTick);
        return previousRenderPosition.lerp(renderPosition, Mth.clamp(partialTick, 0.0F, 1.0F));
    }

    private void updateRenderDirection() {
        Vec3 syncedDirection = getBeamDirection();
        if (syncedDirection.lengthSqr() < 1.0E-8D) return;
        syncedDirection = syncedDirection.normalize();
        if (renderDirection.lengthSqr() < 1.0E-8D) {
            previousRenderDirection = syncedDirection;
            renderDirection = syncedDirection;
            return;
        }
        previousRenderDirection = renderDirection;
        Vec3 damped = renderDirection.lerp(syncedDirection, DIRECTION_FOLLOW_FACTOR);
        renderDirection = damped.lengthSqr() < 1.0E-8D ? syncedDirection : damped.normalize();
    }

    public Vec3 getInterpolatedRenderDirection(float partialTick) {
        if (renderDirection.lengthSqr() < 1.0E-8D) return getBeamDirection();
        Vec3 interpolated = previousRenderDirection.lerp(renderDirection, Mth.clamp(partialTick, 0.0F, 1.0F));
        return interpolated.lengthSqr() < 1.0E-8D ? renderDirection : interpolated.normalize();
    }

    public float getVisualWidthScale(float partialTick) {
        float age = tickCount + partialTick;
        float grow = Mth.clamp(age / GROW_TICKS, 0.0F, 1.0F);
        float shrink = Mth.clamp((entityData.get(DURATION) - age) / SHRINK_TICKS, 0.0F, 1.0F);
        return Math.min(grow, shrink);
    }

    private void updateRotation() {
        Vec3 direction = getBeamDirection();
        if (direction.lengthSqr() < 1.0E-8D) {
            return;
        }
        direction = direction.normalize();
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        setYRot((float) (Mth.atan2(-direction.x, direction.z) * Mth.RAD_TO_DEG));
        setXRot((float) (Mth.atan2(-direction.y, horizontal) * Mth.RAD_TO_DEG));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    private void updateRaycastAndLength() {
        Vec3 requestedDirection = getTargetPosition().subtract(position());
        if (requestedDirection.lengthSqr() < 1.0E-8D) {
            entityData.set(BEAM_LENGTH, 0.0F);
            impactPosition = null;
            return;
        }
        Vec3 maxEnd = position().add(requestedDirection.normalize().scale(maxRange));
        HitResult hit = level().clip(new ClipContext(position(), maxEnd,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 end = hit.getType() == HitResult.Type.MISS ? maxEnd : hit.getLocation();
        impactPosition = hit instanceof BlockHitResult ? hit.getLocation() : null;
        entityData.set(BEAM_LENGTH, (float) position().distanceTo(end));
    }

    private void applyLaserDamage() {
        Entity owner = getOwnerEntity();
        Vec3 direction = getTargetPosition().subtract(position()).normalize();
        Vec3 end = position().add(direction.scale(getBeamLength()));
        double width = getBeamWidth();
        AABB search = new AABB(position(), end).inflate(width);

        Set<UUID> damagedTargets = new HashSet<>();
        for (Entity hitEntity : level().getEntities(this, search, this::isLaserDamageCandidate)) {
            Entity actualTarget = resolveDamageTarget(hitEntity);
            if (actualTarget == null || !canDamage(owner, actualTarget)) continue;

            // Test the beam segment against the complete AABB instead of only
            // measuring from its center. This also makes large/tall targets reliable.
            if (hitEntity.getBoundingBox().inflate(width).clip(position(), end).isEmpty()) continue;
            if (!damagedTargets.add(actualTarget.getUUID())) continue;

            // Lasers intentionally deal damage at their configured interval
            // instead of being throttled by vanilla LivingEntity i-frames.
            if (actualTarget instanceof LivingEntity livingTarget) {
                livingTarget.invulnerableTime = 0;
            }
            if (hitEntity != actualTarget && hitEntity instanceof LivingEntity livingHitBox) {
                livingHitBox.invulnerableTime = 0;
            }
            ProjectileUtil.hurt(hitEntity, this, owner == null ? this : owner, damage);
        }
    }

    private void spawnClientImpactParticles() {
        boolean fullWidth = tickCount >= GROW_TICKS && entityData.get(DURATION) - tickCount >= SHRINK_TICKS;
        if (!fullWidth) return;
        if (getMode() == Mode.PULSE) {
            if (pulseImpactParticlesSpawned) return;
            pulseImpactParticlesSpawned = true;
        } else if (tickCount % 2 != 0) {
            return;
        }

        Vec3 direction = getBeamDirection();
        if (direction.lengthSqr() < 1.0E-8D || getBeamLength() <= 0.0F) return;
        direction = direction.normalize();
        Vec3 start = position();
        Vec3 beamEnd = start.add(direction.scale(getBeamLength()));
        Vec3 impact = findClientImpact(start, beamEnd, direction);
        if (impact == null) return;

        Vec3 axisA = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (axisA.lengthSqr() < 1.0E-8D) axisA = direction.cross(new Vec3(1.0D, 0.0D, 0.0D));
        axisA = axisA.normalize();
        Vec3 axisB = direction.cross(axisA).normalize();
        int count = Mth.clamp(Mth.ceil(getBeamWidth() * 3.0F), 1, 12);
        double radius = Math.max(0.15D, getBeamWidth() * 0.7D);
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = Math.sqrt(random.nextDouble()) * radius;
            Vec3 offset = axisA.scale(Math.cos(angle) * distance)
                    .add(axisB.scale(Math.sin(angle) * distance));
            Vec3 particlePos = impact.add(offset).subtract(direction.scale(0.08D));
            Vec3 velocity = offset.scale(0.04D).subtract(direction.scale(0.04D));
            level().addParticle(ParticleTypes.EXPLOSION,
                    particlePos.x, particlePos.y, particlePos.z,
                    velocity.x, velocity.y, velocity.z);
        }
    }

    private Vec3 findClientImpact(Vec3 start, Vec3 beamEnd, Vec3 direction) {
        Vec3 blockProbeEnd = beamEnd.add(direction.scale(0.2D));
        HitResult blockHit = level().clip(new ClipContext(start, blockProbeEnd,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        Vec3 nearest = blockHit.getType() == HitResult.Type.MISS ? null : blockHit.getLocation();
        double nearestDistanceSqr = nearest == null ? Double.MAX_VALUE : start.distanceToSqr(nearest);

        AABB search = new AABB(start, beamEnd).inflate(getBeamWidth());
        Entity owner = getOwnerEntity();
        for (Entity candidate : level().getEntities(this, search, this::isLaserDamageCandidate)) {
            Entity actualTarget = resolveDamageTarget(candidate);
            if (actualTarget == null || !canDamage(owner, actualTarget)) continue;
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(getBeamWidth()).clip(start, beamEnd);
            if (hit.isEmpty()) continue;
            double distanceSqr = start.distanceToSqr(hit.get());
            if (distanceSqr < nearestDistanceSqr) {
                nearest = hit.get();
                nearestDistanceSqr = distanceSqr;
            }
        }
        return nearest;
    }

    private boolean isLaserDamageCandidate(Entity entity) {
        if (entity instanceof SmallMobHitBoxEntity) return true;
        if (ProjectileUtil.isSuperbWarfareVehicle(entity) || entity.isAttackable()) return true;
        if (!(entity instanceof LivingEntity)) return false;
        // These entities expose separate, larger damage hitboxes. Waiting for
        // those avoids double hits and ensures boss damage is forwarded properly.
        if (entity instanceof BaseBossEntity) return false;
        return !(entity instanceof BaseSmallMonsterEntity small && small.getDamageHitBox() != null);
    }

    private Entity resolveDamageTarget(Entity entity) {
        if (entity instanceof BossHitBoxEntity hitBox) return hitBox.getParentEntity();
        if (entity instanceof SmallMobHitBoxEntity hitBox) return hitBox.getParentMob();
        return entity;
    }

    private boolean canDamage(Entity owner, Entity target) {
        if (target == owner || target.getRootVehicle() == owner) return false;
        if (owner != null && owner.getRootVehicle() == target.getRootVehicle()) return false;
        if (owner instanceof BaseBossEntity boss && boss.isSelf(target)) return false;
        if (ProjectileUtil.isSuperbWarfareVehicle(target)) return true;
        return !(owner instanceof LivingEntity living)
                || !(target instanceof LivingEntity targetLiving)
                || living.canAttack(targetLiving);
    }

    private void applyImpactEffect() {
        if (impactPosition == null || explosionScale <= 0) return;
        Entity owner = getOwnerEntity();
        Entity cause = owner == null ? this : owner;
        if (explosionScale > 5) {
            ExplosionEntity explosion = new ExplosionEntity(PomkotsMechs.EXPLOSION.get(), level(), explosionScale);
            explosion.setPos(impactPosition);
            level().addFreshEntity(explosion);
        } else {
            Utils.explode(cause, impactPosition.x, impactPosition.y, impactPosition.z,
                    explosionScale, false, Level.ExplosionInteraction.BLOCK, level());
        }
    }

    private Entity getOwnerEntity() {
        Entity owner = level().getEntity(entityData.get(OWNER_ID));
        if (owner != null) return owner;
        if (!(level() instanceof ServerLevel serverLevel)) return null;
        return entityData.get(OWNER_UUID).map(serverLevel::getEntity).orElse(null);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(MODE, Mode.PULSE.ordinal());
        entityData.define(BEAM_LENGTH, 0.0F);
        entityData.define(BEAM_WIDTH, 1.0F);
        entityData.define(DURATION, GROW_TICKS + SHRINK_TICKS + 1);
        entityData.define(TARGET_X, 0.0F);
        entityData.define(TARGET_Y, 0.0F);
        entityData.define(TARGET_Z, 0.0F);
        entityData.define(DIRECTION_X, 0.0F);
        entityData.define(DIRECTION_Y, 0.0F);
        entityData.define(DIRECTION_Z, 1.0F);
        entityData.define(OWNER_UUID, Optional.empty());
        entityData.define(OWNER_ID, -1);
        entityData.define(HALO_COLOR, ReusableLaserEmitter.HALO_RED);
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) { }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
    @Override public boolean shouldBeSaved() { return false; }
    @Override public boolean shouldRender(double cameraX, double cameraY, double cameraZ) { return true; }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return true; }
    @Override public boolean isPickable() { return false; }
    @Override public boolean isAttackable() { return false; }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "laser", 0, state -> state.setAndContinue(
                RawAnimation.begin().thenLoop(getMode() == Mode.PULSE
                        ? "animation.reusable_laser.pulse"
                        : "animation.reusable_laser.sustain"))));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return geoCache; }
}
