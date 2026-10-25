package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.BossActionController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttackBossGoal extends BaseBossGoal {
   protected final BossActionController.BossAction action;
   private boolean adjustRotation = false;
   public static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");

   public AttackBossGoal(BossActionController.BossAction action, GenericPomkotsMonster mob, float speedModifier) {
      this(action, mob, speedModifier, false);
   }

   public AttackBossGoal(BossActionController.BossAction action, GenericPomkotsMonster mob, float speedModifier, boolean adjustRotation) {
      super(mob, speedModifier);
      this.action = action;
      this.adjustRotation = adjustRotation;
   }

   @Override
   public boolean m_8036_() {
      return this.action.canAction() && this.mob.m_217043_().m_188501_() < 0.6F;
   }

   @Override
   public boolean m_8045_() {
      return this.action.isInAction();
   }

   @Override
   public void m_8056_() {
      if (this.action.tryAction()) {
         if (!this.adjustRotation) {
            this.mob.m_21573_().m_26573_();
         }

         this.mob.rotateToTarget(this.mob.m_5448_());
      }
   }

   @Override
   public void m_8041_() {
   }

   @Override
   public void m_8037_() {
      if (this.adjustRotation && this.mob.m_5448_() != null) {
         this.mob.rotateToTarget(this.mob.m_5448_());
      }
   }
}
