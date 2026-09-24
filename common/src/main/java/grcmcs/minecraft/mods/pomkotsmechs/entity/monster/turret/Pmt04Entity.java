package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.goal.ContinuousAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

// Charging Mob
public class Pmt04Entity extends BaseTurretEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 1.0f;
    private static final int MISSILES_PER_BURST = 6;
    private static final int SLOTS_PER_ROW = 3;
    private static final int MISSILE_INTERVAL_TICKS = 3;
    /** Match PMB02's fast, aggressive missile tuning. */
    private static final float MISSILE_SPEED_MULTIPLIER = 2.25F;
    private static final float MISSILE_TURN_RATE_DEG = 7.0F;

    @Override
    public String getMechName() {
        return "pmt04";
    }

    public Pmt04Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
        alwaysLookAtTarget = false;
    }

    protected void registerTargetSelectorGoals() {
        super.registerTargetSelectorGoals();
        // The goal decrements before firing: remaining ticks 18, 15, 12, 9, 6, 3.
        this.goalSelector.addGoal(1, new ContinuousAttackGoal(this, 0.8F,
                MISSILES_PER_BURST * MISSILE_INTERVAL_TICKS + 1, 60, MISSILE_INTERVAL_TICKS));
    }

    private int curSlot = 0;
    public void startActionAnimation() {
        this.triggerAnim("shoot_controller", "shoot");
        curSlot = 0;
    }

    public void stopActionAnimation() {
        this.triggerAnim("shoot_controller", "stop");
    }

    @Override
    public void doAttack() {
        var target = this.getTarget();
        if (target != null) {
            float missileSpeed = getMechData().missileSpeed * MISSILE_SPEED_MULTIPLIER;

            var offset = this.position();

            int slot = curSlot++ % MISSILES_PER_BURST;
            float slotX = 1 + slot % SLOTS_PER_ROW;
            float slotZ = 1 + slot / SLOTS_PER_ROW;

            var muzzlPos = new Vec3(slotX, 2, slotZ);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                    getMechData().missileDamage, missileSpeed);
            be.setMaxRotationAnglePerTick(MISSILE_TURN_RATE_DEG);
            be.setPos(offset.add(muzzlPos));

            be.shootFromRotation(be, -90, this.getYRot(), this.getFallFlyingTicks(), missileSpeed, 0F);

            this.level().addFreshEntity(be);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0,
                event -> event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmt04.idle"))));

        controllers.add(new AnimationController<>(this, "shoot_controller", state -> PlayState.STOP)
                .triggerableAnim("shoot", RawAnimation.begin().thenPlay("animation.pmt04.attack"))
                .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.pmt04.idle"))
                .setSoundKeyframeHandler(this::registerAnimationSoundHandlers)
        );
    }

    @Override
    public int getMaxAttackCooltime() {
        return 2;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
