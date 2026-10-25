package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BossActionController {
   private Map<String, BossActionController.BossAction> actionMap = new HashMap<>();

   public void registerAction(String name, BossActionController.BossAction action) {
      this.actionMap.put(name, action);
   }

   public BossActionController.BossAction getAction(String name) {
      return this.actionMap.get(name);
   }

   public void tick() {
      for (BossActionController.BossAction action : this.actionMap.values()) {
         action.tick();
      }
   }

   public void reset() {
      for (BossActionController.BossAction action : this.actionMap.values()) {
         action.reset();
      }
   }

   public boolean isInActionAll() {
      for (BossActionController.BossAction action : this.actionMap.values()) {
         if (action.isInAction()) {
            return true;
         }
      }

      return false;
   }

   public static class BossAction {
      public int maxCoolTick;
      public int maxActionTick;
      public int maxActionTickPerLoop;
      public int actualActionTick;
      public int actionLoopNum;
      public int currentCoolTick = 0;
      public int currentActionTick = 0;
      public boolean isOnEnd = false;
      private Consumer<Void> startAction;
      private Consumer<Void> actualAction;

      BossAction(int maxCoolTick, int maxActionTick, int actualActionTick, Consumer<Void> startAction, Consumer<Void> actualAction) {
         this(maxCoolTick, maxActionTick, actualActionTick, 1, startAction, actualAction);
      }

      BossAction(int maxCoolTick, int maxActionTick, int actualActionTick, int actionLoopNum, Consumer<Void> startAction, Consumer<Void> actualAction) {
         this.maxCoolTick = maxCoolTick;
         this.maxActionTick = maxActionTick * actionLoopNum;
         this.maxActionTickPerLoop = maxActionTick;
         this.actionLoopNum = actionLoopNum;
         this.startAction = startAction;
         this.actualActionTick = actualActionTick;
         this.actualAction = actualAction;
      }

      public void tick() {
         if (this.currentCoolTick > 0) {
            this.currentCoolTick--;
         }

         if (this.currentActionTick > 0) {
            this.currentActionTick++;
            if (this.currentActionTick % this.maxActionTickPerLoop == this.actualActionTick) {
               this.actualAction.accept(null);
            }

            if (this.currentActionTick == this.maxActionTick) {
               this.currentActionTick = 0;
               this.isOnEnd = true;
            }
         }
      }

      public boolean isInAction() {
         return this.currentActionTick > 0;
      }

      public boolean isInCooltime() {
         return this.currentCoolTick > 0;
      }

      public boolean tryAction() {
         if (!this.isInAction() && !this.isInCooltime()) {
            this.startAction();
            return true;
         } else {
            return false;
         }
      }

      public boolean canAction() {
         return !this.isInAction() && !this.isInCooltime();
      }

      private void startAction() {
         this.currentActionTick = 1;
         this.currentCoolTick = this.maxCoolTick;
         this.startAction.accept(null);
      }

      private void reset() {
         this.currentActionTick = 0;
         this.currentCoolTick = 0;
      }
   }
}
