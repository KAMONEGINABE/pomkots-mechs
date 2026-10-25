package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BossAerialDiveGoal2;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossWalkGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthbreakEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthraiseEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.SlashEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeLargeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEnemyEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericLargeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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

public class Pmb01mk2Entity extends BaseBossEntity {
   public static final float DEFAULT_SCALE = 2.0F;
   private boolean punched = false;
   private final AnimationController<Pmb01mk2Entity> trigger = new AnimationController(this, "action_controller", state -> PlayState.STOP)
      .triggerableAnim("attackpunch", RawAnimation.begin().thenPlay("animation.pmb01.attackpunch"))
      .triggerableAnim("attackupper", RawAnimation.begin().thenPlay("animation.pmb01.attackupper"))
      .triggerableAnim("attackjump", RawAnimation.begin().thenPlay("animation.pmb01.attackjump"))
      .triggerableAnim("attackgatling", RawAnimation.begin().thenPlayXTimes("animation.pmb01.attackgatling", 5))
      .triggerableAnim("attackgrenade", RawAnimation.begin().thenPlay("animation.pmb01.attackgrenade"))
      .triggerableAnim("attackmissile", RawAnimation.begin().thenPlay("animation.pmb01.attackmissile"))
      .triggerableAnim("attacklargemissile", RawAnimation.begin().thenPlay("animation.pmb01.attacklargemissile"))
      .triggerableAnim("attack_arial_punch_01", RawAnimation.begin().thenPlay("animation.pmb01.attack_arial_punch_01"))
      .triggerableAnim("attack_arial_punch_fly", RawAnimation.begin().thenLoop("animation.pmb01.fly"))
      .triggerableAnim("attack_arial_punch_02", RawAnimation.begin().thenPlay("animation.pmb01.attack_arial_punch_02"))
      .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.pmb01.jump"))
      .triggerableAnim("onground", RawAnimation.begin().thenPlay("animation.pmb01.onground"))
      .triggerableAnim("hurt", RawAnimation.begin().thenPlay("animation.pmb01.hurt"))
      .triggerableAnim("down", RawAnimation.begin().thenPlay("animation.pmb01.down"))
      .triggerableAnim("break", RawAnimation.begin().thenPlay("animation.pmb01.bodyfrontbreak"))
      .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.pmb01.stop"))
      .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb01.boot"))
      .triggerableAnim("on_stun", RawAnimation.begin().thenPlay("animation.pmb01.hurt").thenPlayAndHold("animation.pmb01.down"))
      .triggerableAnim("off_stun", RawAnimation.begin().thenPlay("animation.pmb01.up"))
      .triggerableAnim("on_small_down", RawAnimation.begin().thenPlay("animation.pmb01.hurt"))
      .triggerableAnim("reset", RawAnimation.begin().thenPlay("animation.pmb01.idle"))
      .setSoundKeyframeHandler(this::playSounds);
   private final AnimationController<Pmb01mk2Entity> base = new AnimationController(
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
      return "pmb01mk2";
   }

