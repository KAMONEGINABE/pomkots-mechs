package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BossAerialDiveGoal2;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BossDashAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BossDashGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossWalkGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthraiseEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.WaveHorizontalEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEnemyEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;

public class Pmb04Entity extends BaseBossEntity {
   private final AnimationController<Pmb04Entity> trigger = new AnimationController(this, "action_controller", state -> PlayState.STOP)
      .triggerableAnim("saber_tate", RawAnimation.begin().thenPlay("animation.pmb01.attacktate"))
      .triggerableAnim("saber_upper", RawAnimation.begin().thenPlay("animation.pmb01.attackupper"))
      .triggerableAnim("saber_circle", RawAnimation.begin().thenPlay("animation.pmb01.attackcircle"))
      .triggerableAnim("saber_jump1", RawAnimation.begin().thenPlay("animation.pmb01.attackjump1").thenLoop("animation.pmb01.attackjump2"))
      .triggerableAnim("saber_jump2", RawAnimation.begin().thenPlay("animation.pmb01.attackjump3"))
      .triggerableAnim("gatling", RawAnimation.begin().thenPlayXTimes("animation.pmb01.attackgatling", 5))
      .triggerableAnim("missile", RawAnimation.begin().thenPlay("animation.pmb01.attackmissile"))
      .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.pmb01.jump"))
      .triggerableAnim("onground", RawAnimation.begin().thenPlay("animation.pmb01.onground"))
      .triggerableAnim("hurt", RawAnimation.begin().thenPlay("animation.pmb01.hurt"))
      .triggerableAnim("down", RawAnimation.begin().thenPlay("animation.pmb01.down"))
      .triggerableAnim("break", RawAnimation.begin().thenPlay("animation.pmb01.bodyfrontbreak"))
      .triggerableAnim("dash", RawAnimation.begin().thenPlay("animation.pmb01.dash1").thenLoop("animation.pmb01.dash2"))
      .triggerableAnim("dash_attack", RawAnimation.begin().thenPlay("animation.pmb01.dashAttack1").thenLoop("animation.pmb01.dashAttack2"))
      .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.pmb01.stop"))
      .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb01.boot"))
      .triggerableAnim("on_stun", RawAnimation.begin().thenPlay("animation.pmb01.hurt").thenPlayAndHold("animation.pmb01.down"))
      .triggerableAnim("off_stun", RawAnimation.begin().thenPlay("animation.pmb01.up"))
      .triggerableAnim("on_small_down", RawAnimation.begin().thenPlay("animation.pmb01.hurt"))
      .setSoundKeyframeHandler(this::playSounds);
   private final AnimationController<Pmb04Entity> base = new AnimationController(
         this,
         "basic_move",
         0,
         event -> {
            if (!this.trigger.isPlayingTriggeredAnimation()) {
               if (this.getAiMode() == -2) {
                  return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.inactive"));
               }

               if (event.isMoving()) {
                  if (!this.m_20068_()) {
                     BaseBossEntity.MoveDirection dir = this.getDominantMoveDirection();
                     if (dir == BaseBossEntity.MoveDirection.LEFT) {
                        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.walk_right"));
                     } else {
                        return dir == BaseBossEntity.MoveDirection.RIGHT
                           ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.walk_left"))
                           : event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.walk"));
                     }
                  } else {
                     return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.fly"));
                  }
               } else {
                  return !this.m_20068_()
                     ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.idle"))
                     : event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.fly"));
               }
            } else {
               event.getController().forceAnimationReset();
               return PlayState.STOP;
            }
         }
      )
      .setSoundKeyframeHandler(this::playSounds);

   @Override
   public String getMechName() {
      return "pmb04";
   }

