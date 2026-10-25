package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BulletMachineEntity extends PomkotsCustomDecayedThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

   public BulletMachineEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      this(entityType, level, null, 20, BattleBalance.MECH_MACHINEGUN_LARGE_DAMAGE, 2);
   }

   public BulletMachineEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, float damage) {
      this(entityType, level, shooter, 20, damage, 1);
   }

   public BulletMachineEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, int maxLifeTicks, float damage, int stun) {
      super(entityType, level, shooter, maxLifeTicks, damage, stun);
      this.m_20242_(true);
      this.f_19811_ = true;
      this.f_19794_ = true;
      this.shooter = shooter;
      this.damage = damage;
   }

   @Override
   public void m_8119_() {
      super.m_8119_();
      this.f_19812_ = true;
   }

   @Override
   protected void m_5790_(EntityHitResult entityHitResult) {
      super.m_5790_(entityHitResult);
      this.m_146870_();
   }

   protected void m_8097_() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.bullet.idle")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   @Override
   public PomkotsCustomThrowableProjectile.RangeCategory calcRangeCategory(int distance) {
      return calcRangeCategoryStatic(distance);
   }

   @Override
   protected float getDamageModifier(PomkotsCustomThrowableProjectile.RangeCategory range) {
      return switch (range) {
         case OPTIMAL -> 1.0F;
         case EFFECTIVE -> 0.8F;
         case MAXIMUM -> 0.5F;
         case OUT -> 0.1F;
         default -> 1.0F;
      };
   }

   public static PomkotsCustomThrowableProjectile.RangeCategory calcRangeCategoryStatic(int distance) {
      if (distance < 50) {
         return PomkotsCustomThrowableProjectile.RangeCategory.OPTIMAL;
      } else if (distance < 80) {
         return PomkotsCustomThrowableProjectile.RangeCategory.EFFECTIVE;
      } else {
         return distance < 200 ? PomkotsCustomThrowableProjectile.RangeCategory.MAXIMUM : PomkotsCustomThrowableProjectile.RangeCategory.OUT;
      }
   }
}
