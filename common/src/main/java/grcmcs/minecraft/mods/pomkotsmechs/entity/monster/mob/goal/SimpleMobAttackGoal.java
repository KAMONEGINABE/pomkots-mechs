package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class SimpleMobAttackGoal extends SmallMobGoalBase {
   public SimpleMobAttackGoal(BaseSmallMonsterEntity mob, float speedModifier) {
      super(mob, speedModifier);
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      if (!super.canUse()) {
         return false;
      }

      LivingEntity target = this.mob.getTarget();
      return target != null && target.isAlive() && this.mob.getRandom().nextFloat() < 0.7F;
   }

   public boolean canContinueToUse() {
      return this.mob.getRandom().nextFloat() < 0.7F;
   }

   public void tick() {
      LivingEntity target = this.mob.getTarget();
      if (target != null) {
         this.mob.tryAttack();
      }
   }
}
