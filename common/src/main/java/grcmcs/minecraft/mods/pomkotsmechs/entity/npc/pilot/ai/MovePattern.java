package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai;

import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.SBUnitProtoTypeItem;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

public abstract class MovePattern {
    public record MovePatternEntry(
            int baseWeight,
            Class<? extends MovePattern> patternClass,
            Supplier<MovePattern> factory
    ) {
    }

    protected final LivingEntity pilot;
    protected final Pmvc01Entity mech;
    protected final RandomSource random;

    protected LivingEntity target;

    protected int tick;
    protected int maxTicks;

    protected float preferredDistance;

    protected MechAutoController controller;

    protected MovePattern(
            LivingEntity pilot,
            Pmvc01Entity mech,
            MechAutoController controller,
            float preferredDistance
    ) {
        this.pilot = pilot;
        this.mech = mech;
        this.preferredDistance = preferredDistance;
        this.random = RandomSource.create();
        this.controller = controller;
    }

    public void start(LivingEntity target) {
        this.tick = 0;
        this.target = target;
    }

    public abstract void tick(DriverInput input);

    protected void lookAtYaw(
            float yaw
    ) {
        pilot.setYRot(yaw);
        pilot.setYHeadRot(yaw);
        pilot.yRotO = yaw;
    }

    public boolean shouldExit() {
        return tick >= maxTicks;
    }

    public boolean controlsAim() {
        return false;
    }

    public void stop() {
    }

    protected double distanceToTarget() {
        return pilot.distanceTo(target);
    }

    protected void setMove(
            float xxa,
            float zza,
            DriverInput input
    ) {
        pilot.xxa = xxa;
        pilot.zza = zza;

        input.setForwardPressed(zza > 0);
        input.setBackPressed(zza < 0);

        input.setLeftPressed(xxa > 0);
        input.setRightPressed(xxa < 0);
    }

    public static class OrbitLeftPattern extends MovePattern {
        public OrbitLeftPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(LivingEntity target) {
            super.start(target);
            maxTicks = 20 + random.nextInt(40);
        }

        @Override
        public void tick(DriverInput input) {
            tick++;

            double distance = distanceToTarget();

            float zza;
            if (distance < preferredDistance * 0.8F) {
                zza = -0.4F;
            } else if (distance > preferredDistance * 1.2F) {
                zza = 1.0F;
            } else {
                zza = 0.2F;
            }

            setMove(
                    1.0F,
                    zza,
                    input
            );
        }
    }

    public static class OrbitRightPattern extends MovePattern {
        public OrbitRightPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(LivingEntity target) {
            super.start(target);
            maxTicks = 20 + random.nextInt(40);
        }

        @Override
        public void tick(DriverInput input) {
            if (tick == 0) {
                input.setEvasionPressed(true);
            }

            tick++;

            double distance = distanceToTarget();
            float zza;

            if (distance < preferredDistance * 0.8F) {
                zza = -0.4F;
            } else if (distance > preferredDistance * 1.2F) {
                zza = 1.0F;
            } else {
                zza = 0.2F;
            }

            setMove(
                    -1.0F,
                    zza,
                    input
            );
        }
    }

    public static class RetreatPattern extends MovePattern {
        public RetreatPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(LivingEntity target) {
            super.start(target);
            maxTicks = 20 + random.nextInt(40);
        }

        @Override
        public void tick(DriverInput input) {
            if (tick == 0) {
                input.setEvasionPressed(true);
            }

            tick++;

            setMove(
                    0F,
                    -1F,
                    input
            );
        }

        @Override
        public boolean shouldExit() {
            if (distanceToTarget() > preferredDistance) {
                return true;
            }

            return super.shouldExit();
        }
    }

    public static class ChargePattern extends MovePattern {

        private final float chargeDistance;

        public ChargePattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance,
                float chargeDistance
        ) {
            super(pilot, mech, controller, preferredDistance);

            this.chargeDistance = chargeDistance;
        }

        @Override
        public void start(LivingEntity target) {
            super.start(target);
            maxTicks = 60;
        }

