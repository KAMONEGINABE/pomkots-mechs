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
      this.setNoGravity(true);
   }

   @Override
   public void tick() {
      this.setNoGravity(true);
      HitResult hitResult = ProjectileUtil.raycastBoundingCheck(this);
      if (hitResult.getType() != Type.MISS) {
         this.onHit(hitResult);
      }

      super.tick();
      this.hasImpulse = true;
      if (this.lifeTicks++ >= 40) {
         this.discard();
      }
   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.getEntity();
      if (!entity.equals(this.shooter)) {
         entity.hurt(entity.damageSources().thrown(this, (Entity)(this.getOwner() != null ? this.getOwner() : this)), this.damage);
         entity.invulnerableTime = 0;
         this.discard();
      }
   }

   protected void onHitBlock(BlockHitResult blockHitResult) {
      this.discard();
   }

   protected void defineSynchedData() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.bulletmiddle.idle")))
         }
      );
   }

   protected void updateRotationBasedOnVelocity() {
      Vec3 velocity = this.getDeltaMovement();
      if (!velocity.equals(Vec3.ZERO)) {
         double yaw = Math.toDegrees(Math.atan2(velocity.x, velocity.z));
         this.setYRot((float)yaw);
         double horizontalDistance = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
         double pitch = Math.toDegrees(Math.atan2(velocity.y, horizontalDistance));
         this.setXRot((float)pitch);
         this.yRotO = this.getYRot();
         this.xRotO = this.getXRot();
      }
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
