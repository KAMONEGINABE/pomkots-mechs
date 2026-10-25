package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms01Entity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class ChargingMobGoal extends SmallMobGoalBase {
   protected int walkCount = 0;
   protected Pms01Entity pms1;
   private static final int shortTo = 30;
   private static final int longTo = 100;

   public ChargingMobGoal(Pms01Entity mob, float speedModifier) {
      super(mob, speedModifier);
      this.pms1 = mob;
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
            if (!this.pms1.isAttacking()) {
               if (this.isInShortAttackRange(distance)) {
                  long pattern = this.mob.tickCount % 3;
                  if (pattern == 0L) {
                     this.mob.getNavigation().stop();
                     this.mob.tryAttack();
                  } else {
                     this.startRandomWalk(this.speedModifier);
                  }
               } else if (this.isInLongAttackRange(distance)) {
                  long pattern = this.mob.tickCount % 3;
                  if (pattern == 0L) {
                     this.mob.getNavigation().stop();
                     this.mob.tryAttack();
                  } else {
                     this.startWalk(target, this.speedModifier);
                  }
               } else {
                  this.startWalk(target, this.speedModifier);
               }
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
