package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.FlyingBossPositionRecoveryGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.OrbitalFlyGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissilePodEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.SplittingMissileEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class Pmb08Entity extends BaseBossEntity {
    @Override
    protected boolean shouldStayAboveWaterSurface() { return true; }

    @Override
    public String getMechName() {
        return "pmb08";
    }

    public Pmb08Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);

        this.setNoGravity(true);

        actionController.registerAction("missile_straight", new BossActionController.BossAction(60,20, false, this::missileStraightAction));
        actionController.registerAction("missile_horizontal", new BossActionController.BossAction(60,20, false, this::missileHorizontalAction));
        actionController.registerAction("missile_vertical", new BossActionController.BossAction(60,20, false, this::missileVerticalAction));

        actionController.registerAction("missile_pod", new BossActionController.BossAction(200,20, false, this::missilePodAction));
        actionController.registerAction("missile_splitting", new BossActionController.BossAction(160,20, false, this::missileSplittingAction));
        actionController.registerAction("missile_horizontal_oni", new BossActionController.BossAction(90,18, false, this::missileHorizontalOniAction));
        actionController.registerAction("missile_straight_oni", new BossActionController.BossAction(100,18, false, this::missileStraightOniAction));
        actionController.registerAction("missile_straight_oni_barrage", new BossActionController.BossAction(100,18, false, this::missileStraightOniBarrageAction));
        actionController.registerAction("missile_vertical_oni", new BossActionController.BossAction(110,18, false, this::missileVerticalOniAction));

        actionController.registerAction("gatling", new BossActionController.BossAction(40,5, false, this::gatlingAction));

        actionController.registerAction("onsmalldown", new BossActionController.BossAction(20,15, this::smallDown));

        this.goalSelector.addGoal(0, new FlyingBossPositionRecoveryGoal(this));
        this.registerMovementGoal(
                new OrbitalFlyGoal(this, getMechData().speed * 3.0D, 40, 100, 10, 30),
                AI_MODE_ALL,
                10
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("gatling"), this, true, 5, true),
                AI_MODE_ALL,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_straight"), this, true, true),
                AI_MODE_ALL,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_horizontal"), this, false),
                AI_MODE_ALL,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_vertical"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_splitting"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_pod"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_horizontal_oni"), this, false),
                AI_MODE_ALL, 10);
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_straight_oni"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4}, 10);
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_straight_oni_barrage"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4}, 10);
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_vertical_oni"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4}, 10);

    }

    public void tick() {
        if (this.firstTick && this.isServerSide()) {
            this.registerAdditionalHitBox(new BossHitBoxEntity(PomkotsMechs.HITBOX_PMB08.get(), this.level(), this));
        }
        this.setNoGravity(true);
        super.tick();
    }

    private void gatlingAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "gatling");

        } else if (action.currentActionTick == 3) {
            for (int side = -1; side < 2; side += 2) {
                BulletGrenadeEntity be = new BulletGrenadeEntity(PomkotsMechs.BULLET_GRENADE.get(), this.level(), this,
                        getMechData().bulletDamage);
                be.setNoGravity(true);
                be.setExplosionScale(1);
                be.setStunPoint(2);

                var offset = this.position();

                var muzzlPos = new Vec3(8 * side, 10, 3);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), getMechData().bulletSpeed, 0F);

                this.level().addFreshEntity(be);

                ParticleUtil.spawnAttachedMuzzleFlash(
                        (ServerLevel) level(),
                        this,
                        50D,
                        new Vec3(8 * side, 10, 6)
                );
            }
        }
    }

    private float missileModifier = 2F;

    private void missileStraightAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");

        } else if (action.currentActionTick == 2) {
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed * missileModifier);
                        be.setMaxRotationAnglePerTick(2);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotX = (col * 0.8F + 15F) * side;
                        float slotY = row + 3F;

                        var muzzlPos = new Vec3(slotX, slotY, 3);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                        be.setPos(offset.add(muzzlPos));

                        float[] angle = Utils.getShootingAngle(be, target, true);

                        be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(),
                                getMechData().missileSpeed, 0F);

                        this.level().addFreshEntity(be);
                    }
                }
            }
        }
    }

    private void missileHorizontalAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");

        } else if (action.currentActionTick == 2) {
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed * missileModifier);
                        be.setMaxRotationAnglePerTick(2);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotX = (col * 0.8F + 15F) * side;
                        float slotY = row + 3F;

                        var muzzlPos = new Vec3(slotX, slotY, 3);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                        be.setPos(offset.add(muzzlPos));

                        float[] angle = Utils.getShootingAngle(be, target, true);

                        be.shootFromRotation(be, angle[0], angle[1] - side * 30, this.getFallFlyingTicks(),
                                getMechData().missileSpeed, 0F);

                        this.level().addFreshEntity(be);
                    }
                }
            }
        }
    }

    private void missileVerticalAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");

        } else if (action.currentActionTick == 2) {
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed * missileModifier);
                        be.setMaxRotationAnglePerTick(7);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotX = (col * 0.8F + 15F) * side;
                        float slotY = row + 3F;

                        var muzzlPos = new Vec3(slotX, slotY, 3);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                        be.setPos(offset.add(muzzlPos));

                        float[] angle = Utils.getShootingAngle(be, target, true);

                        be.shootFromRotation(be, -80, angle[1], this.getFallFlyingTicks(),
                                getMechData().missileSpeed, 0F);

                        this.level().addFreshEntity(be);
                    }
                }
            }
        }
    }

    private void missilePodAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");
        } else if (action.currentActionTick == 2) {
            for (int side = -1; side < 2; side += 2) {
                MissilePodEntity be = new MissilePodEntity(PomkotsMechs.MISSILE_POD.get(), this.level(), this, target,
                        getMechData().missileDamage, getMechData().missileSpeed * missileModifier);

                var offset = this.position();

                var muzzlPos = new Vec3(15 * side, 3.5, 7);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, -15, angle[1], this.getFallFlyingTicks(),
                        getMechData().missileSpeed, 0F);

                this.level().addFreshEntity(be);
            }
        }
    }

    private void missileHorizontalOniAction(BossActionController.BossAction action) {
        LivingEntity target = getTarget();
        if (target == null) return;
        if (action.onStartOfAction()) {
            triggerAnim("action_controller", "missile");
        } else if (isOniVolleyTick(action)) {
            for (int side = -1; side <= 1; side += 2) {
                for (int col = 0; col < 2; col++) {
                    Vec3 muzzle = localMuzzle(side * (14.5D + col * 0.8D), 3.5D, 5.5D + col);
                    spawnOniMissile(target, muzzle, null, -side * (34.0F + col * 5.0F), 4.0F);
                }
            }
        }
    }

    private void missileStraightOniAction(BossActionController.BossAction action) {
        LivingEntity target = getTarget();
        if (target == null) return;
        if (action.onStartOfAction()) {
            triggerAnim("action_controller", "missile");
        } else if (isOniVolleyTick(action)) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    spawnOniMissile(target,
                            localMuzzle((col - 1) * 2.2D, 3.0D + row * 1.4D, 8.0D),
                            0.0F, 0.0F, 3.0F);
                }
            }
        }
    }

    private void missileStraightOniBarrageAction(BossActionController.BossAction action) {
        LivingEntity target = getTarget();
        if (target == null) return;
        if (action.onStartOfAction()) {
            triggerAnim("action_controller", "missile");
        } else if (isOniVolleyTick(action)) {
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    spawnOniMissile(target,
                            localMuzzle((col - 0.5D) * 3.0D, 3.5D + row * 1.8D, 8.0D),
                            0.0F, 0.0F, 3.0F);
                }
            }
        }
    }

    private void missileVerticalOniAction(BossActionController.BossAction action) {
        LivingEntity target = getTarget();
        if (target == null) return;
        if (action.onStartOfAction()) {
            triggerAnim("action_controller", "missile");
        } else if (isOniVolleyTick(action)) {
            for (int side = -1; side <= 1; side += 2) {
                for (int col = 0; col < 3; col++) {
                    spawnOniMissile(target,
                            localMuzzle(side * (14.0D + col), 5.0D, 4.0D),
                            -78.0F, side * (18.0F + col * 4.0F), 7.0F);
                }
            }
        }
    }

    private boolean isOniVolleyTick(BossActionController.BossAction action) {
        return action.currentActionTick == 3 || action.currentActionTick == 6
                || action.currentActionTick == 9 || action.currentActionTick == 12
                || action.currentActionTick == 15 || action.currentActionTick == 18;
    }

    private Vec3 localMuzzle(double x, double y, double z) {
        return new Vec3(x, y, z)
                .yRot((float) Math.toRadians(-getYRot()))
                .add(position());
    }

    private void spawnOniMissile(LivingEntity target, Vec3 muzzle,
                                 Float fixedPitch, float yawOffset, float turnRate) {
        float speed = getMechData().missileSpeed * missileModifier;
        MissileGenericEntity missile = new MissileGenericEntity(
                PomkotsMechs.MISSILE_GENERIC.get(), level(), this, target,
                getMechData().missileDamage, speed);
        missile.setMaxRotationAnglePerTick(turnRate);
        missile.setPos(muzzle);
        float[] angle = Utils.getShootingAngle(missile, target, true);
        missile.shootFromRotation(this, fixedPitch == null ? angle[0] : fixedPitch,
                angle[1] + yawOffset, getFallFlyingTicks(), speed, 0.0F);
        level().addFreshEntity(missile);
    }

    private void missileSplittingAction(BossActionController.BossAction action) {
        LivingEntity target = getTarget();
        if (target == null) return;
        if (action.onStartOfAction()) {
            triggerAnim("action_controller", "missile");
        } else if (action.currentActionTick == 4) {
            float childSpeed = getMechData().missileSpeed * missileModifier;
            float parentSpeed = childSpeed * 1.35F;
            for (int side = -1; side <= 1; side += 2) {
                SplittingMissileEntity missile = new SplittingMissileEntity(
                        PomkotsMechs.MISSILE_SPLITTING.get(), level(), this, target,
                        getMechData().missileDamage, parentSpeed, childSpeed);
                missile.setPos(localMuzzle(side * 14.5D, 4.0D, 7.0D));
                missile.setMaxRotationAnglePerTick(3.0F);
                float[] angle = Utils.getShootingAngle(missile, target, true);
                missile.shootFromRotation(this, angle[0], angle[1], getFallFlyingTicks(),
                        parentSpeed, 0.0F);
                level().addFreshEntity(missile);
            }
        }
    }

    @Override
    protected void onStun() {
        this.triggerAnim("action_controller", "on_stun");
    }

    @Override
    protected void offStun() {
        this.triggerAnim("action_controller", "off_stun");
    }

    @Override
    protected void onSmallDown() {
        this.actionController.reset();
        this.actionController.getAction("onsmalldown").startAction();
        this.triggerAnim("action_controller", "on_small_down");
    }

    private void smallDown(BossActionController.BossAction action) {

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            if (this.getAiMode() == AI_MODE_INACTIVE && !this.isInactiveStanding()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.inactive"));

            } else if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.idle"));

            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.idle"));

            }
        }));


        controllers.add(new AnimationController<>(this, "rotor", 1, event -> {
            if (this.getAiMode() == AI_MODE_INACTIVE) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.idle"));

            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.rotor"));

            }
        }).setSoundKeyframeHandler(this::playSounds)
        );

        controllers.add(new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
                .triggerableAnim("dash", RawAnimation.begin().thenPlay("animation.pmb03.boost1").thenLoop("animation.pmb03.boost2"))
                .triggerableAnim("gatling", RawAnimation.begin().thenPlayXTimes("animation.pmb03.gatling", 5))
                .triggerableAnim("missile", RawAnimation.begin().thenPlay("animation.pmb03.missilepod"))
                .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb03.boot"))

                .triggerableAnim("on_stun", RawAnimation.begin().thenPlay("animation.pmb03.hurt").thenPlayAndHold("animation.pmb03.down"))
                .triggerableAnim("off_stun", RawAnimation.begin().thenPlay("animation.pmb03.up"))

                .triggerableAnim("on_small_down", RawAnimation.begin().thenPlay("animation.pmb03.hurt"))

                .setSoundKeyframeHandler(this::playSounds)
        );
    }

    @Override
    public void boot() {
        super.boot();
        this.triggerAnim("action_controller", "boot");
    }

    @Override
    protected void applyPlayerControll() {

    }
}
