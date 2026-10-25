package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidObjectiveEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.RaidTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class BaseSmallMonsterEntity extends GenericPomkotsMonster {
   private int closedTick = 0;
   private static final EntityDataAccessor<Boolean> CLOSED = SynchedEntityData.defineId(BaseSmallMonsterEntity.class, EntityDataSerializers.BOOLEAN);

   public static Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_KNOCKBACK).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.FOLLOW_RANGE, 100.0);
   }

   protected BaseSmallMonsterEntity(EntityType<? extends Monster> entityType, Level level) {
      super(entityType, level);
      this.setMaxUpStep(this.getMechData().maxStepUp);
      this.setSpeed(this.getMechData().speed);
      this.setNoGravity(false);
      this.setYRot(0.0F);
   }

   @Override
   public void tick() {
      if (this.firstTick && this.isInEvent()) {
         this.noCulling = true;
      }

      super.tick();
      if (this.isServerSide() && this.isClosed()) {
         if (this.closedTick == 40) {
            this.fireCloseAnimation();
            this.closedTick--;
         }

         if (this.onGround()) {
            if (this.closedTick > 0) {
               this.closedTick--;
            }

            if (this.closedTick == 20) {
               this.fireOpenAnimation();
            } else if (this.closedTick <= 0) {
               this.setClosed(false);
            }
         }
      }
   }

   protected abstract void fireOpenAnimation();

   protected abstract void fireCloseAnimation();

   public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(CLOSED, false);
   }

   public void setClosed(boolean value) {
      if (value) {
         this.closedTick = 40;
      }

      this.entityData.set(CLOSED, value);
   }

   public boolean isClosed() {
      return (Boolean)this.entityData.get(CLOSED);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.registerBaseGoals();
   }

   protected void registerBaseGoals() {
      this.targetSelector
         .addGoal(
            1, new RaidTargetGoal<RaidObjectiveEntity>(this, RaidObjectiveEntity.class, this.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue(), 200, this::isInRaid)
         );
      this.targetSelector.addGoal(2, new HurtByTargetGoal(this, new Class[0]));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Player.class, true, this::predicate));
   }

   public boolean predicate(Object target) {
      if (target instanceof Player p) {
         boolean ridingMech = p.getVehicle() instanceof PomkotsVehicleBase;
         if (ridingMech) {
            return true;
         }

         if (p.distanceTo(this) <= this.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue() * 0.25) {
            double dx = p.getX() - this.getX();
            double dz = p.getZ() - this.getZ();
            Vec3 look = this.getLookAngle();
            double dot = dx * look.x + dz * look.z;
            if (dot > 0.0) {
               return p.getMainHandItem().is((Item)PomkotsMechs.CARTON.get()) ? p.isSprinting() : true;
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
