package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.BaseTurretEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;

public class ContinuousAttackGoal extends Goal {
   protected final BaseTurretEntity mob;
   protected float speedModifier;
   protected int currentCoolTime = 0;
   protected int maxCoolTime = 20;
   protected int maxActionTime = 20;
   protected int currentActionTime = 0;
   protected int interval = 10;

   public ContinuousAttackGoal(BaseTurretEntity mob, float speedModifier, int maxActionTime, int maxCoolTime, int interval) {
      this.mob = mob;
      this.speedModifier = speedModifier;
      this.currentCoolTime = 0;
      this.maxCoolTime = maxCoolTime;
      this.maxActionTime = maxActionTime;
      this.interval = interval;
      this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK));
   }

   public boolean m_8036_() {
      if (this.currentCoolTime > 0) {
         this.currentCoolTime--;
      }

      return this.currentCoolTime <= 0 && this.mob.m_5448_() != null;
   }

   public boolean m_8045_() {
      return this.currentActionTime > 0;
   }

   public void m_8056_() {
      this.currentCoolTime = this.maxCoolTime;
      this.currentActionTime = this.maxActionTime;
      this.mob.startActionAnimation();
   }

   public void m_8041_() {
      this.mob.stopActionAnimation();
   }

   public void m_8037_() {
      if (this.currentActionTime > 0) {
         this.currentActionTime--;
      }

      LivingEntity target = this.mob.m_5448_();
      if (target != null && this.currentActionTime > 0 && this.currentActionTime % this.interval == 0) {
         this.mob.tryAttack();
      }
   }

   public boolean m_6767_() {
      return false;
   }

   public boolean m_183429_() {
      return true;
   }
}
