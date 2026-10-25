package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BossActionController {
   private final Map<String, BossActionController.BossAction> actionMap = new HashMap<>();

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
      public int currentCoolTick = 0;
      public int currentActionTick = 0;
      private int currentLoopNum = 0;
      private boolean continueFlag = false;
      Consumer<BossActionController.BossAction> tickInAction;

      BossAction(int maxCoolTick, int maxActionTick, Consumer<BossActionController.BossAction> tickInAction) {
         this.currentCoolTick = this.maxCoolTick = maxCoolTick;
         this.maxActionTick = maxActionTick;
         this.tickInAction = tickInAction;
      }

      public void tick() {
         if (this.currentCoolTick > 0) {
            this.currentCoolTick--;
         } else {
            if (this.currentActionTick > 0) {
               this.tickInAction.accept(this);
               this.currentActionTick++;
            }

            if (this.currentActionTick == this.maxActionTick + 1) {
               if (this.consumeContinueFlag()) {
                  this.currentActionTick = 1;
                  this.currentLoopNum++;
               } else {
                  this.currentActionTick = 0;
                  this.currentCoolTick = this.maxCoolTick;
               }
            }
         }
      }

      private boolean consumeContinueFlag() {
         boolean tmp = this.continueFlag;
         this.continueFlag = false;
         return tmp;
      }

      public void setContinue() {
         this.continueFlag = true;
      }

      public boolean isInAction() {
         return this.currentActionTick > 0;
      }

      public boolean isInCooltime() {
         return this.currentCoolTick > 0;
      }

      public boolean onStartOfAction() {
         return this.currentActionTick == 1;
      }

      public boolean onEndOfAction() {
         return this.currentActionTick == this.maxActionTick - 1;
      }

      public boolean isFirstLoop() {
         return this.currentLoopNum == 0;
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

      public void startAction() {
         this.currentActionTick = 1;
         this.continueFlag = false;
         this.currentLoopNum = 0;
      }

      private void reset() {
         this.currentActionTick = 0;
         this.currentCoolTick = 0;
         this.continueFlag = false;
         this.currentLoopNum = 0;
      }

      public void stopAction() {
         this.currentActionTick = -1;
         this.currentCoolTick = this.maxCoolTick;
         this.continueFlag = false;
         this.currentLoopNum = 0;
      }
   }
}
