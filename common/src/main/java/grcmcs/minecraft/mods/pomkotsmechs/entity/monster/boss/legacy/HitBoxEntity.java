package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.NonNullList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class HitBoxEntity extends LivingEntity {
   protected final LivingEntity parentEntity;
   protected Consumer<Void> breakCallback;
   private float damageCount = 39.0F;

   public static Builder createMobAttributes() {
      return LivingEntity.m_21183_().m_22266_(Attributes.f_22282_).m_22268_(Attributes.f_22276_, 50.0);
   }

   public HitBoxEntity(EntityType<? extends LivingEntity> entityType, Level world) {
      this(entityType, world, null);
   }

   public HitBoxEntity(EntityType<? extends LivingEntity> entityType, Level world, LivingEntity parent) {
      super(entityType, world);
      this.parentEntity = parent;
      this.f_19794_ = true;
      this.m_20242_(true);
   }

   public void setBreakCallback(Consumer<Void> breakCallback) {
      this.breakCallback = breakCallback;
   }

   public void m_8119_() {
      super.m_8119_();
      if (!this.m_9236_().f_46443_) {
         AABB bossBoundingBox = this.m_20191_();
         List<PomkotsThrowableProjectile> projectiles = this.m_9236_()
            .m_6443_(PomkotsThrowableProjectile.class, bossBoundingBox.m_82400_(1.0), projectilex -> !projectilex.m_213877_());
         new ArrayList();

         for (PomkotsThrowableProjectile projectile : projectiles) {
            if (bossBoundingBox.m_82381_(projectile.m_20191_())) {
               projectile.onHitEntityPublic(this);
            }
         }
      }

      if (!this.m_9236_().f_46443_ && !this.f_19803_ && !this.isParentActive()) {
         this.m_6074_();
      }
   }

   protected boolean isParentActive() {
      return this.parentEntity != null && !this.parentEntity.m_21224_();
   }

   public boolean m_6469_(DamageSource source, float amount) {
      if (this.isParentActive()) {
         if (this.damageCount < 0.0F) {
            return this.parentEntity.m_6469_(source, amount);
         }

         if (source.m_276093_(DamageTypes.f_268464_)) {
            this.damageCount -= amount;
            if (this.damageCount < 0.0F && this.breakCallback != null) {
               this.breakCallback.accept(null);
            }
         }

         this.parentEntity.f_19802_ = 0;
         return this.parentEntity.m_6469_(source, amount * 0.5F);
      } else {
         return super.m_6469_(source, amount);
      }
   }

   public boolean m_6051_() {
      return false;
   }

   public Iterable<ItemStack> m_6168_() {
      return NonNullList.m_122780_(4, ItemStack.f_41583_);
   }

   public ItemStack m_6844_(EquipmentSlot equipmentSlot) {
      return ItemStack.f_41583_;
   }

   public void m_8061_(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public HumanoidArm m_5737_() {
      return null;
   }
}
