package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.BossActionController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.Pmb01Entity;

public class AttackBreakBossGoal extends AttackBossGoal {
   public AttackBreakBossGoal(BossActionController.BossAction action, GenericPomkotsMonster mob, float speedModifier) {
      super(action, mob, speedModifier);
   }

   @Override
   public boolean m_8036_() {
      boolean flag = ((Pmb01Entity)this.mob).consumeBreakFlag();
      return this.action.canAction() && flag;
   }
}
