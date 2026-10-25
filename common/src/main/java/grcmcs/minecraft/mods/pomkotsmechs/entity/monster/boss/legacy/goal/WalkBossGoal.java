package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class WalkBossGoal extends BaseBossGoal {
   protected static final int MAX_WALK_COUNT = 40;
   protected int walkCount = 0;

   public WalkBossGoal(GenericPomkotsMonster mob, float speedModifier) {
      super(mob, speedModifier);
   }

   @Override
   public boolean canContinueToUse() {
      return this.walkCount < 40;
   }

   @Override
   public void start() {
      this.walkCount = 0;
   }

   @Override
   public void stop() {
   }

   @Override
   public void tick() {
      LivingEntity target = this.mob.getTarget();
      if (target != null) {
         if (this.walkCount > 0) {
            this.walkCount++;
         } else if (this.isInShortAttackRange()) {
            this.startRandomWalk(this.speedModifier);
         } else if (this.isInLongAttackRange()) {
            this.startWalk(target, this.speedModifier);
         } else {
            this.startWalk(target, this.speedModifier);
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
}