   public Pmb04Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.alwaysLookAtTarget = false;
      this.actionController.registerAction("saber_tate", new BossActionController.BossAction(40, 16, this::saber_tate));
      this.actionController.registerAction("saber_upper", new BossActionController.BossAction(40, 20, this::saber_upper));
      this.actionController.registerAction("saber_circle", new BossActionController.BossAction(40, 30, this::saber_circle));
      this.actionController.registerAction("wave_horizontal", new BossActionController.BossAction(40, 30, this::wave_horizontal));
      this.actionController.registerAction("saber_jump", new BossActionController.BossAction(80, 60, this::saber_jump));
      this.actionController.registerAction("gatling", new BossActionController.BossAction(40, 5, this::gatlingAction));
      this.actionController.registerAction("missile", new BossActionController.BossAction(40, 21, this::missileHorizontalAction));
      this.actionController.registerAction("missilev", new BossActionController.BossAction(40, 21, this::missileVerticalAction));
      this.actionController.registerAction("onground", new BossActionController.BossAction(20, 15, this::onGroundAction));
      this.actionController.registerAction("onsmalldown", new BossActionController.BossAction(20, 15, this::smallDown));
      this.registerActionGoal(new SimpleBossWalkGoal(this, this.getMechData().speed, 10.0), AI_MODE_ALL, 10);
      this.registerActionGoal(new BossAerialDiveGoal2(this, 10.0, 3.0, 3.0, 3.0, 20.0, 100), AI_MODE_ALL, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("saber_tate"), this, false), AI_MODE_ALL, 0.0F, 30.0F, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("saber_circle"), this, false), AI_MODE_ALL, 0.0F, 30.0F, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("wave_horizontal"), this, false), new int[]{2, 3, 4}, 10);
      this.registerActionGoal(new BossDashGoal(this, this.getMechData().speed * 8.0F, 15.0, 40.0, 20.0, 10, 0.3), AI_MODE_ALL, 20);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("saber_jump"), this, false), new int[]{2, 3, 4}, 20);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile"), this, true, true), new int[]{2, 3, 4}, 15);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missilev"), this, true, true), AI_MODE_ALL, 15);
      this.registerActionGoal(new BossDashAttackGoal(this, this.getMechData().speed * 6.0F, 15.0, 40.0, 15.0, 30, 0.3), new int[]{3, 4}, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("gatling"), this, true, 5, true), new int[]{2, 3, 4}, 15);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("saber_upper"), this, false), new int[]{2, 3, 4}, 15);
      this.m_20242_(false);
   }

   @Override
   public void m_8119_() {
      if (this.f_19803_ && this.isServerSide()) {
         this.registerAdditionalHitBox(new BossHitBoxEntity((EntityType<? extends LivingEntity>)PomkotsMechs.HITBOX_PMB03.get(), this.m_9236_(), this));
      }

      super.m_8119_();
   }

   private void saber_tate(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "saber_tate");
         } else if (action.currentActionTick == 8) {
            this.rangeAttack(new Vec3(10.0, 18.0, 30.0), new Vec3(-10.0, -3.0, -5.0), this.getMechData().meleeDamage, 2);
         }
      }
   }

   private void saber_circle(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "saber_circle");
         } else if (action.currentActionTick == 8) {
            this.rangeAttack(new Vec3(20.0, 10.0, 20.0), new Vec3(-20.0, -3.0, -20.0), this.getMechData().meleeDamage * 2.0F / 3.0F, 2);
         }
      }
   }

   private void wave_horizontal(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "saber_circle");
         } else if (action.currentActionTick == 8) {
            WaveHorizontalEntity be = new WaveHorizontalEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.WAVE_HOR.get(), this.m_9236_());
            be.m_20242_(true);
            Vec3 offset = this.m_20182_();
            Vec3 muzzlPos = new Vec3(0.0, 8.0, 10.0);
            muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
            be.m_146884_(offset.m_82549_(muzzlPos));
            float[] angle = Utils.getShootingAngle(be, target, true);
            be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().bulletSpeed, 0.0F);
            this.m_9236_().m_7967_(be);
         }
      }
   }

   private void saber_upper(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "saber_upper");
         } else if (action.currentActionTick == 8) {
            Vec3 offset = this.m_20182_();
            Vec3 muzzlPos = new Vec3(0.0, 0.0, 10.0);
            muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
            Vec3 vec = muzzlPos.m_82541_().m_82542_(6.0, 6.0, 6.0);
            muzzlPos = offset.m_82549_(muzzlPos);
            EarthraiseEntity be = new EarthraiseEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EARTHRAISE.get(), this.m_9236_(), vec, this, 8);
            be.m_146884_(muzzlPos);
            be.m_146922_((float)(Math.toDegrees(Math.atan2(vec.f_82481_, vec.f_82479_)) - 90.0));
            this.m_9236_().m_7967_(be);
         }
      }
   }

   private void saber_jump(BossActionController.BossAction action) {
      float attackRange = 20.0F;
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "saber_jump1");
         } else if (action.currentActionTick > 2 && action.currentActionTick < 10) {
            this.rotateToTarget(target);
            this.f_19812_ = true;
         } else if (action.currentActionTick == 10) {
            Vec3 startPos = this.m_20182_();
            Vec3 targetPos = target.m_20182_();
            double deltaX = targetPos.f_82479_ - startPos.f_82479_;
            double deltaY = targetPos.f_82480_ - startPos.f_82480_;
            double deltaZ = targetPos.f_82481_ - startPos.f_82481_;
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            double gravity = this.gravity;
            double velocityY = Math.sqrt(gravity * Math.max(4.0, deltaY + 4.0));
            double time = Math.sqrt(2.0 * (deltaY + velocityY * velocityY / (2.0 * gravity)) / gravity);
            double velocityX = deltaX / time;
            double velocityZ = deltaZ / time;
            double maxHorizontalSpeed = 10.0;
            double horizontalSpeed = Math.sqrt(velocityX * velocityX + velocityZ * velocityZ);
            if (horizontalSpeed > maxHorizontalSpeed) {
               velocityX = velocityX / horizontalSpeed * maxHorizontalSpeed;
               velocityZ = velocityZ / horizontalSpeed * maxHorizontalSpeed;
            }

            this.m_20334_(velocityX, velocityY, velocityZ);
            this.f_19812_ = true;
         } else if (action.currentActionTick > 11
            && action.currentActionTick < action.maxActionTick - 12
            && (this.m_20096_() || this.m_20280_(target) <= attackRange * attackRange || action.onEndOfAction())) {
            this.rangeAttack(new Vec3(20.0, 10.0, 20.0), new Vec3(-20.0, -6.0, -20.0), this.getMechData().meleeDamage * 3.0F, 2);
            this.triggerAnim("action_controller", "saber_jump2");
            action.currentActionTick = action.maxActionTick - 11;
         }
      }
   }

   private void saber_jump2(BossActionController.BossAction action) {
      float attackRange = 20.0F;
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "saber_jump1");
         } else if (action.currentActionTick > 2 && action.currentActionTick < 10) {
            this.rotateToTarget(target);
            this.f_19812_ = true;
         } else if (action.currentActionTick == 10) {
            Vec3 startPos = this.m_20182_();
            Vec3 targetPos = target.m_20182_();
            double dx = targetPos.f_82479_ - startPos.f_82479_;
            double dy = targetPos.f_82480_ - startPos.f_82480_;
            double dz = targetPos.f_82481_ - startPos.f_82481_;
            int flightTicks = 8;
            double gravity = 0.18;
            double vx = dx / flightTicks;
            double vz = dz / flightTicks;
            double vy = (dy + gravity * flightTicks * (flightTicks + 1) / 2.0) / flightTicks;
            double maxHSpeed = 12.0;
            double hSpeed = Math.sqrt(vx * vx + vz * vz);
            if (hSpeed > maxHSpeed) {
               vx = vx / hSpeed * maxHSpeed;
               vz = vz / hSpeed * maxHSpeed;
            }

            this.m_20334_(vx, vy, vz);
            this.f_19812_ = true;
         } else if (action.currentActionTick > 11
            && action.currentActionTick < action.maxActionTick - 12
            && (this.m_20096_() || this.m_20280_(target) <= attackRange * attackRange || action.onEndOfAction())) {
            this.rangeAttack(new Vec3(20.0, 10.0, 20.0), new Vec3(-20.0, -6.0, -20.0), this.getMechData().meleeDamage * 3.0F, 2);
            this.triggerAnim("action_controller", "saber_jump2");
            action.currentActionTick = action.maxActionTick - 11;
         }
      }
   }

   private void gatlingAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "gatling");
         } else if (action.currentActionTick == 3) {
            BulletGrenadeEntity be = new BulletGrenadeEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE.get(), this.m_9236_(), this, this.getMechData().bulletDamage
            );
            be.m_20242_(true);
            be.setExplosionScale(1);
            be.setStunPoint(2);
            Vec3 offset = this.m_20182_();
            Vec3 muzzlPos = new Vec3(7.0, 14.0, 10.0);
            muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
            be.m_146884_(offset.m_82549_(muzzlPos));
            float[] angle = Utils.getShootingAngle(be, target, true);
            be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().bulletSpeed, 0.0F);
            this.m_9236_().m_7967_(be);
            ParticleUtil.spawnAttachedMuzzleFlash((ServerLevel)this.m_9236_(), this, 50.0, new Vec3(7.0, 11.0, 17.0));
         }
      }
   }

   private void missileHorizontalAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");
         } else if (action.currentActionTick == 15) {
            for (int row = 0; row < 4; row++) {
               for (int col = 0; col < 1; col++) {
                  for (int side = -1; side < 2; side += 2) {
                     MissileGenericEnemyEntity be = new MissileGenericEnemyEntity(
                        (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                        this.m_9236_(),
                        this,
                        target,
                        this.getMechData().missileDamage,
                        this.getMechData().missileSpeed * 2.0F
                     );
                     be.setMaxRotationAnglePerTick(2.0F);
                     Vec3 offset = this.m_20182_();
                     float slotZ = col * 0.8F + 8.0F;
                     float slotY = row + 8.0F;
                     Vec3 muzzlPos = new Vec3(side * 7, slotY, slotZ);
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
         } else if (action.currentActionTick == 15) {
            for (int row = 0; row < 3; row++) {
               for (int col = 0; col < 1; col++) {
                  for (int side = -1; side < 2; side += 2) {
                     MissileGenericEntity be = new MissileGenericEntity(
                        (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                        this.m_9236_(),
                        this,
                        target,
                        this.getMechData().missileDamage,
                        this.getMechData().missileSpeed * 2.0F
                     );
                     be.setMaxRotationAnglePerTick(7.0F);
                     Vec3 offset = this.m_20182_();
                     float slotZ = -row * 0.8F;
                     Vec3 muzzlPos = new Vec3(side * 10, 16.0, slotZ);
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

   private void onGroundAction(BossActionController.BossAction action) {
      if (action.onStartOfAction()) {
         this.triggerAnim("action_controller", "onground");
         Level level = this.m_9236_();
         BlockPos landingPos = this.m_20183_();
         AABB damageArea = new AABB(landingPos).m_82400_(10.0);

         for (LivingEntity entity : level.m_45976_(LivingEntity.class, damageArea)) {
            if (!this.isSelf(entity)) {
               entity.m_6469_(this.m_269291_().m_269264_(), 30.0F);
               Vec3 knockback = entity.m_20182_().m_82546_(this.m_20182_()).m_82541_().m_82490_(1.5);
               entity.m_147240_(2.0, knockback.f_82479_, knockback.f_82481_);
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
   public void boot() {
      super.boot();
      this.triggerAnim("action_controller", "boot");
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController[]{this.base});
      controllers.add(new AnimationController[]{this.trigger});
   }

   @Override
   protected void applyPlayerControll() {
   }
}
