package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.KutaraItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.PomkotsCustomThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.ReusableLaserEmitter;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.ReusableLaserEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Three-stage charge laser rifle for player-controlled mechs. */
public class KutaraItem extends BasePartsItem.WeaponShoulder {
    private static final Map<LaserSlotKey, Integer> ACTIVE_LASERS = new ConcurrentHashMap<>();
    private static final Map<LaserSlotKey, PulseBurst> ACTIVE_PULSE_BURSTS = new ConcurrentHashMap<>();
    /**
     * A simultaneous dual-wield discharge is one trigger operation.  Both hand
     * actions are ticked independently, so remember the server tick on which
     * the mech already paid the sustained-shot energy cost.
     */
    private static final Map<UUID, Long> SUSTAIN_ENERGY_PAID_TICK = new ConcurrentHashMap<>();
    private static final int CHARGE_2_TICKS = 20;
    private static final int CHARGE_3_TICKS = 40;
    private static final float MAX_RANGE = 200.0F;
    public static int CHARGE_2_BURST_INTERVAL_TICKS = 3;

    // Kutara sustain tuning parameters.
    public static int SUSTAIN_ENERGY_COST = 67;
    public static float SUSTAIN_DAMAGE_MULTIPLIER = 0.5F;
    public static int SUSTAIN_DURATION_TICKS = 80;
    public static int SUSTAIN_DAMAGE_INTERVAL_TICKS = 3;
    public static int COOLDOWN_TICKS = 40;

    public KutaraItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public KutaraItemRenderer newRenderer() {
        return new KutaraItemRenderer();
    }

