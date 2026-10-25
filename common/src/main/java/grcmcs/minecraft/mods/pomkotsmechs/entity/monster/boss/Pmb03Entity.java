package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BossAerialDiveGoal2;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossWalkGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeLargeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEnemyEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissilePodEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
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

public class Pmb03Entity extends BaseBossEntity {
   private final AnimationController<Pmb03Entity> trigger = new AnimationController(this, "action_controller", state -> PlayState.STOP)
      .triggerableAnim("grenade", RawAnimation.begin().thenPlay("animation.pmb03.grenade"))
      .triggerableAnim("gatling", RawAnimation.begin().thenPlayXTimes("animation.pmb03.gatling", 5))
      .triggerableAnim("missilepod", RawAnimation.begin().thenPlay("animation.pmb03.missilepod"))
      .triggerableAnim("stomp", RawAnimation.begin().thenPlay("animation.pmb03.stomp"))
      .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.pmb03.jump"))
      .triggerableAnim("onground", RawAnimation.begin().thenPlay("animation.pmb03.onground"))
      .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb03.boot"))
      .triggerableAnim("on_stun", RawAnimation.begin().thenPlay("animation.pmb03.hurt").thenPlayAndHold("animation.pmb03.down"))
      .triggerableAnim("off_stun", RawAnimation.begin().thenPlay("animation.pmb03.up"))
      .triggerableAnim("on_small_down", RawAnimation.begin().thenPlay("animation.pmb03.hurt"))
      .setSoundKeyframeHandler(this::playSounds);
   private final AnimationController<Pmb03Entity> base = new AnimationController(
         this,
         "basic_move",
         0,
         event -> {
            if (!this.trigger.isPlayingTriggeredAnimation()) {
               if (this.getAiMode() == -2) {
                  return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.inactive"));
               }

               if (event.isMoving()) {
                  if (!this.m_20068_()) {
                     BaseBossEntity.MoveDirection dir = this.getDominantMoveDirection();
                     if (dir == BaseBossEntity.MoveDirection.LEFT) {
                        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.walk_right"));
                     } else {
                        return dir == BaseBossEntity.MoveDirection.RIGHT
                           ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.walk_left"))
                           : event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.walk"));
                     }
                  } else {
                     return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.fly"));
                  }
               } else {
                  return !this.m_20068_()
                     ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.idle"))
                     : event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.fly"));
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
      return "pmb03";
   }

   public Pmb03Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.alwaysLookAtTarget = false;
      this.actionController.registerAction("grenade", new BossActionController.BossAction(80, 20, this::grenadeAction));
      this.actionController.registerAction("gatling", new BossActionController.BossAction(40, 5, this::gatlingAction));
      this.actionController.registerAction("missilepod", new BossActionController.BossAction(80, 20, this::missilePodAction));
      this.actionController.registerAction("missile", new BossActionController.BossAction(40, 21, this::missileHorizontalAction));
      this.actionController.registerAction("missilev", new BossActionController.BossAction(40, 21, this::missileVerticalAction));
      this.actionController.registerAction("stomp", new BossActionController.BossAction(40, 40, this::stompAction));
      this.actionController.registerAction("onground", new BossActionController.BossAction(20, 16, this::onGroundAction));
      this.actionController.registerAction("onsmalldown", new BossActionController.BossAction(20, 15, this::smallDown));
      this.registerActionGoal(new SimpleBossWalkGoal(this, this.getMechData().speed, 30.0), AI_MODE_ALL, 15);
      this.registerActionGoal(new BossAerialDiveGoal2(this, 10.0, 3.0, 3.0, 3.0, 20.0, 100), AI_MODE_ALL, 15);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("gatling"), this, true, 5, true), AI_MODE_ALL, 30);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile"), this, true, true), AI_MODE_ALL, 20);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missilev"), this, true, true), AI_MODE_ALL, 20);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("stomp"), this, false), AI_MODE_ALL, 0.0F, 20.0F, 40);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("grenade"), this, true, true), new int[]{3, 4}, 15);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missilepod"), this, true, true), new int[]{3, 4}, 15);
      this.m_20242_(false);
   }

   @Override
   public void m_8119_() {
      if (this.f_19803_ && this.isServerSide()) {
         this.registerAdditionalHitBox(new BossHitBoxEntity((EntityType<? extends LivingEntity>)PomkotsMechs.HITBOX_PMB03.get(), this.m_9236_(), this));
      }

      super.m_8119_();
   }

   private void stompAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "stomp");
         } else if (action.currentActionTick == 22) {
            Vec3 pilePos1 = new Vec3(20.0, 3.0, 20.0).m_82549_(this.m_20182_());
            Vec3 pilePos2 = new Vec3(-20.0, -3.0, -20.0).m_82549_(this.m_20182_());
            Level world = this.m_9236_();
            Vec3 kbVel = new Vec3(0.0, 0.0, -1.0).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));

            for (Entity ent : world.m_45933_(null, new AABB(pilePos1, pilePos2))) {
               if (!this.isSelf(ent) && ent instanceof LivingEntity le) {
                  le.m_147240_(2.0, kbVel.f_82479_, kbVel.f_82481_);
                  DamageSource ds = this.m_269291_().m_269264_();
                  float damage = this.getMechData().meleeDamage;
                  le.m_6469_(ds, damage);
               }
            }
         }
      }
   }

   private void grenadeAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "grenade");
         } else if (action.currentActionTick == 5) {
            for (int side = -1; side < 2; side += 2) {
               BulletGrenadeLargeEntity be = new BulletGrenadeLargeEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE_LARGE.get(), this.m_9236_(), this, this.getMechData().grenadeDamage
               );
               be.m_20242_(true);
               be.setExplosionScale((int)this.getMechData().grenadeExplosionScale);
               Vec3 offset = this.m_20182_();
               Vec3 muzzlPos = new Vec3(5.5 * side, 11.0, 10.0);
               muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
               be.m_146884_(offset.m_82549_(muzzlPos));
               float[] angle = Utils.getShootingAngle(be, target, true);
               be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().grenadeSpeed, 0.0F);
               this.m_9236_().m_7967_(be);
               ParticleUtil.spawnAttachedMuzzleFlash((ServerLevel)this.m_9236_(), this, 80.0, new Vec3(5.5 * side, 11.0, 13.0));
            }
         }
      }
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
               Vec3 muzzlPos = new Vec3(5.5 * side, 14.0, 10.0);
               muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
               be.m_146884_(offset.m_82549_(muzzlPos));
               float[] angle = Utils.getShootingAngle(be, target, true);
               be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().bulletSpeed, 0.0F);
               this.m_9236_().m_7967_(be);
               ParticleUtil.spawnAttachedMuzzleFlash((ServerLevel)this.m_9236_(), this, 50.0, new Vec3(5.5 * side, 14.0, 13.0));
            }
         }
      }
   }

   private void missileHorizontalAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missilepod");
         } else if (action.currentActionTick == 5) {
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
                     float slotY = row + 10.0F;
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
            this.triggerAnim("action_controller", "missilepod");
         } else if (action.currentActionTick == 5) {
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
                     float slotZ = col * 0.8F + 8.0F;
                     float slotY = row + 10.0F;
                     Vec3 muzzlPos = new Vec3(side * 7, slotY, slotZ);
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
            this.triggerAnim("action_controller", "missilepod");
         } else if (action.currentActionTick == 5) {
            for (int side = -1; side < 2; side += 2) {
               MissilePodEntity be = new MissilePodEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_POD.get(),
                  this.m_9236_(),
                  this,
                  target,
                  this.getMechData().missileDamage,
                  this.getMechData().missileSpeed
               );
               be.m_20242_(true);
               Vec3 offset = this.m_20182_();
               Vec3 muzzlPos = new Vec3(4 * side, 17.0, 4.0);
               muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
               be.m_146884_(offset.m_82549_(muzzlPos));
               float[] angle = Utils.getShootingAngle(be, target, true);
               be.m_37251_(be, -15.0F, angle[1], this.m_21256_(), this.getMechData().missileSpeed, 0.0F);
               this.m_9236_().m_7967_(be);
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
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController[]{this.base});
      controllers.add(new AnimationController[]{this.trigger});
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
