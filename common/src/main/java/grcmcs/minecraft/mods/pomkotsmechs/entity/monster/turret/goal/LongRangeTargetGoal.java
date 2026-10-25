package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.NearestEntityTargetGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

public class LongRangeTargetGoal extends NearestEntityTargetGoal<Player> {
   public LongRangeTargetGoal(Mob mob) {
      super(mob, Player.class, false, false);
   }

   public boolean m_8045_() {
      LivingEntity livingEntity = this.f_26135_.m_5448_();
      if (livingEntity == null) {
         livingEntity = this.f_26137_;
      }

      if (livingEntity == null) {
         return false;
      }

      if (!this.f_26135_.m_6779_(livingEntity)) {
         return false;
      }

      Team team = this.f_26135_.m_5647_();
      Team team2 = livingEntity.m_5647_();
      if (team != null && team2 == team) {
         return false;
      }

      double d = this.m_7623_();
      System.out.println(d);
      if (this.f_26135_.m_20280_(livingEntity) > d * d) {
         return false;
      }

      this.f_26135_.m_6710_(livingEntity);
      return true;
   }
}