    @Override
    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mech, int tick, boolean isOnFire) {
        if (!mech.getWorld().isClientSide()) {
            updateSustainedLaser(mech);
            updatePulseBurst(mech);
        }
        if (!mech.getWorld().isClientSide() && mech.isCharging()) {
            if (tick == CHARGE_2_TICKS) triggerItemAnimation(mech, "charge2");
            if (tick == CHARGE_3_TICKS) triggerItemAnimation(mech, "charge3");
        }
        if (!isOnFire) return;

        int chargeTicks = Math.max(0, tick - 5); // CHARGE motions fire five ticks after release.
        int energyCost = chargeTicks >= CHARGE_3_TICKS
                ? SUSTAIN_ENERGY_COST
                : chargeTicks >= CHARGE_2_TICKS ? 100 : 50;
        if (chargeTicks >= CHARGE_3_TICKS) {
            if (!consumeSustainEnergyOncePerTick(mech, energyCost)) return;
        } else if (!mech.consumeEnergy(energyCost)) {
            return;
        }

        if (!mech.getWorld().isClientSide()) {
            triggerItemAnimation(mech, chargeTicks >= CHARGE_3_TICKS ? "fire_sustain" : "fire");
        }

        if (mech.getWorld().isClientSide()) {
            if (chargeTicks >= CHARGE_3_TICKS) {
                // Start the local movement lock on the release frame instead
                // of waiting for the server's synced laser entity/slot mask.
                mech.getMechEntity().setKutaraSustainActive(mech.getWeaponItemSlot(), true);
                mech.playSoundEffect(PomkotsMechs.SE_BEAM1.get());
                mech.startLoopSound(getSustainSoundKey(mech), PomkotsMechs.SE_BEAM2_LOOP.get());
            } else {
                mech.playSoundEffect(PomkotsMechs.SE_BEAM1.get());
            }
            return;
        }

        ServerLevel level = (ServerLevel) mech.getWorld();
        Vec3 start = getMuzzlePosition(mech);
        Vec3 target = getCrosshairTarget(mech);
        float baseDamage = mech.applySkillDamageModifier(
                getDamage(mech.getItemStack()), getWeaponCategory());

        if (chargeTicks >= CHARGE_3_TICKS) {
            ReusableLaserEntity laser = ReusableLaserEmitter.sustained(
                    level, mech.getMechEntity(), start, target,
                    1.0F, MAX_RANGE, baseDamage * SUSTAIN_DAMAGE_MULTIPLIER,
                    SUSTAIN_DURATION_TICKS, SUSTAIN_DAMAGE_INTERVAL_TICKS, 0,
                    ReusableLaserEmitter.HALO_BLUE);
            if (laser != null) {
                ACTIVE_LASERS.put(getLaserSlotKey(mech), laser.getId());
                mech.getMechEntity().setKutaraSustainActive(mech.getWeaponItemSlot(), true);
            }
        } else if (chargeTicks >= CHARGE_2_TICKS) {
            fireCharge2Pulse(level, mech);
            ACTIVE_PULSE_BURSTS.put(getLaserSlotKey(mech),
                    new PulseBurst(2, level.getGameTime() + CHARGE_2_BURST_INTERVAL_TICKS));
        } else {
            ReusableLaserEmitter.pulse(level, mech.getMechEntity(), start, target,
                    0.35F, MAX_RANGE, baseDamage, 12, 0, ReusableLaserEmitter.HALO_BLUE);
        }
    }

    private void updatePulseBurst(ActionWeapon.WeaponMechInterface mech) {
        LaserSlotKey key = getLaserSlotKey(mech);
        PulseBurst burst = ACTIVE_PULSE_BURSTS.get(key);
        if (burst == null || mech.getWorld().getGameTime() < burst.nextFireTick()) return;

        ServerLevel level = (ServerLevel) mech.getWorld();
        fireCharge2Pulse(level, mech);
        level.playSound(null, mech.getMechEntity().blockPosition(), PomkotsMechs.SE_BEAM1.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);

        if (burst.remainingShots() <= 1) {
            ACTIVE_PULSE_BURSTS.remove(key, burst);
        } else {
            ACTIVE_PULSE_BURSTS.put(key, new PulseBurst(
                    burst.remainingShots() - 1,
                    level.getGameTime() + CHARGE_2_BURST_INTERVAL_TICKS));
        }
    }

    private void fireCharge2Pulse(ServerLevel level, ActionWeapon.WeaponMechInterface mech) {
        float damage = mech.applySkillDamageModifier(
                getDamage(mech.getItemStack()), getWeaponCategory());
        ReusableLaserEmitter.pulse(level, mech.getMechEntity(),
                getMuzzlePosition(mech), getCrosshairTarget(mech),
                1.0F, MAX_RANGE, damage, 12, 0, ReusableLaserEmitter.HALO_BLUE);
    }

    private void updateSustainedLaser(ActionWeapon.WeaponMechInterface mech) {
        LaserSlotKey key = getLaserSlotKey(mech);
        Integer entityId = ACTIVE_LASERS.get(key);
        if (entityId == null) return;
        Entity entity = mech.getWorld().getEntity(entityId);
        if (entity instanceof ReusableLaserEntity laser && !laser.isRemoved()) {
            Vec3 start = getMuzzlePosition(mech);
            laser.setEndpoints(start, getCrosshairTarget(mech));
        } else {
            ACTIVE_LASERS.remove(key, entityId);
            mech.getMechEntity().setKutaraSustainActive(mech.getWeaponItemSlot(), false);
        }
    }

    private LaserSlotKey getLaserSlotKey(ActionWeapon.WeaponMechInterface mech) {
        return new LaserSlotKey(mech.getMechEntity().getUUID(), mech.getWeaponItemSlot());
    }

    private String getSustainSoundKey(ActionWeapon.WeaponMechInterface mech) {
        return "kutara_sustain_" + mech.getWeaponItemSlot();
    }

    private boolean consumeSustainEnergyOncePerTick(ActionWeapon.WeaponMechInterface mech, int energyCost) {
        UUID mechId = mech.getMechEntity().getUUID();
        long gameTime = mech.getWorld().getGameTime();
        if (SUSTAIN_ENERGY_PAID_TICK.getOrDefault(mechId, Long.MIN_VALUE) == gameTime) return true;
        if (!mech.consumeEnergy(energyCost)) return false;
        SUSTAIN_ENERGY_PAID_TICK.put(mechId, gameTime);
        return true;
    }

    private void discardActiveLaser(ActionWeapon.WeaponMechInterface mech) {
        Integer entityId = ACTIVE_LASERS.remove(getLaserSlotKey(mech));
        if (entityId != null) {
            Entity entity = mech.getWorld().getEntity(entityId);
            if (entity instanceof ReusableLaserEntity laser && !laser.isRemoved()) laser.discard();
        }
        mech.getMechEntity().setKutaraSustainActive(mech.getWeaponItemSlot(), false);
    }

    private Vec3 getMuzzlePosition(ActionWeapon.WeaponMechInterface mech) {
        Vec3 mount = new Vec3(1.5D * mech.isRight(), 5.0D, 0.0D);
        Vec3 muzzle = mech.getAimAdjustedMuzzleOffset(
                new Vec3(1.5D * mech.isRight(), 5.7D, 5.0D), mount);
        return mech.getOffset().add(muzzle);
    }

    private Vec3 getCrosshairTarget(ActionWeapon.WeaponMechInterface mech) {
        return mech.getCameraTargetPosition();
    }

    @Override
    public void startUsing(ActionWeapon.WeaponMechInterface mech) {
        ACTIVE_PULSE_BURSTS.remove(getLaserSlotKey(mech));
        mech.getMechEntity().setKutaraSustainActive(mech.getWeaponItemSlot(), false);
        if (mech.getWorld().isClientSide()) mech.stopLoopSound(getSustainSoundKey(mech));
        if (!mech.getWorld().isClientSide()) {
            discardActiveLaser(mech);
            triggerItemAnimation(mech, "charge1");
        }
    }

    @Override
    public void endUsing(ActionWeapon.WeaponMechInterface mech) {
        ACTIVE_PULSE_BURSTS.remove(getLaserSlotKey(mech));
        mech.getMechEntity().setKutaraSustainActive(mech.getWeaponItemSlot(), false);
        if (mech.getWorld().isClientSide()) mech.stopLoopSound(getSustainSoundKey(mech));
        if (!mech.getWorld().isClientSide()) {
            discardActiveLaser(mech);
            triggerItemAnimation(mech, "idle");
        }
    }

    private void triggerItemAnimation(ActionWeapon.WeaponMechInterface mech, String animation) {
        if (!(mech.getWorld() instanceof ServerLevel serverLevel) || mech.getPlayer() == null) return;
        triggerAnim(mech.getPlayer(), GeoItem.getOrAssignId(mech.getItemStack(), serverLevel),
                "Activation", animation);
    }

    @Override public WeaponAttachPoint getWeaponAttachPoint() { return WeaponAttachPoint.ATTACH_POINT_SHOULDER; }
    @Override public boolean usesShoulderPitchAiming() { return true; }
    @Override public WeaponCategory getWeaponCategory() { return WeaponCategory.RIFLE; }
    @Override public Motion getMotion() { return Motion.CHARGE_LASER; }
    @Override public int getCoolTime() { return COOLDOWN_TICKS; }
    @Override public String getPartsSeriesName() { return "kutara"; }
    @Override public PomkotsCustomThrowableProjectile.RangeCategory getCurrentRange(int distance) {
        return distance <= MAX_RANGE
                ? PomkotsCustomThrowableProjectile.RangeCategory.EFFECTIVE
                : PomkotsCustomThrowableProjectile.RangeCategory.OUT;
    }
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Activation", 0,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop("animation.kutara.idle")))
                .triggerableAnim("idle", RawAnimation.begin().thenLoop("animation.kutara.idle"))
                .triggerableAnim("charge1", RawAnimation.begin().thenLoop("animation.kutara.charge1"))
                .triggerableAnim("charge2", RawAnimation.begin().thenLoop("animation.kutara.charge2"))
                .triggerableAnim("charge3", RawAnimation.begin().thenLoop("animation.kutara.charge3"))
                .triggerableAnim("fire", RawAnimation.begin()
                        .thenPlay("animation.kutara.fire")
                        .thenLoop("animation.kutara.idle"))
                .triggerableAnim("fire_sustain", RawAnimation.begin()
                        .thenPlay("animation.kutara.fire")
                        .thenLoop("animation.kutara.fire_sustain")));
    }

    private record LaserSlotKey(UUID mechId, int weaponSlot) { }
    private record PulseBurst(int remainingShots, long nextFireTick) { }
}
