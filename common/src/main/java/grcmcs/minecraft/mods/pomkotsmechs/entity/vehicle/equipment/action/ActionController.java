package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ActionController {
   private boolean isDash = false;
   private Map<Integer, Action> actionMap = new HashMap<>();
   private Map<ActionController.ActionType, Action> actionTypeMap = new HashMap<>();

   public void registerAction(Integer key, Action action, ActionController.ActionType type) {
      if (this.actionMap.containsKey(key)) {
         throw new IllegalStateException("Already registered. key:" + key);
      }

      this.actionMap.put(key, action);
      if (!ActionController.ActionType.BASE.equals(type)) {
         this.actionTypeMap.put(type, action);
      }
   }

   public Action getAction(int key) {
      return this.actionMap.get(key);
   }

   public Action getActionFromType(ActionController.ActionType type) {
      return this.actionTypeMap.get(type);
   }

   public void tick() {
      for (Action action : this.actionMap.values()) {
         action.tick();
      }
   }

   public void reset() {
      for (Action action : this.actionMap.values()) {
         action.reset();
      }

      this.setBoost(false);
   }

   public boolean isInActionAll() {
      for (Action action : this.actionMap.values()) {
         if (action.isInAction()) {
            return true;
         }
      }

      return false;
   }

   public boolean isBoost() {
      return this.isDash;
   }

   public void setBoost(boolean b) {
      this.isDash = b;
   }

   public Collection<Action> getAllActions() {
      return this.actionMap.values();
   }

   public enum ActionType {
      R_ARM_MAIN,
      L_ARM_MAIN,
      R_SHL_MAIN,
      L_SHL_MAIN,
      R_ARM_SUB,
      L_ARM_SUB,
      R_SHL_SUB,
      L_SHL_SUB,
      BASE;
   }
}
