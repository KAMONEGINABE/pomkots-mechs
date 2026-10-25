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
      this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
   }

   @Override
   protected void registerActions() {
      this.actionController.registerAction(0, new Action(0, 2, 12), ActionController.ActionType.R_ARM_MAIN);
   }

   @Override
   public void tick() {
      super.tick();
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
      Level world = this.level();
      if (this.actionController.getAction(0).isOnStart() && this.isServerSide()) {
         this.muzzleSide = -1;
      }

      if (this.actionController.getAction(0).isOnFire() && this.isServerSide()) {
         BulletGrenadeEntity be = new BulletGrenadeEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_MACHINE_LARGE.get(), world, this, 40.0F
         );
         be.setNoGravity(true);
         be.setExplosionScale(2);
         Vec3 offset = this.position();
         Vec3 muzzlPos = new Vec3(0.5 * this.muzzleSide, 1.0, 2.0);
         muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         be.setPos(offset.add(muzzlPos));
         float[] angle = this.getShootingAngle(be, false);
         be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), BattleBalance.MECH_BULLET_GRENADE_SPEED, 2.0F);
         world.addFreshEntity(be);
         this.muzzleSide *= -1;
      }
   }

   @Override
   protected float[] getShootingAngle(Entity bullet, boolean useLockTarget) {
      double xRot = 0.0;
      double yRot = 0.0;
      Entity lockTarget = this.lockTargets.getLockTargetHard() != null ? this.lockTargets.getLockTargetHard() : this.lockTargets.getLockTargetSoft();
      if (useLockTarget && lockTarget != null) {
         Vec3 positionA = bullet.position();
         Vec3 positionB = lockTarget.getBoundingBox().getCenter();
         double deltaX = positionB.x - positionA.x;
         double deltaY = positionB.y - positionA.y;
         double deltaZ = positionB.z - positionA.z;
         yRot = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0;
         double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
         xRot = -Math.toDegrees(Math.atan2(deltaY, distanceXZ));
      } else {
         LivingEntity driver = this.getDrivingPassenger();
         Vec3 lookAngle = driver.getLookAngle();
         xRot = -Math.toDegrees(Math.asin(lookAngle.y)) - 3.5;
         yRot = Math.toDegrees(Math.atan2(lookAngle.z, lookAngle.x)) - 90.0;
      }

      return new float[]{(float)xRot, (float)yRot};
   }

   @Override
   public void travel(Vec3 pos) {
      if (this.isAlive() && this.isVehicle() && this.canWork()) {
         LivingEntity pilot = this.getDrivingPassenger();
         this.setYRot(pilot.getYRot());
         this.yRotO = this.getYRot();
         this.setXRot(pilot.getXRot() * 0.5F);
         this.setRot(this.getYRot(), this.getXRot());
         this.setYBodyRot(this.getYRot());
         this.setYHeadRot(this.getYRot());
         super.travelBypass(pos);
         this.hasImpulse = true;
      } else {
         super.travel(pos);
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
   public double getPassengersRidingOffset() {
      return 0.5;
   }

   public boolean ignoreExplosion() {
      return true;
   }

   @Override
   public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   @Override
   protected String getMechName() {
      return "pmvt01";
   }
}
