package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MissileGenericEntity extends PomkotsCustomThrowableProjectile implements GeoEntity, GeoAnimatable {
   protected final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   protected float speed;
   protected float maxRotationAnglePerTick = 3.0F;
   protected LivingEntity target = null;
   protected int switchTick;
   private int remainingTicks = 40;
   private boolean terminalPhase = false;
   private boolean straight = false;
   private Vec3 prevClientPos = null;
   private float totalRotatedDeg = 0.0F;
   private Vec3 lastDirection = null;
   private float maxTotalRotationDeg = 200.0F;
   private boolean rotationLimitReached = false;

   public MissileGenericEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      this(entityType, level, null, null);
   }

   public MissileGenericEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, LivingEntity target) {
      this(entityType, level, shooter, target, BattleBalance.MECH_MISSILE_DAMAGE, 1.0F);
   }

   public MissileGenericEntity(
      EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, LivingEntity target, float damage, float speed
   ) {
      this(entityType, level, shooter, target, damage, speed, 80, 2);
   }

   public MissileGenericEntity(
      EntityType<? extends ThrowableProjectile> entityType,
      Level level,
      LivingEntity shooter,
      LivingEntity target,
      float damage,
      float speed,
      int maxLifeTicks,
      int stun
   ) {
      super(entityType, level, shooter, maxLifeTicks, damage, stun);
      this.setNoGravity(true);
      this.shooter = shooter;
      this.target = target;
      this.damage = damage;
      this.speed = speed;
   }

   public float getScale() {
      return 1.0F;
   }

   @Override
   public void tick() {
      this.setOldPosAndRot();
      if (this.level().isClientSide) {
         this.clientTick();
      } else if (this.lifeTicks++ >= this.maxLifeTicks) {
         this.createExplosion(this.position());
         this.discard();
      } else {
         this.checkInsideBlocks();
         if (this.performSubStepMovement()) {
            if (this.target != null && this.target.isAlive() && !this.straight) {
               if (this.lifeTicks > 3) {
                  this.homingUpdate();
               }

               this.setPos(this.position().add(this.getDeltaMovement()));
               this.hasImpulse = true;
            } else {
               this.moveStraight();
               this.setPos(this.position().add(this.getDeltaMovement()));
               this.hasImpulse = true;
            }
         }
      }
   }

   @Override
   protected void setPosInSubStep(Vec3 pos) {
   }

   public void setStraight(boolean value) {
      this.straight = value;
   }

   public boolean getStraight() {
      return this.straight;
   }

   private void moveStraight() {
      Vec3 velocity = this.getDeltaMovement();
      if (velocity.lengthSqr() < 1.0E-4) {
         float yaw = this.getYRot();
         float pitch = this.getXRot();
         Vec3 forward = Vec3.directionFromRotation(pitch, yaw);
         velocity = forward.scale(1.2);
      }

      double maxSpeed = this.getSpeed();
      if (velocity.length() > maxSpeed) {
         velocity = velocity.normalize().scale(maxSpeed);
      }

      this.setDeltaMovement(velocity);
      this.updateRotationFromVelocity(velocity);
   }

   private void homingUpdate() {
      Vec3 position = this.position();
      Vec3 velocity = this.getDeltaMovement();
      Vec3 targetPos = this.target.getBoundingBox().getCenter();
      Vec3 diff = targetPos.subtract(position);
      if (!this.rotationLimitReached && !this.isRotationLimitExceeded(velocity)) {
         if (this.lifeTicks > 60) {
            this.terminalPhase = true;
         }

         if (!this.terminalPhase || this.target instanceof BaseSmallMonsterEntity) {
            float period = Math.max(this.remainingTicks / 20.0F, 0.05F);
            float homingRate = 2.0F;
            if (this.target instanceof BaseSmallMonsterEntity) {
               homingRate = 7.0F;
            }

            Vec3 acceleration = diff.subtract(velocity.scale(period)).scale(homingRate / (period * period));
            double maxAcc = this.getSpeed() * 8.0;
            if (acceleration.length() > maxAcc) {
               acceleration = acceleration.normalize().scale(maxAcc);
            }

            double dt = 0.05;
            velocity = velocity.add(acceleration.scale(dt));
         }

         double maxSpeed = this.getSpeed();
         if (velocity.length() > maxSpeed) {
            velocity = velocity.normalize().scale(maxSpeed);
         }

         this.setDeltaMovement(velocity);
         this.updateRotationFromVelocity(velocity);
         this.remainingTicks--;
      } else {
         this.rotationLimitReached = true;
         this.moveStraight();
      }
   }

   private boolean isRotationLimitExceeded(Vec3 currentVelocity) {
      if (currentVelocity.lengthSqr() < 1.0E-4) {
         return false;
      }

      Vec3 currentDir = currentVelocity.normalize();
      if (this.lastDirection != null) {
         double dot = Mth.clamp(this.lastDirection.dot(currentDir), -1.0, 1.0);
         float deltaDeg = (float)Math.toDegrees(Math.acos(dot));
         this.totalRotatedDeg += deltaDeg;
      }

      this.lastDirection = currentDir;
      return this.totalRotatedDeg >= this.maxTotalRotationDeg;
   }

   protected void updateRotationFromVelocity(Vec3 vel) {
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

   @Override
   protected void onHitEntity(EntityHitResult entityHitResult) {
      super.onHitEntity(entityHitResult);
      this.createExplosion(this.position());
      this.discard();
   }

   @Override
   protected void onHitBlock(BlockHitResult blockHitResult) {
      this.createExplosion(this.position());
      this.discard();
   }

   public void clientTick() {
      Vec3 currentPos = this.position();
      if (this.prevClientPos != null) {
         this.spawnSmokeBetween(this.prevClientPos, currentPos);
      }

      this.prevClientPos = currentPos;
   }

   private void spawnSmokeBetween(Vec3 from, Vec3 to) {
      Level level = this.level();
      Vec3 diff = to.subtract(from);
      double length = diff.length();
      if (!(length < 0.001)) {
         Vec3 dir = diff.normalize();
         double spacing = 0.5;
         int count = (int)(length / spacing);

         for (int i = 0; i <= count; i++) {
            double d = i * spacing;
            Vec3 pos = from.add(dir.scale(d));
            level.addAlwaysVisibleParticle((ParticleOptions)PomkotsMechs.MISSILE_SMOKE.get(), true, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
         }
      }
   }

   protected void createExplosion(Vec3 pos) {
      Level world = this.level();
      if (world.isClientSide) {
         this.addParticles(4.0F, this.position());
      } else if (Utils.isBlockDestructionAllowed(this.shooter)) {
         Utils.explode(this, pos.x, pos.y, pos.z, BattleBalance.MECH_MISSILE_EXPLOSION, false, ExplosionInteraction.BLOCK, this.level());
      } else {
         Utils.explode(this, pos.x, pos.y, pos.z, BattleBalance.MECH_MISSILE_EXPLOSION, false, ExplosionInteraction.NONE, this.level());
      }
   }

   protected void addParticles(float scale, Vec3 offset) {
      Level level = this.level();
      if (!(scale < 2.0F)) {
         level.addParticle(ParticleTypes.EXPLOSION_EMITTER, offset.x, offset.y, offset.z, 1.0, 0.0, 0.0);
      } else {
         level.addParticle(ParticleTypes.EXPLOSION, offset.x, offset.y, offset.z, 1.0, 0.0, 0.0);
      }
   }

   @Override
   public void onClientRemoval() {
      if (this.level().isClientSide) {
         this.addParticles(1.0F, this.position());
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
            new AnimationController(
               this,
               "rotation",
               0,
               event -> event.setAndContinue(RawAnimation.begin().thenPlay("animation.missile.start").thenPlayAndHold("animation.missile.idle"))
            )
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   @Override
   public float getHitDamage() {
      return this.getDamage();
   }

   protected int getSwitchTick() {
      return this.switchTick;
   }

   public void setSwitchTick(int val) {
      this.switchTick = val;
   }

   protected double getSpeed() {
      return this.speed;
   }

   protected int getSeekRange() {
      return 100;
   }

   protected float getMaxRotationAnglePerTick() {
      return this.lifeTicks == this.getSwitchTick() + 1 ? 90.0F : this.maxRotationAnglePerTick;
   }

   public float setMaxRotationAnglePerTick(float angle) {
      return this.maxRotationAnglePerTick = angle;
   }

   protected float getDamage() {
      return this.damage;
   }

   protected int getMaxLifeTicks() {
      return this.maxLifeTicks;
   }

   public void setMaxLifeTick(int maxLifeTick) {
      this.maxLifeTicks = maxLifeTick;
   }

   protected Vec3 getNonTargetVelocity() {
      return this.getDeltaMovement();
   }

   protected Class getTargetClass() {
      return LivingEntity.class;
   }
}
