package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai;

import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.pilot.PilotLicenseItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.pilot.PilotRoleItem;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class MechAutoController {
   private static final int MAX_SEEK_RANGE = 100;
   private static final int VERTICAL_STUCK_TRIGGER = 200;
   private static final double VERTICAL_DIFF = 30.0;
   private static final double VERTICAL_CHECK_RANGE = 40.0;
   private int verticalStuckTick;
   private final LivingEntity pilot;
   private final Pmvc01Entity mech;
   private LivingEntity target;
   private final RandomSource random = RandomSource.create();
   private MechAutoController.PilotRole role;
   private MechAutoController.PilotRank rank;
   private final MechAutoController.CombatStyle combatStyle;
   private final WeaponAIProfile rightHandProfile;
   private final WeaponAIProfile leftHandProfile;
   private final WeaponAIProfile rightShoulderProfile;
   private final WeaponAIProfile leftShoulderProfile;
   private MechAutoController.SystemMode mode = MechAutoController.SystemMode.IDLE;
   private final List<MovePattern.MovePatternEntry> movePatternTable;
   private MovePattern curMovePattern = null;
   private Class<? extends MovePattern> lastMovePattern;
   private final MechAutoController.WeaponAIState rightHandState = new MechAutoController.WeaponAIState();
   private final MechAutoController.WeaponAIState leftHandState = new MechAutoController.WeaponAIState();
   private final MechAutoController.WeaponAIState rightShoulderState = new MechAutoController.WeaponAIState();
   private final MechAutoController.WeaponAIState leftShoulderState = new MechAutoController.WeaponAIState();
   private DriverInput prevInput = new DriverInput((short)0);

   public MechAutoController(LivingEntity pilot, Pmvc01Entity mech) {
      this.pilot = pilot;
      this.mech = mech;
      this.role = this.chooseRole(pilot, mech);
      this.rank = this.chooseRank(pilot, mech);
      this.combatStyle = this.chooseStyle(pilot, mech);
      this.movePatternTable = MovePatterns.createCombatMovePatternTable(this.combatStyle, this.role, this.rank, pilot, mech, this);
      this.rightHandProfile = this.createWeaponProfile(mech.getRightArmWeapon());
      this.leftHandProfile = this.createWeaponProfile(mech.getLeftArmWeapon());
      this.rightShoulderProfile = this.createWeaponProfile(mech.getRightShoulderWeapon());
      this.leftShoulderProfile = this.createWeaponProfile(mech.getLeftShoulderWeapon());
   }

   private WeaponAIProfile createWeaponProfile(ItemStack stack) {
      if (stack.getItem() instanceof BasePartsItem.Weapon weapon) {
         BasePartsItem.WeaponCategory category = weapon.getWeaponCategory();
         return WeaponAIProfile.getProfile(category, this.combatStyle, this.rank);
      } else {
         return WeaponAIProfile.PROF_DEFAULT;
      }
   }

   public void tick() {
      DriverInput currentInput = new DriverInput((short)0, this.prevInput);
      switch (this.mode) {
         case IDLE:
            this.tickIdle(currentInput);
            break;
         case COMBAT:
            this.tickCombat(currentInput);
      }

      this.mech.setDriverInput(currentInput);
      this.prevInput = currentInput;
   }

   private void tickIdle(DriverInput input) {
      if (this.mech.tickCount % 100 == 0) {
         LivingEntity found = this.findCombatTarget();
         if (found != null) {
            this.target = found;
            this.mode = MechAutoController.SystemMode.COMBAT;
            return;
         }
      }

      this.updateMovePatternModeIdle();
      if (this.curMovePattern != null) {
         this.curMovePattern.tick(input);
      }
   }

   private LivingEntity findCombatTarget() {
      return switch (this.role) {
         case GUARD -> this.findTargetGuard();
         case RAIDER -> this.findTargetRaider();
         case WINGMAN -> this.findTargetWingman();
         case GLADIATOR -> this.findTargetGladiator();
      };
   }

   private LivingEntity findTargetGuard() {
      List<GenericPomkotsMonster> candidates = this.mech.level().getEntitiesOfClass(GenericPomkotsMonster.class, this.mech.getBoundingBox().inflate(100.0));
      LivingEntity best = null;
      double bestDistance = Double.MAX_VALUE;

      for (GenericPomkotsMonster monster : candidates) {
         if (this.mech.hasLineOfSight(monster) && !(monster instanceof BaseBossEntity boss && !boss.isActivated())) {
            double distance = this.mech.distanceToSqr(monster);
            if (distance < bestDistance) {
               bestDistance = distance;
               best = monster;
            }
         }
      }

      return best;
   }

   private boolean isInFov(LivingEntity target, float fovDegrees) {
      Vec3 look = this.mech.getLookAngle().normalize();
      Vec3 toTarget = target.position().subtract(this.mech.position()).normalize();
      double dot = look.dot(toTarget);
      double limit = Math.cos(Math.toRadians(fovDegrees * 0.5));
      return dot >= limit;
   }

   private LivingEntity findTargetRaider() {
      List<Pmvc01Entity> mechs = this.mech.level().getEntitiesOfClass(Pmvc01Entity.class, this.mech.getBoundingBox().inflate(100.0));
      double bestDistance = Double.MAX_VALUE;
      Pmvc01Entity best = null;

      for (Pmvc01Entity other : mechs) {
         if (other != this.mech) {
            LivingEntity pilot = other.getDrivingPassenger();
            if (pilot != null && !(pilot.getOffhandItem().getItem() instanceof PilotRoleItem.PlotRoleRaider)) {
               double distance = this.mech.distanceToSqr(other);
               if (distance < bestDistance) {
                  bestDistance = distance;
                  best = other;
               }
            }
         }
      }

      return best;
   }

   private LivingEntity findTargetWingman() {
      return this.findTargetGuard();
   }

   private LivingEntity findTargetGladiator() {
      return null;
   }

   private void updateMovePatternModeIdle() {
      if (this.curMovePattern == null || this.curMovePattern.shouldExit()) {
         this.curMovePattern = this.chooseIdlePattern();
         this.curMovePattern.start(null);
      }
   }

   private MovePattern chooseIdlePattern() {
      return switch (this.role) {
         case GUARD, RAIDER -> new MovePattern.PatrolPattern(this.pilot, this.mech, this, 40.0F);
         case WINGMAN -> new MovePattern.FollowPlayerPattern(this.pilot, this.mech, this, 40.0F);
         case GLADIATOR -> new MovePattern.IdlePattern(this.pilot, this.mech, this, 40.0F);
      };
   }

   public void startArenaBattle(LivingEntity target) {
      if (this.role == MechAutoController.PilotRole.GLADIATOR) {
         if (target != null) {
            this.target = target;
            this.mode = MechAutoController.SystemMode.COMBAT;
         }
      }
   }

   private LivingEntity findNearestArenaTarget() {
      List<Pmvc01Entity> mechs = this.mech.level().getEntitiesOfClass(Pmvc01Entity.class, this.mech.getBoundingBox().inflate(100.0));
      double bestDistance = Double.MAX_VALUE;
      Pmvc01Entity best = null;

      for (Pmvc01Entity other : mechs) {
         if (other != this.mech) {
            LivingEntity pilot = other.getDrivingPassenger();
            if (pilot != null) {
               double distance = this.mech.distanceToSqr(other);
               if (distance < bestDistance) {
                  bestDistance = distance;
                  best = other;
               }
            }
         }
      }

      return best;
   }

   private void tickCombat(DriverInput input) {
      if (this.target != null && this.target.isAlive()) {
         if (this.target instanceof Pmvc01Entity enemyMech && enemyMech.isBroken()) {
            this.leaveCombat();
         } else {
            this.mech.getLockTargets().lockTargetSoft(this.target);
            this.updateRotation(this.target);
            this.updateVerticalStuck();
            this.updateMovePatternModeCombat();
            this.curMovePattern.tick(input);
            this.updateWeapons(input);
         }
      } else {
         this.leaveCombat();
      }
   }

   private void leaveCombat() {
      this.target = this.findCombatTarget();
      if (this.target == null) {
         this.curMovePattern = null;
         this.mech.getLockTargets().lockTargetSoft(null);
         this.mode = MechAutoController.SystemMode.IDLE;
      }
   }

   private void updateVerticalStuck() {
      if (this.target == null) {
         this.verticalStuckTick = 0;
      } else {
         double yDiff = Math.abs(this.target.getY() - this.mech.getY());
         double horizontalDistance = Math.sqrt(
            Mth.square(this.target.getX() - this.mech.getX()) + Mth.square(this.target.getZ() - this.mech.getZ())
         );
         if (yDiff >= 30.0 && horizontalDistance <= 40.0) {
            this.verticalStuckTick++;
         } else {
            this.verticalStuckTick = 0;
         }
      }
   }

   private void updateWeapons(DriverInput input) {
      if (this.target != null) {
         double distance = this.mech.distanceTo(this.target);
         this.evaluateWeapon(
            this.mech.getRightArmWeapon(),
            this.mech.getLockTargets().getLockTargetMulti(6),
            this.rightHandProfile,
            this.rightHandState,
            distance,
            input::setWeaponRightHandPressed
         );
         this.evaluateWeapon(
            this.mech.getLeftArmWeapon(),
            this.mech.getLockTargets().getLockTargetMulti(7),
            this.leftHandProfile,
            this.leftHandState,
            distance,
            input::setWeaponLeftHandPressed
         );
         this.evaluateWeapon(
            this.mech.getRightShoulderWeapon(),
            this.mech.getLockTargets().getLockTargetMulti(8),
            this.rightShoulderProfile,
            this.rightShoulderState,
            distance,
            input::setWeaponRightShoulderPressed
         );
         this.evaluateWeapon(
            this.mech.getLeftShoulderWeapon(),
            this.mech.getLockTargets().getLockTargetMulti(9),
            this.leftShoulderProfile,
            this.leftShoulderState,
            distance,
            input::setWeaponLeftShoulderPressed
         );
      }
   }

   private void evaluateWeapon(
      ItemStack itemStack,
      List<Entity> lockTargets,
      WeaponAIProfile profile,
      MechAutoController.WeaponAIState state,
      double distance,
      Consumer<Boolean> trigger
   ) {
      if (itemStack.getItem() instanceof BasePartsItem.Weapon weapon) {
         if (!(distance < profile.minRange())) {
            if (!(distance > profile.maxRange())) {
               long gameTime = this.mech.level().getGameTime();
               if (state.burstTick > 0) {
                  state.burstTick--;
                  trigger.accept(true);
               } else if (gameTime - state.lastFireTick >= state.nextFireDelay) {
                  trigger.accept(true);
                  BasePartsItem.WeaponCategory category = weapon.getWeaponCategory();
                  if (category == BasePartsItem.WeaponCategory.MISSILE && this.target != null) {
                     lockTargets.clear();

                     for (int i = 0; i < weapon.maxMultiLockNum(); i++) {
                        lockTargets.add(this.target);
                     }
                  }

                  state.lastFireTick = gameTime;
                  state.nextFireDelay = profile.minFireDelay() + this.random.nextInt(profile.maxFireDelay() - profile.minFireDelay() + 1);
                  if (profile.burstFire()) {
                     state.burstTick = profile.burstDuration() + this.random.nextInt(profile.burstDuration() / 2 + 1);
                  }
               }
            }
         }
      }
   }

   private void updateMovePatternModeCombat() {
      if (this.curMovePattern == null || this.curMovePattern.shouldExit()) {
         this.pilot.xxa = 0.0F;
         this.pilot.zza = 0.0F;
         this.curMovePattern = this.chooseMovePattern();
         this.curMovePattern.start(this.target);
      }
   }

   private MovePattern chooseMovePattern() {
      if (this.verticalStuckTick >= 200) {
         this.verticalStuckTick = 0;
         return new MovePattern.VerticalApproachPattern(this.pilot, this.mech, this, this.combatStyle.getPreferedDistance());
      }

      int totalWeight = 0;

      for (MovePattern.MovePatternEntry entry : this.movePatternTable) {
         totalWeight += this.getWeight(entry);
      }

      int roll = this.random.nextInt(totalWeight);

      for (MovePattern.MovePatternEntry entry : this.movePatternTable) {
         roll -= this.getWeight(entry);
         if (roll < 0) {
            MovePattern pattern = entry.factory().get();
            this.lastMovePattern = entry.patternClass();
            return pattern;
         }
      }

      MovePattern.MovePatternEntry fallback = this.movePatternTable.get(0);
      this.lastMovePattern = fallback.patternClass();
      return fallback.factory().get();
   }

   private int getWeight(MovePattern.MovePatternEntry entry) {
      int weight = entry.baseWeight();
      if (entry.patternClass() == this.lastMovePattern) {
         weight /= 4;
      }

      return Math.max(weight, 1);
   }

   void updateRotation(LivingEntity target) {
      double dx = target.getX() - this.mech.getX();
      double dz = target.getZ() - this.mech.getZ();
      float yaw = (float)(Math.atan2(-dx, dz) * 180.0 / Math.PI);
      this.pilot.setYRot(yaw);
      this.pilot.setYHeadRot(yaw);
   }

   private LivingEntity findTarget() {
      if (this.target != null && this.target.isAlive() && this.mech.distanceToSqr(this.target) < 10000.0) {
         return this.target;
      }

      List<Pmvc01Entity> list = this.mech
         .level()
         .getEntitiesOfClass(
            Pmvc01Entity.class,
            this.mech.getBoundingBox().inflate(100.0),
            ex -> ex != this.pilot && ex != this.mech && ex.isAlive() && ex.getDrivingPassenger() != null
         );
      double nearest = Double.MAX_VALUE;
      LivingEntity result = null;

      for (LivingEntity e : list) {
         double dist = this.mech.distanceToSqr(e);
         if (dist < nearest) {
            nearest = dist;
            result = e;
         }
      }

      return result;
   }

   public void handleHurt(DamageSource source, float amount) {
   }

   void stopBoosting() {
      this.mech.setBoost(false);
      this.mech.actionController.setBoost(false);
   }

   void startBoosting() {
      this.mech.setBoost(true);
      this.mech.actionController.setBoost(true);
   }

   private MechAutoController.CombatStyle chooseStyle(LivingEntity pilot, Pmvc01Entity mech) {
      int meleeWeapons = 0;
      int midWeapons = 0;
      int longWeapons = 0;

      for (int i = 0; i < 4; i++) {
         if (mech.getItem(i + 6).getItem() instanceof BasePartsItem.Weapon weapon) {
            switch (weapon.getWeaponCategory()) {
               case MELEE:
                  meleeWeapons++;
                  break;
               case MACHINE_GUN:
               case SHOT_GUN:
                  midWeapons++;
                  break;
               case RIFLE:
               case GRENADE:
               case MISSILE:
                  longWeapons++;
            }
         }
      }

      if (meleeWeapons > 1) {
         return MechAutoController.CombatStyle.CLOSE_RANGE;
      } else if (midWeapons > 0) {
         return meleeWeapons > 0 ? MechAutoController.CombatStyle.MID_RANGE_MELEE : MechAutoController.CombatStyle.MID_RANGE;
      } else {
         return longWeapons > 1 ? MechAutoController.CombatStyle.LONG_RANGE : MechAutoController.CombatStyle.MID_RANGE_MELEE;
      }
   }

   private MechAutoController.PilotRank chooseRank(LivingEntity pilot, Pmvc01Entity mech) {
      Item item = pilot.getOffhandItem().getItem();
      if (item instanceof PilotLicenseItem.PilotLicenseNovice) {
         return MechAutoController.PilotRank.NOVICE;
      } else if (item instanceof PilotLicenseItem.PilotLicenseIntermediate) {
         return MechAutoController.PilotRank.INTERMEDIATE;
      } else if (item instanceof PilotLicenseItem.PilotLicenseAdvanced) {
         return MechAutoController.PilotRank.ADVANCED;
      } else {
         return item instanceof PilotLicenseItem.PilotLicenseLegend ? MechAutoController.PilotRank.LEGEND : MechAutoController.PilotRank.NOVICE;
      }
   }

   private MechAutoController.PilotRole chooseRole(LivingEntity pilot, Pmvc01Entity mech) {
      Item item = pilot.getOffhandItem().getItem();
      if (item instanceof PilotRoleItem.PlotRoleGuardian) {
         return MechAutoController.PilotRole.GUARD;
      } else if (item instanceof PilotRoleItem.PlotRoleWingman) {
         return MechAutoController.PilotRole.WINGMAN;
      } else if (item instanceof PilotRoleItem.PlotRoleGladiator) {
         return MechAutoController.PilotRole.GLADIATOR;
      } else {
         return item instanceof PilotRoleItem.PlotRoleRaider ? MechAutoController.PilotRole.RAIDER : MechAutoController.PilotRole.GUARD;
      }
   }

   public enum CombatStyle {
      CLOSE_RANGE(30.0F),
      MID_RANGE(70.0F),
      MID_RANGE_MELEE(70.0F),
      LONG_RANGE(120.0F);

      float preferedDistance;

      CombatStyle(float d) {
         this.preferedDistance = d;
      }

      float getPreferedDistance() {
         return this.preferedDistance;
      }
   }

   public enum PilotRank {
      NOVICE,
      INTERMEDIATE,
      ADVANCED,
      LEGEND;
   }

   public enum PilotRole {
      GUARD,
      RAIDER,
      WINGMAN,
      GLADIATOR;
   }

   public enum SystemMode {
      IDLE,
      COMBAT;
   }

   public static class WeaponAIState {
      public int burstTick;
      public long lastFireTick;
      public int nextFireDelay;
   }
}
