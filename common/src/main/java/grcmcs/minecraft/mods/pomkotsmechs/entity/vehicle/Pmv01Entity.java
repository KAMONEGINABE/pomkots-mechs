package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileVerticalEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;

public class Pmv01Entity extends PomkotsVehicleBase {
   protected static final int ACT_HUMMER = 2;
   protected static final int ACT_SCOP = 3;
   protected static final int ACT_GATRING = 4;
   protected static final int ACT_PILE = 5;
   protected static final int ACT_GRENADE = 6;
   protected static final int ACT_MISSILE = 7;

   @Override
   protected String getMechName() {
      return "pmv01";
   }

   public Pmv01Entity(EntityType<? extends LivingEntity> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   protected void registerActions() {
      super.registerActions();
      this.actionController.registerAction(2, new Action(20, 12, 8), ActionController.ActionType.R_ARM_SUB);
      this.actionController.registerAction(3, new Action(20, 12, 8), ActionController.ActionType.L_ARM_SUB);
      this.actionController.registerAction(4, new Action(0, 7, 2), ActionController.ActionType.R_ARM_MAIN);
      this.actionController.registerAction(5, new Action(20, 10, 10), ActionController.ActionType.L_ARM_MAIN);
      this.actionController.registerAction(6, new Action(60, 4, 10), ActionController.ActionType.L_SHL_MAIN);
      this.actionController.registerAction(7, new Action(60, 0, 20), ActionController.ActionType.R_SHL_MAIN);
   }

   @Override
   protected void applyPlayerInputWeapons(DriverInput driverInput) {
      if (this.isMainMode()) {
         if (driverInput.isWeaponRightHandPressed()) {
            this.actionController.getAction(4).startAction();
         } else if (driverInput.isWeaponLeftHandPressed()) {
            if (!this.actionController.getAction(5).isInAction()) {
               this.startPileBunker();
            }

            this.actionController.getAction(5).startAction();
         } else if (driverInput.isWeaponLeftShoulderPressed()) {
            this.actionController.getAction(6).startAction();
         } else if (this.lockTargets.consumeMultiLockComplete()) {
            this.actionController.getAction(7).startAction();
         }
      } else if (driverInput.isWeaponRightHandPressed()) {
         this.actionController.getAction(2).startAction();
      } else if (driverInput.isWeaponLeftHandPressed()) {
         this.actionController.getAction(3).startAction();
      }
   }

   private void startPileBunker() {
      if (this.isServerSide()) {
         Vec3 vel;
         if (this.lockTargets.getLockTargetHard() != null) {
            vel = this.m_20182_().m_82505_(this.lockTargets.getLockTargetHard().m_20182_()).m_82541_();
         } else if (this.lockTargets.getLockTargetSoft() != null) {
            vel = this.m_20182_().m_82505_(this.lockTargets.getLockTargetSoft().m_20182_()).m_82541_();
         } else {
            vel = new Vec3(0.0, 0.0, 1.0);
            vel = vel.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         }

         double distance = 1.4249999999999998;
         if (!this.m_20096_()) {
            distance = 0.475;
         }

         vel = vel.m_82490_(distance);
         this.m_5997_(vel.f_82479_, vel.f_82480_, vel.f_82481_);
      }
   }

   @Override
   protected void fireWeapons() {
      Level level = this.m_9236_();
      if (this.actionController.getAction(2).isOnFire()) {
         this.fireHummer(level);
      } else if (this.actionController.getAction(3).isOnFire()) {
         this.fireScop(level);
      } else if (this.actionController.getAction(4).isOnFire()) {
         this.fireGatring(level);
      } else if (this.actionController.getAction(5).isOnFire()) {
         this.firePilebunker(level);
      } else if (this.actionController.getAction(6).isOnFire()) {
         this.fireGrenade(level);
      } else if (this.actionController.getAction(7).isInFire() && this.actionController.getAction(7).currentFireTime % 3 == 0) {
         this.fireMissile(level, this.actionController.getAction(7).currentFireTime / 3 - 1);
      }
   }

   private void fireHummer(Level world) {
      if (!world.m_5776_()) {
         BlockPos curBP = this.m_20183_();
         Vec3 vec = new Vec3(0.0, 0.0, 5.0);
         vec = vec.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         BlockPos tgtOffset = new BlockPos(curBP.m_123341_() + (int)vec.f_82479_, curBP.m_123342_() + (int)vec.f_82480_, curBP.m_123343_() + (int)vec.f_82481_);

         for (int i = 0; i < 4; i++) {
            for (Vec3i v : Utils.circlePosRad9) {
               Utils.destroyBlock(
                  world,
                  new BlockPos(tgtOffset.m_123341_() + v.m_123341_(), tgtOffset.m_123342_() + v.m_123342_() + i, tgtOffset.m_123343_() + v.m_123343_()),
                  false
               );
            }
         }

         for (Vec3i v : Utils.circlePosRad7) {
            Utils.destroyBlock(
               world,
               new BlockPos(tgtOffset.m_123341_() + v.m_123341_(), tgtOffset.m_123342_() + v.m_123342_() + 4, tgtOffset.m_123343_() + v.m_123343_()),
               false
            );
         }

         for (Vec3i v : Utils.circlePosRad5) {
            Utils.destroyBlock(
               world,
               new BlockPos(tgtOffset.m_123341_() + v.m_123341_(), tgtOffset.m_123342_() + v.m_123342_() + 5, tgtOffset.m_123343_() + v.m_123343_()),
               false
            );
         }
      }
   }

   private void fireScop(Level world) {
      if (!world.m_5776_()) {
         BlockPos curBP = this.m_20183_();
         Vec3 vec = new Vec3(0.0, 0.0, 8.0);
         vec = vec.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         BlockPos tgtOffset = new BlockPos(curBP.m_123341_() + (int)vec.f_82479_, curBP.m_123342_() + (int)vec.f_82480_, curBP.m_123343_() + (int)vec.f_82481_);

         for (Vec3i v : Utils.circlePosRad9) {
            Utils.destroyBlock(
               world, new BlockPos(tgtOffset.m_123341_() + v.m_123341_(), tgtOffset.m_123342_() - 0, tgtOffset.m_123343_() + v.m_123343_()), false
            );
         }

         for (Vec3i v : Utils.circlePosRad9) {
            Utils.destroyBlock(
               world, new BlockPos(tgtOffset.m_123341_() + v.m_123341_(), tgtOffset.m_123342_() - 1, tgtOffset.m_123343_() + v.m_123343_()), false
            );
         }

         for (Vec3i v : Utils.circlePosRad7) {
            Utils.destroyBlock(
               world, new BlockPos(tgtOffset.m_123341_() + v.m_123341_(), tgtOffset.m_123342_() - 2, tgtOffset.m_123343_() + v.m_123343_()), false
            );
         }

         for (Vec3i v : Utils.circlePosRad5) {
            Utils.destroyBlock(
               world, new BlockPos(tgtOffset.m_123341_() + v.m_123341_(), tgtOffset.m_123342_() - 3, tgtOffset.m_123343_() + v.m_123343_()), false
            );
         }
      }
   }

   private void fireGatring(Level world) {
      if (!world.m_5776_()) {
         BulletEntity be = new BulletEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET.get(), world, this);
         Vec3 offset = this.posHistory.getFirst();
         Vec3 muzzlPos = new Vec3(-1.65, 2.0, 3.5);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         be.m_146884_(offset.m_82549_(muzzlPos));
         float[] angle = this.getShootingAngle(be, true);
         be.m_37251_(be, angle[0], angle[1], this.m_21256_(), 0.9F, 2.0F);
         world.m_7967_(be);
      } else if (this.f_19797_ % 7 == 0) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GATLING_EVENT.get());
      }
   }

   private void firePilebunker(Level world) {
      Entity driver = this.getDrivingPassenger();
      Vec3 pilePos1 = new Vec3(6.5, 3.0, 18.0).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_())).m_82549_(this.m_20182_());
      Vec3 pilePos2 = new Vec3(-2.0, -3.0, -2.0).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_())).m_82549_(this.m_20182_());
      Vec3 kbVel = new Vec3(0.0, 0.0, -1.0).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));

      for (Entity ent : world.m_45933_(null, new AABB(pilePos1, pilePos2))) {
         if (!this.isSelf(ent) && ent instanceof LivingEntity le) {
            if (!world.m_5776_()) {
               le.m_147240_(2.0, kbVel.f_82479_, kbVel.f_82481_);
               DamageSource ds;
               if (driver instanceof Player p) {
                  ds = this.m_269291_().m_269075_(p);
               } else {
                  ds = this.m_269291_().m_269264_();
               }

               le.m_6469_(ds, BattleBalance.MECH_PILE_DAMAGE);
            } else {
               this.addHitParticles(le);
            }
         }
      }

      if (this.m_9236_().f_46443_) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get());
      }
   }

   private void addHitParticles(Entity target) {
      Vec3 offset = new Vec3(target.m_20182_().f_82479_, target.m_20191_().m_82399_().f_82480_, target.m_20182_().f_82481_);

      for (int i = 0; i < 40; i++) {
         double velocityX = this.f_19796_.m_188500_() * 4.3 - 1.0;
         double velocityY = this.f_19796_.m_188500_() * 4.3 - 1.0;
         double velocityZ = this.f_19796_.m_188500_() * 4.3 - 1.0;
         this.m_9236_()
            .m_6485_((ParticleOptions)PomkotsMechs.SPARK.get(), true, offset.m_7096_(), offset.m_7098_(), offset.m_7094_(), velocityX, velocityY, velocityZ);
      }
   }

   private void fireMissile(Level world, int slot) {
      if (!world.m_5776_()) {
         Vec3 offset = this.posHistory.getFirst();
         Vec3 muzzlPos = new Vec3(-1.0, 3.5, 0.8F);
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

         MissileVerticalEntity be = new MissileVerticalEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_VERTICAL.get(), world, this, target
         );
         be.m_146884_(offset.m_82549_(worldMuzzlPos));
         be.m_37251_(be, -89.0F, this.m_146908_(), this.m_21256_(), 1.0F, 0.0F);
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

   private void fireGrenade(Level world) {
      if (!world.m_5776_()) {
         GrenadeEntity be = new GrenadeEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.GRENADE.get(),
            world,
            this,
            BattleBalance.MECH_GRENADE_DAMAGE,
            BattleBalance.MECH_GRENADE_EXPLOSION
         );
         Vec3 offset = this.posHistory.getFirst();
         Vec3 muzzlPos = new Vec3(1.65, 3.0, 3.5);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         be.m_146884_(offset.m_82549_(muzzlPos));
         float[] angle = this.getShootingAngle(be, false);
         be.m_37251_(be, angle[0], angle[1], this.m_21256_(), 0.9F, 0.0F);
         world.m_7967_(be);
         Vec3 knockBackvel = new Vec3(0.0, 0.0, -3.0).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         this.m_246865_(knockBackvel);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GRENADE_EVENT.get());
      }
   }

   @Override
   protected PlayState controllAnimationWeapons(AnimationState<PomkotsVehicleBase> event) {
      if (this.actionController.getAction(5).isInAction()) {
         if (this.actionController.getAction(5).isOnStart()) {
            event.getController().forceAnimationReset();
         }

         return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".pilebunker"));
      } else if (this.actionController.getAction(6).isInAction()) {
         if (this.actionController.getAction(6).isOnStart()) {
            event.getController().forceAnimationReset();
         }

         return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".grenade"));
      } else if (this.actionController.getAction(2).isInAction()) {
         if (this.actionController.getAction(2).isOnStart()) {
            event.getController().forceAnimationReset();
         }

         return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".hummer"));
      } else if (this.actionController.getAction(3).isInAction()) {
         if (this.actionController.getAction(3).isOnStart()) {
            event.getController().forceAnimationReset();
         }

         return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".scop"));
      } else {
         return null;
      }
   }

   @Override
   protected void addExtraAnimationController(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               "gatling",
               0,
               event -> {
                  if (this.actionController.getAction(4).isInAction()) {
                     if (this.actionController.getAction(4).isOnStart()) {
                        event.getController().forceAnimationReset();
                     }

                     return !this.actionController.getAction(4).isInFire()
                        ? event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".gatringgun1"))
                        : event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".gatringgun2"));
                  } else {
                     return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"));
                  }
               }
            )
         }
      );
      controllers.add(new AnimationController[]{new AnimationController(this, "weapons", 0, event -> {
         if (this.actionController.getAction(6).isInAction()) {
            if (this.actionController.getAction(6).isOnStart()) {
               event.getController().forceAnimationReset();
            }

            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".weapon_grenade"));
         } else if (this.actionController.getAction(7).isInAction()) {
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

   @Override
   public boolean shouldLockMulti(DriverInput driverInput) {
      return driverInput.isWeaponRightShoulderPressed();
   }

   @Override
   public boolean shouldLockWeak(DriverInput driverInput) {
      return driverInput.isWeaponRightHandPressed();
   }

   @Override
   public boolean shouldLockStrong(DriverInput driverInput) {
      return driverInput.isLockPressed();
   }
}