        @Override
        public void tick(DriverInput input) {
            if (tick == 0) {
                input.setEvasionPressed(true);
            }

            tick++;

            setMove(0F, 1F, input);
        }

        @Override
        public boolean shouldExit() {
            if (distanceToTarget() <= chargeDistance) {
                return true;
            }

            return super.shouldExit();
        }
    }

    public static class KitePattern extends MovePattern {

        private final boolean leftOrbit;

        public KitePattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);

            this.leftOrbit = random.nextBoolean();
        }

        @Override
        public void start(LivingEntity target) {
            super.start(target);
            maxTicks = 80 + random.nextInt(80);
        }

        @Override
        public void tick(DriverInput input) {
            tick++;

            float xxa = leftOrbit ? 0.6F : -0.6F;

            double distance = distanceToTarget();

            float zza;

            if (distance < preferredDistance) {
                zza = -1F;
            } else {
                zza = -0.2F;
            }

            setMove(
                    xxa,
                    zza,
                    input
            );
        }
    }

    public static class AirBoostEvasionPattern extends MovePattern {
        private float moveX;
        private float moveZ;

        public AirBoostEvasionPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        private void initMovement() {
            int dir = random.nextInt(13);

            switch (dir) {
                case 0, 1 -> { moveX = 0; moveZ = 1; }     // 前
                case 2, 3 -> { moveX = 1; moveZ = 1; }     // 左前
                case 4, 5 -> { moveX = -1; moveZ = 1; }    // 右前
                case 6, 7 -> { moveX = 1; moveZ = 0; }     // 左
                case 8, 9 -> { moveX = -1; moveZ = 0; }    // 右

                case 10 -> { moveX = 0; moveZ = -1; }    // 後
                case 11 -> { moveX = 1; moveZ = -1; }    // 左後
                default -> { moveX = -1; moveZ = -1; }  // 右後
            }
        }

        @Override
        public void start(LivingEntity target) {
            super.start(target);

            maxTicks = 60;
            initMovement();
        }

        @Override
        public void tick(
                DriverInput input
        ) {
            tick++;

            setMove(
                    moveX,
                    moveZ,
                    input
            );

            if (tick < 5) {
                // 離陸
                input.setJumpPressed(true);

            } else if (tick < 15) {
                // ブースト上昇
                input.setJumpPressed(true);

            } else if (tick < 25) {
                // 空中回避
                input.setJumpPressed(false);
                input.setEvasionPressed(true);

            } else {
                // 慣性移動
                input.setJumpPressed(false);
                input.setEvasionPressed(false);
            }
        }

        @Override
        public boolean shouldExit() {
            if (tick > 10 && mech.onGround() || mech.getEnergy() == 0) {
                return true;
            }

            return super.shouldExit();
        }
    }

    public static class VerticalApproachPattern extends MovePattern {
        private boolean verticalAscending;
        private float ceilingEscapeStrafe;

        public VerticalApproachPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(
                LivingEntity target
        ) {
            super.start(target);

            // 4～6秒くらい継続
            maxTicks = 80 + random.nextInt(40);
            ceilingEscapeStrafe = random.nextBoolean() ? 1.0F : -1.0F;
        }

        @Override
        public void tick(
                DriverInput input
        ) {
            if (tick == 0) {
                input.setEvasionPressed(true);
            }

            tick++;

            // 常に前進
            double signedYDifference = target == null ? 0.0D : target.getY() - mech.getY();
            if (signedYDifference > 0.0D) {
                double probeHeight = Math.max(8.0D, mech.getBbHeight() * 1.5D);
                boolean overheadClear = true;
                for (int i = 1; i <= 3; i++) {
                    if (!mech.level().noCollision(
                            mech,
                            mech.getBoundingBox().move(0.0D, probeHeight * i / 3.0D, 0.0D))) {
                        overheadClear = false;
                        break;
                    }
                }
                if (overheadClear) {
                    setMove(0.0F, 1.0F, input);
                    input.setJumpPressed(updateVerticalBoost(true));
                } else {
                    verticalAscending = false;
                    setMove(ceilingEscapeStrafe, 0.15F, input);
                    input.setJumpPressed(false);
                }
            } else {
                float strafe = (tick / 30) % 2 == 0 ? 0.8F : -0.8F;
                setMove(strafe, 0.35F, input);
                input.setJumpPressed(false);
            }

            // 常にジャンプ押しっぱ
        }

        private boolean updateVerticalBoost(boolean overheadClear) {
            if (!overheadClear) {
                verticalAscending = false;
                return false;
            }

            int energyPerTick = Math.max(0, mech.getEnergyConsumeVertical());
            int reserve = Math.max(energyPerTick * 6, Math.round(mech.getMaxEnergy() * 0.15F));
            if (verticalAscending) {
                if (mech.getEnergy() <= reserve) {
                    verticalAscending = false;
                    return false;
                }
                return true;
            }

            int requiredToStart = Math.min(
                    mech.getMaxEnergy(),
                    Math.max(reserve + energyPerTick * 12, Math.round(mech.getMaxEnergy() * 0.85F)));
            if (mech.getEnergy() < requiredToStart) return false;
            verticalAscending = true;
            return true;
        }

        @Override
        public boolean shouldExit() {

            if (target == null) {
                return true;
            }

            double yDiff =
                    Math.abs(
                            target.getY()
                                    - mech.getY()
                    );

            // 高低差が十分縮まった
            if (yDiff < 10) {
                return true;
            }

            return super.shouldExit();
        }
    }

    /**
     * High-energy assault shared by melee and ranged builds. The launch tracks
     * with a capped turn rate, then commits so a moving player can still dodge.
     */
    public static class SuperBoostAssaultPattern extends MovePattern {
        private static final int ACTIVATION_TIMEOUT_TICKS = 30;
        private static final int MAX_ACTIVE_RUSH_TICKS = 100;
        private static final int MELEE_ATTACK_TICKS = 10;
        private static final int RANGED_FULL_BURST_TICKS = 30;
        private static final double RANGED_BRAKE_DISTANCE = 16.0D;
        // PUNCH weapons such as Kagenobu apply their hit on action tick 10.
        // NPC assault speed is 6 blocks/tick, so begin the swing roughly 60
        // blocks before contact and leave a little allowance for the weapon's
        // forward hit volume. Homing stops here, preserving a dodge window.
        private static final double MELEE_ATTACK_DISTANCE = 50.0D;
        private static final double RANGED_ATTACK_DISTANCE = 45.0D;
        private static final float MAX_YAW_TURN_PER_TICK = 8.0F;
        private static final float MAX_PITCH_TURN_PER_TICK = 6.0F;

        private final boolean meleeAssault;
        private int superBoostSlot;
        private int activeRushTicks;
        private int attackTicks;
        private boolean attackStarted;
        private boolean finished;
        private boolean hasActivated;

        public SuperBoostAssaultPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance,
                boolean meleeAssault) {
            super(pilot, mech, controller, preferredDistance);
            this.meleeAssault = meleeAssault;
        }

        @Override
        public void start(LivingEntity target) {
            super.start(target);
            maxTicks = ACTIVATION_TIMEOUT_TICKS + MAX_ACTIVE_RUSH_TICKS
                    + (meleeAssault ? MELEE_ATTACK_TICKS : RANGED_FULL_BURST_TICKS);
            superBoostSlot = findSuperBoostSlot();
            mech.setNpcSuperBoostAssaultActive(true);
        }

        @Override
        public void tick(DriverInput input) {
            tick++;
            setMove(0.0F, 0.0F, input);

            if (target == null || !target.isAlive() || superBoostSlot == 0) {
                finished = true;
                return;
            }

            // Extension actions trigger on release, so create a one-tick pulse.
            setSuperBoostExtension(input, tick == 1);
            if (tick == 2) {
                setSuperBoostExtension(input, false);
            }

            if (!mech.isSuperBoost()) {
                aimTowardTarget();
                if (tick >= ACTIVATION_TIMEOUT_TICKS && !hasActivated && !attackStarted) {
                    finished = true;
                }
                if (attackStarted) tickAttack(input);
                return;
            }

            hasActivated = true;

            activeRushTicks++;
            double distance = mech.distanceTo(target);
            double attackDistance = meleeAssault ? MELEE_ATTACK_DISTANCE : RANGED_ATTACK_DISTANCE;

            // Stop homing near the victim (or after a short pursuit) and commit
            // to the current vector. This leaves a real sidestep dodge window.
            if (!attackStarted && distance > attackDistance
                    && activeRushTicks < MAX_ACTIVE_RUSH_TICKS) {
                aimTowardTarget();
                return;
            }

            if (!attackStarted) {
                attackStarted = true;
            }
            tickAttack(input);
        }

        private void tickAttack(DriverInput input) {
            attackTicks++;
            if (meleeAssault) {
                setWeaponInputs(input, true);
                if (attackTicks >= MELEE_ATTACK_TICKS) {
                    mech.cancelSuperBoost();
                    finished = true;
                }
            } else {
                // Keep the Super Boost committed during the full burst. Aim
                // correction remains capped, so this is a fast strafing rush
                // rather than an unavoidable homing attack.
                aimTowardTarget();
                setWeaponInputs(input, false);
                // Start firing at range, but brake once the rush has actually
                // reached its victim. The remainder of the burst continues
                // from nearby instead of overshooting by dozens of blocks.
                if (mech.isSuperBoost() && mech.distanceTo(target) <= RANGED_BRAKE_DISTANCE) {
                    mech.cancelSuperBoost();
                }
                // Do not end merely because the burst timer elapsed. A fast
                // target can still be far away at that point; keep pursuing
                // until the rush reaches braking distance, then finish.
                if (attackTicks >= RANGED_FULL_BURST_TICKS
                        && (!mech.isSuperBoost()
                        || mech.distanceTo(target) <= RANGED_BRAKE_DISTANCE)) {
                    mech.cancelSuperBoost();
                    finished = true;
                }
            }
        }

        private void setWeaponInputs(DriverInput input, boolean meleeOnly) {
            setWeaponInput(input, mech.getRightArmWeapon(), meleeOnly, 0);
            setWeaponInput(input, mech.getLeftArmWeapon(), meleeOnly, 1);
            setWeaponInput(input, mech.getRightShoulderWeapon(), meleeOnly, 2);
            setWeaponInput(input, mech.getLeftShoulderWeapon(), meleeOnly, 3);
        }

        private void setWeaponInput(DriverInput input, net.minecraft.world.item.ItemStack stack,
                                    boolean meleeOnly, int slot) {
            if (!(stack.getItem() instanceof BasePartsItem.Weapon weapon)) return;
            boolean melee = weapon.getWeaponCategory() == BasePartsItem.WeaponCategory.MELEE;
            if (meleeOnly != melee) return;
            switch (slot) {
                case 0 -> input.setWeaponRightHandPressed(true);
                case 1 -> input.setWeaponLeftHandPressed(true);
                case 2 -> input.setWeaponRightShoulderPressed(true);
                case 3 -> input.setWeaponLeftShoulderPressed(true);
            }
        }

        private void aimTowardTarget() {
            Vec3 delta = target.getBoundingBox().getCenter().subtract(mech.getEyePosition());
            double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
            float desiredYaw = (float)Math.toDegrees(Math.atan2(-delta.x, delta.z));
            float desiredPitch = (float)-Math.toDegrees(Math.atan2(delta.y, horizontal));
            float yaw = pilot.getYRot() + Mth.clamp(
                    Mth.wrapDegrees(desiredYaw - pilot.getYRot()),
                    -MAX_YAW_TURN_PER_TICK, MAX_YAW_TURN_PER_TICK);
            float pitch = pilot.getXRot() + Mth.clamp(
                    Mth.wrapDegrees(desiredPitch - pilot.getXRot()),
                    -MAX_PITCH_TURN_PER_TICK, MAX_PITCH_TURN_PER_TICK);
            lookAtYaw(yaw);
            pilot.setXRot(pitch);
            pilot.xRotO = pitch;
        }

        private int findSuperBoostSlot() {
            if (mech.getExtension1Weapon().getItem() instanceof SBUnitProtoTypeItem) return 1;
            if (mech.getExtension2Weapon().getItem() instanceof SBUnitProtoTypeItem) return 2;
            return 0;
        }

        private void setSuperBoostExtension(DriverInput input, boolean pressed) {
            if (superBoostSlot == 1) input.setExtension1Pressed(pressed);
            if (superBoostSlot == 2) input.setExtension2Pressed(pressed);
        }

        @Override
        public boolean controlsAim() {
            return true;
        }

        @Override
        public boolean shouldExit() {
            return finished || super.shouldExit();
        }

        @Override
        public void stop() {
            mech.setNpcSuperBoostAssaultActive(false);
            mech.cancelSuperBoost();
        }
    }

    public static class IdlePattern extends MovePattern {
        public IdlePattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(
                LivingEntity target
        ) {
            super.start(target);

            maxTicks = 100;
        }

        @Override
        public void tick(
                DriverInput input
        ) {
            tick++;

            setMove(
                    0F,
                    0F,
                    input
            );
        }
    }

    public static class PatrolPattern extends MovePattern {

        private float moveX;
        private float moveZ;

        private float moveYaw;

        public PatrolPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(
                LivingEntity target
        ) {
            super.start(target);

            controller.stopBoosting();

            maxTicks =
                    20 + random.nextInt(40);

            chooseDirection();
        }

        private void chooseDirection() {
            moveYaw =
                    random.nextFloat() * 360F;

            float rad =
                    moveYaw * Mth.DEG_TO_RAD;

            moveX = -Mth.sin(rad);
            moveZ = Mth.cos(rad);
        }

        @Override
        public void tick(
                DriverInput input
        ) {
            tick++;

            if (tick % 10 == 0) {

                moveYaw +=
                        random.nextFloat() * 20F
                                - 10F;
            }

            lookAtYaw(moveYaw);

            setMove(
                    0F,
                    1F,
                    input
            );

            if (
                    tick % 40 == 0
                            && random.nextFloat() < 0.3F
            ) {
                chooseDirection();
            }
        }
    }

    public static class FollowPlayerPattern extends MovePattern {
        private Player followTarget;

        public FollowPlayerPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(
                LivingEntity target
        ) {
            super.start(target);

            maxTicks = 100;

            followTarget = findNearestPlayer();
        }

        private Player findNearestPlayer() {
            var players =
                    mech.level()
                            .players();

            if (pilot instanceof MechPilotEntity mechPilot
                    && mechPilot.getWingmanMasterId().isPresent()) {
                var masterId = mechPilot.getWingmanMasterId().get();
                return players.stream()
                        .filter(player -> player.getUUID().equals(masterId))
                        .findFirst()
                        .orElse(null);
            }

            double bestDistance =
                    Double.MAX_VALUE;

            Player best = null;

            for (Player player : players) {

                double distance =
                        mech.distanceToSqr(player);

                if (distance < bestDistance) {

                    bestDistance = distance;
                    best = player;
                }
            }

            return best;
        }

        @Override
        public void tick(
                DriverInput input
        ) {
            tick++;

            if (followTarget == null) {
                return;
            }

            controller.updateRotation(followTarget);

            double distance =
                    mech.distanceTo(followTarget);

            if (followTarget.position().y - mech.position().y > 10) {
                input.setJumpPressed(true);
            }

            //
            // 遠いなら追従
            //
            if (distance > 15) {
                if (distance > 100) {
                    input.setEvasionPressed(true);
                } else if (distance > 40) {
                    controller.startBoosting();
                }

                setMove(
                        0F,
                        1F,
                        input
                );
            }
            //
            // 近いなら停止
            //
            else {
                controller.stopBoosting();
                setMove(
                        0F,
                        0F,
                        input
                );
            }
        }
    }
}
