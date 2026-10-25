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
      this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean m_8036_() {
      if (this.pilot.m_20159_()) {
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

   public boolean m_8045_() {
      if (this.pilot.m_20159_()) {
         return false;
      } else if (this.targetMech == null) {
         return false;
      } else {
         return !this.isRidableMech(this.targetMech) ? false : this.targetMech.m_20197_().isEmpty();
      }
   }

   public void m_8056_() {
      this.pilot.m_21573_().m_5624_(this.targetMech, 0.7);
   }

   public void m_8037_() {
      if (this.targetMech != null) {
         this.pilot.m_21563_().m_24960_(this.targetMech, 30.0F, 30.0F);
         double distanceSq = this.pilot.m_20280_(this.targetMech);
         if (distanceSq <= 4.0) {
            this.pilot.m_7998_(this.targetMech, true);
         } else {
            if (this.pilot.m_21573_().m_26571_()) {
               this.pilot.m_21573_().m_5624_(this.targetMech, 0.7);
            }
         }
      }
   }

   public void m_8041_() {
      this.pilot.m_21573_().m_26573_();
      this.targetMech = null;
   }

   private Pmvc01Entity findNearestFreeMech() {
      AABB box = this.pilot.m_20191_().m_82400_(32.0);
      List<Pmvc01Entity> mechs = this.pilot.m_9236_().m_45976_(Pmvc01Entity.class, box);
      Pmvc01Entity nearest = null;
      double nearestDistance = Double.MAX_VALUE;

      for (Pmvc01Entity mech : mechs) {
         if (this.isRidableMech(mech) && mech.m_20197_().isEmpty()) {
            double distance = this.pilot.m_20280_(mech);
            if (distance < nearestDistance) {
               nearestDistance = distance;
               nearest = mech;
            }
         }
      }

      return nearest;
   }

   private boolean isRidableMech(Pmvc01Entity mech) {
      return mech != null && mech.m_6084_() && !mech.isBroken();
   }
}
