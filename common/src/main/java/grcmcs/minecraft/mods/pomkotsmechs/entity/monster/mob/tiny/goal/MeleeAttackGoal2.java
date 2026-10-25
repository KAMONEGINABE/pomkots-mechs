package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class MeleeAttackGoal2 extends MeleeAttackGoal {
   public MeleeAttackGoal2(PathfinderMob pathfinderMob, double d, boolean bl) {
      super(pathfinderMob, d, bl);
   }

   protected void m_6739_(LivingEntity livingEntity, double d) {
      if (this.f_25540_ instanceof BaseSmallMonsterEntity small) {
         if (d <= this.m_6639_(livingEntity)) {
            small.tryAttack();
         }
      } else {
         super.m_6739_(livingEntity, d);
      }
   }

   protected double m_6639_(LivingEntity livingEntity) {
      return this.f_25540_.m_20205_() * 3.0F * this.f_25540_.m_20205_() * 3.0F + livingEntity.m_20205_();
   }
}
