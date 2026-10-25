package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class SimpleMobGoal extends SmallMobGoalBase {
   protected int walkCount = 0;
   private static final int shortTo = 30;
   private static final int longTo = 100;

   public SimpleMobGoal(BaseSmallMonsterEntity mob, float speedModifier) {
      super(mob, speedModifier);
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean canContinueToUse() {
      return this.canUse();
   }

   public void start() {
   }

   public void stop() {
   }

   public void tick() {
      LivingEntity target = this.mob.getTarget();
      if (target != null) {
         if (this.walkCount > 0) {
            if (this.walkCount++ > 40) {
               this.walkCount = 0;
            }
         } else {
            double distance = target.position().distanceTo(this.mob.position());
            if (this.isInShortAttackRange(distance)) {
               long pattern = this.mob.level().getGameTime() % 5L;
               if (pattern == 0L) {
                  this.startRandomWalk(this.speedModifier);
               } else {
                  this.mob.getNavigation().stop();
                  this.mob.tryAttack();
               }
            } else if (this.isInLongAttackRange(distance)) {
               long pattern = this.mob.level().getGameTime() % 5L;
               if (pattern == 0L) {
                  this.startWalk(target, this.speedModifier);
               } else {
                  this.mob.getNavigation().stop();
                  this.mob.tryAttack();
               }
            } else {
               this.startWalk(target, this.speedModifier);
            }
         }
      }
   }

   protected void startRandomWalk(float speedModifier) {
      Vec3 rnd = DefaultRandomPos.getPos(this.mob, 10, 7);
      if (rnd != null) {
         this.mob.getNavigation().moveTo(rnd.x, rnd.y, rnd.z, this.speedModifier);
         this.walkCount = 1;
      }
   }

   protected void startWalk(LivingEntity target, float speedModifier) {
      this.mob.getNavigation().moveTo(target, this.speedModifier);
      this.walkCount = 1;
   }

   protected boolean isInShortAttackRange(double distance) {
      return distance < 30.0;
   }

   protected boolean isInLongAttackRange(double distance) {
      return 30.0 < distance && distance < 100.0;
   }

   public boolean isInterruptable() {
      return false;
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }
}
