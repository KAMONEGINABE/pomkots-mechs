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
   public boolean canUse() {
      return this.action.canAction() && this.mob.getRandom().nextFloat() < 0.6F;
   }

   @Override
   public boolean canContinueToUse() {
      return this.action.isInAction();
   }

   @Override
   public void start() {
      if (this.action.tryAction()) {
         if (!this.adjustRotation) {
            this.mob.getNavigation().stop();
         }

         this.mob.rotateToTarget(this.mob.getTarget());
      }
   }

   @Override
   public void stop() {
   }

   @Override
   public void tick() {
      if (this.adjustRotation && this.mob.getTarget() != null) {
         this.mob.rotateToTarget(this.mob.getTarget());
      }
   }
}
