package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public abstract class BaseBossGoal extends Goal {
   protected BaseBossEntity mob;
   protected GoalDice goalDice;

   public BaseBossGoal(BaseBossEntity mob) {
      this.mob = mob;
      this.goalDice = mob.goalDice;
   }

   public boolean canUse() {
      LivingEntity target = this.mob.getTarget();
      return target != null && target.isAlive() && !this.mob.getActionController().isInActionAll() && this.goalDice.canUse(this);
   }

   abstract boolean canUseInternal();
}
