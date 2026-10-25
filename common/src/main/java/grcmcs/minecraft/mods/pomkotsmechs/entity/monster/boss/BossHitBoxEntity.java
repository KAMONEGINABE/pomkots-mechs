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
      return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_KNOCKBACK).add(Attributes.MAX_HEALTH, 50.0);
   }

   public BossHitBoxEntity(EntityType<? extends LivingEntity> entityType, Level world) {
      this(entityType, world, null);
   }

   public BossHitBoxEntity(EntityType<? extends LivingEntity> entityType, Level world, BaseBossEntity parent) {
      super(entityType, world);
      this.parentEntity = parent;
      this.noPhysics = true;
      this.setNoGravity(true);
   }

   public void setBreakCallback(Consumer<Void> breakCallback) {
      this.breakCallback = breakCallback;
   }

   public Vec3 getRelativeParentPos() {
      return Vec3.ZERO;
   }

   public void tick() {
      super.tick();
      if (!this.isParentActive() || this.parentEntity.getAiMode() != -2) {
         if (!this.level().isClientSide) {
            AABB bossBoundingBox = this.getBoundingBox();
            List<PomkotsThrowableProjectile> projectiles = this.level()
               .getEntitiesOfClass(PomkotsThrowableProjectile.class, bossBoundingBox.inflate(1.0), projectilex -> !projectilex.isRemoved());
            new ArrayList();

            for (PomkotsThrowableProjectile projectile : projectiles) {
               if (!this.isRelatedShooter(projectile) && bossBoundingBox.intersects(projectile.getBoundingBox())) {
                  projectile.onHitEntityPublic(this);
               }
            }
         }

         if (!this.level().isClientSide && !this.firstTick && !this.isParentActive()) {
            this.discard();
         }
      }
   }

   public boolean isRelatedShooter(PomkotsThrowableProjectile projectile) {
      return this.parentEntity != null && this.parentEntity.equals(projectile.getShooter());
   }

   protected boolean isParentActive() {
      return this.parentEntity != null && !this.parentEntity.isDeadOrDying();
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.isParentActive()) {
         this.parentEntity.invulnerableTime = 0;
         this.invulnerableTime = 0;
         return this.parentEntity.hurtFromAdditionalHitBox(source, amount);
      } else {
         return Utils.isSystemicDamage(source) ? super.hurt(source, amount) : false;
      }
   }

   public void addStunPoint(int point) {
      if (this.isParentActive()) {
         this.parentEntity.addStunPoint(point);
      }
   }

   public boolean displayFireAnimation() {
      return false;
   }

   public Iterable<ItemStack> getArmorSlots() {
      return NonNullList.withSize(4, ItemStack.EMPTY);
   }

   public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
      return ItemStack.EMPTY;
   }

   public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public HumanoidArm getMainArm() {
      return null;
   }
}
