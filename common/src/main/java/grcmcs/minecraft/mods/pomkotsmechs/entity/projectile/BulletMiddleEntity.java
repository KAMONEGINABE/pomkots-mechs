package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BulletMiddleEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final float DAMAGE = 20.0F;
   private static final int MAX_LIFE_TICKS = 40;
   private int lifeTicks = 0;
   private LivingEntity shooter = null;
   private float damage;

   public BulletMiddleEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      this(entityType, world, null, 20.0F);
   }

   public BulletMiddleEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
      this(entityType, world, shooter, 20.0F);
   }

   public BulletMiddleEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, float damage) {
      super(entityType, shooter, world);
      this.damage = damage;
      this.shooter = shooter;
      this.m_20242_(true);
   }

   @Override
   public void m_8119_() {
      this.m_20242_(true);
      HitResult hitResult = ProjectileUtil.raycastBoundingCheck(this);
      if (hitResult.m_6662_() != Type.MISS) {
         this.m_6532_(hitResult);
      }

      super.m_8119_();
      this.f_19812_ = true;
      if (this.lifeTicks++ >= 40) {
         this.m_146870_();
      }
   }

   protected void m_5790_(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.m_82443_();
      if (!entity.equals(this.shooter)) {
         entity.m_6469_(entity.m_269291_().m_269390_(this, (Entity)(this.m_19749_() != null ? this.m_19749_() : this)), this.damage);
         entity.f_19802_ = 0;
         this.m_146870_();
      }
   }

   protected void m_8060_(BlockHitResult blockHitResult) {
      this.m_146870_();
   }

   protected void m_8097_() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.bulletmiddle.idle")))
         }
      );
   }

   protected void updateRotationBasedOnVelocity() {
      Vec3 velocity = this.m_20184_();
      if (!velocity.equals(Vec3.f_82478_)) {
         double yaw = Math.toDegrees(Math.atan2(velocity.f_82479_, velocity.f_82481_));
         this.m_146922_((float)yaw);
         double horizontalDistance = Math.sqrt(velocity.f_82479_ * velocity.f_82479_ + velocity.f_82481_ * velocity.f_82481_);
         double pitch = Math.toDegrees(Math.atan2(velocity.f_82480_, horizontalDistance));
         this.m_146926_((float)pitch);
         this.f_19859_ = this.m_146908_();
         this.f_19860_ = this.m_146909_();
      }
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
