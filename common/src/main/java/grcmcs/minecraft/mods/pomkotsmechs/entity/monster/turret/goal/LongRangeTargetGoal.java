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

   public boolean canContinueToUse() {
      LivingEntity livingEntity = this.mob.getTarget();
      if (livingEntity == null) {
         livingEntity = this.targetMob;
      }

      if (livingEntity == null) {
         return false;
      }

      if (!this.mob.canAttack(livingEntity)) {
         return false;
      }

      Team team = this.mob.getTeam();
      Team team2 = livingEntity.getTeam();
      if (team != null && team2 == team) {
         return false;
      }

      double d = this.getFollowDistance();
      System.out.println(d);
      if (this.mob.distanceToSqr(livingEntity) > d * d) {
         return false;
      }

      this.mob.setTarget(livingEntity);
      return true;
   }
}
