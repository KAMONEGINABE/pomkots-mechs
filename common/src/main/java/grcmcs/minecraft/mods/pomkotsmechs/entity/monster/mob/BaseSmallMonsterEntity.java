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
   private static final EntityDataAccessor<Boolean> CLOSED = SynchedEntityData.m_135353_(BaseSmallMonsterEntity.class, EntityDataSerializers.f_135035_);

   public static Builder createMobAttributes() {
      return LivingEntity.m_21183_().m_22266_(Attributes.f_22282_).m_22268_(Attributes.f_22278_, 1.0).m_22268_(Attributes.f_22277_, 100.0);
   }

   protected BaseSmallMonsterEntity(EntityType<? extends Monster> entityType, Level level) {
      super(entityType, level);
      this.m_274367_(this.getMechData().maxStepUp);
      this.m_7910_(this.getMechData().speed);
      this.m_20242_(false);
      this.m_146922_(0.0F);
   }

   @Override
   public void m_8119_() {
      if (this.f_19803_ && this.isInEvent()) {
         this.f_19811_ = true;
      }

      super.m_8119_();
      if (this.isServerSide() && this.isClosed()) {
         if (this.closedTick == 40) {
            this.fireCloseAnimation();
            this.closedTick--;
         }

         if (this.m_20096_()) {
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

   public boolean m_142535_(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   @Override
   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(CLOSED, false);
   }

   public void setClosed(boolean value) {
      if (value) {
         this.closedTick = 40;
      }

      this.f_19804_.m_135381_(CLOSED, value);
   }

   public boolean isClosed() {
      return (Boolean)this.f_19804_.m_135370_(CLOSED);
   }

   @Override
   public void m_7380_(CompoundTag compound) {
      super.m_7380_(compound);
   }

   @Override
   public void m_7378_(CompoundTag compound) {
      super.m_7378_(compound);
   }

   @Override
   protected void m_8099_() {
      super.m_8099_();
      this.registerBaseGoals();
   }

   protected void registerBaseGoals() {
      this.f_21346_
         .m_25352_(
            1, new RaidTargetGoal<RaidObjectiveEntity>(this, RaidObjectiveEntity.class, this.m_21051_(Attributes.f_22277_).m_22115_(), 200, this::isInRaid)
         );
      this.f_21346_.m_25352_(2, new HurtByTargetGoal(this, new Class[0]));
      this.f_21346_.m_25352_(3, new NearestAttackableTargetGoal(this, Player.class, true, this::predicate));
   }

   public boolean predicate(Object target) {
      if (target instanceof Player p) {
         boolean ridingMech = p.m_20202_() instanceof PomkotsVehicleBase;
         if (ridingMech) {
            return true;
         }

         if (p.m_20270_(this) <= this.m_21051_(Attributes.f_22277_).m_22115_() * 0.25) {
            double dx = p.m_20185_() - this.m_20185_();
            double dz = p.m_20189_() - this.m_20189_();
            Vec3 look = this.m_20154_();
            double dot = dx * look.f_82479_ + dz * look.f_82481_;
            if (dot > 0.0) {
               return p.m_21205_().m_150930_((Item)PomkotsMechs.CARTON.get()) ? p.m_20142_() : true;
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
