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

public class BulletBeamEntity extends PomkotsCustomDecayedThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private float explosionScale = 5.0F;

   public BulletBeamEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      this(entityType, level, null, 20, BattleBalance.MECH_BEAM_DAMAGE, 10);
   }

   public BulletBeamEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, float damage) {
      this(entityType, level, shooter, 20, damage, 10);
   }

   public BulletBeamEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, int maxLifeTicks, float damage, int stun) {
      super(entityType, level, shooter, maxLifeTicks, damage, stun);
      this.setNoGravity(true);
      this.noCulling = true;
      this.noPhysics = true;
      this.shooter = shooter;
      this.damage = damage;
   }

   public boolean shouldRender(double a, double b, double c) {
      return true;
   }

   public boolean shouldRenderAtSqrDistance(double e) {
      return true;
   }

   @Override
   public void tick() {
      super.tick();
      this.hasImpulse = true;
   }

   @Override
   protected void onHitEntity(EntityHitResult entityHitResult) {
      super.onHitEntity(entityHitResult);
      this.discard();
   }

   protected void defineSynchedData() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.bulletbeam.idle")))
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
      if (distance < 80) {
         return PomkotsCustomThrowableProjectile.RangeCategory.OPTIMAL;
      } else if (distance < 150) {
         return PomkotsCustomThrowableProjectile.RangeCategory.EFFECTIVE;
      } else {
         return distance < 200 ? PomkotsCustomThrowableProjectile.RangeCategory.MAXIMUM : PomkotsCustomThrowableProjectile.RangeCategory.OUT;
      }
   }
}
