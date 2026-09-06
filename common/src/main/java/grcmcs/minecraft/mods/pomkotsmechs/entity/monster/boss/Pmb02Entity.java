package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.OrbitalFlyGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.FlyingBossPositionRecoveryGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissilePodEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.SplittingMissileEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class Pmb02Entity extends BaseBossEntity {
    @Override
    protected boolean shouldStayAboveWaterSurface() { return true; }

    @Override
    public String getMechName() {
        return "pmb02";
    }

    public Pmb02Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);

        this.setNoGravity(true);

        actionController.registerAction("missile_straight", new BossActionController.BossAction(60,20, false, this::missileStraightAction));
        actionController.registerAction("missile_horizontal", new BossActionController.BossAction(60,20, false, this::missileHorizontalAction));
        actionController.registerAction("missile_vertical", new BossActionController.BossAction(60,20, false, this::missileVerticalAction));
        actionController.registerAction("missile_full", new BossActionController.BossAction(1000,20, false, this::missileFullAction));
        actionController.registerAction("gatling", new BossActionController.BossAction(40,5, false, this::gatlingAction));
        actionController.registerAction("missile_horizontal_oni", new BossActionController.BossAction(90,18, false, this::missileHorizontalOniAction));
        actionController.registerAction("missile_straight_oni", new BossActionController.BossAction(100,18, false, this::missileStraightOniAction));
        actionController.registerAction("missile_straight_oni_barrage", new BossActionController.BossAction(100,18, false, this::missileStraightOniBarrageAction));
        actionController.registerAction("missile_vertical_oni", new BossActionController.BossAction(110,18, false, this::missileVerticalOniAction));
        actionController.registerAction("missile_splitting", new BossActionController.BossAction(160,20, false, this::missileSplittingAction));
        actionController.registerAction("missile_splitting_vertical", new BossActionController.BossAction(160,20, false, this::missileSplittingVerticalAction));

        actionController.registerAction("missile_pod", new BossActionController.BossAction(200,20, false, this::missilePodAction));
        actionController.registerAction("canon", new BossActionController.BossAction(60,20, false, this::canonAction));
        actionController.registerAction("saber_horizontal", new BossActionController.BossAction(60,105, this::saberHorizontal));

        actionController.registerAction("onsmalldown", new BossActionController.BossAction(20,15, this::smallDown));

        this.goalSelector.addGoal(0, new FlyingBossPositionRecoveryGoal(this));

        this.registerMovementGoal(
                // Keep an XZ orbit radius of at least 40 blocks. This makes staying
                // directly below the boss unreliable without adding a hard, high-speed
                // retreat that would shut melee attackers out completely.
                new OrbitalFlyGoal(this, getMechData().speed * 3.0D, 40, 100, 10, 30),
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
        this.registerActionGoal(new SimpleBossAttackGoal(actionController.getAction("saber_horizontal"), this, false),
                AI_MODE_ALL,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_pod"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_1, AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("canon"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_vertical"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_full"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("gatling"), this, true, 5, true),
                new int[]{AI_MODE_BATTLE_PHASE_1, AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4}, 10);
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_horizontal_oni"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_1, AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4}, 10);
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_straight_oni"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4}, 10);
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_straight_oni_barrage"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4}, 10);
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_vertical_oni"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4}, 10);
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_splitting"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4}, 10);
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_splitting_vertical"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4}, 10);

    }

    public void tick() {
        if (this.firstTick && this.isServerSide()) {
            this.registerAdditionalHitBox(new BossHitBoxEntity(PomkotsMechs.HITBOX_PMB02.get(), this.level(), this));
        }
        this.setNoGravity(true);
        super.tick();
    }

    private void saberHorizontal(BossActionController.BossAction action) {
        final float speed = 9;
        final float homingBlendPerTick = 0.32F;
        final float attackDistanceSq = 15F * 15F;
        final int startChargeTick = 15;
        final int maxHomingTicks = 60;

        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "dash");

        } else if (action.currentActionTick > startChargeTick && action.currentActionTick <= maxHomingTicks + startChargeTick) {
            Vec3 mobPos = this.position();
            // Lead fast targets slightly so the charge does not keep aiming at an
            // already stale position.
            Vec3 targetPos = target.position().add(target.getDeltaMovement().scale(2.5D));
            Vec3 direction = targetPos.subtract(mobPos).normalize();

            // 現在の向き
            Vec3 forward = this.getLookAngle().normalize();

            // 補間して新しい向きを計算
            Vec3 newForward = forward.lerp(direction, homingBlendPerTick).normalize();

            // 新しい速度ベクトル
            double distance = mobPos.distanceTo(target.position());
            double approachSpeed = net.minecraft.util.Mth.clamp(
                    (distance - 8.0D) * 0.45D, 2.0D, speed);
            Vec3 movement = newForward.scale(approachSpeed);

            this.setDeltaMovement(movement);
            this.rotateDegree((float)(Math.toDegrees(Math.atan2(newForward.z, newForward.x)) - 90.0));

            double distSq = this.distanceToSqr(target);

            if (distSq <= attackDistanceSq || action.currentActionTick == maxHomingTicks + startChargeTick) {
                if (distSq <= attackDistanceSq) {
                    action.currentActionTick = maxHomingTicks + startChargeTick;
                }
                // Remove the charge inertia while the strike animation catches up.
                this.setDeltaMovement(newForward.scale(0.35D));
                this.triggerAnim("action_controller", "saber_horizontal");
            }
        } else if (action.currentActionTick == maxHomingTicks + startChargeTick + 10) {
            var pilePos1 = new Vec3(14, 4F, 14).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
            var pilePos2 = new Vec3(-14, -4F, -4F).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
            var kbVel = new Vec3(0, 0, -1F).yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            for (var ent : this.level().getEntities(null, new AABB(pilePos1, pilePos2))) {
                if (!isSelf(ent) && ent instanceof LivingEntity le) {
                    le.knockback(2, kbVel.x, kbVel.z);
                    le.hurt(this.damageSources().generic(), getMechData().meleeDamage);
                }
            }
        }
    }

    private void canonAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "canon");

        } else if (action.currentActionTick == 7) {
            for (int side = -1; side < 2; side += 2) {
                BulletGrenadeEntity be = new BulletGrenadeEntity(PomkotsMechs.BULLET_GRENADE.get(), this.level(), this,
                        getMechData().bulletDamage);
                be.setNoGravity(true);
                be.setExplosionScale(3);

                var offset = this.position();

                var muzzlPos = new Vec3(2 * side, 4, 7);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(),
                        getMechData().bulletSpeed, 0F);

                this.level().addFreshEntity(be);
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
                        getMechData().missileDamage, pmb02MissileSpeed());

                var offset = this.position();

                var muzzlPos = new Vec3(4 * side, 3.5, 7);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(this, -15, angle[1], this.getFallFlyingTicks(),
                        pmb02MissileSpeed(), 0F);

                this.level().addFreshEntity(be);
            }
        }
    }

    /** Existing effective homing speed (1.5x) boosted by another 1.5x. */
    private static final float MISSILE_SPEED_MULTIPLIER = 2.25F;
    private static final float SPLITTING_PARENT_SPEED_MULTIPLIER = 1.35F;

    private float pmb02MissileSpeed() {
        return getMechData().missileSpeed * MISSILE_SPEED_MULTIPLIER;
    }

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
                                getMechData().missileDamage, pmb02MissileSpeed());
                        be.setMaxRotationAnglePerTick(2);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotX = (col * 0.8F + 1.5F) * side;
                        float slotY = row + 0.5F;

                        var muzzlPos = new Vec3(slotX, slotY, 3);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                        be.setPos(offset.add(muzzlPos));

                        float[] angle = Utils.getShootingAngle(be, target, true);

                        be.shootFromRotation(this, angle[0], angle[1], this.getFallFlyingTicks(),
                                pmb02MissileSpeed(), 0F);

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
                                getMechData().missileDamage, pmb02MissileSpeed());
                        be.setMaxRotationAnglePerTick(2);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotZ = (col * 0.8F + 1.5F);
                        float slotY = row + 0.8F;

                        var muzzlPos = new Vec3(side * 7, slotY, slotZ);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                        be.setPos(offset.add(muzzlPos));

                        float[] angle = Utils.getShootingAngle(be, target, true);

                        be.shootFromRotation(this, angle[0], angle[1] - side * 30, this.getFallFlyingTicks(),
                                pmb02MissileSpeed(), 0F);

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
                                getMechData().missileDamage, pmb02MissileSpeed());
                        be.setMaxRotationAnglePerTick(7);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotX = (col * 0.8F + 1.5F) * side;
                        float slotZ = - row - 0.8F;

                        var muzzlPos = new Vec3(slotX, 8, slotZ);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                        be.setPos(offset.add(muzzlPos));

                        float[] angle = Utils.getShootingAngle(be, target, true);

                        be.shootFromRotation(this, -80, angle[1], this.getFallFlyingTicks(),
                                pmb02MissileSpeed(), 0F);

                        this.level().addFreshEntity(be);
                    }
                }
            }
        }
    }

    private void missileFullAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");

        } else if (action.currentActionTick == 2) {
            int mNum = 9;

            for (int i = 0; i < mNum; i++) {
                for (int j = 0; j < mNum; j++) {
                    MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                            getMechData().missileDamage, pmb02MissileSpeed());
                    be.setMaxRotationAnglePerTick(6);

                    be.setPos(this.position());
                    be.shootFromRotation(this, -i * (90/mNum), -90 + (j * (180/mNum)),
                            this.getFallFlyingTicks(), pmb02MissileSpeed(), 0F);
                    this.level().addFreshEntity(be);

                }
            }
        }
    }

    private void gatlingAction(BossActionController.BossAction action) {
        LivingEntity target = getTarget();
        if (target == null) return;

        if (action.onStartOfAction() && action.isFirstLoop()) {
            triggerAnim("action_controller", "canon");
        } else if (action.currentActionTick == 3) {
            for (int side = -1; side <= 1; side += 2) {
                BulletGrenadeEntity bullet = new BulletGrenadeEntity(
                        PomkotsMechs.BULLET_GRENADE.get(), level(), this,
                        getMechData().bulletDamage);
                bullet.setNoGravity(true);
                bullet.setExplosionScale(1);
                bullet.setStunPoint(2);
                Vec3 localMuzzle = new Vec3(side * 2.0D, 4.0D, 7.0D);
                Vec3 muzzle = position().add(localMuzzle.yRot((float) Math.toRadians(-getYRot())));
                bullet.setPos(muzzle);
                float[] angle = Utils.getShootingAngle2(
                        bullet, target, true, false, getMechData().bulletSpeed, 0);
                bullet.shootFromRotation(bullet, angle[0], angle[1], 0,
                        getMechData().bulletSpeed, 0.0F);
                level().addFreshEntity(bullet);

                if (level() instanceof ServerLevel serverLevel) {
                    ParticleUtil.spawnAttachedMuzzleFlash(serverLevel, this, 50.0D,
                            new Vec3(side * 2.0D, 4.0D, 7.5D));
                }
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
                    Vec3 muzzle = new Vec3(side * 7.0D, 1.5D, 1.5D + col)
                            .yRot((float) Math.toRadians(-getYRot())).add(position());
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
                    Vec3 muzzle = new Vec3((col - 1) * 1.8D, 1.0D + row * 1.2D, 4.0D)
                            .yRot((float) Math.toRadians(-getYRot())).add(position());
                    spawnOniMissile(target, muzzle, 0.0F, 0.0F, 3.0F);
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
                    Vec3 muzzle = new Vec3((col - 0.5D) * 2.4D, 1.6D + row * 1.6D, 4.0D)
                            .yRot((float) Math.toRadians(-getYRot())).add(position());
                    spawnOniMissile(target, muzzle, 0.0F, 0.0F, 3.0F);
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
                    Vec3 muzzle = new Vec3(side * (5.0D + col * 0.9D), 5.0D, 0.5D)
                            .yRot((float) Math.toRadians(-getYRot())).add(position());
                    spawnOniMissile(target, muzzle, -78.0F, side * (18.0F + col * 4.0F), 7.0F);
                }
            }
        }
    }

    private boolean isOniVolleyTick(BossActionController.BossAction action) {
        return action.currentActionTick == 3
                || action.currentActionTick == 6
                || action.currentActionTick == 9
                || action.currentActionTick == 12
                || action.currentActionTick == 15
                || action.currentActionTick == 18;
    }

    private void spawnOniMissile(LivingEntity target, Vec3 muzzle,
                                 Float fixedPitch, float yawOffset, float turnRate) {
        MissileGenericEntity missile = new MissileGenericEntity(
                PomkotsMechs.MISSILE_GENERIC.get(), level(), this, target,
                getMechData().missileDamage, pmb02MissileSpeed());
        missile.setMaxRotationAnglePerTick(turnRate);
        missile.setPos(muzzle);
        float[] angle = Utils.getShootingAngle(missile, target, true);
        missile.shootFromRotation(this, fixedPitch == null ? angle[0] : fixedPitch,
                angle[1] + yawOffset, getFallFlyingTicks(), pmb02MissileSpeed(), 0.0F);
        level().addFreshEntity(missile);
    }

    private void missileSplittingAction(BossActionController.BossAction action) {
        LivingEntity target = getTarget();
        if (target == null) return;
        if (action.onStartOfAction()) {
            triggerAnim("action_controller", "missile");
        } else if (action.currentActionTick == 4) {
            float parentSpeed = pmb02MissileSpeed() * SPLITTING_PARENT_SPEED_MULTIPLIER;
            // Fire from widely separated launchers so the two parent missiles
            // visibly converge before opening into their child volleys.
            for (int side = -1; side <= 1; side += 2) {
                SplittingMissileEntity missile = new SplittingMissileEntity(
                        PomkotsMechs.MISSILE_SPLITTING.get(), level(), this, target,
                        getMechData().missileDamage, parentSpeed, pmb02MissileSpeed());
                Vec3 muzzle = new Vec3(side * 8.0D, 5.0D, 5.0D)
                        .yRot((float) Math.toRadians(-getYRot())).add(position());
                missile.setPos(muzzle);
                missile.setMaxRotationAnglePerTick(3.0F);
                float[] angle = Utils.getShootingAngle(missile, target, true);
                missile.shootFromRotation(this, angle[0], angle[1], getFallFlyingTicks(),
                        parentSpeed, 0.0F);
                level().addFreshEntity(missile);
            }
        }
    }

    private void missileSplittingVerticalAction(BossActionController.BossAction action) {
        LivingEntity target = getTarget();
        if (target == null) return;
        if (action.onStartOfAction()) {
            triggerAnim("action_controller", "missile");
        } else if (action.currentActionTick == 4) {
            float parentSpeed = pmb02MissileSpeed() * SPLITTING_PARENT_SPEED_MULTIPLIER;
            SplittingMissileEntity missile = new SplittingMissileEntity(
                    PomkotsMechs.MISSILE_SPLITTING.get(), level(), this, target,
                    getMechData().missileDamage, parentSpeed, pmb02MissileSpeed());
            Vec3 muzzle = new Vec3(0.0D, 7.0D, 0.0D)
                    .yRot((float) Math.toRadians(-getYRot())).add(position());
            missile.setPos(muzzle);
            missile.setMaxRotationAnglePerTick(3.0F);
            float[] angle = Utils.getShootingAngle(missile, target, true);
            missile.shootFromRotation(this, -78.0F, angle[1], getFallFlyingTicks(),
                    parentSpeed, 0.0F);
            level().addFreshEntity(missile);
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
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb02.inactive"));

            } else if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb02.idle"));

            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb02.idle"));

            }
        }));

        controllers.add(new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
                .triggerableAnim("dash", RawAnimation.begin().thenPlay("animation.pmb02.dash1").thenLoop("animation.pmb02.dash2"))
                .triggerableAnim("saber_horizontal", RawAnimation.begin().thenPlay("animation.pmb02.attack_arm_horizontal"))
                .triggerableAnim("canon", RawAnimation.begin().thenPlay("animation.pmb02.canon"))
                .triggerableAnim("missile", RawAnimation.begin().thenPlay("animation.pmb02.missile"))
                .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb02.boot"))

                .triggerableAnim("on_stun", RawAnimation.begin().thenPlay("animation.pmb02.hurt").thenPlayAndHold("animation.pmb02.down"))
                .triggerableAnim("off_stun", RawAnimation.begin().thenPlay("animation.pmb02.up"))

                .triggerableAnim("on_small_down", RawAnimation.begin().thenPlay("animation.pmb02.hurt"))

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
