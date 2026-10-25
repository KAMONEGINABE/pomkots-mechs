package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.turret;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;

public class Pmvt01Entity extends PomkotsVehicleBase {
   protected static final int ACT_MAIN_WEAPON = 0;
   private int muzzleSide = -1;

   public Pmvt01Entity(EntityType<? extends LivingEntity> entityType, Level level) {
      super(entityType, level);
      this.m_21051_(Attributes.f_22278_).m_22100_(1.0);
   }

   @Override
   protected void registerActions() {
      this.actionController.registerAction(0, new Action(0, 2, 12), ActionController.ActionType.R_ARM_MAIN);
   }

   @Override
   public void m_8119_() {
      super.m_8119_();
   }

   @Override
   protected void applyPlayerInput(DriverInput driverInput) {
      this.getUserIntentionForDirectionFromKey(driverInput);
      if (driverInput.isWeaponRightHandPressed()) {
         this.actionController.getAction(0).startAction();
      }
   }

   @Override
   protected void fireWeapons() {
      Level world = this.m_9236_();
      if (this.actionController.getAction(0).isOnStart() && this.isServerSide()) {
         this.muzzleSide = -1;
      }

      if (this.actionController.getAction(0).isOnFire() && this.isServerSide()) {
         BulletGrenadeEntity be = new BulletGrenadeEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_MACHINE_LARGE.get(), world, this, 40.0F
         );
         be.m_20242_(true);
         be.setExplosionScale(2);
         Vec3 offset = this.m_20182_();
         Vec3 muzzlPos = new Vec3(0.5 * this.muzzleSide, 1.0, 2.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         be.m_146884_(offset.m_82549_(muzzlPos));
         float[] angle = this.getShootingAngle(be, false);
         be.m_37251_(be, angle[0], angle[1], this.m_21256_(), BattleBalance.MECH_BULLET_GRENADE_SPEED, 2.0F);
         world.m_7967_(be);
         this.muzzleSide *= -1;
      }
   }

   @Override
   protected float[] getShootingAngle(Entity bullet, boolean useLockTarget) {
      double xRot = 0.0;
      double yRot = 0.0;
      Entity lockTarget = this.lockTargets.getLockTargetHard() != null ? this.lockTargets.getLockTargetHard() : this.lockTargets.getLockTargetSoft();
      if (useLockTarget && lockTarget != null) {
         Vec3 positionA = bullet.m_20182_();
         Vec3 positionB = lockTarget.m_20191_().m_82399_();
         double deltaX = positionB.f_82479_ - positionA.f_82479_;
         double deltaY = positionB.f_82480_ - positionA.f_82480_;
         double deltaZ = positionB.f_82481_ - positionA.f_82481_;
         yRot = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0;
         double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
         xRot = -Math.toDegrees(Math.atan2(deltaY, distanceXZ));
      } else {
         LivingEntity driver = this.getDrivingPassenger();
         Vec3 lookAngle = driver.m_20154_();
         xRot = -Math.toDegrees(Math.asin(lookAngle.f_82480_)) - 3.5;
         yRot = Math.toDegrees(Math.atan2(lookAngle.f_82481_, lookAngle.f_82479_)) - 90.0;
      }

      return new float[]{(float)xRot, (float)yRot};
   }

   @Override
   public void m_7023_(Vec3 pos) {
      if (this.m_6084_() && this.m_20160_() && this.canWork()) {
         LivingEntity pilot = this.getDrivingPassenger();
         this.m_146922_(pilot.m_146908_());
         this.f_19859_ = this.m_146908_();
         this.m_146926_(pilot.m_146909_() * 0.5F);
         this.m_19915_(this.m_146908_(), this.m_146909_());
         this.m_5618_(this.m_146908_());
         this.m_5616_(this.m_146908_());
         super.travelBypass(pos);
         this.f_19812_ = true;
      } else {
         super.m_7023_(pos);
      }
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController[]{new AnimationController(this, "controller", 0, event -> {
         if (this.actionController.getAction(0).isInAction()) {
            if (this.actionController.getAction(0).isOnStart()) {
               event.getController().forceAnimationReset();
            }

            return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmt01.attack"));
         } else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmt01.idle"));
         }
      }).setSoundKeyframeHandler(soundKeyframeEvent -> this.registerAnimationSoundHandlers(soundKeyframeEvent))});
   }

   @Override
   protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
      if ("small_canon".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_SHOTGUN.get());
      }
   }

   @Override
   public double m_6048_() {
      return 0.5;
   }

   public boolean m_6128_() {
      return true;
   }

   @Override
   public boolean m_142535_(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   @Override
   protected String getMechName() {
      return "pmvt01";
   }
}
