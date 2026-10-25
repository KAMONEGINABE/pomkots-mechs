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
      return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_KNOCKBACK).add(Attributes.MAX_HEALTH, 50.0);
   }

   public HitBoxEntity(EntityType<? extends LivingEntity> entityType, Level world) {
      this(entityType, world, null);
   }

   public HitBoxEntity(EntityType<? extends LivingEntity> entityType, Level world, LivingEntity parent) {
      super(entityType, world);
      this.parentEntity = parent;
      this.noPhysics = true;
      this.setNoGravity(true);
   }

   public void setBreakCallback(Consumer<Void> breakCallback) {
      this.breakCallback = breakCallback;
   }

   public void tick() {
      super.tick();
      if (!this.level().isClientSide) {
         AABB bossBoundingBox = this.getBoundingBox();
         List<PomkotsThrowableProjectile> projectiles = this.level()
            .getEntitiesOfClass(PomkotsThrowableProjectile.class, bossBoundingBox.inflate(1.0), projectilex -> !projectilex.isRemoved());
         new ArrayList();

         for (PomkotsThrowableProjectile projectile : projectiles) {
            if (bossBoundingBox.intersects(projectile.getBoundingBox())) {
               projectile.onHitEntityPublic(this);
            }
         }
      }

      if (!this.level().isClientSide && !this.firstTick && !this.isParentActive()) {
         this.kill();
      }
   }

   protected boolean isParentActive() {
      return this.parentEntity != null && !this.parentEntity.isDeadOrDying();
   }

   public boolean hurt(DamageSource source, float amount) {
      if (this.isParentActive()) {
         if (this.damageCount < 0.0F) {
            return this.parentEntity.hurt(source, amount);
         }

         if (source.is(DamageTypes.PLAYER_ATTACK)) {
            this.damageCount -= amount;
            if (this.damageCount < 0.0F && this.breakCallback != null) {
               this.breakCallback.accept(null);
            }
         }

         this.parentEntity.invulnerableTime = 0;
         return this.parentEntity.hurt(source, amount * 0.5F);
      } else {
         return super.hurt(source, amount);
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
