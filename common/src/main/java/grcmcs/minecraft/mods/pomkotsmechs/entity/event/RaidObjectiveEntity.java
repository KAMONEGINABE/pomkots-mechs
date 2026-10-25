package grcmcs.minecraft.mods.pomkotsmechs.entity.event;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RaidObjectiveEntity extends LivingEntity {
   protected final BaseBossEntity parentEntity;

   public static Builder createMobAttributes() {
      return LivingEntity.m_21183_()
         .m_22266_(Attributes.f_22282_)
         .m_22268_(Attributes.f_22278_, 1.0)
         .m_22268_(Attributes.f_22276_, 500.0)
         .m_22268_(Attributes.f_22284_, 20.0)
         .m_22268_(Attributes.f_22285_, 1.0);
   }

   public RaidObjectiveEntity(EntityType<? extends LivingEntity> entityType, Level world) {
      this(entityType, world, null);
   }

   public RaidObjectiveEntity(EntityType<? extends LivingEntity> entityType, Level world, BaseBossEntity parent) {
      super(entityType, world);
      this.parentEntity = parent;
      this.f_19794_ = true;
      this.m_20242_(true);
   }

   public void m_8119_() {
      super.m_8119_();
   }

   public boolean m_6469_(DamageSource source, float amount) {
      return super.m_6469_(source, amount / 5.0F);
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

   public boolean m_6128_() {
      return true;
   }

   public HumanoidArm m_5737_() {
      return null;
   }
}
