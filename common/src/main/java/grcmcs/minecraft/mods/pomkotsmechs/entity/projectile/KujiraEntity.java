package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KujiraEntity extends LivingEntity implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 300;
   private int lifeTicks = 0;

   public static Builder createMobAttributes() {
      return LivingEntity.m_21183_().m_22266_(Attributes.f_22282_).m_22268_(Attributes.f_22276_, 50.0);
   }

   public KujiraEntity(EntityType<? extends KujiraEntity> entityType, Level world) {
      super(entityType, world);
      this.m_20242_(true);
      this.f_19811_ = true;
   }

   public void m_8119_() {
      this.m_20242_(true);
      super.m_8119_();
      if (!this.m_9236_().f_46443_) {
         this.f_19812_ = true;
      }

      if (this.lifeTicks++ >= 300) {
         this.m_146870_();
      }
   }

   private void generateLava() {
      if (this.m_9236_().f_46443_) {
         RandomSource random = this.m_9236_().m_213780_();

         for (int i = 0; i < 20; i++) {
            double velocityX = random.m_188500_() * 2.0 - 1.0;
            double velocityY = random.m_188500_() * 2.0 - 1.0;
            double velocityZ = random.m_188500_() * 2.0 - 1.0;
            this.m_9236_().m_7106_(ParticleTypes.f_123756_, this.m_20185_(), this.m_20186_(), this.m_20189_(), velocityX, velocityY, velocityZ);
         }
      }
   }

   protected void m_8097_() {
      super.m_8097_();
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.kujira.happy")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public boolean m_6000_(double cameraX, double cameraY, double cameraZ) {
      double maxDistance = 1000.0;
      return true;
   }

   public boolean m_6783_(double d) {
      return true;
   }

   public Iterable<ItemStack> m_6168_() {
      return NonNullList.m_122780_(4, ItemStack.f_41583_);
   }

   public ItemStack m_6844_(EquipmentSlot equipmentSlot) {
      return ItemStack.f_41583_;
   }

   public void m_8061_(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public boolean m_142535_(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   public HumanoidArm m_5737_() {
      return null;
   }
}
