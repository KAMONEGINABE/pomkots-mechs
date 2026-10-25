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
      this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean m_8036_() {
      return true;
   }

   public boolean m_8045_() {
      return this.m_8036_();
   }

   public boolean m_6767_() {
      return false;
   }

   public boolean m_183429_() {
      return true;
   }

   public void m_8056_() {
   }

   public void m_8041_() {
   }

   public void m_8037_() {
   }

   protected boolean isInShortAttackRange() {
      LivingEntity target = this.mob.m_5448_();
      if (target == null) {
         return false;
      }

      double distance = target.m_20182_().m_82554_(this.mob.m_20182_());
      return distance < 40.0;
   }

   protected boolean isInLongAttackRange() {
      LivingEntity target = this.mob.m_5448_();
      if (target == null) {
         return false;
      }

      double distance = target.m_20182_().m_82554_(this.mob.m_20182_());
      return 40.0 < distance && distance < 200.0;
   }
}
