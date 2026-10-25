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
      this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean m_8045_() {
      return this.m_8036_();
   }

   public void m_8056_() {
   }

   public void m_8041_() {
   }

   public void m_8037_() {
      LivingEntity target = this.mob.m_5448_();
      if (target != null) {
         if (this.walkCount > 0) {
            if (this.walkCount++ > 40) {
               this.walkCount = 0;
            }
         } else {
            double distance = target.m_20182_().m_82554_(this.mob.m_20182_());
            if (this.isInShortAttackRange(distance)) {
               long pattern = this.mob.m_9236_().m_46467_() % 5L;
               if (pattern == 0L) {
                  this.startRandomWalk(this.speedModifier);
               } else {
                  this.mob.m_21573_().m_26573_();
                  this.mob.tryAttack();
               }
            } else if (this.isInLongAttackRange(distance)) {
               long pattern = this.mob.m_9236_().m_46467_() % 5L;
               if (pattern == 0L) {
                  this.startWalk(target, this.speedModifier);
               } else {
                  this.mob.m_21573_().m_26573_();
                  this.mob.tryAttack();
               }
            } else {
               this.startWalk(target, this.speedModifier);
            }
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

   protected boolean isInShortAttackRange(double distance) {
      return distance < 30.0;
   }

   protected boolean isInLongAttackRange(double distance) {
      return 30.0 < distance && distance < 100.0;
   }

   public boolean m_6767_() {
      return false;
   }

   public boolean m_183429_() {
      return true;
   }
}
