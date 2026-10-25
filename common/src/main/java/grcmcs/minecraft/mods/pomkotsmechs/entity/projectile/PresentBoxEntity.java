package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PresentBoxEntity extends LivingEntity implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int SIZE = 1;
   private int openCount = 0;

   public static Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_KNOCKBACK).add(Attributes.MAX_HEALTH, 50.0);
   }

   public PresentBoxEntity(EntityType<? extends PresentBoxEntity> type, Level level) {
      super(type, level);
      this.setNoGravity(false);
   }

   public boolean isPushable() {
      return false;
   }

   public void tick() {
      super.tick();
      this.hasImpulse = true;
      if (this.onGround() && this.openCount == 0) {
         this.openCount = 1;
      }

      if (this.openCount > 0) {
         this.openCount++;
         if (this.openCount > 80) {
            this.openCount = -1;
            if (!this.level().isClientSide) {
               KujiraEntity blockMass = new KujiraEntity((EntityType<? extends KujiraEntity>)PomkotsMechs.KUJIRA.get(), this.level());
               float rot = this.getYRot();
               blockMass.setYRot(rot);
               blockMass.setYBodyRot(rot);
               blockMass.setYHeadRot(rot);
               blockMass.yRotO = rot;
               blockMass.setPos(this.getX(), this.getY(), this.getZ());
               this.level().addFreshEntity(blockMass);
            }
         }
      }
   }

   public InteractionResult interact(Player p, InteractionHand h) {
      this.openCount = 1;
      return InteractionResult.SUCCESS;
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               "rotation",
               0,
               event -> {
                  if (this.openCount > 0) {
                     return event.setAndContinue(RawAnimation.begin().thenLoop("animation.presentbox.open1"));
                  } else {
                     return this.openCount < 0
                        ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.presentbox.open2"))
                        : event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.presentbox.idle"));
                  }
               }
            )
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

   protected void defineSynchedData() {
      super.defineSynchedData();
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
