package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class GoalDice {
   protected BaseBossEntity mob;
   protected Map<Integer, Set<GoalDice.GoalDefinition>> modeGoalMap = new HashMap<>();
   protected Goal nextGoal = null;

   public GoalDice(BaseBossEntity mob) {
      this.mob = mob;
   }

   public void registerGoal(BaseBossGoal goal, int[] availableMode, float availableDistanceMin, float availableDistanceMax, int weight) {
      GoalDice.GoalDefinition def = new GoalDice.GoalDefinition(goal, availableDistanceMin, availableDistanceMax, weight);

      for (int mode : availableMode) {
         Set<GoalDice.GoalDefinition> set = this.modeGoalMap.computeIfAbsent(mode, k -> new HashSet<>());
         set.add(def);
      }
   }

   public void tick() {
      LivingEntity target = this.mob.getTarget();
      if (target != null) {
         Set<GoalDice.GoalDefinition> goalCandidates = this.modeGoalMap.get(this.mob.getAiMode());
         if (goalCandidates != null && !goalCandidates.isEmpty()) {
            this.nextGoal = this.getRandomWeighted(goalCandidates, target);
         } else {
            this.nextGoal = null;
         }
      }
   }

   private Goal getRandomWeighted(Set<GoalDice.GoalDefinition> entries, Entity target) {
      double distance = this.mob.distanceToSqr(target);
      int totalWeight = 0;
      HashSet<GoalDice.GoalDefinition> cand2 = new HashSet<>();

      for (GoalDice.GoalDefinition entry : entries) {
         if ((!(entry.availableDistanceMin >= 0.0F) || !(entry.availableDistanceMin > distance))
            && (!(entry.availableDistanceMax >= 0.0F) || !(entry.availableDistanceMax < distance))
            && entry.goal.canUseInternal()) {
            totalWeight += entry.weight;
            cand2.add(entry);
         }
      }

      if (totalWeight == 0) {
         return null;
      }

      int r = this.mob.getRandom().nextInt(totalWeight);
      int cumulative = 0;

      for (GoalDice.GoalDefinition entry : cand2) {
         cumulative += entry.weight;
         if (r < cumulative) {
            return entry.goal;
         }
      }

      throw new IllegalStateException("Should never reach here if weights are positive.");
   }

   public boolean canUse(Goal tgt) {
      return tgt == this.nextGoal && !this.mob.isStunning();
   }

   protected static class GoalDefinition {
      protected final BaseBossGoal goal;
      protected final float availableDistanceMin;
      protected final float availableDistanceMax;
      protected final int weight;

      public GoalDefinition(BaseBossGoal goal, float availableDistanceMin, float availableDistanceMax, int weight) {
         this.goal = goal;
         if (availableDistanceMin < 0.0F) {
            this.availableDistanceMin = -1.0F;
         } else {
            this.availableDistanceMin = availableDistanceMin * availableDistanceMin;
         }

         if (availableDistanceMax < 0.0F) {
            this.availableDistanceMax = -1.0F;
         } else {
            this.availableDistanceMax = availableDistanceMax * availableDistanceMax;
         }

         this.weight = weight;
      }
   }
}