   public Pmb01mk2Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.alwaysLookAtTarget = false;
      this.actionController.registerAction("punch1", new BossActionController.BossAction(40, 20, this::punchAction1));
      this.actionController.registerAction("punch2", new BossActionController.BossAction(40, 20, this::punchAction2));
      this.actionController.registerAction("upper", new BossActionController.BossAction(40, 20, this::upperAction));
      this.actionController.registerAction("jump_attack", new BossActionController.BossAction(200, 25, this::jumpAttackAction));
      this.actionController.registerAction("aerial_attack", new BossActionController.BossAction(200, 100, this::aerial_punch));
      this.actionController.registerAction("gatling", new BossActionController.BossAction(40, 5, this::gatlingAction));
      this.actionController.registerAction("grenade", new BossActionController.BossAction(40, 20, this::grenadeAction));
      this.actionController.registerAction("missile", new BossActionController.BossAction(40, 21, this::missileHorizontalAction));
      this.actionController.registerAction("missilev", new BossActionController.BossAction(40, 21, this::missileVerticalAction));
      this.actionController.registerAction("large_missile", new BossActionController.BossAction(600, 20, this::missileLargeAction));
      this.actionController.registerAction("onground", new BossActionController.BossAction(20, 15, this::onGroundAction));
      this.actionController.registerAction("break", new BossActionController.BossAction(20, 95, this::breakAction));
      this.actionController.registerAction("onsmalldown", new BossActionController.BossAction(20, 15, this::smallDown));
      this.registerActionGoal(new SimpleBossWalkGoal(this, this.getMechData().speed, 30.0), AI_MODE_ALL, 10);
      this.registerActionGoal(new BossAerialDiveGoal2(this, 10.0, 3.0, 3.0, 3.0, 20.0, 100), AI_MODE_ALL, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("gatling"), this, true, 5, true), AI_MODE_ALL, 20);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile"), this, true, true), AI_MODE_ALL, 20);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missilev"), this, true, true), AI_MODE_ALL, 20);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("punch1"), this, false), AI_MODE_ALL, 0.0F, 30.0F, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("upper"), this, false), new int[]{2, 3, 4}, 0.0F, 50.0F, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("grenade"), this, true, true), new int[]{2, 3, 4}, 15);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("aerial_attack"), this, false), new int[]{2, 3, 4}, 50.0F, 300.0F, 20);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("punch2"), this, false), new int[]{3, 4}, 0.0F, 50.0F, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("jump_attack"), this, false), new int[]{3, 4}, 0.0F, 50.0F, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("large_missile"), this, true, true), new int[]{4}, 10);
      this.m_20242_(false);
   }

   @Override
   public void m_8119_() {
      if (this.f_19803_ && this.isServerSide()) {
         this.registerAdditionalHitBox(new BossHitBoxEntity((EntityType<? extends LivingEntity>)PomkotsMechs.HITBOX_PMB03.get(), this.m_9236_(), this));
         this.m_20242_(false);
      }

      super.m_8119_();
   }

   private void punchAction1(BossActionController.BossAction action) {
      if (action.onStartOfAction()) {
         this.triggerAnim("action_controller", "attackpunch");
      } else if (action.currentActionTick == 15) {
         this.rangeAttack(new Vec3(20.0, 10.0, 20.0), new Vec3(-20.0, -3.0, -20.0), this.getMechData().meleeDamage, 2);
      }
   }

   private void punchAction2(BossActionController.BossAction action) {
      if (action.onStartOfAction()) {
         this.triggerAnim("action_controller", "attackpunch");
      } else if (action.currentActionTick == 15) {
         Vec3 offset = this.m_20182_();

         for (int i = 0; i < 12; i++) {
            SlashEntity be = new SlashEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOADSLASH.get(), this.m_9236_());
            be.m_146884_(offset);
            be.m_37251_(be, 0.0F, -180 + i * 30, this.m_21256_(), 1.5F, 0.0F);
            this.m_9236_().m_7967_(be);
         }
      }
   }

   private void upperAction(BossActionController.BossAction action) {
      if (action.onStartOfAction()) {
         this.triggerAnim("action_controller", "attackupper");
      } else if (action.currentActionTick == 12) {
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

   private void jumpAttackAction(BossActionController.BossAction action) {
      if (action.onStartOfAction()) {
         this.triggerAnim("action_controller", "attackjump");
      } else if (action.currentActionTick == 20) {
         Vec3 offset = this.m_20182_();
         Vec3 muzzlPos = new Vec3(0.0, 0.0, -3.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         muzzlPos = offset.m_82549_(muzzlPos);
         EarthbreakEntity be = new EarthbreakEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EARTHBREAK.get(), this.m_9236_(), this);
         be.m_146884_(muzzlPos);
         be.m_37251_(be, 0.0F, 0.0F, this.m_21256_(), 0.0F, 0.0F);
         this.m_9236_().m_7967_(be);
      }
   }

   private void aerial_punch(BossActionController.BossAction action) {
      float punchRange = 35.0F;
      LivingEntity target = this.m_5448_();
      if (target == null) {
         this.m_20242_(false);
      } else {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "attack_arial_punch_01");
            this.punched = false;
         } else if (action.currentActionTick == 10) {
            Vec3 toTarget = target.m_20182_().m_82546_(this.m_20182_()).m_82541_();
            this.m_20334_(toTarget.f_82479_ * 1.5, 4.0, toTarget.f_82481_ * 1.5);
            this.f_19812_ = true;
            this.m_20242_(true);
         } else if (action.currentActionTick > 10 && action.currentActionTick < action.maxActionTick - 20) {
            Vec3 myPos = this.m_20182_();
            Vec3 targetPos = target.m_20191_().m_82399_();
            Vec3 toTarget = targetPos.m_82546_(myPos);
            double dist = toTarget.m_82553_();
            Vec3 current = this.m_20184_();
            Vec3 desired = toTarget.m_82541_().m_82490_(5.0);
            Vec3 newVelocity = current.m_82490_(0.85).m_82549_(desired.m_82490_(0.15));
            double maxSpeed = 5.0;
            if (newVelocity.m_82553_() > maxSpeed) {
               newVelocity = newVelocity.m_82541_().m_82490_(maxSpeed);
            }

            if (newVelocity.f_82480_ >= 2.0) {
               newVelocity = new Vec3(newVelocity.f_82479_, 2.0, newVelocity.f_82481_);
            }

            this.m_20256_(newVelocity);
            this.rotateToTarget(target);
            if (dist <= punchRange) {
               this.punched = true;
               this.triggerAnim("action_controller", "attack_arial_punch_02");
               action.currentActionTick = action.maxActionTick - 19;
            }
         } else if (action.currentActionTick >= action.maxActionTick - 20) {
            if (!this.punched && this.m_20096_()) {
               action.currentActionTick = action.maxActionTick;
               this.m_20242_(false);
            }

            if (this.punched && action.currentActionTick - (action.maxActionTick - 20) == 7) {
               this.firePunch(target);
            }

            if (action.onEndOfAction()) {
               this.m_20242_(false);
            }
         } else {
            this.m_20242_(false);
         }
      }
   }

   private void firePunch(LivingEntity target) {
      if (!this.m_9236_().m_5776_()) {
         Vec3 forward = new Vec3(0.0, 0.0, 30.0).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         Vec3 punchCenter = this.m_20182_().m_82549_(forward);
         AABB punchBox = new AABB(
            punchCenter.f_82479_ - 15.0,
            punchCenter.f_82480_ - 15.0,
            punchCenter.f_82481_ - 15.0,
            punchCenter.f_82479_ + 15.0,
            punchCenter.f_82480_ + 15.0,
            punchCenter.f_82481_ + 15.0
         );

         for (Entity ent : this.m_9236_().m_45933_(this, punchBox)) {
            if (ent instanceof LivingEntity le && !this.isSelf(le)) {
               le.m_6469_(this.m_269291_().m_269333_(this), 50.0F);
               Vec3 kb = le.m_20182_().m_82546_(this.m_20182_()).m_82541_();
               le.m_147240_(3.0, -kb.f_82479_, -kb.f_82481_);
            }
         }
      }
   }

   private void gatlingAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "attackgatling");
         } else if (action.currentActionTick == 3) {
            BulletGrenadeEntity be = new BulletGrenadeEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE.get(), this.m_9236_(), this, this.getMechData().bulletDamage
            );
            be.m_20242_(true);
            be.setExplosionScale(1);
            be.setStunPoint(2);
            Vec3 offset = this.m_20182_();
            Vec3 muzzlPos = new Vec3(-8.0, 8.0, 10.0);
            muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
            be.m_146884_(offset.m_82549_(muzzlPos));
            float[] angle = Utils.getShootingAngle2(be, target, true, false, this.getMechData().bulletSpeed, 0.0F);
            be.m_37251_(be, angle[0], angle[1], 0.0F, this.getMechData().bulletSpeed, 0.0F);
            this.m_9236_().m_7967_(be);
            ParticleUtil.spawnAttachedMuzzleFlash((ServerLevel)this.m_9236_(), this, 50.0, new Vec3(-8.0, 9.0, 15.0));
         }
      }
   }

   private void grenadeAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "attackgrenade");
         } else if (action.currentActionTick == 12) {
            BulletGrenadeLargeEntity be = new BulletGrenadeLargeEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE_LARGE.get(), this.m_9236_(), this, this.getMechData().grenadeDamage
            );
            be.m_20242_(true);
            be.setExplosionScale((int)this.getMechData().grenadeExplosionScale);
            Vec3 offset = this.m_20182_();
            Vec3 muzzlPos = new Vec3(8.0, 8.0, 10.0);
            muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
            be.m_146884_(offset.m_82549_(muzzlPos));
            float[] angle = Utils.getShootingAngle2(be, target, true, false, this.getMechData().grenadeSpeed, 0.0F);
            be.m_37251_(be, angle[0], angle[1], 0.0F, this.getMechData().grenadeSpeed, 0.0F);
            this.m_9236_().m_7967_(be);
            ParticleUtil.spawnAttachedMuzzleFlash((ServerLevel)this.m_9236_(), this, 80.0, new Vec3(8.0, 8.0, 15.0));
         }
      }
   }

   private void missileHorizontalAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "attackmissile");
         } else if (action.currentActionTick == 15) {
            for (int row = 0; row < 3; row++) {
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
                     be.setSwitchTick(12);
                     Vec3 offset = this.m_20182_();
                     float slotZ = col * 0.8F + 8.0F;
                     float slotY = row + 15;
                     Vec3 muzzlPos = new Vec3(side * 10, slotY, 0.0);
                     muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
                     be.m_146884_(offset.m_82549_(muzzlPos));
                     float[] angle = Utils.getShootingAngle(be, target, true);
                     be.m_37251_(be, angle[0], angle[1] - side * 80, this.m_21256_(), this.getMechData().missileSpeed, 0.0F);
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
            this.triggerAnim("action_controller", "attackmissile");
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

   private void missileLargeAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "attacklargemissile");
         } else if (action.currentActionTick == 15) {
            Vec3 offset = this.m_20182_();
            Vec3 muzzlPos = new Vec3(0.0, 16.0, 1.0);
            muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
            muzzlPos = offset.m_82549_(muzzlPos);
            MissileGenericLargeEntity be = new MissileGenericLargeEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC_LARGE.get(),
               this.m_9236_(),
               this,
               target,
               this.getMechData().missileDamage * 2.0F,
               this.getMechData().missileSpeed
            );
            be.m_146884_(muzzlPos);
            be.m_37251_(be, -89.0F, this.m_146908_(), this.m_21256_(), 1.5F, 0.0F);
            this.m_9236_().m_7967_(be);
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
               Vec3 knockback = entity.m_20182_().m_82546_(this.m_20182_()).m_82541_().m_82490_(1.5);
               entity.m_20256_(entity.m_20184_().m_82520_(knockback.f_82479_, 0.5, knockback.f_82481_));
            }
         }
      }
   }

   private void breakAction(BossActionController.BossAction action) {
      if (action.onStartOfAction()) {
         this.triggerAnim("action_controller", "bodyfrontbreak");
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
