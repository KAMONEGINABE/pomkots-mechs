package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class BaseBossGoal extends Goal {
   protected final GenericPomkotsMonster mob;
   protected float speedModifier;
   private static final int shortTo = 40;
   private static final int longTo = 200;

   public BaseBossGoal(GenericPomkotsMonster mob, float speedModifier) {
      this.mob = mob;
      this.speedModifier = speedModifier;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean canUse() {
      return true;
   }

   public boolean canContinueToUse() {
      return this.canUse();
   }

   public boolean isInterruptable() {
      return false;
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }

   public void start() {
   }

   public void stop() {
   }

   public void tick() {
   }

   protected boolean isInShortAttackRange() {
      LivingEntity target = this.mob.getTarget();
      if (target == null) {
         return false;
      }

      double distance = target.position().distanceTo(this.mob.position());
      return distance < 40.0;
   }

   protected boolean isInLongAttackRange() {
      LivingEntity target = this.mob.getTarget();
      if (target == null) {
         return false;
      }

      double distance = target.position().distanceTo(this.mob.position());
      return 40.0 < distance && distance < 200.0;
   }
}
