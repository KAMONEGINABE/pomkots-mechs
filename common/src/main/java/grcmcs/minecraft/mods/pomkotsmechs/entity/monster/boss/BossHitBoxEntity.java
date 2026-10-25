package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BossHitBoxEntity extends LivingEntity {
   protected final BaseBossEntity parentEntity;
   protected Consumer<Void> breakCallback;

   public static Builder createMobAttributes() {
      return LivingEntity.m_21183_().m_22266_(Attributes.f_22282_).m_22268_(Attributes.f_22276_, 50.0);
   }

   public BossHitBoxEntity(EntityType<? extends LivingEntity> entityType, Level world) {
      this(entityType, world, null);
   }

   public BossHitBoxEntity(EntityType<? extends LivingEntity> entityType, Level world, BaseBossEntity parent) {
      super(entityType, world);
      this.parentEntity = parent;
      this.f_19794_ = true;
      this.m_20242_(true);
   }

   public void setBreakCallback(Consumer<Void> breakCallback) {
      this.breakCallback = breakCallback;
   }

   public Vec3 getRelativeParentPos() {
      return Vec3.f_82478_;
   }

   public void m_8119_() {
      super.m_8119_();
      if (!this.isParentActive() || this.parentEntity.getAiMode() != -2) {
         if (!this.m_9236_().f_46443_) {
            AABB bossBoundingBox = this.m_20191_();
            List<PomkotsThrowableProjectile> projectiles = this.m_9236_()
               .m_6443_(PomkotsThrowableProjectile.class, bossBoundingBox.m_82400_(1.0), projectilex -> !projectilex.m_213877_());
            new ArrayList();

            for (PomkotsThrowableProjectile projectile : projectiles) {
               if (!this.isRelatedShooter(projectile) && bossBoundingBox.m_82381_(projectile.m_20191_())) {
                  projectile.onHitEntityPublic(this);
               }
            }
         }

         if (!this.m_9236_().f_46443_ && !this.f_19803_ && !this.isParentActive()) {
            this.m_146870_();
         }
      }
   }

   public boolean isRelatedShooter(PomkotsThrowableProjectile projectile) {
      return this.parentEntity != null && this.parentEntity.equals(projectile.getShooter());
   }

   protected boolean isParentActive() {
      return this.parentEntity != null && !this.parentEntity.m_21224_();
   }

   public boolean m_6469_(DamageSource source, float amount) {
      if (this.isParentActive()) {
         this.parentEntity.f_19802_ = 0;
         this.f_19802_ = 0;
         return this.parentEntity.hurtFromAdditionalHitBox(source, amount);
      } else {
         return Utils.isSystemicDamage(source) ? super.m_6469_(source, amount) : false;
      }
   }

   public void addStunPoint(int point) {
      if (this.isParentActive()) {
         this.parentEntity.addStunPoint(point);
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
