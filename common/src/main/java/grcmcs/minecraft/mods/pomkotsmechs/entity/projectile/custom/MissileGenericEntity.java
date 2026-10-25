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
      this.m_20242_(true);
      this.shooter = shooter;
      this.target = target;
      this.damage = damage;
      this.speed = speed;
   }

   public float getScale() {
      return 1.0F;
   }

   @Override
   public void m_8119_() {
      this.m_146867_();
      if (this.m_9236_().f_46443_) {
         this.clientTick();
      } else if (this.lifeTicks++ >= this.maxLifeTicks) {
         this.createExplosion(this.m_20182_());
         this.m_146870_();
      } else {
         this.m_20101_();
         if (this.performSubStepMovement()) {
            if (this.target != null && this.target.m_6084_() && !this.straight) {
               if (this.lifeTicks > 3) {
                  this.homingUpdate();
               }

               this.m_146884_(this.m_20182_().m_82549_(this.m_20184_()));
               this.f_19812_ = true;
            } else {
               this.moveStraight();
               this.m_146884_(this.m_20182_().m_82549_(this.m_20184_()));
               this.f_19812_ = true;
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
      Vec3 velocity = this.m_20184_();
      if (velocity.m_82556_() < 1.0E-4) {
         float yaw = this.m_146908_();
         float pitch = this.m_146909_();
         Vec3 forward = Vec3.m_82498_(pitch, yaw);
         velocity = forward.m_82490_(1.2);
      }

      double maxSpeed = this.getSpeed();
      if (velocity.m_82553_() > maxSpeed) {
         velocity = velocity.m_82541_().m_82490_(maxSpeed);
      }

      this.m_20256_(velocity);
      this.updateRotationFromVelocity(velocity);
   }

   private void homingUpdate() {
      Vec3 position = this.m_20182_();
      Vec3 velocity = this.m_20184_();
      Vec3 targetPos = this.target.m_20191_().m_82399_();
      Vec3 diff = targetPos.m_82546_(position);
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

            Vec3 acceleration = diff.m_82546_(velocity.m_82490_(period)).m_82490_(homingRate / (period * period));
            double maxAcc = this.getSpeed() * 8.0;
            if (acceleration.m_82553_() > maxAcc) {
               acceleration = acceleration.m_82541_().m_82490_(maxAcc);
            }

            double dt = 0.05;
            velocity = velocity.m_82549_(acceleration.m_82490_(dt));
         }

         double maxSpeed = this.getSpeed();
         if (velocity.m_82553_() > maxSpeed) {
            velocity = velocity.m_82541_().m_82490_(maxSpeed);
         }

         this.m_20256_(velocity);
         this.updateRotationFromVelocity(velocity);
         this.remainingTicks--;
      } else {
         this.rotationLimitReached = true;
         this.moveStraight();
      }
   }

   private boolean isRotationLimitExceeded(Vec3 currentVelocity) {
      if (currentVelocity.m_82556_() < 1.0E-4) {
         return false;
      }

      Vec3 currentDir = currentVelocity.m_82541_();
      if (this.lastDirection != null) {
         double dot = Mth.m_14008_(this.lastDirection.m_82526_(currentDir), -1.0, 1.0);
         float deltaDeg = (float)Math.toDegrees(Math.acos(dot));
         this.totalRotatedDeg += deltaDeg;
      }

      this.lastDirection = currentDir;
      return this.totalRotatedDeg >= this.maxTotalRotationDeg;
   }

   protected void updateRotationFromVelocity(Vec3 vel) {
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

   @Override
   protected void m_5790_(EntityHitResult entityHitResult) {
      super.m_5790_(entityHitResult);
      this.createExplosion(this.m_20182_());
      this.m_146870_();
   }

   @Override
   protected void m_8060_(BlockHitResult blockHitResult) {
      this.createExplosion(this.m_20182_());
      this.m_146870_();
   }

   public void clientTick() {
      Vec3 currentPos = this.m_20182_();
      if (this.prevClientPos != null) {
         this.spawnSmokeBetween(this.prevClientPos, currentPos);
      }

      this.prevClientPos = currentPos;
   }

   private void spawnSmokeBetween(Vec3 from, Vec3 to) {
      Level level = this.m_9236_();
      Vec3 diff = to.m_82546_(from);
      double length = diff.m_82553_();
      if (!(length < 0.001)) {
         Vec3 dir = diff.m_82541_();
         double spacing = 0.5;
         int count = (int)(length / spacing);

         for (int i = 0; i <= count; i++) {
            double d = i * spacing;
            Vec3 pos = from.m_82549_(dir.m_82490_(d));
            level.m_6485_((ParticleOptions)PomkotsMechs.MISSILE_SMOKE.get(), true, pos.f_82479_, pos.f_82480_, pos.f_82481_, 0.0, 0.0, 0.0);
         }
      }
   }

   protected void createExplosion(Vec3 pos) {
      Level world = this.m_9236_();
      if (world.f_46443_) {
         this.addParticles(4.0F, this.m_20182_());
      } else if (Utils.isBlockDestructionAllowed(this.shooter)) {
         Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, BattleBalance.MECH_MISSILE_EXPLOSION, false, ExplosionInteraction.BLOCK, this.m_9236_());
      } else {
         Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, BattleBalance.MECH_MISSILE_EXPLOSION, false, ExplosionInteraction.NONE, this.m_9236_());
      }
   }

   protected void addParticles(float scale, Vec3 offset) {
      Level level = this.m_9236_();
      if (!(scale < 2.0F)) {
         level.m_7106_(ParticleTypes.f_123812_, offset.f_82479_, offset.f_82480_, offset.f_82481_, 1.0, 0.0, 0.0);
      } else {
         level.m_7106_(ParticleTypes.f_123813_, offset.f_82479_, offset.f_82480_, offset.f_82481_, 1.0, 0.0, 0.0);
      }
   }

   @Override
   public void m_142036_() {
      if (this.m_9236_().f_46443_) {
         this.addParticles(1.0F, this.m_20182_());
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
      return this.m_20184_();
   }

   protected Class getTargetClass() {
      return LivingEntity.class;
   }
}
