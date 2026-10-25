package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.BaseTurretEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class ContinuousAttackGoal extends Goal {
   protected final BaseTurretEntity mob;
   protected float speedModifier;
   protected int currentCoolTime = 0;
   protected int maxCoolTime = 20;
   protected int maxActionTime = 20;
   protected int currentActionTime = 0;
   protected int interval = 10;

   public ContinuousAttackGoal(BaseTurretEntity mob, float speedModifier, int maxActionTime, int maxCoolTime, int interval) {
      this.mob = mob;
      this.speedModifier = speedModifier;
      this.currentCoolTime = 0;
      this.maxCoolTime = maxCoolTime;
      this.maxActionTime = maxActionTime;
      this.interval = interval;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean canUse() {
      if (this.currentCoolTime > 0) {
         this.currentCoolTime--;
      }

      return this.currentCoolTime <= 0 && this.mob.getTarget() != null;
   }

   public boolean canContinueToUse() {
      return this.currentActionTime > 0;
   }

   public void start() {
      this.currentCoolTime = this.maxCoolTime;
      this.currentActionTime = this.maxActionTime;
      this.mob.startActionAnimation();
   }

   public void stop() {
      this.mob.stopActionAnimation();
   }

   public void tick() {
      if (this.currentActionTime > 0) {
         this.currentActionTime--;
      }

      LivingEntity target = this.mob.getTarget();
      if (target != null && this.currentActionTime > 0 && this.currentActionTime % this.interval == 0) {
         this.mob.tryAttack();
      }
   }

   public boolean isInterruptable() {
      return false;
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }
}
