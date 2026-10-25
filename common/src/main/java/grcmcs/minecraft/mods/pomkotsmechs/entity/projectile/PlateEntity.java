package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PlateEntity extends Mob implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 300;
   private int breakTicks = 0;

   public static Builder createMobAttributes() {
      return LivingEntity.m_21183_()
         .m_22266_(Attributes.f_22282_)
         .m_22268_(Attributes.f_22278_, 1.0)
         .m_22268_(Attributes.f_22277_, 200.0)
         .m_22268_(Attributes.f_22276_, 300.0);
   }

   public PlateEntity(EntityType<? extends PlateEntity> entityType, Level world) {
      super(entityType, world);
      this.m_20242_(true);
      this.f_19811_ = true;
   }

   public void m_8119_() {
      if (!this.m_9236_().f_46443_) {
      }

      super.m_8119_();
      if (!this.m_9236_().f_46443_) {
         if (this.breakTicks > 0) {
            if (this.breakTicks++ >= 300) {
               this.m_146870_();
            }

            if (!this.m_20068_()) {
               this.m_5997_(0.0, -0.17640000343322754, 0.0);
            }
         } else {
            if (this.m_9236_() instanceof ServerLevel serverLevel) {
               moveEntityToNearestAir(serverLevel, this, 2);
            }

            snapYawTo90(this);
         }
      }
   }

   public static void snapYawTo90(Entity entity) {
      float yaw = entity.m_146908_();
      yaw = Mth.m_14177_(yaw);
      float snappedYaw = Math.round(yaw / 90.0F) * 90.0F;
      snappedYaw = Mth.m_14177_(snappedYaw);
      entity.m_146922_(snappedYaw);
      entity.m_5618_(snappedYaw);
      entity.m_5616_(snappedYaw);
      entity.f_19859_ = snappedYaw;
   }

   public static boolean moveEntityToNearestAir(ServerLevel level, Entity entity, int maxRadius) {
      if (level.m_45786_(entity)) {
         return false;
      }

      BlockPos origin = entity.m_20183_();
      AABB box = entity.m_20191_();

      for (int r = 1; r <= maxRadius; r++) {
         for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
               for (int dz = -r; dz <= r; dz++) {
                  if (dx * dx + dy * dy + dz * dz <= r * r) {
                     BlockPos pos = origin.m_7918_(dx, dy, dz);
                     double x = pos.m_123341_() + 0.5;
                     double y = pos.m_123342_();
                     double z = pos.m_123343_() + 0.5;
                     AABB movedBox = box.m_82386_(x - entity.m_20185_(), y - entity.m_20186_(), z - entity.m_20189_());
                     if (level.m_45756_(entity, movedBox)) {
                        entity.m_6021_(x, y, z);
                        entity.m_20256_(Vec3.f_82478_);
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   public boolean m_6469_(DamageSource source, float amount) {
      if (!this.m_9236_().f_46443_ && this.m_9236_() instanceof ServerLevel serverLevel && this.breakTicks == 0) {
         this.triggerAnim("action_controller", "break");
         this.breakTicks = 1;
         moveEntityToNearestAir(serverLevel, this, 2);
         this.m_6853_(false);
         this.m_20242_(false);
         Vec3 v = this.m_20184_();
         if (v.f_82480_ >= 0.0) {
            this.m_20334_(v.f_82479_, -0.08, v.f_82481_);
         }
      }

      return super.m_6469_(source, amount);
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "action_controller", state -> PlayState.STOP)
               .triggerableAnim("break", RawAnimation.begin().thenPlayAndHold("animation.bossbox.break"))
         }
      );
   }

   public boolean m_21532_() {
      return true;
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public boolean m_6128_() {
      return false;
   }

   public boolean m_6000_(double cameraX, double cameraY, double cameraZ) {
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

   public boolean m_6094_() {
      return false;
   }

   protected void m_20101_() {
   }
}
