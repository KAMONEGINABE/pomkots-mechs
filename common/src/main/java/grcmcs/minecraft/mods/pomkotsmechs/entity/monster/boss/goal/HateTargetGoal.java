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
      this.m_7021_(EnumSet.of(Flag.TARGET));
   }

   public boolean m_8036_() {
      this.updateHateValues();
      LivingEntity newTarget = this.selectHighestHateTarget();
      if (newTarget != this.currentTarget) {
         this.currentTarget = newTarget;
         this.mob.m_6710_(this.currentTarget);
      }

      return this.currentTarget != null;
   }

   public boolean m_8045_() {
      if (--this.recheckDelay <= 0) {
         this.recheckDelay = 20;
         this.updateHateValues();
         LivingEntity newTarget = this.selectHighestHateTarget();
         if (newTarget != this.currentTarget) {
            this.currentTarget = newTarget;
            this.mob.m_6710_(this.currentTarget);
         }
      }

      return this.currentTarget != null && this.currentTarget.m_6084_() && this.mob.m_20280_(this.currentTarget) <= this.maxHateRange * this.maxHateRange;
   }

   public void m_8056_() {
      this.recheckDelay = 20;
   }

   public void m_8041_() {
      this.currentTarget = null;
      this.mob.m_6710_(null);
   }

   public void addDamageHate(LivingEntity entity, float damage) {
      if (entity != null && this.isValidTarget(entity)) {
         UUID entityId = entity.m_20148_();
         HateTargetGoal.HateData hateData = this.hateMap.computeIfAbsent(entityId, k -> new HateTargetGoal.HateData());
         hateData.addHate(damage * 2.0F);
      }
   }

   public void addCustomHate(LivingEntity entity, float hate) {
      if (entity != null && this.isValidTarget(entity)) {
         UUID entityId = entity.m_20148_();
         HateTargetGoal.HateData hateData = this.hateMap.computeIfAbsent(entityId, k -> new HateTargetGoal.HateData());
         hateData.addHate(hate);
      }
   }

   public void setHate(LivingEntity entity, float hate) {
      if (entity != null && this.isValidTarget(entity)) {
         UUID entityId = entity.m_20148_();
         HateTargetGoal.HateData hateData = this.hateMap.computeIfAbsent(entityId, k -> new HateTargetGoal.HateData());
         hateData.setHate(hate);
      }
   }

   public float getHate(LivingEntity entity) {
      if (entity == null) {
         return 0.0F;
      }

      HateTargetGoal.HateData hateData = this.hateMap.get(entity.m_20148_());
      return hateData != null ? hateData.getHate() : 0.0F;
   }

   private boolean isValidTarget(LivingEntity entity) {
      if (entity == null || !entity.m_6084_()) {
         return false;
      }

      if (this.mob.isSelf(entity)) {
         return false;
      }

      if (entity instanceof BaseBossEntity && !(entity instanceof Pmb99Entity)) {
         return false;
      }

      Team mobTeam = this.mob.m_5647_();
      Team entityTeam = entity.m_5647_();
      if (mobTeam != null && entityTeam != null) {
         if (mobTeam.equals(entityTeam)) {
            return false;
         }

         if (mobTeam.m_83536_(entityTeam)) {
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
            double distance = this.mob.m_20280_(entity);
            if (!le.m_6084_()) {
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
            && le.m_6084_()
            && this.isValidTarget(le)
            && this.mob.m_20280_(le) <= this.maxHateRange * this.maxHateRange) {
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
      double targetRange = this.mob.m_21051_(Attributes.f_22277_).m_22115_();
      if (this.mob.m_9236_() instanceof ServerLevel level) {
         Vec3 var7 = this.mob.m_20182_();
         AABB area = new AABB(
            var7.f_82479_ - targetRange,
            var7.f_82480_ - 100.0,
            var7.f_82481_ - targetRange,
            var7.f_82479_ + targetRange,
            var7.f_82480_ + 100.0,
            var7.f_82481_ + targetRange
         );
         List<RaidObjectiveEntity> list = level.m_6443_(RaidObjectiveEntity.class, area, e -> e.m_6084_() && !e.m_213877_());
         return list.isEmpty() ? null : list.get(0);
      } else {
         return null;
      }
   }

   private List<LivingEntity> getNearbyEntities() {
      AABB searchBox = new AABB(
         this.mob.m_20185_() - this.maxHateRange,
         this.mob.m_20186_() - this.maxHateRange,
         this.mob.m_20189_() - this.maxHateRange,
         this.mob.m_20185_() + this.maxHateRange,
         this.mob.m_20186_() + this.maxHateRange,
         this.mob.m_20189_() + this.maxHateRange
      );
      return this.mob.m_9236_().m_6443_(LivingEntity.class, searchBox, this::isValidTarget);
   }

   private Entity findEntityById(UUID entityId) {
      return ((ServerLevel)this.mob.m_9236_()).m_8791_(entityId);
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
      this.mob.m_6710_(null);
   }

   public void clearHate(LivingEntity entity) {
      if (entity != null) {
         this.hateMap.remove(entity.m_20148_());
         if (this.currentTarget == entity) {
            this.currentTarget = null;
            this.mob.m_6710_(null);
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
