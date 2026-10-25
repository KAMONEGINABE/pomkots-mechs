package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import java.util.HashSet;
import java.util.Set;

public abstract class Motion {
   protected int maxActionTick;
   protected int interval;
   protected Set<Integer> actualFireTick;
   public static Motion RIFLE = (new Motion() {
      public Motion init() {
         this.maxActionTick = 10;
         this.interval = -1;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(2);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.ONE_SHOT;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return "animation.pmv01.w_rifle_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return true;
      }
   }).init();
   public static Motion GATLING_GUN = (new Motion() {
      public Motion init() {
         this.maxActionTick = 9;
         this.interval = 2;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(4);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.CONTINUOUS;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return !act.isInFire() ? "animation.pmv01.w_gatring1_" + side : "animation.pmv01.w_gatring2_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return true;
      }
   }).init();
   public static Motion SUB_MACHINE_GUN = (new Motion() {
      public Motion init() {
         this.maxActionTick = 9;
         this.interval = 2;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(4);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.CONTINUOUS;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return !act.isInFire() ? "animation.pmv01.w_gatring1_" + side : "animation.pmv01.w_gatring2_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return true;
      }
   }).init();
   public static Motion SHOULDER_MACHINE_GUN = (new Motion() {
      public Motion init() {
         this.maxActionTick = 9;
         this.interval = 2;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(4);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.CONTINUOUS;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return !act.isInFire() ? "animation.pmv01.idle" : "animation.pmv01.idle";
      }

      @Override
      public boolean concurrentAvailable() {
         return true;
      }
   }).init();
   public static Motion HAND_MISSILE = (new Motion() {
      public Motion init() {
         this.maxActionTick = 10;
         this.interval = -1;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(2);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.MULTI_LOCK;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return "animation.pmv01.w_rifle_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return true;
      }
   }).init();
   public static Motion PUNCH = (new Motion() {
      public Motion init() {
         this.maxActionTick = 19;
         this.interval = -1;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(10);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.ONE_SHOT;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return "animation.pmv01.w_punch_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return false;
      }
   }).init();
   public static Motion SABER = (new Motion() {
      public Motion init() {
         this.maxActionTick = 19;
         this.interval = -1;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(10);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.ONE_SHOT;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return "animation.pmv01.w_saber_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return false;
      }
   }).init();
   public static Motion CIRCLE_BLADE = (new Motion() {
      public Motion init() {
         this.maxActionTick = 19;
         this.interval = -1;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(7);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.ONE_SHOT;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return "animation.pmv01.w_circle_blade_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return false;
      }
   }).init();
   public static Motion SHOULDER_BLADE = (new Motion() {
      public Motion init() {
         this.maxActionTick = 19;
         this.interval = -1;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(7);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.ONE_SHOT;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return "animation.pmv01.w_blade_shoulder_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return true;
      }
   }).init();
   public static Motion YARI = (new Motion() {
      public Motion init() {
         this.maxActionTick = 19;
         this.interval = -1;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(10);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.ONE_SHOT;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return "animation.pmv01.w_yari_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return false;
      }
   }).init();
   public static Motion DRILL = (new Motion() {
      public Motion init() {
         this.maxActionTick = 20;
         this.interval = 10;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(10);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.CONTINUOUS;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return !act.isInFire() ? "animation.pmv01.w_drill1_" + side : "animation.pmv01.w_drill2_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return false;
      }
   }).init();
   public static Motion CHARGE_HUMMER = (new Motion() {
      public Motion init() {
         this.maxActionTick = 19;
         this.interval = -1;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(Integer.MAX_VALUE - this.maxActionTick - 1);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.CHARGE;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return !act.isInFire() ? "animation.pmv01.w_hummer1_" + side : "animation.pmv01.w_hummer2_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return true;
      }
   }).init();
   public static Motion MULTI_MISSILE = (new Motion() {
      public Motion init() {
         this.maxActionTick = 14;
         this.interval = -1;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(2);
         this.actualFireTick.add(4);
         this.actualFireTick.add(6);
         this.actualFireTick.add(8);
         this.actualFireTick.add(10);
         this.actualFireTick.add(12);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.MULTI_LOCK;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return "animation.pmv01.w_missile_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return true;
      }
   }).init();
   public static Motion GRENADE = (new Motion() {
      public Motion init() {
         this.maxActionTick = 14;
         this.interval = -1;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(4);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.ONE_SHOT;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return "animation.pmv01.w_grenade_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return true;
      }
   }).init();
   public static Motion LARGE_MISSILE = (new Motion() {
      public Motion init() {
         this.maxActionTick = 14;
         this.interval = -1;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(4);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.MULTI_LOCK;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return "animation.pmv01.w_largemissile";
      }

      @Override
      public boolean concurrentAvailable() {
         return false;
      }
   }).init();
   public static Motion GRENADE_HAND = (new Motion() {
      public Motion init() {
         this.maxActionTick = 14;
         this.interval = -1;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(4);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.ONE_SHOT;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return "animation.pmv01.w_baz_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return true;
      }
   }).init();
   public static Motion ROAD_ROLLER = (new Motion() {
      public Motion init() {
         this.maxActionTick = 21;
         this.interval = 10;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(20);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.CONTINUOUS;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         return !act.isInFire() ? "animation.pmv01.w_roller1_" + side : "animation.pmv01.w_roller2_" + side;
      }

      @Override
      public boolean concurrentAvailable() {
         return false;
      }
   }).init();
   public static Motion RAISE_THROW = (new Motion() {
      public Motion init() {
         this.maxActionTick = 9;
         this.interval = -1;
         this.actualFireTick = new HashSet<>();
         this.actualFireTick.add(4);
         return this;
      }

      @Override
      public Motion.MotionType getType() {
         return Motion.MotionType.TOGGLE;
      }

      @Override
      public String getAnimationName(ActionWeapon act, String side) {
         Pmvc01Entity mech = act.getOwner();
         if (mech != null) {
            if (act.isInAction()) {
               return act.isToggleOnStart() ? "animation.pmv01.w_throw_1" : "animation.pmv01.w_throw_3";
            } else {
               return act.isToggleOn() ? "animation.pmv01.w_throw_2" : "animation.pmv01.idle";
            }
         } else {
            return "animation.pmv01.idle";
         }
      }

      @Override
      public boolean concurrentAvailable() {
         return false;
      }
   }).init();

   public int getMaxActionTick() {
      return this.maxActionTick;
   }

   public int getInterval() {
      return this.interval;
   }

   public Set<Integer> getActualFireTick() {
      return this.actualFireTick;
   }

   public abstract Motion.MotionType getType();

   public abstract String getAnimationName(ActionWeapon var1, String var2);

   public abstract boolean concurrentAvailable();

   public enum MotionType {
      ONE_SHOT,
      CONTINUOUS,
      MULTI_LOCK,
      CHARGE,
      TOGGLE;
   }
}
