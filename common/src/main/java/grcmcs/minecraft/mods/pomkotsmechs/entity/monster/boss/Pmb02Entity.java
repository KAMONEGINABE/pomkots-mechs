package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.OrbitalFlyGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissilePodEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
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

public class Pmb02Entity extends BaseBossEntity {
   private float missileModifier = 1.5F;

   @Override
   public String getMechName() {
      return "pmb02";
   }

   public Pmb02Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.m_20242_(true);
      this.actionController.registerAction("missile_straight", new BossActionController.BossAction(60, 20, this::missileStraightAction));
      this.actionController.registerAction("missile_horizontal", new BossActionController.BossAction(60, 20, this::missileHorizontalAction));
      this.actionController.registerAction("missile_vertical", new BossActionController.BossAction(60, 20, this::missileVerticalAction));
      this.actionController.registerAction("missile_full", new BossActionController.BossAction(1000, 20, this::missileFullAction));
      this.actionController.registerAction("missile_pod", new BossActionController.BossAction(200, 20, this::missilePodAction));
      this.actionController.registerAction("canon", new BossActionController.BossAction(60, 20, this::canonAction));
      this.actionController.registerAction("saber_horizontal", new BossActionController.BossAction(60, 105, this::saberHorizontal));
      this.actionController.registerAction("onsmalldown", new BossActionController.BossAction(20, 15, this::smallDown));
      this.registerActionGoal(new OrbitalFlyGoal(this, this.getMechData().speed, 20.0, 100.0, 10.0, 30.0), AI_MODE_ALL, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile_straight"), this, true, true), AI_MODE_ALL, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile_horizontal"), this, false), AI_MODE_ALL, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("saber_horizontal"), this, false), AI_MODE_ALL, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile_pod"), this, true, true), new int[]{2, 3, 4}, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("canon"), this, false), new int[]{3, 3, 4}, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile_vertical"), this, false), new int[]{3, 3, 4}, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile_full"), this, false), new int[]{4}, 10);
   }

   @Override
   public void m_8119_() {
      if (this.f_19803_ && this.isServerSide()) {
         this.registerAdditionalHitBox(new BossHitBoxEntity((EntityType<? extends LivingEntity>)PomkotsMechs.HITBOX_PMB02.get(), this.m_9236_(), this));
      }

      this.m_20242_(true);
      super.m_8119_();
   }

   private void saberHorizontal(BossActionController.BossAction action) {
      float speed = 6.0F;
      float maxTurnRadiansPerTick = 0.15F;
      float attackDistanceSq = 1900.0F;
      int startChargeTick = 15;
      int maxHomingTicks = 60;
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "dash");
         } else if (action.currentActionTick > 15 && action.currentActionTick <= 75) {
            Vec3 mobPos = this.m_20182_();
            Vec3 targetPos = target.m_20182_();
            Vec3 direction = targetPos.m_82546_(mobPos).m_82541_();
            Vec3 forward = this.m_20154_().m_82541_();
            Vec3 newForward = forward.m_165921_(direction, 0.15F).m_82541_();
            Vec3 movement = newForward.m_82490_(6.0);
            this.m_20256_(movement);
            this.rotateDegree((float)(Math.toDegrees(Math.atan2(newForward.f_82481_, newForward.f_82479_)) - 90.0));
            double distSq = this.m_20280_(target);
            if (distSq <= 1900.0 || action.currentActionTick == 75) {
               if (distSq <= 1900.0) {
                  action.currentActionTick = 75;
               }

               this.triggerAnim("action_controller", "saber_horizontal");
            }
         } else if (action.currentActionTick == 85) {
            Vec3 pilePos1 = new Vec3(14.0, 4.0, 14.0).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_())).m_82549_(this.m_20182_());
            Vec3 pilePos2 = new Vec3(-14.0, -4.0, -4.0).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_())).m_82549_(this.m_20182_());
            Vec3 kbVel = new Vec3(0.0, 0.0, -1.0).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));

            for (Entity ent : this.m_9236_().m_45933_(null, new AABB(pilePos1, pilePos2))) {
               if (!this.isSelf(ent) && ent instanceof LivingEntity le) {
                  le.m_147240_(2.0, kbVel.f_82479_, kbVel.f_82481_);
                  le.m_6469_(this.m_269291_().m_269264_(), this.getMechData().meleeDamage);
               }
            }
         }
      }
   }

   private void canonAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "canon");
         } else if (action.currentActionTick == 7) {
            for (int side = -1; side < 2; side += 2) {
               BulletGrenadeEntity be = new BulletGrenadeEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE.get(), this.m_9236_(), this, this.getMechData().bulletDamage
               );
               be.m_20242_(true);
               be.setExplosionScale(3);
               Vec3 offset = this.m_20182_();
               Vec3 muzzlPos = new Vec3(2 * side, 4.0, 7.0);
               muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
               be.m_146884_(offset.m_82549_(muzzlPos));
               float[] angle = Utils.getShootingAngle(be, target, true);
               be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().bulletSpeed, 0.0F);
               this.m_9236_().m_7967_(be);
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
               Vec3 muzzlPos = new Vec3(4 * side, 3.5, 7.0);
               muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
               be.m_146884_(offset.m_82549_(muzzlPos));
               float[] angle = Utils.getShootingAngle(be, target, true);
               be.m_37251_(be, -15.0F, angle[1], this.m_21256_(), this.getMechData().missileSpeed, 0.0F);
               this.m_9236_().m_7967_(be);
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
                     float slotX = (col * 0.8F + 1.5F) * side;
                     float slotY = row + 0.5F;
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
                     float slotZ = col * 0.8F + 1.5F;
                     float slotY = row + 0.8F;
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
                     float slotX = (col * 0.8F + 1.5F) * side;
                     float slotZ = -row - 0.8F;
                     Vec3 muzzlPos = new Vec3(slotX, 8.0, slotZ);
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

   private void missileFullAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");
         } else if (action.currentActionTick == 2) {
            int mNum = 9;

            for (int i = 0; i < mNum; i++) {
               for (int j = 0; j < mNum; j++) {
                  MissileGenericEntity be = new MissileGenericEntity(
                     (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                     this.m_9236_(),
                     this,
                     target,
                     this.getMechData().missileDamage,
                     this.getMechData().missileSpeed * this.missileModifier
                  );
                  be.setMaxRotationAnglePerTick(6.0F);
                  be.m_146884_(this.m_20182_());
                  be.m_37251_(be, -i * (90 / mNum), -90 + j * (180 / mNum), this.m_21256_(), 1.5F, 0.0F);
                  this.m_9236_().m_7967_(be);
               }
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
                     return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb02.inactive"));
                  } else {
                     return event.isMoving()
                        ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb02.idle"))
                        : event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb02.idle"));
                  }
               }
            )
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "action_controller", state -> PlayState.STOP)
               .triggerableAnim("dash", RawAnimation.begin().thenPlay("animation.pmb02.dash1").thenLoop("animation.pmb02.dash2"))
               .triggerableAnim("saber_horizontal", RawAnimation.begin().thenPlay("animation.pmb02.attack_arm_horizontal"))
               .triggerableAnim("canon", RawAnimation.begin().thenPlay("animation.pmb02.canon"))
               .triggerableAnim("missile", RawAnimation.begin().thenPlay("animation.pmb02.missile"))
               .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb02.boot"))
               .triggerableAnim("on_stun", RawAnimation.begin().thenPlay("animation.pmb02.hurt").thenPlayAndHold("animation.pmb02.down"))
               .triggerableAnim("off_stun", RawAnimation.begin().thenPlay("animation.pmb02.up"))
               .triggerableAnim("on_small_down", RawAnimation.begin().thenPlay("animation.pmb02.hurt"))
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
