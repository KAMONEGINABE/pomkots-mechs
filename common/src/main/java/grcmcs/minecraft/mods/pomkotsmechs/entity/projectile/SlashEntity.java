package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
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

public class SlashEntity extends ThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 40;
   private int lifeTicks = 0;

   public SlashEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      super(entityType, world);
      this.m_20242_(true);
   }

   public void m_8119_() {
      this.m_20242_(true);
      super.m_8119_();
      if (this.lifeTicks++ >= 40) {
         this.m_146870_();
      } else if (this.lifeTicks == 5) {
         this.generateLava();
      }

      this.updateRotationBasedOnVelocity();
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

   private void generateLava() {
      if (this.m_9236_().f_46443_) {
         RandomSource random = this.m_9236_().m_213780_();

         for (int i = 0; i < 20; i++) {
            double velocityX = random.m_188500_() * 2.0 - 1.0;
            double velocityY = random.m_188500_() * 2.0 - 1.0;
            double velocityZ = random.m_188500_() * 2.0 - 1.0;
            this.m_9236_().m_7106_(ParticleTypes.f_123756_, this.m_20185_(), this.m_20186_(), this.m_20189_(), velocityX, velocityY, velocityZ);
         }
      }
   }

   protected void m_5790_(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.m_82443_();
      if (entity instanceof LivingEntity) {
         entity.m_6469_(entity.m_269291_().m_269390_(this, (Entity)(this.m_19749_() != null ? this.m_19749_() : this)), 20.0F);
         entity.f_19802_ = 20;
         Vec3 velocity = this.m_20184_();
         Vec3 kbVel = new Vec3(velocity.f_82479_, 0.0, velocity.f_82481_).m_82541_();
         ((LivingEntity)entity).m_147240_(2.0, kbVel.f_82479_, kbVel.f_82481_);
      }
   }

   protected void m_8060_(BlockHitResult blockHitResult) {
   }

   protected void m_8097_() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.slash.new")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
