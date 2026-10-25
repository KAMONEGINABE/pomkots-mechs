package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossActionController;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class SimpleBossAttackGoal extends BaseBossGoal {
   protected final BossActionController.BossAction action;
   private boolean adjustRotation = false;
   protected int maxRepeatNum = 0;
   protected int currentRepeatNum = 0;
   protected boolean rotateOnSamePlace = false;

   public SimpleBossAttackGoal(BossActionController.BossAction action, BaseBossEntity mob, boolean adjustRotation) {
      this(action, mob, adjustRotation, 1);
   }

   public SimpleBossAttackGoal(BossActionController.BossAction action, BaseBossEntity mob, boolean adjustRotation, int repeatNum) {
      this(action, mob, adjustRotation, repeatNum, false);
   }

   public SimpleBossAttackGoal(BossActionController.BossAction action, BaseBossEntity mob, boolean adjustRotation, boolean rotateOnSamePlace) {
      this(action, mob, adjustRotation, 1, rotateOnSamePlace);
   }

   public SimpleBossAttackGoal(BossActionController.BossAction action, BaseBossEntity mob, boolean adjustRotation, int repeatNum, boolean rotateOnSamePlace) {
      super(mob);
      this.action = action;
      this.adjustRotation = adjustRotation;
      this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK));
      this.maxRepeatNum = repeatNum;
      this.rotateOnSamePlace = rotateOnSamePlace;
   }

   @Override
   public boolean canUseInternal() {
      return !this.action.isInCooltime();
   }

   public boolean m_8045_() {
      return this.action.isInAction();
   }

   public boolean m_183429_() {
      return true;
   }

   public void m_8056_() {
      if (this.action.tryAction()) {
         if (!this.adjustRotation) {
            this.mob.m_21573_().m_26573_();
         }

         this.currentRepeatNum = this.maxRepeatNum;
         this.mob.rotateToTarget(this.mob.m_5448_());
      }
   }

   public void m_8041_() {
   }

   public void m_8037_() {
      if (this.adjustRotation && this.mob.m_5448_() != null) {
         this.mob.rotateToTarget(this.mob.m_5448_());
         if (this.rotateOnSamePlace) {
            this.mob.choiMove();
         }
      }

      if (this.action.onEndOfAction() && --this.currentRepeatNum > 0) {
         this.action.setContinue();
      }
   }
}
