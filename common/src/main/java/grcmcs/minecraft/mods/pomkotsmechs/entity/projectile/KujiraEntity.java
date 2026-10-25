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
      return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_KNOCKBACK).add(Attributes.MAX_HEALTH, 50.0);
   }

   public KujiraEntity(EntityType<? extends KujiraEntity> entityType, Level world) {
      super(entityType, world);
      this.setNoGravity(true);
      this.noCulling = true;
   }

   public void tick() {
      this.setNoGravity(true);
      super.tick();
      if (!this.level().isClientSide) {
         this.hasImpulse = true;
      }

      if (this.lifeTicks++ >= 300) {
         this.discard();
      }
   }

   private void generateLava() {
      if (this.level().isClientSide) {
         RandomSource random = this.level().getRandom();

         for (int i = 0; i < 20; i++) {
            double velocityX = random.nextDouble() * 2.0 - 1.0;
            double velocityY = random.nextDouble() * 2.0 - 1.0;
            double velocityZ = random.nextDouble() * 2.0 - 1.0;
            this.level().addParticle(ParticleTypes.LAVA, this.getX(), this.getY(), this.getZ(), velocityX, velocityY, velocityZ);
         }
      }
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
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

   public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
      double maxDistance = 1000.0;
      return true;
   }

   public boolean shouldRenderAtSqrDistance(double d) {
      return true;
   }

   public Iterable<ItemStack> getArmorSlots() {
      return NonNullList.withSize(4, ItemStack.EMPTY);
   }

   public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
      return ItemStack.EMPTY;
   }

   public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   public HumanoidArm getMainArm() {
      return null;
   }
}
