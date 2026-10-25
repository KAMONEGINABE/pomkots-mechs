package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.function.Predicate;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class MissileBaseEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
   protected final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   protected int lifeTicks = 0;
   protected LivingEntity shooter = null;
   protected LivingEntity target = null;
   protected double firstYaw = -999.0;

   public MissileBaseEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      this(entityType, world, null, null);
   }

   public MissileBaseEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target) {
      super(entityType, shooter, world);
      this.setNoGravity(true);
      this.shooter = shooter;
      this.target = target;
      this.noCulling = true;
   }

   protected abstract int getSwitchTick();

   protected abstract double getSpeed();

   protected abstract int getSeekRange();

   protected abstract float getMaxRotationAnglePerTick();

   protected abstract float getDamage();

   protected abstract int getMaxLifeTicks();

   protected abstract Vec3 getNonTargetVelocity();

   protected abstract Class<? extends Entity> getTargetClass();

   public float getScale() {
      return 1.0F;
   }

   @Override
   public void tick() {
      if (this.level().isClientSide()) {
         this.generateSmokeParticles();
      }

      super.tick();
      if (this.firstYaw == -999.0) {
         this.firstYaw = -1.0 * this.getYRot();
      }

      if (this.lifeTicks++ >= this.getMaxLifeTicks()) {
         this.createExplosion(this.position());
         this.discard();
      }

      if (!this.level().isClientSide) {
         if (this.lifeTicks <= this.getSwitchTick()) {
            this.setDeltaMovement(this.getDeltaMovement());
         } else {
            if (this.target == null) {
               this.target = this.findTarget();
            }

            if (this.target != null && !this.target.isDeadOrDying()) {
               this.updateHomingMovement();
            } else {
               Vec3 vel = this.getNonTargetVelocity();
               this.setDeltaMovement(vel);
            }
         }
      }

      this.updateRotationBasedOnVelocity();
   }

   protected LivingEntity findTarget() {
      int range = this.getSeekRange();

      for (Entity candidate : this.level()
         .getEntitiesOfClass(
            this.getTargetClass(),
            new AABB(
               this.getX() - range,
               this.getY() - range,
               this.getZ() - range,
               this.getX() + range,
               this.getY() + range,
               this.getZ() + range
            ),
            new MissileBaseEntity.MissileTargetPredicate()
         )) {
         BlockHitResult res = this.level().clip(new ClipContext(this.position(), candidate.position(), Block.OUTLINE, Fluid.NONE, this));
         if (res.getType().equals(Type.MISS)) {
            return (LivingEntity)candidate;
         }
      }

      return null;
   }

   protected void updateHomingMovement() {
      Vec3 currentPosition = this.position();
      Vec3 targetPosition = this.target.position();
      Vec3 currentVelocity = this.getDeltaMovement().normalize();
      Vec3 directionToTarget = targetPosition.subtract(currentPosition).normalize();
      double angleBetween = Math.acos(currentVelocity.dot(directionToTarget));
      double maxRotationRadians = Math.toRadians(this.getMaxRotationAnglePerTick());
      Vec3 newVelocity;
      if (angleBetween > maxRotationRadians) {
         newVelocity = this.rotateTowards(currentVelocity, directionToTarget, maxRotationRadians);
      } else {
         newVelocity = directionToTarget;
      }

      this.setDeltaMovement(newVelocity.scale(this.getSpeed()));
   }

   protected Vec3 rotateTowards(Vec3 currentDirection, Vec3 targetDirection, double maxRotationRadians) {
      return currentDirection.lerp(targetDirection, maxRotationRadians / Math.acos(currentDirection.dot(targetDirection)));
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

   protected void generateSmokeParticles() {
      Vec3 velocity = this.getDeltaMovement();
      Vec3 directionBackwards = velocity.normalize().scale(-0.2);

      for (int i = 0; i < 10; i++) {
         Vec3 particlePos = this.position().add(directionBackwards.scale(i));
         this.level()
            .addAlwaysVisibleParticle((ParticleOptions)PomkotsMechs.MISSILE_SMOKE.get(), true, particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
      }
   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.getEntity();
      if (entity instanceof LivingEntity) {
         if (entity.equals(this.shooter)) {
            return;
         }

         entity.invulnerableTime = 0;
         entity.hurt(entity.damageSources().thrown(this, (Entity)(this.getOwner() != null ? this.getOwner() : this)), this.getDamage());
      }

      this.createExplosion(this.position());
      this.discard();
   }

   protected void onHitBlock(BlockHitResult blockHitResult) {
      this.createExplosion(this.position());
      this.discard();
   }

   protected void createExplosion(Vec3 pos) {
      Level world = this.level();
      if (!world.isClientSide) {
         Utils.explode(this, pos.x, pos.y, pos.z, BattleBalance.MECH_MISSILE_EXPLOSION, false, ExplosionInteraction.NONE, this.level());
      } else {
         this.addParticles(this.position());
      }
   }

   public void onClientRemoval() {
      if (this.level().isClientSide) {
         this.addParticles(this.position());
      }
   }

   protected void addParticles(Vec3 offset) {
      Level world = this.level();

      for (int i = 0; i < 3; i++) {
         int rad = Math.abs(i - 3);

         for (int j = 0; j < 3 - rad; j++) {
            for (int k = 0; k < 3 - rad; k++) {
               world.addParticle(
                  ParticleTypes.EXPLOSION,
                  offset.x - (k - (1 - rad / 2)) * 2,
                  offset.y - (i - 1) * 2,
                  offset.z - (j - (1 - rad / 2)) * 2,
                  0.0,
                  0.0,
                  0.0
               );
            }
         }
      }
   }

   @Override
   public LivingEntity getShooter() {
      return this.shooter;
   }

   protected void defineSynchedData() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.missile.idle")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   private class MissileTargetPredicate implements Predicate<Entity> {
      @Override
      public boolean test(Entity entity) {
         return true;
      }
   }
}
