package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class HitBoxLegsEntity extends HitBoxEntity {
   private float damageCount = 100.0F;

   public static Builder createMobAttributes() {
      return createLivingAttributes().add(Attributes.ATTACK_KNOCKBACK).add(Attributes.MAX_HEALTH, 50.0);
   }

   public HitBoxLegsEntity(EntityType<? extends LivingEntity> entityType, Level world) {
      this(entityType, world, null);
   }

   public HitBoxLegsEntity(EntityType<? extends LivingEntity> entityType, Level world, LivingEntity parent) {
      super(entityType, world, parent);
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (this.isParentActive()) {
         this.damageCount -= amount;
         if (this.damageCount < 0.0F && this.breakCallback != null) {
            this.breakCallback.accept(null);
            this.damageCount = 100.0F;
         }

         return this.parentEntity.hurt(source, amount * 0.5F);
      } else {
         return super.hurt(source, amount);
      }
   }

   public InteractionResult interact(Player player, InteractionHand hand) {
      return this.isParentActive() ? this.parentEntity.interact(player, hand) : super.interact(player, hand);
   }
}
