package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.sounds.SoundEvent;
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

public class MissilePodEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 60;
   private int lifeTicks = 0;
   private LivingEntity shooter = null;
   private LivingEntity target = null;
   private float damage = 0.0F;
   private float speed = 0.0F;
   private MissilePodEntity.Mode mode = MissilePodEntity.Mode.DOWN;

   public MissilePodEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      this(entityType, world, null, null, BattleBalance.MECH_MISSILE_DAMAGE, BattleBalance.MECH_MISSILE_GENERIC_SPEED);
   }

   public MissilePodEntity(
      EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target, float damage, float speed
   ) {
      super(entityType, shooter, world);
      this.shooter = shooter;
      this.target = target;
      this.damage = damage;
      this.speed = speed;
   }

   public void setMode(MissilePodEntity.Mode mode) {
      this.mode = mode;
   }

   @Override
   public void m_8119_() {
      super.m_8119_();
      if (!this.m_9236_().f_46443_) {
         this.f_19812_ = true;
         switch (this.mode) {
            case DOWN:
               if (this.lifeTicks >= 10 && this.lifeTicks % 5 == 0) {
                  this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());

                  for (int row = 0; row < 2; row++) {
                     for (int col = 0; col < 2; col++) {
                        for (int side = -1; side < 2; side += 2) {
                           MissileGenericEnemyEntity be = new MissileGenericEnemyEntity(
                              (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                              this.m_9236_(),
                              this.shooter,
                              this.target,
                              this.damage,
                              this.speed
                           );
                           be.setStraight(true);
                           Vec3 offset = this.m_20182_();
                           float slotZ = col * 0.8F + 1.5F;
                           float slotY = row + 0.8F;
                           Vec3 muzzlPos = new Vec3(side * 3, slotY, slotZ);
                           muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
                           be.m_146884_(offset.m_82549_(muzzlPos));
                           be.m_37251_(be, 70.0F, this.m_146908_() - 10.0F + 20 * side, 0.0F, this.speed, 0.0F);
                           this.m_9236_().m_7967_(be);
                        }
                     }
                  }
               }
               break;
            case SPHERE:
               if (Utils.isInRangeOnAxisXZ(this, this.target, 20.0F)) {
                  this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());
                  this.shootSphericalMissiles(this.m_9236_(), this.m_20182_(), this.shooter, this.target, this.damage, this.speed, 10);
                  this.m_146870_();
               }
         }

         this.updateRotationBasedOnVelocity();
         if (this.lifeTicks++ >= 60) {
            this.m_146870_();
         }
      }
   }

   public void shootSphericalMissiles(Level level, Vec3 centerPos, LivingEntity shooter, LivingEntity target, Float damage, Float speed, int count) {
      if (!level.f_46443_) {
         for (Vec3 direction : calculateSphereDirections(count)) {
            MissileGenericEnemyEntity missile = new MissileGenericEnemyEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(), level, shooter, target, damage, speed
            );
            missile.setStraight(true);
            missile.m_146884_(centerPos);
            Vec3 velocity = direction.m_82541_().m_82490_(speed.floatValue());
            missile.m_20256_(velocity);
            level.m_7967_(missile);
         }
      }
   }

   private static List<Vec3> calculateSphereDirections(int count) {
      List<Vec3> directions = new ArrayList<>();
      double goldenRatio = (1.0 + Math.sqrt(5.0)) / 2.0;
      double angleIncrement = (Math.PI * 2) * goldenRatio;

      for (int i = 0; i < count; i++) {
         double t = (double)i / (count - 1);
         double inclination = Math.acos(1.0 - 2.0 * t);
         double azimuth = angleIncrement * i;
         double x = Math.sin(inclination) * Math.cos(azimuth);
         double y = Math.sin(inclination) * Math.sin(azimuth);
         double z = Math.cos(inclination);
         directions.add(new Vec3(x, y, z));
      }

      return directions;
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

   protected void m_5790_(EntityHitResult entityHitResult) {
      this.m_146870_();
   }

   protected void m_8060_(BlockHitResult blockHitResult) {
      if (!this.m_9236_().f_46443_ && this.mode == MissilePodEntity.Mode.SPHERE) {
         this.shootSphericalMissiles(this.m_9236_(), this.m_20182_(), this.shooter, this.target, this.damage, this.speed, 10);
      }

      this.m_146870_();
   }

   protected void m_8097_() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.missilepod.idle")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public enum Mode {
      DOWN,
      SPHERE;
   }
}
