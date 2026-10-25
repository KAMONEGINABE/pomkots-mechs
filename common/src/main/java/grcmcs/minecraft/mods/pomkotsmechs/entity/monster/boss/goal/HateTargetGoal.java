package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidObjectiveEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb99Entity;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class HateTargetGoal extends Goal {
   private final BaseBossEntity mob;
   private final double maxHateRange;
   private final double proximityRange;
   private final Map<UUID, HateTargetGoal.HateData> hateMap;
   private LivingEntity currentTarget;
   private int recheckDelay;
   private static final float HATE_DECAY_RATE = 200.0F;
   private static final float PROXIMITY_HATE_RATE = 1.0F;
   private static final float DISTANCE_DECAY_MULTIPLIER = 10.0F;
   private static final int RECHECK_INTERVAL = 20;
   private RaidObjectiveEntity raidTarget = null;

   public HateTargetGoal(BaseBossEntity mob, double maxHateRange, double proximityRange) {
      this.mob = mob;
      this.maxHateRange = maxHateRange;
      this.proximityRange = proximityRange;
      this.hateMap = new ConcurrentHashMap<>();
      this.setFlags(EnumSet.of(Flag.TARGET));
   }

   public boolean canUse() {
      this.updateHateValues();
      LivingEntity newTarget = this.selectHighestHateTarget();
      if (newTarget != this.currentTarget) {
         this.currentTarget = newTarget;
         this.mob.setTarget(this.currentTarget);
      }

      return this.currentTarget != null;
   }

   public boolean canContinueToUse() {
      if (--this.recheckDelay <= 0) {
         this.recheckDelay = 20;
         this.updateHateValues();
         LivingEntity newTarget = this.selectHighestHateTarget();
         if (newTarget != this.currentTarget) {
            this.currentTarget = newTarget;
            this.mob.setTarget(this.currentTarget);
         }
      }

      return this.currentTarget != null && this.currentTarget.isAlive() && this.mob.distanceToSqr(this.currentTarget) <= this.maxHateRange * this.maxHateRange;
   }

   public void start() {
      this.recheckDelay = 20;
   }

   public void stop() {
      this.currentTarget = null;
      this.mob.setTarget(null);
   }

   public void addDamageHate(LivingEntity entity, float damage) {
      if (entity != null && this.isValidTarget(entity)) {
         UUID entityId = entity.getUUID();
         HateTargetGoal.HateData hateData = this.hateMap.computeIfAbsent(entityId, k -> new HateTargetGoal.HateData());
         hateData.addHate(damage * 2.0F);
      }
   }

   public void addCustomHate(LivingEntity entity, float hate) {
      if (entity != null && this.isValidTarget(entity)) {
         UUID entityId = entity.getUUID();
         HateTargetGoal.HateData hateData = this.hateMap.computeIfAbsent(entityId, k -> new HateTargetGoal.HateData());
         hateData.addHate(hate);
      }
   }

   public void setHate(LivingEntity entity, float hate) {
      if (entity != null && this.isValidTarget(entity)) {
         UUID entityId = entity.getUUID();
         HateTargetGoal.HateData hateData = this.hateMap.computeIfAbsent(entityId, k -> new HateTargetGoal.HateData());
         hateData.setHate(hate);
      }
   }

   public float getHate(LivingEntity entity) {
      if (entity == null) {
         return 0.0F;
      }

      HateTargetGoal.HateData hateData = this.hateMap.get(entity.getUUID());
      return hateData != null ? hateData.getHate() : 0.0F;
   }

   private boolean isValidTarget(LivingEntity entity) {
      if (entity == null || !entity.isAlive()) {
         return false;
      }

      if (this.mob.isSelf(entity)) {
         return false;
      }

      if (entity instanceof BaseBossEntity && !(entity instanceof Pmb99Entity)) {
         return false;
      }

      Team mobTeam = this.mob.getTeam();
      Team entityTeam = entity.getTeam();
      if (mobTeam != null && entityTeam != null) {
         if (mobTeam.equals(entityTeam)) {
            return false;
         }

         if (mobTeam.isAlliedTo(entityTeam)) {
            return false;
         }
      }

      return true;
   }

   private void updateHateValues() {
      Iterator<Entry<UUID, HateTargetGoal.HateData>> iterator = this.hateMap.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<UUID, HateTargetGoal.HateData> entry = iterator.next();
         UUID entityId = entry.getKey();
         HateTargetGoal.HateData hateData = entry.getValue();
         Entity entity = this.findEntityById(entityId);
         if (entity instanceof LivingEntity le) {
            double distance = this.mob.distanceToSqr(entity);
            if (!le.isAlive()) {
               iterator.remove();
            } else if (distance > this.maxHateRange * this.maxHateRange) {
               hateData.decay(20.0F);
            } else if (this.isValidTarget(le)) {
               if (distance <= this.proximityRange * this.proximityRange) {
                  hateData.addHate(0.05F);
               }

               float decayRate = 10.0F;
               if (distance > this.proximityRange * this.proximityRange) {
                  double distanceRatio = Math.sqrt(distance) / this.maxHateRange;
                  decayRate = (float)(decayRate * (1.0 + 10.0 * distanceRatio));
               }

               hateData.decay(decayRate);
               if (hateData.getHate() <= 0.0F) {
                  iterator.remove();
               }
            } else {
               hateData.decay(20.0F);
            }
         } else {
            iterator.remove();
         }
      }
   }

   private LivingEntity selectHighestHateTarget() {
      LivingEntity bestTarget = null;
      float highestHate = 0.0F;

      for (Entry<UUID, HateTargetGoal.HateData> entry : this.hateMap.entrySet()) {
         if (entry.getValue().getHate() > highestHate
            && this.findEntityById(entry.getKey()) instanceof LivingEntity le
            && le.isAlive()
            && this.isValidTarget(le)
            && this.mob.distanceToSqr(le) <= this.maxHateRange * this.maxHateRange) {
            bestTarget = le;
            highestHate = entry.getValue().getHate();
         }
      }

      if (this.mob.isInRaid()) {
         if (this.raidTarget == null) {
            this.raidTarget = this.findRaidTarget();
         }

         if (highestHate < 300.0F && this.raidTarget != null) {
            return this.raidTarget;
         }
      }

      return bestTarget;
   }

   private RaidObjectiveEntity findRaidTarget() {
      double targetRange = this.mob.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue();
      if (this.mob.level() instanceof ServerLevel level) {
         Vec3 var7 = this.mob.position();
         AABB area = new AABB(
            var7.x - targetRange,
            var7.y - 100.0,
            var7.z - targetRange,
            var7.x + targetRange,
            var7.y + 100.0,
            var7.z + targetRange
         );
         List<RaidObjectiveEntity> list = level.getEntitiesOfClass(RaidObjectiveEntity.class, area, e -> e.isAlive() && !e.isRemoved());
         return list.isEmpty() ? null : list.get(0);
      } else {
         return null;
      }
   }

   private List<LivingEntity> getNearbyEntities() {
      AABB searchBox = new AABB(
         this.mob.getX() - this.maxHateRange,
         this.mob.getY() - this.maxHateRange,
         this.mob.getZ() - this.maxHateRange,
         this.mob.getX() + this.maxHateRange,
         this.mob.getY() + this.maxHateRange,
         this.mob.getZ() + this.maxHateRange
      );
      return this.mob.level().getEntitiesOfClass(LivingEntity.class, searchBox, this::isValidTarget);
   }

   private Entity findEntityById(UUID entityId) {
      return ((ServerLevel)this.mob.level()).getEntity(entityId);
   }

   public Map<UUID, Float> getHateMap() {
      Map<UUID, Float> result = new HashMap<>();

      for (Entry<UUID, HateTargetGoal.HateData> entry : this.hateMap.entrySet()) {
         result.put(entry.getKey(), entry.getValue().getHate());
      }

      return result;
   }

   public void clearHate() {
      this.hateMap.clear();
      this.currentTarget = null;
      this.mob.setTarget(null);
   }

   public void clearHate(LivingEntity entity) {
      if (entity != null) {
         this.hateMap.remove(entity.getUUID());
         if (this.currentTarget == entity) {
            this.currentTarget = null;
            this.mob.setTarget(null);
         }
      }
   }

   private static class HateData {
      private float hate = 0.0F;

      public HateData() {
      }

      public void addHate(float amount) {
         this.hate = Math.min(1000.0F, this.hate + amount);
      }

      public void setHate(float hate) {
         this.hate = Math.min(1000.0F, hate);
      }

      public void decay(float amount) {
         this.hate = Math.max(1.0F, this.hate - amount);
      }

      public float getHate() {
         return this.hate;
      }

      @Override
      public String toString() {
         return this.hate + "";
      }
   }
}
