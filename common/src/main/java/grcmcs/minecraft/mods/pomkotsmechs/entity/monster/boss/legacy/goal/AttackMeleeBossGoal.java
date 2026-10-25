package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.BossActionController;

public class AttackMeleeBossGoal extends AttackBossGoal {
   public AttackMeleeBossGoal(BossActionController.BossAction action, GenericPomkotsMonster mob, float speedModifier) {
      super(action, mob, speedModifier);
   }

   @Override
   public boolean m_8036_() {
      return this.action.canAction() && this.mob.m_217043_().m_188501_() < 0.7F && this.isInShortAttackRange();
   }
}
