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
      return LivingEntity.m_21183_().m_22266_(Attributes.f_22282_).m_22268_(Attributes.f_22276_, 50.0);
   }

   public PresentBoxEntity(EntityType<? extends PresentBoxEntity> type, Level level) {
      super(type, level);
      this.m_20242_(false);
   }

   public boolean m_6094_() {
      return false;
   }

   public void m_8119_() {
      super.m_8119_();
      this.f_19812_ = true;
      if (this.m_20096_() && this.openCount == 0) {
         this.openCount = 1;
      }

      if (this.openCount > 0) {
         this.openCount++;
         if (this.openCount > 80) {
            this.openCount = -1;
            if (!this.m_9236_().f_46443_) {
               KujiraEntity blockMass = new KujiraEntity((EntityType<? extends KujiraEntity>)PomkotsMechs.KUJIRA.get(), this.m_9236_());
               float rot = this.m_146908_();
               blockMass.m_146922_(rot);
               blockMass.m_5618_(rot);
               blockMass.m_5616_(rot);
               blockMass.f_19859_ = rot;
               blockMass.m_6034_(this.m_20185_(), this.m_20186_(), this.m_20189_());
               this.m_9236_().m_7967_(blockMass);
            }
         }
      }
   }

   public InteractionResult m_6096_(Player p, InteractionHand h) {
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

   public boolean m_6000_(double cameraX, double cameraY, double cameraZ) {
      double maxDistance = 1000.0;
      return true;
   }

   public boolean m_6783_(double d) {
      return true;
   }

   protected void m_8097_() {
      super.m_8097_();
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
