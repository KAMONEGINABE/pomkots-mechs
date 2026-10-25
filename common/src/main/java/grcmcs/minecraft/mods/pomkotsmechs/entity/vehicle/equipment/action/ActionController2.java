package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;

public class ActionController2 extends ActionController {
   private PomkotsVehicleBase mech;

   public ActionController2(PomkotsVehicleBase mech) {
      this.mech = mech;
   }

   @Override
   public boolean isBoost() {
      return this.mech.isBoost();
   }

   @Override
   public void setBoost(boolean b) {
      if (!this.mech.m_9236_().f_46443_) {
         this.mech.setBoost(b);
      }
   }
}
