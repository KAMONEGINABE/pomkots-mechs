package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BossAerialDiveGoal2;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossWalkGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEnemyEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;

public class Pmb05Entity extends BaseBossEntity {
   private List<Vec3> targetPosHistory = new ArrayList<>();
   private Vec3 laserLookAtPos = Vec3.f_82478_;
   private Vec3 laserStart;
   private Vec3 laserEnd;
   private Vec3 laserDirection;
   private Vec3 laserHitBlockPos;
   private double currentLaserLength = 0.0;
   private double LASER_WIDTH = 3.0;
   private double LASER_MAX_RANGE = 200.0;
   public final AnimationController<Pmb05Entity> trigger = new AnimationController(this, "action_controller", state -> PlayState.STOP)
      .triggerableAnim("laser_short", RawAnimation.begin().thenPlay("animation.pmb03.laser1"))
      .triggerableAnim("laser_long", RawAnimation.begin().thenPlay("animation.pmb03.laser2"))
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
   private final AnimationController<Pmb05Entity> base = new AnimationController(
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
               return PlayState.CONTINUE;
            }
         }
      )
      .setSoundKeyframeHandler(this::playSounds);
   private static final EntityDataAccessor<Integer> LASER_LENGTH = SynchedEntityData.m_135353_(Pmb05Entity.class, EntityDataSerializers.f_135028_);

   @Override
   public String getMechName() {
      return "pmb05";
   }

   public Pmb05Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.alwaysLookAtTarget = false;
      this.actionController.registerAction("leaser_short", new BossActionController.BossAction(30, 30, this::laserShort));
      this.actionController.registerAction("leaser_long", new BossActionController.BossAction(40, 55, this::laserLong));
      this.actionController.registerAction("gatling", new BossActionController.BossAction(40, 5, this::gatlingAction));
      this.actionController.registerAction("missile", new BossActionController.BossAction(40, 20, this::missileHorizontalAction));
      this.actionController.registerAction("stomp", new BossActionController.BossAction(40, 40, this::stompAction));
      this.actionController.registerAction("onground", new BossActionController.BossAction(20, 16, this::onGroundAction));
      this.actionController.registerAction("onsmalldown", new BossActionController.BossAction(20, 15, this::smallDown));
      this.registerActionGoal(new SimpleBossWalkGoal(this, this.getMechData().speed, 30.0), AI_MODE_ALL, 7);
      this.registerActionGoal(new BossAerialDiveGoal2(this, 10.0, 3.0, 3.0, 3.0, 20.0, 100), AI_MODE_ALL, 20);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("leaser_short"), this, false, 3), AI_MODE_ALL, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile"), this, true, true), new int[]{2, 3, 4}, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("gatling"), this, true, 5, true), new int[]{3, 4}, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("leaser_long"), this, false), new int[]{3, 4}, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("stomp"), this, false), AI_MODE_ALL, 0.0F, 20.0F, 10);
      this.m_20242_(false);
   }

   @Override
   public void m_8119_() {
      if (this.f_19803_ && this.isServerSide()) {
         this.registerAdditionalHitBox(new BossHitBoxEntity((EntityType<? extends LivingEntity>)PomkotsMechs.HITBOX_PMB03.get(), this.m_9236_(), this));
      }

      super.m_8119_();
   }

   private void laserShort(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "laser_short");
            this.laserLookAtPos = target.m_20182_();
            this.setLaserLength(1);
         } else if (action.currentActionTick < 14) {
            this.laserLookAtPos = target.m_20182_();
         } else if (action.currentActionTick == 14) {
            this.setLaserLength(1);
         } else if (action.currentActionTick == 17) {
            this.fireLaser(this.m_20182_().m_82520_(0.0, this.m_20192_(), 0.0), this.laserLookAtPos, this.getMechData().laserDamage, 6);
            this.setLaserLength((int)this.currentLaserLength);
         } else if (action.onEndOfAction()) {
            this.setLaserLength(0);
         }

         this.f_21365_.m_24964_(this.laserLookAtPos);
      }
   }

   private void laserLong(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         this.targetPosHistory.add(target.m_20182_());
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "laser_long");
            this.targetPosHistory.clear();
            this.laserLookAtPos = target.m_20182_();
         } else if (action.currentActionTick < 14) {
            this.laserLookAtPos = target.m_20182_();
         } else if (action.currentActionTick == 14) {
            this.setLaserLength(1);
         } else if (action.currentActionTick > 17 && action.currentActionTick < 50 && action.currentActionTick % 3 == 0) {
            if (this.targetPosHistory.size() > 6) {
               this.laserLookAtPos = this.targetPosHistory.get(this.targetPosHistory.size() - 5);
            }

            this.fireLaser(this.m_20182_().m_82520_(0.0, this.m_20192_(), 0.0), this.laserLookAtPos, this.getMechData().laserDamage / 3.0F, 2);
            this.setLaserLength((int)this.currentLaserLength);
         } else if (action.onEndOfAction()) {
            this.setLaserLength(0);
         }

         this.f_21365_.m_24964_(this.laserLookAtPos);
      }
   }

   private void fireLaser(Vec3 startPos, Vec3 targetPos, float damage, int explosionScale) {
      this.laserStart = startPos;
      this.laserDirection = targetPos.m_82546_(this.laserStart).m_82541_();
      this.calculateLaserEnd();
      this.damageEntitiesInLaserPath(damage);
      if (this.laserHitBlockPos != Vec3.f_82478_) {
         if (explosionScale > 5) {
            ExplosionEntity e = new ExplosionEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOSION.get(), this.m_9236_(), explosionScale);
            e.m_146884_(this.laserHitBlockPos);
            this.m_9236_().m_7967_(e);
         } else if (explosionScale > 0) {
            Vec3 pos = this.laserHitBlockPos;
            Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, explosionScale, false, ExplosionInteraction.BLOCK, this.m_9236_());
         }
      }
   }

   private void calculateLaserEnd() {
      Vec3 start = this.laserStart;
      Vec3 end = start.m_82549_(this.laserDirection.m_82490_(this.LASER_MAX_RANGE));
      BlockHitResult hitResult = this.m_9236_().m_45547_(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, this));
      if (hitResult.m_6662_() != Type.MISS) {
         this.currentLaserLength = start.m_82554_(hitResult.m_82450_());
         this.laserHitBlockPos = hitResult.m_82450_();
         this.laserEnd = hitResult.m_82450_();
      } else {
         this.currentLaserLength = this.LASER_MAX_RANGE;
         this.laserHitBlockPos = Vec3.f_82478_;
         this.laserEnd = end;
      }
   }

   private void damageEntitiesInLaserPath(float damage) {
      Vec3 start = this.laserStart;
      Vec3 end = start.m_82549_(this.laserDirection.m_82490_(this.currentLaserLength));
      AABB searchBox = new AABB(start, end).m_82400_(this.LASER_WIDTH);

      for (LivingEntity entity : this.m_9236_().m_6443_(LivingEntity.class, searchBox, entityx -> entityx != this && this.m_6779_(entityx))) {
         if (!this.isSelf(entity) && this.isEntityInLaserPath(entity)) {
            entity.m_6469_(this.m_269291_().m_269333_(this), damage);
         }
      }
   }

   private boolean isEntityInLaserPath(LivingEntity entity) {
      Vec3 entityPos = entity.m_20182_().m_82520_(0.0, entity.m_20206_() * 0.5, 0.0);
      Vec3 toEntity = entityPos.m_82546_(this.laserStart);
      double projectionLength = toEntity.m_82526_(this.laserDirection);
      if (!(projectionLength < 0.0) && !(projectionLength > this.currentLaserLength)) {
         Vec3 closestPoint = this.laserStart.m_82549_(this.laserDirection.m_82490_(projectionLength));
         double distance = entityPos.m_82554_(closestPoint);
         return distance <= this.LASER_WIDTH + entity.m_20205_() * 0.5;
      } else {
         return false;
      }
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
               Vec3 offset = this.m_20182_();
               Vec3 muzzlPos = new Vec3(2 * side, 6.0, 10.0);
               muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
               be.m_146884_(offset.m_82549_(muzzlPos));
               float[] angle = Utils.getShootingAngle(be, target, true);
               be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().bulletSpeed, 0.0F);
               this.m_9236_().m_7967_(be);
            }
         }
      }
   }

   private void missileHorizontalAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missilepod");
         } else if (action.currentActionTick == 10) {
            for (int row = 0; row < 2; row++) {
               for (int col = 0; col < 2; col++) {
                  for (int side = -1; side < 2; side += 2) {
                     MissileGenericEnemyEntity be = new MissileGenericEnemyEntity(
                        (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                        this.m_9236_(),
                        this,
                        target,
                        this.getMechData().missileDamage,
                        this.getMechData().missileSpeed
                     );
                     be.setMaxRotationAnglePerTick(2.0F);
                     Vec3 offset = this.m_20182_();
                     float slotX = side * (col * 1.5F + 7.0F);
                     float slotY = row + 18.0F;
                     Vec3 muzzlPos = new Vec3(slotX, slotY, 6.0);
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

   @Override
   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(LASER_LENGTH, 0);
   }

   private void setLaserLength(int value) {
      this.f_19804_.m_135381_(LASER_LENGTH, value);
   }

   public int getLaserLength() {
      return (Integer)this.f_19804_.m_135370_(LASER_LENGTH);
   }
}
