package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai;

import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.phys.AABB;

public class FindAndEnterMechGoal extends Goal {
   private static final double SEARCH_RANGE = 32.0;
   private static final double ENTER_DISTANCE = 2.0;
   private static final double DASH_SPEED = 0.7;
   private static final int SEARCH_COOLDOWN = 100;
   private final MechPilotEntity pilot;
   private Pmvc01Entity targetMech;
   private int searchCooldown;

   public FindAndEnterMechGoal(MechPilotEntity pilot) {
      this.pilot = pilot;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean canUse() {
      if (this.pilot.isPassenger()) {
         return false;
      } else if (this.searchCooldown > 0) {
         this.searchCooldown--;
         return false;
      } else {
         this.searchCooldown = 100;
         this.targetMech = this.findNearestFreeMech();
         return this.targetMech != null;
      }
   }

   public boolean canContinueToUse() {
      if (this.pilot.isPassenger()) {
         return false;
      } else if (this.targetMech == null) {
         return false;
      } else {
         return !this.isRidableMech(this.targetMech) ? false : this.targetMech.getPassengers().isEmpty();
      }
   }

   public void start() {
      this.pilot.getNavigation().moveTo(this.targetMech, 0.7);
   }

   public void tick() {
      if (this.targetMech != null) {
         this.pilot.getLookControl().setLookAt(this.targetMech, 30.0F, 30.0F);
         double distanceSq = this.pilot.distanceToSqr(this.targetMech);
         if (distanceSq <= 4.0) {
            this.pilot.startRiding(this.targetMech, true);
         } else {
            if (this.pilot.getNavigation().isDone()) {
               this.pilot.getNavigation().moveTo(this.targetMech, 0.7);
            }
         }
      }
   }

   public void stop() {
      this.pilot.getNavigation().stop();
      this.targetMech = null;
   }

   private Pmvc01Entity findNearestFreeMech() {
      AABB box = this.pilot.getBoundingBox().inflate(32.0);
      List<Pmvc01Entity> mechs = this.pilot.level().getEntitiesOfClass(Pmvc01Entity.class, box);
      Pmvc01Entity nearest = null;
      double nearestDistance = Double.MAX_VALUE;

      for (Pmvc01Entity mech : mechs) {
         if (this.isRidableMech(mech) && mech.getPassengers().isEmpty()) {
            double distance = this.pilot.distanceToSqr(mech);
            if (distance < nearestDistance) {
               nearestDistance = distance;
               nearest = mech;
            }
         }
      }

      return nearest;
   }

   private boolean isRidableMech(Pmvc01Entity mech) {
      return mech != null && mech.isAlive() && !mech.isBroken();
   }
}
