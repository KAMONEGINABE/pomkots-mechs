package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public abstract class SmallMobGoalBase extends Goal {
   protected BaseSmallMonsterEntity mob;
   protected float speedModifier;

   public SmallMobGoalBase(BaseSmallMonsterEntity mob, float speedModifier) {
      this.mob = mob;
      this.speedModifier = speedModifier;
   }

   public boolean canUse() {
      return !this.mob.isClosed();
   }
}
