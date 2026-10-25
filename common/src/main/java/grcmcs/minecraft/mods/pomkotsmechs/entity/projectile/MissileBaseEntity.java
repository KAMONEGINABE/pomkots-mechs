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
      this.m_20242_(true);
      this.shooter = shooter;
      this.target = target;
      this.f_19811_ = true;
   }

   protected abstract int getSwitchTick();

   protected abstract double getSpeed();

   protected abstract int getSeekRange();

   protected abstract float getMaxRotationAnglePerTick();

   protected abstract float getDamage();

   protected abstract int getMaxLifeTicks();

   protected abstract Vec3 getNonTargetVelocity();

   protected abstract Class getTargetClass();

   public float getScale() {
      return 1.0F;
   }

   @Override
   public void m_8119_() {
      if (this.m_9236_().m_5776_()) {
         this.generateSmokeParticles();
      }

      super.m_8119_();
      if (this.firstYaw == -999.0) {
         this.firstYaw = -1.0 * this.m_146908_();
      }

      if (this.lifeTicks++ >= this.getMaxLifeTicks()) {
         this.createExplosion(this.m_20182_());
         this.m_146870_();
      }

      if (!this.m_9236_().f_46443_) {
         if (this.lifeTicks <= this.getSwitchTick()) {
            this.m_20256_(this.m_20184_());
         } else {
            if (this.target == null) {
               this.target = this.findTarget();
            }

            if (this.target != null && !this.target.m_21224_()) {
               this.updateHomingMovement();
            } else {
               Vec3 vel = this.getNonTargetVelocity();
               this.m_20256_(vel);
            }
         }
      }

      this.updateRotationBasedOnVelocity();
   }

   protected LivingEntity findTarget() {
      int range = this.getSeekRange();

      for (Entity candidate : this.m_9236_()
         .m_6443_(
            this.getTargetClass(),
            new AABB(
               this.m_20185_() - range,
               this.m_20186_() - range,
               this.m_20189_() - range,
               this.m_20185_() + range,
               this.m_20186_() + range,
               this.m_20189_() + range
            ),
            new MissileBaseEntity.MissileTargetPredicate()
         )) {
         BlockHitResult res = this.m_9236_().m_45547_(new ClipContext(this.m_20182_(), candidate.m_20182_(), Block.OUTLINE, Fluid.NONE, this));
         if (res.m_6662_().equals(Type.MISS)) {
            return (LivingEntity)candidate;
         }
      }

      return null;
   }

   protected void updateHomingMovement() {
      Vec3 currentPosition = this.m_20182_();
      Vec3 targetPosition = this.target.m_20182_();
      Vec3 currentVelocity = this.m_20184_().m_82541_();
      Vec3 directionToTarget = targetPosition.m_82546_(currentPosition).m_82541_();
      double angleBetween = Math.acos(currentVelocity.m_82526_(directionToTarget));
      double maxRotationRadians = Math.toRadians(this.getMaxRotationAnglePerTick());
      Vec3 newVelocity;
      if (angleBetween > maxRotationRadians) {
         newVelocity = this.rotateTowards(currentVelocity, directionToTarget, maxRotationRadians);
      } else {
         newVelocity = directionToTarget;
      }

      this.m_20256_(newVelocity.m_82490_(this.getSpeed()));
   }

   protected Vec3 rotateTowards(Vec3 currentDirection, Vec3 targetDirection, double maxRotationRadians) {
      return currentDirection.m_165921_(targetDirection, maxRotationRadians / Math.acos(currentDirection.m_82526_(targetDirection)));
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

   protected void generateSmokeParticles() {
      Vec3 velocity = this.m_20184_();
      Vec3 directionBackwards = velocity.m_82541_().m_82490_(-0.2);

      for (int i = 0; i < 10; i++) {
         Vec3 particlePos = this.m_20182_().m_82549_(directionBackwards.m_82490_(i));
         this.m_9236_()
            .m_6485_((ParticleOptions)PomkotsMechs.MISSILE_SMOKE.get(), true, particlePos.f_82479_, particlePos.f_82480_, particlePos.f_82481_, 0.0, 0.0, 0.0);
      }
   }

   protected void m_5790_(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.m_82443_();
      if (entity instanceof LivingEntity) {
         if (entity.equals(this.shooter)) {
            return;
         }

         entity.f_19802_ = 0;
         entity.m_6469_(entity.m_269291_().m_269390_(this, (Entity)(this.m_19749_() != null ? this.m_19749_() : this)), this.getDamage());
      }

      this.createExplosion(this.m_20182_());
      this.m_146870_();
   }

   protected void m_8060_(BlockHitResult blockHitResult) {
      this.createExplosion(this.m_20182_());
      this.m_146870_();
   }

   protected void createExplosion(Vec3 pos) {
      Level world = this.m_9236_();
      if (!world.f_46443_) {
         Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, BattleBalance.MECH_MISSILE_EXPLOSION, false, ExplosionInteraction.NONE, this.m_9236_());
      } else {
         this.addParticles(this.m_20182_());
      }
   }

   public void m_142036_() {
      if (this.m_9236_().f_46443_) {
         this.addParticles(this.m_20182_());
      }
   }

   protected void addParticles(Vec3 offset) {
      Level world = this.m_9236_();

      for (int i = 0; i < 3; i++) {
         int rad = Math.abs(i - 3);

         for (int j = 0; j < 3 - rad; j++) {
            for (int k = 0; k < 3 - rad; k++) {
               world.m_7106_(
                  ParticleTypes.f_123813_,
                  offset.f_82479_ - (k - (1 - rad / 2)) * 2,
                  offset.f_82480_ - (i - 1) * 2,
                  offset.f_82481_ - (j - (1 - rad / 2)) * 2,
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

   protected void m_8097_() {
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

   private class MissileTargetPredicate<Entity> implements Predicate<Entity> {
      @Override
      public boolean test(Entity entity) {
         return true;
      }
   }
}
