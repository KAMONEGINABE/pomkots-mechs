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
   public boolean m_8045_() {
      return this.walkCount < 40;
   }

   @Override
   public void m_8056_() {
      this.walkCount = 0;
   }

   @Override
   public void m_8041_() {
   }

   @Override
   public void m_8037_() {
      LivingEntity target = this.mob.m_5448_();
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
      Vec3 rnd = DefaultRandomPos.m_148403_(this.mob, 10, 7);
      if (rnd != null) {
         this.mob.m_21573_().m_26519_(rnd.f_82479_, rnd.f_82480_, rnd.f_82481_, this.speedModifier);
         this.walkCount = 1;
      }
   }

   protected void startWalk(LivingEntity target, float speedModifier) {
      this.mob.m_21573_().m_5624_(target, this.speedModifier);
      this.walkCount = 1;
   }
}
