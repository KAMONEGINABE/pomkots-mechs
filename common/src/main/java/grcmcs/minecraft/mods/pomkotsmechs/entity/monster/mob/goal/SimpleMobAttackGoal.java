package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class SimpleMobAttackGoal extends SmallMobGoalBase {
   public SimpleMobAttackGoal(BaseSmallMonsterEntity mob, float speedModifier) {
      super(mob, speedModifier);
      this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   @Override
   public boolean m_8036_() {
      if (!super.m_8036_()) {
         return false;
      }

      LivingEntity target = this.mob.m_5448_();
      return target != null && target.m_6084_() && this.mob.m_217043_().m_188501_() < 0.7F;
   }

   public boolean m_8045_() {
      return this.mob.m_217043_().m_188501_() < 0.7F;
   }

   public void m_8037_() {
      LivingEntity target = this.mob.m_5448_();
      if (target != null) {
         this.mob.tryAttack();
      }
   }
}
