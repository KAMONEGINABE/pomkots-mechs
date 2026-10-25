package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletRifleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import java.util.List;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;

public class Pmv03Entity extends PomkotsVehicleBase {
   public static float DEFAULT_SCALE = 2.0F;
   protected static final int ACT_HUMMER = 2;
   protected static final int ACT_SCOP = 3;
   protected static final int ACT_R_RIFLE = 4;
   protected static final int ACT_L_RIFLE = 5;
   protected static final int ACT_GRENADE = 6;
   protected static final int ACT_MISSILE = 7;
   protected Vec3 mainCameraPosition = null;

   @Override
   protected String getMechName() {
      return "pmv01";
   }

   public Pmv03Entity(EntityType<? extends LivingEntity> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   protected void registerActions() {
      super.registerActions();
      this.actionController.registerAction(4, new Action(20, 6, 8), ActionController.ActionType.R_ARM_MAIN);
      this.actionController.registerAction(5, new Action(20, 6, 8), ActionController.ActionType.L_ARM_MAIN);
      this.actionController.registerAction(7, new Action(60, 0, 20), ActionController.ActionType.R_SHL_MAIN);
   }

   @Override
   protected void applyPlayerInputWeapons(DriverInput driverInput) {
      if (this.isMainMode()) {
         if (driverInput.isWeaponRightHandPressed()) {
            this.actionController.getAction(4).startAction();
         } else if (driverInput.isWeaponLeftHandPressed()) {
            this.actionController.getAction(5).startAction();
         } else if (this.lockTargets.consumeMultiLockComplete()) {
            this.actionController.getAction(7).startAction();
         }
      }
   }

   @Override
   protected void fireWeapons() {
      Level level = this.m_9236_();
      if (this.actionController.getAction(4).isOnFire()) {
         this.fireRifle(level, -1);
      } else if (this.actionController.getAction(5).isOnFire()) {
         this.fireRifle(level, 1);
      } else if (this.actionController.getAction(7).isInFire() && this.actionController.getAction(7).currentFireTime % 3 == 0) {
         this.fireMissile(level, this.actionController.getAction(7).currentFireTime / 3 - 1);
      }
   }

   public void fireRifle(Level level, int isRight) {
      if (!level.m_5776_()) {
         BulletRifleEntity be = new BulletRifleEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_RIFLE.get(), level, this, 50.0F);
         Vec3 offset = this.posHistory.getFirst();
         Vec3 muzzlPos = new Vec3(2.8 * isRight, 6.0, 5.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         be.m_146884_(offset.m_82549_(muzzlPos));
         float[] angle = this.getShootingAngle(be, true);
         be.m_37251_(be, angle[0], angle[1], this.m_21256_(), BattleBalance.MECH_RIFLE_SPEED, 0.0F);
         level.m_7967_(be);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_RIFLE.get());
      }
   }

   private void fireMissile(Level world, int slot) {
      if (!world.m_5776_()) {
         Vec3 offset = this.posHistory.getFirst();
         Vec3 muzzlPos = new Vec3(-1.5, 11.0, 2.0);
         Vec3 worldMuzzlPos = muzzlPos.m_82520_(0.0, 0.0, -slot * 0.3).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         LivingEntity target = null;
         List<Entity> lockTargetsMulti = this.lockTargets.getLockTargetMulti();
         if (!lockTargetsMulti.isEmpty()) {
            int lockNum = lockTargetsMulti.size();
            int idx = slot;
            if (lockNum <= slot) {
               idx = slot % lockNum;
            }

            target = (LivingEntity)lockTargetsMulti.get(idx);
         }

         MissileGenericEntity be = new MissileGenericEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(), world, this, target, 20.0F, BattleBalance.MECH_MISSILE_GENERIC_SPEED
         );
         be.m_146884_(offset.m_82549_(worldMuzzlPos));
         be.m_37251_(be, -10.0F, this.m_146908_(), this.m_21256_(), 1.0F, 0.0F);
         world.m_7967_(be);
         if (slot == 5) {
            this.lockTargets.clearLockTargetsMulti();
         }
      } else {
         if (slot == 0) {
            this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());
         }

         this.lockTargets.clearLockTargetsMulti();
      }
   }

   @Override
   protected PlayState controllAnimationWeapons(AnimationState<PomkotsVehicleBase> event) {
      return null;
   }

   @Override
   protected void addExtraAnimationController(ControllerRegistrar controllers) {
      controllers.add(new AnimationController[]{new AnimationController(this, "gatling", 0, event -> {
         if (this.actionController.getAction(4).isInAction()) {
            if (this.actionController.getAction(4).isOnStart()) {
               event.getController().forceAnimationReset();
            }

            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".w_rifle_right"));
         } else if (this.actionController.getAction(5).isInAction()) {
            if (this.actionController.getAction(5).isOnStart()) {
               event.getController().forceAnimationReset();
            }

            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".w_rifle_left"));
         } else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"));
         }
      })});
      controllers.add(new AnimationController[]{new AnimationController(this, "weapons", 0, event -> {
         if (this.actionController.getAction(7).isInAction()) {
            if (this.actionController.getAction(7).isOnStart()) {
               event.getController().forceAnimationReset();
            }

            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".weapon_missile"));
         } else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"));
         }
      })});
   }

   @Override
   protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
      if ("se_jump".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_JUMP_EVENT.get());
      } else if ("se_booster".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BOOSTER_EVENT.get());
      } else if ("se_onground".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_JUMP_EVENT.get());
      }
   }

   public Vec3 getMainCameraPosition() {
      return this.mainCameraPosition == null ? Vec3.f_82478_ : this.mainCameraPosition;
   }

   public void setMainCameraPosition(Vec3 pos) {
      this.mainCameraPosition = pos;
   }

   @Override
   public boolean shouldLockMulti(DriverInput driverInput) {
      return driverInput.isWeaponRightShoulderPressed();
   }

   @Override
   public boolean shouldLockWeak(DriverInput driverInput) {
      return driverInput.isWeaponRightHandPressed() || driverInput.isWeaponLeftHandPressed();
   }

   @Override
   public boolean shouldLockStrong(DriverInput driverInput) {
      return driverInput.isLockPressed();
   }

   @Override
   public double m_6048_() {
      return 5.0;
   }
}
