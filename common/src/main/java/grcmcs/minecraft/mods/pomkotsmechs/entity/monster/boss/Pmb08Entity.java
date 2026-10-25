package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.HelicopterHoverMoveGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissilePodEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;

public class Pmb08Entity extends BaseBossEntity {
   private float missileModifier = 2.0F;

   @Override
   public String getMechName() {
      return "pmb08";
   }

   public Pmb08Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.m_20242_(true);
      this.actionController.registerAction("missile_straight", new BossActionController.BossAction(60, 20, this::missileStraightAction));
      this.actionController.registerAction("missile_horizontal", new BossActionController.BossAction(60, 20, this::missileHorizontalAction));
      this.actionController.registerAction("missile_vertical", new BossActionController.BossAction(60, 20, this::missileVerticalAction));
      this.actionController.registerAction("missile_pod", new BossActionController.BossAction(200, 20, this::missilePodAction));
      this.actionController.registerAction("missile_pod2", new BossActionController.BossAction(200, 20, this::missilePodAction2));
      this.actionController.registerAction("gatling", new BossActionController.BossAction(40, 5, this::gatlingAction));
      this.actionController.registerAction("onsmalldown", new BossActionController.BossAction(20, 15, this::smallDown));
      this.f_21345_.m_25352_(1, new HelicopterHoverMoveGoal(this, 2.0, 15.0, 30.0, 20));
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("gatling"), this, true, 5, true), AI_MODE_ALL, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile_straight"), this, true, true), AI_MODE_ALL, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile_horizontal"), this, false), AI_MODE_ALL, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile_vertical"), this, false), new int[]{2, 3, 4}, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile_pod2"), this, true, true), new int[]{2, 3, 4}, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile_pod"), this, true, true), new int[]{3, 4}, 10);
   }

   @Override
   public void m_8119_() {
      if (this.f_19803_ && this.isServerSide()) {
         this.registerAdditionalHitBox(new BossHitBoxEntity((EntityType<? extends LivingEntity>)PomkotsMechs.HITBOX_PMB08.get(), this.m_9236_(), this));
      }

      this.m_20242_(true);
      super.m_8119_();
   }

   private void gatlingAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "gatling");
         } else if (action.currentActionTick == 3) {
            for (int side = -1; side < 2; side += 2) {
               BulletGrenadeEntity be = new BulletGrenadeEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE.get(), this.m_9236_(), this, this.getMechData().bulletDamage
               );
               be.m_20242_(true);
               be.setExplosionScale(1);
               be.setStunPoint(2);
               Vec3 offset = this.m_20182_();
               Vec3 muzzlPos = new Vec3(8 * side, 10.0, 3.0);
               muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
               be.m_146884_(offset.m_82549_(muzzlPos));
               float[] angle = Utils.getShootingAngle(be, target, true);
               be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().bulletSpeed, 0.0F);
               this.m_9236_().m_7967_(be);
               ParticleUtil.spawnAttachedMuzzleFlash((ServerLevel)this.m_9236_(), this, 50.0, new Vec3(8 * side, 10.0, 6.0));
            }
         }
      }
   }

   private void missileStraightAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");
         } else if (action.currentActionTick == 2) {
            for (int row = 0; row < 2; row++) {
               for (int col = 0; col < 2; col++) {
                  for (int side = -1; side < 2; side += 2) {
                     MissileGenericEntity be = new MissileGenericEntity(
                        (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                        this.m_9236_(),
                        this,
                        target,
                        this.getMechData().missileDamage,
                        this.getMechData().missileSpeed * this.missileModifier
                     );
                     be.setMaxRotationAnglePerTick(2.0F);
                     Vec3 offset = this.m_20182_();
                     float slotX = (col * 0.8F + 15.0F) * side;
                     float slotY = row + 3.0F;
                     Vec3 muzzlPos = new Vec3(slotX, slotY, 3.0);
                     muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
                     be.m_146884_(offset.m_82549_(muzzlPos));
                     float[] angle = Utils.getShootingAngle(be, target, true);
                     be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().missileSpeed, 0.0F);
                     this.m_9236_().m_7967_(be);
                  }
               }
            }
         }
      }
   }

   private void missileHorizontalAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");
         } else if (action.currentActionTick == 2) {
            for (int row = 0; row < 2; row++) {
               for (int col = 0; col < 2; col++) {
                  for (int side = -1; side < 2; side += 2) {
                     MissileGenericEntity be = new MissileGenericEntity(
                        (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                        this.m_9236_(),
                        this,
                        target,
                        this.getMechData().missileDamage,
                        this.getMechData().missileSpeed * this.missileModifier
                     );
                     be.setMaxRotationAnglePerTick(2.0F);
                     Vec3 offset = this.m_20182_();
                     float slotX = (col * 0.8F + 15.0F) * side;
                     float slotY = row + 3.0F;
                     Vec3 muzzlPos = new Vec3(slotX, slotY, 3.0);
                     muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
                     be.m_146884_(offset.m_82549_(muzzlPos));
                     float[] angle = Utils.getShootingAngle(be, target, true);
                     be.m_37251_(be, angle[0], angle[1] - side * 30, this.m_21256_(), this.getMechData().missileSpeed, 0.0F);
                     this.m_9236_().m_7967_(be);
                  }
               }
            }
         }
      }
   }

   private void missileVerticalAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");
         } else if (action.currentActionTick == 2) {
            for (int row = 0; row < 2; row++) {
               for (int col = 0; col < 2; col++) {
                  for (int side = -1; side < 2; side += 2) {
                     MissileGenericEntity be = new MissileGenericEntity(
                        (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                        this.m_9236_(),
                        this,
                        target,
                        this.getMechData().missileDamage,
                        this.getMechData().missileSpeed * this.missileModifier
                     );
                     be.setMaxRotationAnglePerTick(7.0F);
                     Vec3 offset = this.m_20182_();
                     float slotX = (col * 0.8F + 15.0F) * side;
                     float slotY = row + 3.0F;
                     Vec3 muzzlPos = new Vec3(slotX, slotY, 3.0);
                     muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
                     be.m_146884_(offset.m_82549_(muzzlPos));
                     float[] angle = Utils.getShootingAngle(be, target, true);
                     be.m_37251_(be, -80.0F, angle[1], this.m_21256_(), this.getMechData().missileSpeed, 0.0F);
                     this.m_9236_().m_7967_(be);
                  }
               }
            }
         }
      }
   }

   private void missilePodAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");
         } else if (action.currentActionTick == 2) {
            for (int side = -1; side < 2; side += 2) {
               MissilePodEntity be = new MissilePodEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_POD.get(),
                  this.m_9236_(),
                  this,
                  target,
                  this.getMechData().missileDamage,
                  this.getMechData().missileSpeed * this.missileModifier
               );
               Vec3 offset = this.m_20182_();
               Vec3 muzzlPos = new Vec3(15 * side, 3.5, 7.0);
               muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
               be.m_146884_(offset.m_82549_(muzzlPos));
               float[] angle = Utils.getShootingAngle(be, target, true);
               be.m_37251_(be, -15.0F, angle[1], this.m_21256_(), this.getMechData().missileSpeed, 0.0F);
               this.m_9236_().m_7967_(be);
            }
         }
      }
   }

   private void missilePodAction2(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");
         } else if (action.currentActionTick == 2) {
            for (int side = -1; side < 2; side += 2) {
               MissilePodEntity be = new MissilePodEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_POD.get(),
                  this.m_9236_(),
                  this,
                  target,
                  this.getMechData().missileDamage,
                  this.getMechData().missileSpeed * this.missileModifier
               );
               be.setMode(MissilePodEntity.Mode.SPHERE);
               Vec3 offset = this.m_20182_();
               Vec3 muzzlPos = new Vec3(15 * side, 3.5, 7.0);
               muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
               be.m_146884_(offset.m_82549_(muzzlPos));
               float[] angle = Utils.getShootingAngle(be, target, true);
               be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().missileSpeed * this.missileModifier, 0.0F);
               this.m_9236_().m_7967_(be);
            }
         }
      }
   }

   @Override
   protected void onStun() {
      this.triggerAnim("action_controller", "on_stun");
   }

   @Override
   protected void offStun() {
      this.triggerAnim("action_controller", "off_stun");
   }

   @Override
   protected void onSmallDown() {
      this.actionController.reset();
      this.actionController.getAction("onsmalldown").startAction();
      this.triggerAnim("action_controller", "on_small_down");
   }

   private void smallDown(BossActionController.BossAction action) {
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               "basic_move",
               0,
               event -> {
                  if (this.getAiMode() == -2) {
                     return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.inactive"));
                  } else {
                     return event.isMoving()
                        ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.idle"))
                        : event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.idle"));
                  }
               }
            )
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(
                  this,
                  "rotor",
                  1,
                  event -> this.getAiMode() == -2
                     ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.idle"))
                     : event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.rotor"))
               )
               .setSoundKeyframeHandler(this::playSounds)
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "action_controller", state -> PlayState.STOP)
               .triggerableAnim("dash", RawAnimation.begin().thenPlay("animation.pmb03.boost1").thenLoop("animation.pmb03.boost2"))
               .triggerableAnim("gatling", RawAnimation.begin().thenPlayXTimes("animation.pmb03.gatling", 5))
               .triggerableAnim("missile", RawAnimation.begin().thenPlay("animation.pmb03.missilepod"))
               .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb03.boot"))
               .triggerableAnim("on_stun", RawAnimation.begin().thenPlay("animation.pmb03.hurt").thenPlayAndHold("animation.pmb03.down"))
               .triggerableAnim("off_stun", RawAnimation.begin().thenPlay("animation.pmb03.up"))
               .triggerableAnim("on_small_down", RawAnimation.begin().thenPlay("animation.pmb03.hurt"))
               .setSoundKeyframeHandler(this::playSounds)
         }
      );
   }

   @Override
   public void boot() {
      super.boot();
      this.triggerAnim("action_controller", "boot");
   }

   @Override
   protected void applyPlayerControll() {
   }
}
