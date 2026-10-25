package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.notinuse;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

public class ActionBase {
   public int maxCoolTick;
   public int maxActionTick;
   public int currentCoolTick = 0;
   public int currentActionTick = 0;
   public boolean isOnEnd = false;
   PomkotsVehicleBase body;
   Map<Integer, ActionBase.ActionProcedure> procMap = new HashMap<>();

   ActionBase(int maxCoolTick, int maxActionTick, PomkotsVehicleBase body) {
      this.maxCoolTick = maxCoolTick;
      this.maxActionTick = maxActionTick;
      this.body = body;
   }

   public void registerActionProcedure(int triggerTick, ActionBase.ActionProcedure proc) {
      this.procMap.put(triggerTick, proc);
   }

   public void tick() {
      if (this.currentCoolTick > 0) {
         this.currentCoolTick--;
      }

      if (this.currentActionTick > 0) {
         this.currentActionTick++;
         ActionBase.ActionProcedure p = this.procMap.get(this.currentActionTick);
         if (p != null && p.animationProcedure != null) {
            p.logicProcedure.accept(this.body);
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

   private void startAction() {
      this.currentActionTick = 1;
      this.currentCoolTick = this.maxCoolTick;
      this.procMap.get(1).logicProcedure.accept(this.body);
   }

   public PlayState tickAnimation(AnimationState<PomkotsVehicleBase> event) {
      if (this.currentActionTick > 0) {
         ActionBase.ActionProcedure p = this.procMap.get(this.currentActionTick);
         return p != null && p.animationProcedure != null ? p.animationProcedure.apply(event) : null;
      } else {
         return null;
      }
   }

   private void reset() {
      this.currentActionTick = 0;
      this.currentCoolTick = 0;
   }

   public static class ActionProcedure {
      final Consumer<PomkotsVehicleBase> logicProcedure;
      final Function<AnimationState<PomkotsVehicleBase>, PlayState> animationProcedure;

      public ActionProcedure(Consumer<PomkotsVehicleBase> l, Function<AnimationState<PomkotsVehicleBase>, PlayState> a) {
         this.logicProcedure = l;
         this.animationProcedure = a;
      }
   }
}
