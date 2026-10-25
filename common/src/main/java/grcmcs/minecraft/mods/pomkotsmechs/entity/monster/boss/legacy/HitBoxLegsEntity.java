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
      return m_21183_().m_22266_(Attributes.f_22282_).m_22268_(Attributes.f_22276_, 50.0);
   }

   public HitBoxLegsEntity(EntityType<? extends LivingEntity> entityType, Level world) {
      this(entityType, world, null);
   }

   public HitBoxLegsEntity(EntityType<? extends LivingEntity> entityType, Level world, LivingEntity parent) {
      super(entityType, world, parent);
   }

   @Override
   public boolean m_6469_(DamageSource source, float amount) {
      if (this.isParentActive()) {
         this.damageCount -= amount;
         if (this.damageCount < 0.0F && this.breakCallback != null) {
            this.breakCallback.accept(null);
            this.damageCount = 100.0F;
         }

         return this.parentEntity.m_6469_(source, amount * 0.5F);
      } else {
         return super.m_6469_(source, amount);
      }
   }

   public InteractionResult m_6096_(Player player, InteractionHand hand) {
      return this.isParentActive() ? this.parentEntity.m_6096_(player, hand) : super.m_6096_(player, hand);
   }
}
