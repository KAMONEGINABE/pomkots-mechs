package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai;

import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.HoverUnitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.SBUnitProtoTypeItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.pilot.PilotLicenseItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.pilot.PilotRoleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class MechAutoController {
    private static final String MISSION_INSTANCE_TAG_PREFIX = "pomkots_mission:";
    private static final int MAX_SEEK_RANGE = 100;
    private static final double REGROUP_ARRIVAL_DISTANCE = 20.0D;
    private static final int REGROUP_FOLLOW_TICKS = 200;

    private static final int VERTICAL_STUCK_TRIGGER = 200;
    private static final double VERTICAL_DIFF = 30.0D;
    private static final double VERTICAL_CHECK_RANGE = 40.0D;
    private static final int WATER_CHECK_DEPTH = 10;
    private static final int HOVER_EVASION_BASE_INTERVAL_TICKS = 60;
    private static final int HOVER_EVASION_INTERVAL_VARIANCE_TICKS = 20;
    private static final double TARGET_RETURN_DISTANCE_MULTIPLIER = 1.5D;
    private static final double TARGET_RETURN_DISTANCE_MARGIN = 20.0D;
    private static final int RETALIATION_MEMORY_TICKS = 100;
    private static final double GUARD_OBJECTIVE_FOLLOW_STOP_DISTANCE = 28.0D;
    private static final double GUARD_OBJECTIVE_FOLLOW_RESUME_DISTANCE = 36.0D;
    private static final double GUARD_OBJECTIVE_CRUISE_DISTANCE = 30.0D;
    private static final double GUARD_OBJECTIVE_FULL_SPEED_DISTANCE = 60.0D;
    private static final double GUARD_OBJECTIVE_MOVING_THRESHOLD = 0.01D;
    private static final double GUARD_OBJECTIVE_CATCHUP_SPEED_PER_BLOCK = 0.025D;
    private static final float GUARD_FOLLOW_THROTTLE_SMOOTHING = 0.15F;
    private static final double GUARD_OBJECTIVE_RETURN_DISTANCE = 100.0D;
    private static final double GUARD_ENGAGEMENT_RANGE = 150.0D;
    private static final double GUARD_FOLLOW_EVASION_MIN_DISTANCE = 100.0D;
    private static final int LOS_CHECK_INTERVAL_TICKS = 4;
    private static final int LOS_RECOVERY_TRIGGER_TICKS = 24;
    private static final int LOS_RECOVERED_STABLE_TICKS = 8;
    private static final int LOS_RECOVERY_REEVALUATE_MIN_TICKS = 20;
    private static final int LOS_RECOVERY_REEVALUATE_VARIANCE_TICKS = 20;
    private static final int LOS_RECOVERY_STUCK_CHECK_TICKS = 12;
    private static final double LOS_RECOVERY_STUCK_DISTANCE_SQR = 0.5D * 0.5D;
    private static final double[] LOS_RECOVERY_SEARCH_RADII = {12.0D, 24.0D, 36.0D};
    private static final int LOS_RECOVERY_MAX_TICKS = 160;
    private static final int LOS_RECOVERY_RETRY_COOLDOWN_TICKS = 40;
    private static final float SUPER_BOOST_ASSAULT_MIN_ENERGY_RATIO = 0.6F;
    private static final int SUPER_BOOST_ASSAULT_INITIAL_DELAY_TICKS = 200;
    private static final int SUPER_BOOST_ASSAULT_COOLDOWN_MIN_TICKS = 160;
    private static final int SUPER_BOOST_ASSAULT_COOLDOWN_VARIANCE_TICKS = 80;
    private static final double SUPER_BOOST_ASSAULT_NEAR_DISTANCE = 50.0D;
    private static final double SUPER_BOOST_ASSAULT_FAR_DISTANCE = 150.0D;
    private static final float SUPER_BOOST_ASSAULT_NEAR_CHANCE = 0.35F;
    private static final float SUPER_BOOST_ASSAULT_NORMAL_CHANCE = 0.70F;
    private static final float SUPER_BOOST_ASSAULT_FAR_CHANCE = 0.95F;
    /** Chance to use the ranged rush when both melee and ranged weapons are equipped. */
    private static final float SUPER_BOOST_RANGED_ASSAULT_CHANCE_WITH_MELEE = 0.20F;

    private int verticalStuckTick;
    private int lostSightTicks;
    private int recoveredSightTicks;
    private boolean recoveryVerticalAscending;
    private static final int WALL_DETOUR_TICKS = 80;
    private static final int WALL_CLIMB_MAX_TICKS = 100;
    private int wallClimbRetryTick;
    private int wallClimbStartTick = -1;
    private boolean recoveryForcedBoost;
    private boolean lineOfSightRecovery;
    private boolean cachedCombatLineOfSight = true;
    private int nextLineOfSightCheckTick;
    private int nextRecoveryEvaluationTick;
    private int nextRecoveryProgressCheckTick;
    private int lineOfSightRecoveryStartTick = -1;
    private int lineOfSightRecoveryCooldownUntilTick;
    private UUID lineOfSightTargetId;
    private Vec3 recoveryWaypoint;
    private Vec3 recoveryDirection;
    private Vec3 recoveryProgressPosition;
    private Vec3 smoothedTargetVelocity = Vec3.ZERO;
    private Vec3 localDetourDirection;
    private int nextLocalDetourTick;
    private int localDetourUntilTick;
    private int detourSide = 1;
    private int nextSuperBoostAssaultTick;

    private final LivingEntity pilot;
    private final Pmvc01Entity mech;

    private LivingEntity target;
    private LivingEntity missionObjective;
    private LivingEntity retaliationTarget;
    private int retaliationUntilTick;
    private boolean followingMissionObjective;
    private float guardianFollowThrottle;

    private final RandomSource random = RandomSource.create();

    private PilotRole role;
    private PilotRank rank;
    private final CombatStyle combatStyle;
    private final float preferredCombatDistance;

    private final WeaponAIProfile rightHandProfile;
    private final WeaponAIProfile leftHandProfile;
    private final WeaponAIProfile rightShoulderProfile;
    private final WeaponAIProfile leftShoulderProfile;

    public enum PilotRole {
        GUARD,
        RAIDER,
        WINGMAN,
        GLADIATOR
    }

    public enum PilotRank {
        NOVICE,
        INTERMEDIATE,
        ADVANCED,
        LEGEND
    }

    public enum SystemMode {
        IDLE,
        COMBAT
    }

    public enum CombatStyle {
        CLOSE_RANGE(30),
        MID_RANGE(70),
        MID_RANGE_MELEE(70),
        LONG_RANGE(120);

        float preferedDistance;

        CombatStyle(float d) {
            preferedDistance = d;
        }

        float getPreferedDistance() {
            return preferedDistance;
        }
    }

    private SystemMode mode = SystemMode.IDLE;
    private final List<MovePattern.MovePatternEntry> movePatternTable;
    private MovePattern curMovePattern = null;
    private Class<? extends MovePattern> lastMovePattern;
    private boolean commandedRegroup;
    private int commandedRegroupTicks = -1;
    private boolean hoverActivationInputPressed;
    private int nextHoverEvasionTick;

    public static class WeaponAIState {
        // 連射中残りTick
        public int burstTick;
        // 最後に発射したTick
        public long lastFireTick;
        // 次回発射可能になるまでの待ちTick
        public int nextFireDelay;
    }

    private final WeaponAIState rightHandState = new WeaponAIState();
    private final WeaponAIState leftHandState = new WeaponAIState();
    private final WeaponAIState rightShoulderState = new WeaponAIState();
    private final WeaponAIState leftShoulderState = new WeaponAIState();

    public MechAutoController(LivingEntity pilot, Pmvc01Entity mech) {
        this.pilot = pilot;
        this.mech = mech;

        this.role = chooseRole(pilot, mech);
        this.rank = chooseRank(pilot, mech);
        this.combatStyle = chooseStyle(pilot, mech);
        this.rightHandProfile = createWeaponProfile(mech.getRightArmWeapon());
        this.leftHandProfile = createWeaponProfile(mech.getLeftArmWeapon());
        this.rightShoulderProfile = createWeaponProfile(mech.getRightShoulderWeapon());
        this.leftShoulderProfile = createWeaponProfile(mech.getLeftShoulderWeapon());
        this.preferredCombatDistance = calculatePreferredCombatDistance();
        this.movePatternTable = MovePatterns.createCombatMovePatternTable(
                combatStyle, role, rank, pilot, mech, this, preferredCombatDistance
        );
    }

    private WeaponAIProfile createWeaponProfile(ItemStack stack) {
        if (!(stack.getItem() instanceof BasePartsItem.Weapon weapon)) {
            return WeaponAIProfile.PROF_DEFAULT;
        }

        var category = weapon.getWeaponCategory();

        return WeaponAIProfile.getProfile(category, combatStyle, rank);
    }

    private float calculatePreferredCombatDistance() {
        float commonMinRange = 0.0F;
        float commonMaxRange = Float.MAX_VALUE;
        boolean hasWeapon = false;

        ItemStack[] weapons = {
                mech.getRightArmWeapon(),
                mech.getLeftArmWeapon(),
                mech.getRightShoulderWeapon(),
                mech.getLeftShoulderWeapon()
        };
        WeaponAIProfile[] profiles = {
                rightHandProfile,
                leftHandProfile,
                rightShoulderProfile,
                leftShoulderProfile
        };

        for (int i = 0; i < weapons.length; i++) {
            if (!(weapons[i].getItem() instanceof BasePartsItem.Weapon)) {
                continue;
            }
            hasWeapon = true;
            commonMinRange = Math.max(commonMinRange, profiles[i].minRange());
            commonMaxRange = Math.min(commonMaxRange, profiles[i].maxRange());
        }

        if (!hasWeapon || commonMinRange > commonMaxRange) {
            return combatStyle.getPreferedDistance();
        }
        return (commonMinRange + commonMaxRange) * 0.5F;
    }

    private DriverInput prevInput = new DriverInput((short)0);

    public void tick() {
        DriverInput currentInput = new DriverInput((short)0, prevInput);
        applyHoveringInputOverWater(currentInput);

        refreshMissionTargetState();
        if (role == PilotRole.GUARD && tickMissionGuardian(currentInput)) {
            applyCombatRun(currentInput);
            applyActiveHoverBoost(currentInput);
            applyFrequentHoverEvasion(currentInput);
            applyLocalMovementSafety(currentInput);
            mech.setDriverInput(currentInput);
            prevInput = currentInput;
            return;
        }

        if (mode != SystemMode.COMBAT && applyActionRangeReturn(currentInput)) {
            clearCurrentMovePattern();
        } else if (commandedRegroup) {
            tickCommandedRegroup(currentInput);
        } else {
            switch (mode) {
                case IDLE ->
                        tickIdle(currentInput);
                case COMBAT ->
                        tickCombat(currentInput);
            }
        }

        applyCombatRun(currentInput);
        applyActiveHoverBoost(currentInput);
        applyFrequentHoverEvasion(currentInput);

        // 終了処理
        applyLocalMovementSafety(currentInput);
        mech.setDriverInput(currentInput);
        prevInput = currentInput;
    }

    private void applyHoveringInputOverWater(DriverInput input) {
        if (hoverActivationInputPressed) {
            hoverActivationInputPressed = false;
            return;
        }

        if (mech.isHoveringEnabled()) {
            return;
        }

        int hoverUnitSlot = getHoverUnitSlot();
        if (hoverUnitSlot == 0 || !isOverWater()) {
            return;
        }

        if (hoverUnitSlot == 1) {
            input.setExtension1Pressed(true);
        } else {
            input.setExtension2Pressed(true);
        }
        hoverActivationInputPressed = true;
    }

    private boolean isOverWater() {
        for (int offsetY = 0; offsetY < WATER_CHECK_DEPTH; offsetY++) {
            BlockPos pos = BlockPos.containing(
                    mech.getX(),
                    mech.getY() - offsetY,
                    mech.getZ()
            );
            var state = mech.level().getBlockState(pos);

            if (state.isAir()) {
                continue;
            }

            if (state.getFluidState().is(FluidTags.WATER)) {
                return true;
            }
            return false;
        }
        return false;
    }

    private int getHoverUnitSlot() {
        if (mech.getExtension1Weapon().getItem() instanceof HoverUnitItem) {
            return 1;
        }
        if (mech.getExtension2Weapon().getItem() instanceof HoverUnitItem) {
            return 2;
        }
        return 0;
    }

    private void applyFrequentHoverEvasion(DriverInput input) {
        if (role == PilotRole.GUARD
                && followingMissionObjective
                && missionObjective != null
                && mech.distanceToSqr(missionObjective)
                < GUARD_FOLLOW_EVASION_MIN_DISTANCE * GUARD_FOLLOW_EVASION_MIN_DISTANCE) {
            return;
        }
        if (nextHoverEvasionTick == 0) {
            scheduleNextHoverEvasion();
            return;
        }
        if (mech.tickCount < nextHoverEvasionTick) {
            return;
        }
        scheduleNextHoverEvasion();

        boolean isMoving = input.isForwardPressed()
                || input.isBackPressed()
                || input.isLeftPressed()
                || input.isRightPressed();
        if (mech.isHoveringEnabled() && isMoving) {
            input.setEvasionPressed(true);
        }
    }

    private void scheduleNextHoverEvasion() {
        int randomOffset = random.nextInt(
                HOVER_EVASION_INTERVAL_VARIANCE_TICKS * 2 + 1)
                - HOVER_EVASION_INTERVAL_VARIANCE_TICKS;
        nextHoverEvasionTick = mech.tickCount
                + HOVER_EVASION_BASE_INTERVAL_TICKS
                + randomOffset;
    }

    private boolean applyActionRangeReturn(DriverInput input) {
        if (!(pilot instanceof MechPilotEntity mechPilot)
                || mechPilot.getCombatActionRange() <= 0.0D
                || mechPilot.isWithinCombatActionRange(mech.position())) {
            return false;
        }

        Vec3 home = mechPilot.getCombatHome().orElse(null);
        if (home == null) {
            return false;
        }
        moveToward(home, input);
        return true;
    }

    private void moveToward(Vec3 destination, DriverInput input) {
        updateRotation(destination);
        setMovementToward(destination, input);
    }

    private void setMovementToward(Vec3 destination, DriverInput input) {
        Vec3 worldDirection = destination.subtract(mech.position());
        if (worldDirection.horizontalDistanceSqr() < 0.0001D) {
            pilot.xxa = 0.0F;
            pilot.zza = 0.0F;
            return;
        }

        Vec3 localDirection = worldDirection
                .normalize()
                .yRot((float) Math.toRadians(pilot.getYRot()));
        float strafe = (float) localDirection.x;
        float forward = (float) localDirection.z;

        pilot.xxa = strafe;
        pilot.zza = forward;
        input.setLeftPressed(strafe > 0.1F);
        input.setRightPressed(strafe < -0.1F);
        input.setForwardPressed(forward > 0.1F);
        input.setBackPressed(forward < -0.1F);
    }

    private void applyActiveHoverBoost(DriverInput input) {
        if (!mech.isHoveringEnabled()) {
            return;
        }

        boolean isMoving = input.isForwardPressed()
                || input.isBackPressed()
                || input.isLeftPressed()
                || input.isRightPressed();
        if (isMoving) {
            startBoosting();
        } else {
            stopBoosting();
        }
    }

    private void tickIdle(
            DriverInput input
    ) {
        if (mech.tickCount % 100 == 0) {
            LivingEntity found = findCombatTarget();

            if (found != null) {
                target = found;
                mode = SystemMode.COMBAT;
                return;
            }
        }

        updateMovePatternModeIdle();

        if (curMovePattern != null) {
            curMovePattern.tick(input);
        }
    }

    private LivingEntity findCombatTarget() {
        LivingEntity found = switch (role) {
            case GUARD ->
                    findTargetGuard();

            case RAIDER ->
                    findTargetRaider();

            case WINGMAN ->
                    findTargetWingman();

            case GLADIATOR ->
                    findTargetGladiator();
        };
        return isWithinActionRange(found) ? found : null;
    }

    private boolean isWithinActionRange(LivingEntity candidate) {
        if (candidate == null || !(pilot instanceof MechPilotEntity mechPilot)) {
            return candidate != null;
        }
        return mechPilot.isWithinCombatActionRange(candidate.position());
    }

    private LivingEntity findTargetGuard() {
        List<GenericPomkotsMonster> candidates =
                mech.level().getEntitiesOfClass(
                        GenericPomkotsMonster.class,
                        mech.getBoundingBox()
                                .inflate(MAX_SEEK_RANGE)
                );

        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;

        for (GenericPomkotsMonster monster : candidates) {
            if (!isWithinActionRange(monster)) {
                continue;
            }

//            if (!isInFov(monster, 120F)) {
//                continue;
//            }

            if (!mech.hasLineOfSight(monster)) {
                continue;
            }

            if (monster instanceof BaseBossEntity boss && !boss.isActivated()) {
                continue;
            }

            double distance = mech.distanceToSqr(monster);

            if (distance < bestDistance) {

                bestDistance = distance;
                best = monster;
            }
        }

        return best;
    }

    private boolean isInFov(
            LivingEntity target,
            float fovDegrees
    ) {

        Vec3 look =
                mech.getLookAngle()
                        .normalize();

        Vec3 toTarget =
                target.position()
                        .subtract(mech.position())
                        .normalize();

        double dot =
                look.dot(toTarget);

        double limit =
                Math.cos(
                        Math.toRadians(
                                fovDegrees * 0.5
                        )
                );

        return dot >= limit;
    }

    private LivingEntity findTargetRaider() {
        LivingEntity priorityTarget = getRaiderPriorityTarget();
        if (priorityTarget != null) {
            return priorityTarget;
        }

        List<Pmvc01Entity> mechs =
                mech.level()
                        .getEntitiesOfClass(
                                Pmvc01Entity.class,
                                mech.getBoundingBox()
                                        .inflate(MAX_SEEK_RANGE)
                        );

        double bestDistance =
                Double.MAX_VALUE;

        Pmvc01Entity best =
                null;

        for (Pmvc01Entity other : mechs) {
            if (other == mech) {
                continue;
            }
            if (!isWithinActionRange(other)) {
                continue;
            }

            LivingEntity pilot = other.getDrivingPassenger();

            if (pilot == null) {
                continue;
            }

            if (pilot.getOffhandItem().getItem() instanceof PilotRoleItem.PlotRoleRaider) {
                continue;
            }

            double distance =
                    mech.distanceToSqr(other);

            if (distance < bestDistance) {

                bestDistance = distance;
                best = other;
            }
        }

        return best;
    }

    private LivingEntity findTargetWingman() {
        LivingEntity best = findTargetGuard();
        double bestDistance = best == null ? Double.MAX_VALUE : mech.distanceToSqr(best);

        List<Pmvc01Entity> hostileMechs = mech.level().getEntitiesOfClass(
                Pmvc01Entity.class,
                mech.getBoundingBox().inflate(MAX_SEEK_RANGE),
                other -> other != mech
                        && other.isAlive()
                        && !other.isBroken()
                        && isWithinActionRange(other)
                        && other.getDrivingPassenger() != null
                        && other.getDrivingPassenger().getOffhandItem().getItem()
                        instanceof PilotRoleItem.PlotRoleRaider
        );
        for (Pmvc01Entity hostileMech : hostileMechs) {
            double distance = mech.distanceToSqr(hostileMech);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = hostileMech;
            }
        }
        return best;
    }

    private LivingEntity findMissionGuardianEnemy() {
        if (missionObjective == null) {
            return null;
        }

        LivingEntity best = null;
        double bestDistanceToObjective = Double.MAX_VALUE;

        List<GenericPomkotsMonster> monsters = mech.level().getEntitiesOfClass(
                GenericPomkotsMonster.class,
                missionObjective.getBoundingBox().inflate(GUARD_ENGAGEMENT_RANGE),
                monster -> monster.isAlive()
                        && isInSameMissionInstance(monster)
                        && isWithinActionRange(monster)
                        && (!(monster instanceof BaseBossEntity boss) || boss.isActivated()));
        for (GenericPomkotsMonster monster : monsters) {
            double distance = missionObjective.distanceToSqr(monster);
            if (distance < bestDistanceToObjective) {
                best = monster;
                bestDistanceToObjective = distance;
            }
        }

        List<Pmvc01Entity> hostileMechs = mech.level().getEntitiesOfClass(
                        Pmvc01Entity.class,
                        missionObjective.getBoundingBox().inflate(GUARD_ENGAGEMENT_RANGE),
                        other -> other != mech
                                && other.isAlive()
                                && !other.isBroken()
                                && isInSameMissionInstance(other)
                                && other.getDrivingPassenger() != null
                                && other.getDrivingPassenger().getOffhandItem().getItem()
                                instanceof PilotRoleItem.PlotRoleRaider);
        for (Pmvc01Entity hostileMech : hostileMechs) {
            double distance = missionObjective.distanceToSqr(hostileMech);
            if (distance < bestDistanceToObjective) {
                best = hostileMech;
                bestDistanceToObjective = distance;
            }
        }
        return best;
    }

    private void applyCombatRun(DriverInput input) {
        if (mode != SystemMode.COMBAT) return;
        boolean moving = input.isForwardPressed() || input.isBackPressed()
                || input.isLeftPressed() || input.isRightPressed();
        if (moving) {
            startBoosting();
        } else {
            stopBoosting();
        }
    }

    private boolean isInSameMissionInstance(Entity candidate) {
        String missionTag = null;
        for (String tag : mech.getTags()) {
            if (tag.startsWith(MISSION_INSTANCE_TAG_PREFIX)) {
                missionTag = tag;
                break;
            }
        }

        // Preserve the existing non-mission Guardian behavior.
        return missionTag == null || candidate.getTags().contains(missionTag);
    }

    private boolean tickMissionGuardian(DriverInput input) {
        if (missionObjective == null) {
            return false;
        }

        double distanceToObjective = mech.distanceTo(missionObjective);
        if (distanceToObjective > GUARD_OBJECTIVE_RETURN_DISTANCE) {
            followingMissionObjective = true;
            target = null;
            mode = SystemMode.IDLE;
            clearCurrentMovePattern();
            mech.getLockTargets().lockTargetSoft(null);
            moveGuardianTowardObjective(input);
            return true;
        }

        if (target != null && (target.distanceTo(missionObjective) > GUARD_ENGAGEMENT_RANGE
                || target instanceof Pmvc01Entity targetMech && targetMech.isBroken())) {
            target = null;
            mode = SystemMode.IDLE;
            clearCurrentMovePattern();
            mech.getLockTargets().lockTargetSoft(null);
        }

        if ((target == null || !target.isAlive()) && mech.tickCount % 20 == 0) {
            LivingEntity enemy = findMissionGuardianEnemy();
            if (enemy != null) {
                followingMissionObjective = false;
                target = enemy;
                mode = SystemMode.COMBAT;
                return false;
            }
        }

        if (target == null) {
            double objectiveSpeed = horizontalSpeed(missionObjective);
            boolean objectiveIsMoving = objectiveSpeed > GUARD_OBJECTIVE_MOVING_THRESHOLD;
            if (objectiveIsMoving) {
                // Keep cruising with a moving escort target instead of repeatedly
                // stopping and catching up at the follow-distance hysteresis.
                followingMissionObjective = true;
            } else {
                if (followingMissionObjective) {
                    if (distanceToObjective <= GUARD_OBJECTIVE_FOLLOW_STOP_DISTANCE) {
                        followingMissionObjective = false;
                    }
                } else if (distanceToObjective >= GUARD_OBJECTIVE_FOLLOW_RESUME_DISTANCE) {
                    followingMissionObjective = true;
                }
            }

            if (followingMissionObjective) {
                moveGuardianTowardObjective(input);
            } else {
                guardianFollowThrottle = 0.0F;
                pilot.xxa = 0.0F;
                pilot.zza = 0.0F;
            }
            return true;
        }
        return false;
    }

    private void moveGuardianTowardObjective(DriverInput input) {
        if (missionObjective == null) {
            return;
        }
        updateRotation(missionObjective.position());
        double distance = mech.distanceTo(missionObjective);
        float requestedThrottle = calculateGuardianFollowThrottle(distance);
        guardianFollowThrottle += (requestedThrottle - guardianFollowThrottle)
                * GUARD_FOLLOW_THROTTLE_SMOOTHING;
        if (guardianFollowThrottle < 0.01F) {
            if (guardianFollowThrottle > -0.01F) {
                guardianFollowThrottle = 0.0F;
            }
        }
        pilot.xxa = 0.0F;
        pilot.zza = guardianFollowThrottle;
        input.setForwardPressed(guardianFollowThrottle > 0.0F);
        input.setBackPressed(guardianFollowThrottle < 0.0F);
        input.setLeftPressed(false);
        input.setRightPressed(false);
    }

    private float calculateGuardianFollowThrottle(double distanceToObjective) {
        if (distanceToObjective >= GUARD_OBJECTIVE_FULL_SPEED_DISTANCE) {
            return 1.0F;
        }

        Vec3 toObjective = missionObjective.position().subtract(mech.position());
        Vec3 horizontalDirection = new Vec3(toObjective.x, 0.0D, toObjective.z);
        if (horizontalDirection.lengthSqr() < 0.0001D) {
            return 0.0F;
        }
        horizontalDirection = horizontalDirection.normalize();

        // Only inherit the objective velocity that changes the separation.
        // Sideways movement must not make the Guardian accelerate into its center.
        Vec3 objectiveMovement = missionObjective.getDeltaMovement();
        Vec3 mechMovement = mech.getDeltaMovement();
        double objectiveRadialSpeed = objectiveMovement.dot(horizontalDirection);
        double mechRadialSpeed = mechMovement.dot(horizontalDirection);
        double distanceError = distanceToObjective - GUARD_OBJECTIVE_CRUISE_DISTANCE;
        double desiredRadialSpeed = objectiveRadialSpeed
                + distanceError * GUARD_OBJECTIVE_CATCHUP_SPEED_PER_BLOCK;

        if (Math.abs(desiredRadialSpeed) <= GUARD_OBJECTIVE_MOVING_THRESHOLD) {
            return 0.0F;
        }

        // The exact maximum speed depends on the equipped parts. Use the
        // observed current speed as feedback so this also works with custom builds.
        double speedReference = Math.max(Math.abs(desiredRadialSpeed), Math.abs(mechRadialSpeed));
        return Mth.clamp((float)(desiredRadialSpeed / speedReference), -1.0F, 1.0F);
    }

    private static double horizontalSpeed(Entity entity) {
        Vec3 movement = entity.getDeltaMovement();
        return Math.sqrt(movement.x * movement.x + movement.z * movement.z);
    }

    private LivingEntity findTargetGladiator() {
        return null;
    }

    private void updateMovePatternModeIdle() {

        if (curMovePattern != null
                && !curMovePattern.shouldExit()) {
            return;
        }

        curMovePattern = chooseIdlePattern();

        curMovePattern.start(null);
    }

    private MovePattern chooseIdlePattern() {
        return switch (role) {
            case GUARD, RAIDER ->
                    new MovePattern.PatrolPattern(
                            pilot,
                            mech,
                            this,
                            40
                    );

            case WINGMAN ->
                    new MovePattern.FollowPlayerPattern(
                            pilot,
                            mech,
                            this,
                            40
                    );

            case GLADIATOR ->
                    new MovePattern.IdlePattern(
                            pilot,
                            mech,
                            this,
                            40
                    );
        };
    }

    public void startArenaBattle(LivingEntity target) {
        if (role != PilotRole.GLADIATOR) {
            return;
        }

        if (target != null) {
            if (this.mode != SystemMode.COMBAT || this.target != target) {
                // Do not carry the arena's pre-battle IdlePattern (100 ticks)
                // into combat: attacks already run while that pattern is idle.
                clearCurrentMovePattern();
                resetLineOfSightRecovery();
                localDetourDirection = null;
                localDetourUntilTick = 0;
                nextLocalDetourTick = mech.tickCount;
            }
            this.target = target;
            this.mode = SystemMode.COMBAT;
        }
    }

    public void commandRegroup() {
        target = null;
        mode = SystemMode.IDLE;
        clearCurrentMovePattern();
        commandedRegroup = true;
        commandedRegroupTicks = -1;
        mech.getLockTargets().clearLockTargets();
        stopBoosting();
    }

    public void commandAttack(LivingEntity commandedTarget) {
        if (commandedTarget == null || !commandedTarget.isAlive()) return;
        commandedRegroup = false;
        commandedRegroupTicks = -1;
        target = commandedTarget;
        mode = SystemMode.COMBAT;
        clearCurrentMovePattern();
    }

    private void tickCommandedRegroup(DriverInput input) {
        Player master = findWingmanMaster();
        if (master == null) {
            clearCurrentMovePattern();
            stopBoosting();
            return;
        }

        if (!(curMovePattern instanceof MovePattern.FollowPlayerPattern)) {
            curMovePattern = new MovePattern.FollowPlayerPattern(pilot, mech, this, 40);
            curMovePattern.start(null);
        }
        curMovePattern.tick(input);

        if (commandedRegroupTicks < 0) {
            if (mech.distanceTo(master) <= REGROUP_ARRIVAL_DISTANCE) {
                commandedRegroupTicks = REGROUP_FOLLOW_TICKS;
            }
            return;
        }

        if (--commandedRegroupTicks <= 0) {
            commandedRegroup = false;
            commandedRegroupTicks = -1;
            clearCurrentMovePattern();
        }
    }

    private Player findWingmanMaster() {
        if (!(pilot instanceof MechPilotEntity mechPilot)) return null;
        var masterId = mechPilot.getWingmanMasterId();
        if (masterId.isEmpty()) return null;
        return mech.level().players().stream()
                .filter(player -> player.getUUID().equals(masterId.get()))
                .findFirst()
                .orElse(null);
    }

    private LivingEntity findNearestArenaTarget() {
        List<Pmvc01Entity> mechs =
                mech.level()
                        .getEntitiesOfClass(
                                Pmvc01Entity.class,
                                mech.getBoundingBox()
                                        .inflate(MAX_SEEK_RANGE)
                        );

        double bestDistance = Double.MAX_VALUE;

        Pmvc01Entity best = null;
        for (Pmvc01Entity other : mechs) {
            if (other == mech) {
                continue;
            }

            LivingEntity pilot = other.getDrivingPassenger();

            if (pilot == null) {
                continue;
            }

            double distance = mech.distanceToSqr(other);

            if (distance < bestDistance) {
                bestDistance = distance;
                best = other;
            }
        }

        return best;
    }

    private void tickCombat(
            DriverInput input
    ) {
        if (target == null || !target.isAlive()) {
            leaveCombat();
            return;
        }

        if (target instanceof Pmvc01Entity enemyMech
                && enemyMech.isBroken()) {
            leaveCombat();
            return;
        }

        if (!isWithinActionRange(target)) {
            target = null;
            leaveCombat();
            return;
        }

        mech.getLockTargets().lockTargetSoft(target);

        Vec3 actionRangeHome = getActionRangeHomeWhenOutside();
        if (actionRangeHome != null) {
            clearCurrentMovePattern();
            updateRotation(target);
            setMovementToward(actionRangeHome, input);
            updateWeapons(input);
            return;
        }

        if (curMovePattern == null || !curMovePattern.controlsAim()) {
            updateRotation(target);
        }

        // The assault owns its pursuit vector from wind-up through impact.
        // Generic recovery used to discard it around the normal combat-range
        // boundary immediately after Super Boost activated.
        boolean superBoostAssaultActive =
                curMovePattern instanceof MovePattern.SuperBoostAssaultPattern;

        if (!superBoostAssaultActive && tickLineOfSightRecovery(input)) {
            if (cachedCombatLineOfSight) {
                updateWeapons(input);
            }
            return;
        }

        if (!superBoostAssaultActive && isBeyondTargetReturnDistance()) {
            clearCurrentMovePattern();
            setMovementToward(target.position(), input);
            if (cachedCombatLineOfSight) updateWeapons(input);
            return;
        }

        updateVerticalStuck();

        updateMovePatternModeCombat();

        curMovePattern.tick(input);

        if (cachedCombatLineOfSight) updateWeapons(input);
    }

    /** Sparse ray tests and local AABB probes, without vanilla mob navigation. */
    private boolean tickLineOfSightRecovery(DriverInput input) {
        if (target == null) {
            resetLineOfSightRecovery();
            return false;
        }

        if (!target.getUUID().equals(lineOfSightTargetId)) {
            resetLineOfSightRecovery();
            lineOfSightTargetId = target.getUUID();
            nextLineOfSightCheckTick = mech.tickCount;
        }

        smoothedTargetVelocity = smoothedTargetVelocity.scale(0.75D)
                .add(target.getDeltaMovement().scale(0.25D));

        if (mech.tickCount >= nextLineOfSightCheckTick) {
            // Visibility is about the target's current body. Prediction is
            // reserved for choosing a useful future firing position.
            cachedCombatLineOfSight = hasCombatLineOfSight(mech.getEyePosition(), target.getEyePosition());
            nextLineOfSightCheckTick = mech.tickCount + LOS_CHECK_INTERVAL_TICKS;
            if (cachedCombatLineOfSight) {
                lostSightTicks = 0;
                if (lineOfSightRecovery) recoveredSightTicks += LOS_CHECK_INTERVAL_TICKS;
            } else {
                recoveredSightTicks = 0;
                lostSightTicks += LOS_CHECK_INTERVAL_TICKS;
            }
        }

        if (!lineOfSightRecovery) {
            if (lostSightTicks < LOS_RECOVERY_TRIGGER_TICKS) return false;
            if (mech.tickCount < lineOfSightRecoveryCooldownUntilTick) return false;
            lineOfSightRecovery = true;
            lineOfSightRecoveryStartTick = mech.tickCount;
            wallClimbRetryTick = mech.tickCount + WALL_DETOUR_TICKS;
            clearCurrentMovePattern();
            recoveryWaypoint = null;
            recoveryProgressPosition = mech.position();
            nextRecoveryEvaluationTick = mech.tickCount;
            nextRecoveryProgressCheckTick = mech.tickCount + LOS_RECOVERY_STUCK_CHECK_TICKS;
        } else if (recoveredSightTicks >= LOS_RECOVERED_STABLE_TICKS) {
            resetLineOfSightRecovery();
            return false;
        } else if (lineOfSightRecoveryStartTick >= 0
                && mech.tickCount - lineOfSightRecoveryStartTick >= LOS_RECOVERY_MAX_TICKS) {
            // Recovery must never monopolize combat indefinitely. Return to the
            // normal movement table for a while so it can try a genuinely
            // different approach. Weapons still require a real line of sight.
            resetLineOfSightRecovery();
            cachedCombatLineOfSight = false;
            lineOfSightTargetId = target.getUUID();
            nextLineOfSightCheckTick = mech.tickCount;
            lineOfSightRecoveryCooldownUntilTick =
                    mech.tickCount + LOS_RECOVERY_RETRY_COOLDOWN_TICKS;
            return false;
        }

        if (mech.tickCount >= nextRecoveryProgressCheckTick) {
            if (recoveryProgressPosition != null
                    && mech.position().distanceToSqr(recoveryProgressPosition) < LOS_RECOVERY_STUCK_DISTANCE_SQR) {
                recoveryWaypoint = null;
                // Do not undo the previous movement and bounce between two
                // points. Turn sideways to search around the obstacle instead.
                if (recoveryDirection != null) {
                    recoveryDirection = new Vec3(
                            -recoveryDirection.z, 0.0D, recoveryDirection.x).normalize();
                }
                input.setEvasionPressed(false);
            }
            recoveryProgressPosition = mech.position();
            nextRecoveryProgressCheckTick = mech.tickCount + LOS_RECOVERY_STUCK_CHECK_TICKS;
        }

        Vec3 predictedTarget = predictedTargetAimPoint();
        double signedHeightDifference = predictedTarget.y - mech.getEyeY();
        boolean overheadClear = hasVerticalClearance(mech.position());
        boolean ceilingBlocked = !overheadClear;

        if (wallClimbStartTick >= 0) {
            if (!overheadClear || mech.tickCount - wallClimbStartTick >= WALL_CLIMB_MAX_TICKS
                    || !updateRecoveryVerticalBoost(true)) {
                wallClimbStartTick = -1;
                recoveryVerticalAscending = false;
                wallClimbRetryTick = mech.tickCount + WALL_DETOUR_TICKS;
                recoveryWaypoint = null;
            } else {
                // Climb in place until the full body can move past the wall.
                // Continue boosting while crossing its lip, even with a level target.
                Vec3 across = predictedTarget.subtract(mech.position()).multiply(1, 0, 1).normalize();
                clearHorizontalMovement(input);
                if (across.lengthSqr() > 0.001D
                        && isShortRoutePassable(mech.position().add(across.scale(4)))) {
                    setMovementToward(mech.position().add(across.scale(4)), input);
                }
                input.setJumpPressed(true);
                input.setEvasionPressed(false);
                return true;
            }
        }
        if (mech.tickCount >= wallClimbRetryTick && overheadClear
                && !mech.getBooster().isEmpty() && !cachedCombatLineOfSight
                && updateRecoveryVerticalBoost(true)) {
            wallClimbStartTick = mech.tickCount;
            clearHorizontalMovement(input);
            input.setJumpPressed(true);
            input.setEvasionPressed(false);
            return true;
        }

        if (mech.tickCount >= nextRecoveryEvaluationTick) {
            recoveryWaypoint = chooseLineOfSightRecoveryWaypoint(predictedTarget, ceilingBlocked);
            nextRecoveryEvaluationTick = mech.tickCount
                    + LOS_RECOVERY_REEVALUATE_MIN_TICKS
                    + random.nextInt(LOS_RECOVERY_REEVALUATE_VARIANCE_TICKS + 1);
        }

        if (recoveryWaypoint != null) {
            Vec3 delta = recoveryWaypoint.subtract(mech.position());
            if (delta.horizontalDistanceSqr() > 2.0D * 2.0D) {
                setMovementToward(recoveryWaypoint, input);
                recoveryDirection = new Vec3(delta.x, 0.0D, delta.z).normalize();
            } else {
                // The waypoint was reached successfully. Continue stepping in
                // the same direction on the next tick, which lets the mech
                // walk out from beneath roofs wider than one search radius.
                recoveryWaypoint = null;
                nextRecoveryEvaluationTick = mech.tickCount;
                recoveryProgressPosition = mech.position();
                nextRecoveryProgressCheckTick = mech.tickCount + LOS_RECOVERY_STUCK_CHECK_TICKS;
            }
        } else {
            clearHorizontalMovement(input);
        }

        boolean movingHorizontally = input.isForwardPressed() || input.isBackPressed()
                || input.isLeftPressed() || input.isRightPressed();
        if (movingHorizontally) {
            startBoosting();
            recoveryForcedBoost = true;
        }

        // A ceiling switches recovery to horizontal edge-seeking. A lower
        // target never causes a jump; lateral movement opens a downward angle.
        boolean canClimb = signedHeightDifference > 8.0D
                && !ceilingBlocked && hasVerticalClearance(mech.position());
        input.setJumpPressed(updateRecoveryVerticalBoost(canClimb));
        return true;
    }

    private boolean updateRecoveryVerticalBoost(boolean canClimb) {
        if (!canClimb) {
            recoveryVerticalAscending = false;
            return false;
        }

        int energyPerTick = Math.max(0, mech.getEnergyConsumeVertical());
        int reserve = Math.max(energyPerTick * 6, Math.round(mech.getMaxEnergy() * 0.15F));
        if (recoveryVerticalAscending) {
            if (mech.getEnergy() <= reserve) {
                recoveryVerticalAscending = false;
                return false;
            }
            return true;
        }

        int requiredToStart = Math.min(
                mech.getMaxEnergy(),
                Math.max(reserve + energyPerTick * 12, Math.round(mech.getMaxEnergy() * 0.85F)));
        if (mech.getEnergy() < requiredToStart) return false;
        recoveryVerticalAscending = true;
        return true;
    }

    private Vec3 predictedTargetAimPoint() {
        if (target == null) return mech.position();
        double predictionTicks = Mth.clamp(mech.distanceTo(target) / 4.0D, 5.0D, 20.0D);
        Vec3 prediction = smoothedTargetVelocity.scale(predictionTicks);
        if (prediction.lengthSqr() > 30.0D * 30.0D) prediction = prediction.normalize().scale(30.0D);
        return target.getEyePosition().add(prediction);
    }

    private boolean hasCombatLineOfSight(Vec3 start, Vec3 end) {
        return mech.level().clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mech)).getType()
                == HitResult.Type.MISS;
    }

    private Vec3 chooseLineOfSightRecoveryWaypoint(Vec3 predictedTarget, boolean escapingCeiling) {
        Vec3 toTarget = new Vec3(predictedTarget.x - mech.getX(), 0.0D, predictedTarget.z - mech.getZ());
        if (toTarget.lengthSqr() < 1.0E-4D) {
            toTarget = recoveryDirection != null
                    ? recoveryDirection : Vec3.directionFromRotation(0.0F, mech.getYRot());
        }
        toTarget = toTarget.normalize();
        Vec3 side = new Vec3(-toTarget.z, 0.0D, toTarget.x);
        Vec3[] directions = {
                toTarget, side, side.scale(-1.0D),
                toTarget.add(side).normalize(), toTarget.subtract(side).normalize(),
                toTarget.scale(-1.0D).add(side).normalize(),
                toTarget.scale(-1.0D).subtract(side).normalize(), toTarget.scale(-1.0D)
        };

        Vec3 best = null;
        double bestScore = -Double.MAX_VALUE;
        for (double radius : LOS_RECOVERY_SEARCH_RADII) {
            for (Vec3 direction : directions) {
                Vec3 candidate = mech.position().add(direction.scale(radius));
                if (!isShortRoutePassable(candidate)) continue;

                boolean overheadClear = hasVerticalClearance(candidate);
                boolean candidateLineOfSight = hasCombatLineOfSight(
                        candidate.add(0.0D, mech.getEyeHeight(), 0.0D), predictedTarget);
                double score = candidateLineOfSight ? 1000.0D : 0.0D;
                if (escapingCeiling && overheadClear) score += 400.0D;
                score -= Math.abs(candidate.distanceTo(predictedTarget) - preferredCombatDistance) * 0.4D;
                score -= radius * 0.15D;
                if (recoveryDirection != null) score += direction.dot(recoveryDirection) * 140.0D;

                if (score > bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }
            if (best != null && bestScore >= (escapingCeiling ? 1300.0D : 900.0D)) break;
        }
        return best;
    }

    private boolean isShortRoutePassable(Vec3 destination) {
        Vec3 delta = destination.subtract(mech.position());
        AABB box = mech.getBoundingBox().deflate(0.01D);
        int steps = Math.max(1, (int) Math.ceil(delta.horizontalDistance() / 0.75D));
        double stepHeight = mech.onGround() ? mech.maxUpStep() : 0.0D;
        double raised = 0.0D;
        for (int i = 1; i <= steps; i++) {
            double fraction = i / (double) steps;
            AABB candidate = box.move(delta.x * fraction, raised, delta.z * fraction);
            if (!mech.level().noCollision(mech, candidate)) {
                // Match the entity's physical step allowance instead of treating
                // the lip around an elevator as an impassable wall.
                if (raised > 0.0D || stepHeight <= 0.0D
                        || !mech.level().noCollision(mech, box.expandTowards(0, stepHeight, 0))
                        || !mech.level().noCollision(mech,
                        box.move(delta.x * fraction, stepHeight, delta.z * fraction))) return false;
                raised = stepHeight;
            }
        }
        return true;
    }

    private boolean hasVerticalClearance(Vec3 position) {
        Vec3 horizontalOffset = new Vec3(position.x - mech.getX(), 0.0D, position.z - mech.getZ());
        AABB box = mech.getBoundingBox().move(horizontalOffset);
        double probeHeight = Math.max(8.0D, mech.getBbHeight() * 1.5D);
        return mech.level().noCollision(mech,
                box.deflate(0.01D).expandTowards(0.0D, probeHeight, 0.0D));
    }

    private void clearHorizontalMovement(DriverInput input) {
        pilot.xxa = 0;
        pilot.zza = 0;
        input.setForwardPressed(false);
        input.setBackPressed(false);
        input.setLeftPressed(false);
        input.setRightPressed(false);
    }

    /** Final safety pass shared by combat, regrouping, escort and returning home. */
    private void applyLocalMovementSafety(DriverInput input) {
        if (input.isJumpPressed() && !hasVerticalClearance(mech.position())) {
            input.setJumpPressed(false);
            recoveryVerticalAscending = false;
        }
        Vec3 requested = new Vec3(pilot.xxa, 0, pilot.zza)
                .yRot((float) Math.toRadians(-pilot.getYRot()));
        if (requested.horizontalDistanceSqr() < 0.0001D) return;
        requested = requested.normalize();
        double lookAhead = Mth.clamp(mech.getDeltaMovement().horizontalDistance() * 4 + 2, 2, 8);
        boolean blocked = !isShortRoutePassable(mech.position().add(requested.scale(lookAhead)));
        boolean detourPassable = localDetourDirection != null
                && isShortRoutePassable(mech.position().add(localDetourDirection.scale(lookAhead)));
        if (!blocked && (!detourPassable || mech.tickCount >= localDetourUntilTick)) {
            localDetourDirection = null;
            localDetourUntilTick = 0;
            nextLocalDetourTick = mech.tickCount;
            return;
        }
        // Hold the chosen side in world space even when the combat target moves.
        if (mech.tickCount >= nextLocalDetourTick) {
            nextLocalDetourTick = mech.tickCount + 20 + random.nextInt(21);
            Vec3 basis = localDetourDirection == null ? requested : localDetourDirection;
            Vec3 selected = null;
            for (int angle : new int[]{0, 45, 90, 135, 180, -45, -90, -135}) {
                Vec3 direction = basis.yRot((float) Math.toRadians(angle * detourSide));
                if (isShortRoutePassable(mech.position().add(direction.scale(lookAhead)))) {
                    selected = direction;
                    break;
                }
            }
            localDetourDirection = selected;
            // Re-evaluation must not continually postpone returning to the
            // requested direction. Otherwise a backward detour can last forever.
            if (mech.tickCount >= localDetourUntilTick) {
                localDetourUntilTick = mech.tickCount + 40;
            }
            if (selected == null) detourSide = -detourSide;
        }
        clearHorizontalMovement(input);
        input.setEvasionPressed(false);
        if (localDetourDirection != null
                && isShortRoutePassable(mech.position().add(localDetourDirection.scale(lookAhead)))) {
            Vec3 local = localDetourDirection.yRot((float) Math.toRadians(pilot.getYRot()));
            pilot.xxa = (float) local.x;
            pilot.zza = (float) local.z;
            input.setLeftPressed(local.x > 0.1D);
            input.setRightPressed(local.x < -0.1D);
            input.setForwardPressed(local.z > 0.1D);
            input.setBackPressed(local.z < -0.1D);
        } else {
            stopBoosting();
        }
    }

    private void resetLineOfSightRecovery() {
        wallClimbStartTick = -1;
        wallClimbRetryTick = 0;
        lineOfSightRecoveryStartTick = -1;
        lineOfSightRecoveryCooldownUntilTick = 0;
        lostSightTicks = 0;
        recoveredSightTicks = 0;
        recoveryVerticalAscending = false;
        if (recoveryForcedBoost) {
            stopBoosting();
            recoveryForcedBoost = false;
        }
        lineOfSightRecovery = false;
        cachedCombatLineOfSight = true;
        lineOfSightTargetId = null;
        recoveryWaypoint = null;
        recoveryDirection = null;
        recoveryProgressPosition = null;
        smoothedTargetVelocity = Vec3.ZERO;
    }

    private Vec3 getActionRangeHomeWhenOutside() {
        if (!(pilot instanceof MechPilotEntity mechPilot)
                || mechPilot.getCombatActionRange() <= 0.0D
                || mechPilot.isWithinCombatActionRange(mech.position())) {
            return null;
        }
        return mechPilot.getCombatHome().orElse(null);
    }

    private void leaveCombat() {
        target = findCombatTarget();

        if (target == null) {
            clearCurrentMovePattern();
            mech.getLockTargets()
                    .lockTargetSoft(null);
            mode = SystemMode.IDLE;
        }

    }


    private void updateVerticalStuck() {
        if (target == null) {
            verticalStuckTick = 0;
            return;
        }

        double yDiff =
                Math.abs(
                        target.getY()
                                - mech.getY()
                );

        double horizontalDistance =
                Math.sqrt(
                        Mth.square(
                                target.getX() - mech.getX()
                        )
                                +
                                Mth.square(
                                        target.getZ() - mech.getZ()
                                )
                );

        if (
                yDiff >= VERTICAL_DIFF
                        &&
                        horizontalDistance <= VERTICAL_CHECK_RANGE
        ) {
            verticalStuckTick++;
        } else {
            verticalStuckTick = 0;
        }
    }

    private void updateWeapons(DriverInput input) {
        if (target == null) {
            return;
        }

        double distance = mech.distanceTo(target);

        evaluateWeapon(
                mech.getRightArmWeapon(),
                mech.getLockTargets().getLockTargetMulti(Pmvc01Entity.INV_WEAPON_RIGHT_HAND),
                rightHandProfile,
                rightHandState,
                distance,
                input::setWeaponRightHandPressed
        );

        evaluateWeapon(
                mech.getLeftArmWeapon(),
                mech.getLockTargets().getLockTargetMulti(Pmvc01Entity.INV_WEAPON_LEFT_HAND),
                leftHandProfile,
                leftHandState,
                distance,
                input::setWeaponLeftHandPressed
        );

        evaluateWeapon(
                mech.getRightShoulderWeapon(),
                mech.getLockTargets().getLockTargetMulti(Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER),
                rightShoulderProfile,
                rightShoulderState,
                distance,
                input::setWeaponRightShoulderPressed
        );

        evaluateWeapon(
                mech.getLeftShoulderWeapon(),
                mech.getLockTargets().getLockTargetMulti(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER),
                leftShoulderProfile,
                leftShoulderState,
                distance,
                input::setWeaponLeftShoulderPressed
        );
    }

    private void evaluateWeapon(
            ItemStack itemStack,
            List<Entity> lockTargets,
            WeaponAIProfile profile,
            WeaponAIState state,
            double distance,
            Consumer<Boolean> trigger
    ) {
        if (!(itemStack.getItem() instanceof BasePartsItem.Weapon weapon)) {
            return;
        }

        if (distance < profile.minRange()) {
            return;
        }

        if (distance > profile.maxRange() * 2.0F) {
            return;
        }

        long gameTime = mech.level().getGameTime();

        // 連射継続中
        if (state.burstTick > 0) {
            state.burstTick--;
            trigger.accept(true);

            // Cooldown begins when the burst ends, not when it starts. The
            // old start-based timestamp elapsed during a 3-5 second machine
            // gun burst and caused the next burst to begin immediately.
            if (state.burstTick == 0) {
                state.lastFireTick = gameTime;
            }

            return;
        }

        // クールダウン中
        if (gameTime - state.lastFireTick
                < state.nextFireDelay) {
            return;
        }

        // 発射
        trigger.accept(true);

        var category = weapon.getWeaponCategory();
        if (category == BasePartsItem.WeaponCategory.MISSILE && target != null) {
            lockTargets.clear();
            for (int i = 0; i < weapon.maxMultiLockNum(); i++) {
                lockTargets.add(target);
            }
        }

        state.lastFireTick = gameTime;

        state.nextFireDelay =
                (int)(profile.minFireDelay()
                        + random.nextInt(
                        profile.maxFireDelay()
                                - profile.minFireDelay()
                                + 1
                ));

        // マシンガン等
        if (profile.burstFire()) {
            state.burstTick =
                    profile.burstDuration()
                            + random.nextInt(
                            profile.burstDuration() / 2 + 1
                    );
        }
    }

    private void updateMovePatternModeCombat() {
        if (curMovePattern == null || curMovePattern.shouldExit()) {
            if (curMovePattern != null) curMovePattern.stop();
            pilot.xxa = 0;
            pilot.zza = 0;

            curMovePattern = chooseMovePattern();
            curMovePattern.start(target);
        }
    }

    private boolean isBeyondTargetReturnDistance() {
        if (target == null) {
            return false;
        }
        double preferredDistance = preferredCombatDistance;
        double returnDistance = Math.max(
                preferredDistance * TARGET_RETURN_DISTANCE_MULTIPLIER,
                preferredDistance + TARGET_RETURN_DISTANCE_MARGIN);
        return mech.distanceToSqr(target) > returnDistance * returnDistance;
    }

    private MovePattern chooseMovePattern() {
        MovePattern superBoostAssault = tryChooseSuperBoostAssault();
        if (superBoostAssault != null) {
            return superBoostAssault;
        }

        if (verticalStuckTick >= VERTICAL_STUCK_TRIGGER) {

            verticalStuckTick = 0;

            return new MovePattern.VerticalApproachPattern(
                    pilot,
                    mech,
                    this,
                    preferredCombatDistance
            );
        }

        int totalWeight = 0;

        for (MovePattern.MovePatternEntry entry : movePatternTable) {
            totalWeight += getWeight(entry);
        }

        int roll = random.nextInt(totalWeight);

        for (MovePattern.MovePatternEntry entry : movePatternTable) {
            roll -= getWeight(entry);

            if (roll < 0) {
                MovePattern pattern = entry.factory().get();

                lastMovePattern = entry.patternClass();

                return pattern;
            }
        }

        MovePattern.MovePatternEntry fallback =
                movePatternTable.get(0);

        lastMovePattern =
                fallback.patternClass();

        return fallback.factory().get();
    }

    private MovePattern tryChooseSuperBoostAssault() {
        if (target == null) {
            return null;
        }

        if (!hasSuperBoostUnit()) {
            return null;
        }
        if (mech.tickCount < SUPER_BOOST_ASSAULT_INITIAL_DELAY_TICKS) {
            return null;
        }
        if (mech.tickCount < nextSuperBoostAssaultTick) {
            return null;
        }
        if (mech.getMaxEnergy() <= 0) {
            return null;
        }
        if (mech.getEnergy() < mech.getMaxEnergy() * SUPER_BOOST_ASSAULT_MIN_ENERGY_RATIO) {
            return null;
        }

        double targetDistance = mech.distanceTo(target);
        float selectionChance = targetDistance > SUPER_BOOST_ASSAULT_FAR_DISTANCE
                ? SUPER_BOOST_ASSAULT_FAR_CHANCE
                : targetDistance < SUPER_BOOST_ASSAULT_NEAR_DISTANCE
                ? SUPER_BOOST_ASSAULT_NEAR_CHANCE
                : SUPER_BOOST_ASSAULT_NORMAL_CHANCE;
        float selectionRoll = random.nextFloat();
        if (selectionRoll >= selectionChance) {
            return null;
        }

        boolean hasMelee = hasWeaponCategory(BasePartsItem.WeaponCategory.MELEE);
        boolean hasRanged = hasRangedWeapon();
        if (!hasMelee && !hasRanged) return null;

        // Mixed builds normally perform the melee assault, but sometimes use
        // the high-speed ranged full burst instead. Melee-only builds never
        // select it because they would have no weapon to fire.
        boolean meleeAssault = hasMelee;
        if (hasMelee && hasRanged
                && random.nextFloat() < SUPER_BOOST_RANGED_ASSAULT_CHANCE_WITH_MELEE) {
            meleeAssault = false;
        }

        nextSuperBoostAssaultTick = mech.tickCount
                + SUPER_BOOST_ASSAULT_COOLDOWN_MIN_TICKS
                + random.nextInt(SUPER_BOOST_ASSAULT_COOLDOWN_VARIANCE_TICKS + 1);
        return new MovePattern.SuperBoostAssaultPattern(
                pilot, mech, this, preferredCombatDistance, meleeAssault);
    }

    private boolean hasSuperBoostUnit() {
        return mech.getExtension1Weapon().getItem() instanceof SBUnitProtoTypeItem
                || mech.getExtension2Weapon().getItem() instanceof SBUnitProtoTypeItem;
    }

    private boolean hasRangedWeapon() {
        return hasAnyWeaponExcept(BasePartsItem.WeaponCategory.MELEE);
    }

    private boolean hasWeaponCategory(BasePartsItem.WeaponCategory category) {
        for (ItemStack stack : equippedWeapons()) {
            if (stack.getItem() instanceof BasePartsItem.Weapon weapon
                    && weapon.getWeaponCategory() == category) return true;
        }
        return false;
    }

    private boolean hasAnyWeaponExcept(BasePartsItem.WeaponCategory category) {
        for (ItemStack stack : equippedWeapons()) {
            if (stack.getItem() instanceof BasePartsItem.Weapon weapon
                    && weapon.getWeaponCategory() != category) return true;
        }
        return false;
    }

    private ItemStack[] equippedWeapons() {
        return new ItemStack[]{
                mech.getRightArmWeapon(), mech.getLeftArmWeapon(),
                mech.getRightShoulderWeapon(), mech.getLeftShoulderWeapon()
        };
    }

    private int getWeight(
            MovePattern.MovePatternEntry entry
    ) {
        int weight = entry.baseWeight();

        if (entry.patternClass() == lastMovePattern) {
            weight /= 4;
        }

        return Math.max(weight, 1);
    }

    void updateRotation(LivingEntity target) {
        updateRotation(target.position());
    }

    private void updateRotation(Vec3 target) {
        double dx = target.x - mech.getX();
        double dz = target.z - mech.getZ();

        float yaw = (float)(
                Math.atan2(-dx, dz)
                        * 180.0D
                        / Math.PI
        );

        pilot.setYRot(yaw);
        pilot.setYHeadRot(yaw);
    }

    private LivingEntity findTarget() {
        if (target != null
                && target.isAlive()
                && isWithinActionRange(target)
                && mech.distanceToSqr(target) < MAX_SEEK_RANGE * MAX_SEEK_RANGE) {
            return target;
        }

        List<Pmvc01Entity> list =
                mech.level().getEntitiesOfClass(
                        Pmvc01Entity.class,
                        mech.getBoundingBox().inflate(MAX_SEEK_RANGE),
                        e -> e != pilot
                                && e != mech
                                && e.isAlive()
                                && isWithinActionRange(e)
                                && e.getDrivingPassenger() != null
                );

        double nearest = Double.MAX_VALUE;
        LivingEntity result = null;

        for (LivingEntity e : list) {

            double dist = mech.distanceToSqr(e);

            if (dist < nearest) {
                nearest = dist;
                result = e;
            }
        }

        return result;
    }

    public void setMissionObjective(LivingEntity objective) {
        this.missionObjective = objective;
        if (role == PilotRole.RAIDER && objective != null && objective.isAlive()) {
            this.target = objective;
            this.mode = SystemMode.COMBAT;
        }
    }

    private void refreshMissionTargetState() {
        if (missionObjective != null && (!missionObjective.isAlive() || missionObjective.isRemoved())) {
            missionObjective = null;
        }
        if (retaliationTarget != null
                && (mech.tickCount >= retaliationUntilTick
                || !retaliationTarget.isAlive()
                || retaliationTarget.isRemoved())) {
            LivingEntity expiredTarget = retaliationTarget;
            retaliationTarget = null;
            if (target == expiredTarget) {
                target = missionObjective != null && missionObjective.isAlive() ? missionObjective : null;
                mode = target == null ? SystemMode.IDLE : SystemMode.COMBAT;
                clearCurrentMovePattern();
            }
        }
    }

    private LivingEntity getRaiderPriorityTarget() {
        if (retaliationTarget != null && retaliationTarget.isAlive()
                && mech.tickCount < retaliationUntilTick) {
            return retaliationTarget;
        }
        return missionObjective != null && missionObjective.isAlive() ? missionObjective : null;
    }

    public void handleHurt(DamageSource source, float amount) {
        if (role != PilotRole.RAIDER) {
            return;
        }
        Entity causingEntity = source.getEntity();
        if (!(causingEntity instanceof LivingEntity attacker)
                || attacker == pilot
                || attacker == mech
                || attacker instanceof Enemy) {
            return;
        }
        retaliationTarget = attacker;
        retaliationUntilTick = mech.tickCount + RETALIATION_MEMORY_TICKS;
        target = attacker;
        mode = SystemMode.COMBAT;
        clearCurrentMovePattern();
    }

    private void clearCurrentMovePattern() {
        if (curMovePattern != null) {
            curMovePattern.stop();
            curMovePattern = null;
        }
    }

    void stopBoosting() {
        mech.setBoost(false);
        mech.actionController.setBoost(false);
    }

    void startBoosting() {
        mech.setBoost(true);
        mech.actionController.setBoost(true);
    }

    private CombatStyle chooseStyle(LivingEntity pilot, Pmvc01Entity mech) {
        int meleeWeapons = 0;
        int midWeapons = 0;
        int longWeapons = 0;

        for (int i = 0; i < 4; i++) {
            var w = mech.getItem(i + Pmvc01Entity.INV_WEAPON_RIGHT_HAND).getItem();
            if (!(w instanceof BasePartsItem.Weapon weapon)) {
                continue;
            }

            switch (weapon.getWeaponCategory()) {
                case MELEE -> meleeWeapons++;
                case MACHINE_GUN, SHOT_GUN -> midWeapons++;
                case RIFLE, GRENADE, MISSILE -> longWeapons++;
            }
        }

        // 近接2本持ち以上は間違いなく近接…っ！！
        if (meleeWeapons > 1) {
            return CombatStyle.CLOSE_RANGE;
        }

        // 中間レンジがあればそっちに寄せる
        if (midWeapons > 0) {
            if (meleeWeapons > 0) {
                return CombatStyle.MID_RANGE_MELEE;
            } else {
                return CombatStyle.MID_RANGE;
            }
        }

        if (longWeapons > 1) {
            return CombatStyle.LONG_RANGE;
        }

        // コンセプトがよくわかんなかったら取り合えずバランス型
        return CombatStyle.MID_RANGE_MELEE;
    }

    private PilotRank chooseRank(LivingEntity pilot, Pmvc01Entity mech) {
        Item item = pilot.getMainHandItem().getItem();

        if (item instanceof PilotLicenseItem.PilotLicenseNovice) {
            return PilotRank.NOVICE;
        } else if (item instanceof PilotLicenseItem.PilotLicenseIntermediate) {
            return PilotRank.INTERMEDIATE;
        } else if (item instanceof PilotLicenseItem.PilotLicenseAdvanced) {
            return PilotRank.ADVANCED;
        } else if (item instanceof PilotLicenseItem.PilotLicenseLegend) {
            return PilotRank.LEGEND;
        } else {
            return PilotRank.NOVICE;
        }
    }

    private PilotRole chooseRole(LivingEntity pilot, Pmvc01Entity mech) {
        Item item = pilot.getOffhandItem().getItem();

        if (item instanceof PilotRoleItem.PlotRoleGuardian) {
            return PilotRole.GUARD;
        } else if (item instanceof PilotRoleItem.PlotRoleWingman) {
            return PilotRole.WINGMAN;
        } else if (item instanceof PilotRoleItem.PlotRoleGladiator) {
            return PilotRole.GLADIATOR;
        } else if (item instanceof PilotRoleItem.PlotRoleRaider) {
            return PilotRole.RAIDER;
        } else {
            return PilotRole.GUARD;
        }
    }
}
