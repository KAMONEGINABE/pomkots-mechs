package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BossAerialDiveGoal2;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.LaunchMobGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossWalkGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletMachineEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEnemyEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;

public class Pmb06Entity extends BaseBossEntity {
   private final List<EntityType<? extends BaseSmallMonsterEntity>> generateTargetMonsterListA = new ArrayList<>();
   private final List<EntityType<? extends BaseSmallMonsterEntity>> generateTargetMonsterListB = new ArrayList<>();
   private final List<EntityType<? extends BaseSmallMonsterEntity>> generateTargetMonsterListC = new ArrayList<>();
   private final List<EntityType<? extends BaseSmallMonsterEntity>> generateTargetMonsterListD = new ArrayList<>();
   private final List<EntityType<? extends BaseSmallMonsterEntity>> generateTargetMonsterListE = new ArrayList<>();
   private final List<EntityType<? extends BaseSmallMonsterEntity>> generateTargetMonsterListF = new ArrayList<>();
   public List<UUID> spawnedMonsters = new ArrayList<>();
   private int spawnedCount = 0;
   private List<EntityType<? extends BaseSmallMonsterEntity>> targetPool = this.generateTargetMonsterListA;
   private boolean isFly = false;
   private int spawnIndex = 0;
   private List<Vec3> targetPosHistory = new ArrayList<>();
   private Vec3 laserLookAtPos = Vec3.f_82478_;
   private Vec3 laserStart;
   private Vec3 laserEnd;
   private Vec3 laserDirection;
   private Vec3 laserHitBlockPos;
   private double currentLaserLength = 0.0;
   private double LASER_WIDTH = 3.0;
   private double LASER_MAX_RANGE = 200.0;
   public final AnimationController<Pmb06Entity> trigger = new AnimationController(this, "action_controller", state -> PlayState.STOP)
      .triggerableAnim("hadoho", RawAnimation.begin().thenPlay("animation.pmb03.hadoho_1").thenPlay("animation.pmb03.hadoho_2"))
      .triggerableAnim("small_canon", RawAnimation.begin().thenPlayXTimes("animation.pmb03.small_canon", 5))
      .triggerableAnim("large_canon", RawAnimation.begin().thenPlay("animation.pmb03.large_canon"))
      .triggerableAnim("mob_launch_front", RawAnimation.begin().thenPlay("animation.pmb03.mob_launch_front"))
      .triggerableAnim("mob_launch_side", RawAnimation.begin().thenPlay("animation.pmb03.mob_launch_side"))
      .triggerableAnim("missile", RawAnimation.begin().thenPlay("animation.pmb03.missile"))
      .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.pmb03.jump"))
      .triggerableAnim("onground", RawAnimation.begin().thenPlay("animation.pmb03.onground"))
      .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb03.boot"))
      .triggerableAnim("on_stun", RawAnimation.begin().thenPlay("animation.pmb03.hurt").thenPlayAndHold("animation.pmb03.down"))
      .triggerableAnim("off_stun", RawAnimation.begin().thenPlay("animation.pmb03.up"))
      .triggerableAnim("on_small_down", RawAnimation.begin().thenPlay("animation.pmb03.hurt"))
      .setSoundKeyframeHandler(this::playSounds);
   private final AnimationController<Pmb06Entity> base = new AnimationController(
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
   private static final EntityDataAccessor<Integer> LASER_LENGTH = SynchedEntityData.m_135353_(Pmb06Entity.class, EntityDataSerializers.f_135028_);
   private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.m_135353_(Pmb06Entity.class, EntityDataSerializers.f_135028_);

   @Override
   public String getMechName() {
      return "pmb06";
   }

   public Pmb06Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.alwaysLookAtTarget = false;
      this.actionController.registerAction("gen_mobs", new BossActionController.BossAction(200, 35, this::generateMobs));
      this.actionController.registerAction("gatling", new BossActionController.BossAction(40, 5, this::gatlingAction));
      this.actionController.registerAction("small_canon", new BossActionController.BossAction(40, 10, this::smallCanonAction));
      this.actionController.registerAction("large_canon", new BossActionController.BossAction(40, 20, this::largeCanonAction));
      this.actionController.registerAction("missile", new BossActionController.BossAction(40, 10, this::missileHorizontalAction));
      this.actionController.registerAction("vmissile", new BossActionController.BossAction(40, 10, this::missileVerticalAction));
      this.actionController.registerAction("hadoho", new BossActionController.BossAction(600, 70, this::hadohoAction));
      this.actionController.registerAction("onground", new BossActionController.BossAction(20, 16, this::onGroundAction));
      this.actionController.registerAction("onsmalldown", new BossActionController.BossAction(20, 15, this::smallDown));
      this.registerActionGoal(new SimpleBossWalkGoal(this, this.getMechData().speed, 30.0), AI_MODE_ALL, 5);
      this.registerActionGoal(new BossAerialDiveGoal2(this, 10.0, 3.0, 3.0, 3.0, 20.0, 100), AI_MODE_ALL, 10);
      this.registerActionGoal(new LaunchMobGoal(this.actionController.getAction("gen_mobs"), this, false), AI_MODE_ALL, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("gatling"), this, true, 10, true), AI_MODE_ALL, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("small_canon"), this, true, 5, true), AI_MODE_ALL, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("missile"), this, true, true), new int[]{2, 3, 4}, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("vmissile"), this, true, true), new int[]{2, 3, 4}, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("large_canon"), this, true, true), new int[]{3, 4}, 10);
      this.registerActionGoal(new SimpleBossAttackGoal(this.actionController.getAction("hadoho"), this, false), new int[]{3, 4}, 10);
      this.m_20242_(false);
      this.generateTargetMonsterListA.add((EntityType<? extends BaseSmallMonsterEntity>)PomkotsMechs.PMS01.get());
      this.generateTargetMonsterListA.add((EntityType<? extends BaseSmallMonsterEntity>)PomkotsMechs.PMS03.get());
      this.generateTargetMonsterListA.add((EntityType<? extends BaseSmallMonsterEntity>)PomkotsMechs.PMS04.get());
      this.generateTargetMonsterListA.add((EntityType<? extends BaseSmallMonsterEntity>)PomkotsMechs.PMS05.get());
      this.generateTargetMonsterListB.add((EntityType<? extends BaseSmallMonsterEntity>)PomkotsMechs.PMS06.get());
      this.generateTargetMonsterListB.add((EntityType<? extends BaseSmallMonsterEntity>)PomkotsMechs.PMS07.get());
      this.generateTargetMonsterListB.add((EntityType<? extends BaseSmallMonsterEntity>)PomkotsMechs.PMS08.get());
      this.generateTargetMonsterListC.add((EntityType<? extends BaseSmallMonsterEntity>)PomkotsMechs.PMS02.get());
      this.generateTargetMonsterListD.add((EntityType<? extends BaseSmallMonsterEntity>)PomkotsMechs.PMS09.get());
      this.generateTargetMonsterListE.add((EntityType<? extends BaseSmallMonsterEntity>)PomkotsMechs.PMS08.get());
      this.generateTargetMonsterListF.add((EntityType<? extends BaseSmallMonsterEntity>)PomkotsMechs.PMS05.get());
      this.generateTargetMonsterListF.add((EntityType<? extends BaseSmallMonsterEntity>)PomkotsMechs.PMS03.get());
   }

   @Override
   public void m_8119_() {
      if (this.isServerSide()) {
         if (this.f_19803_) {
            this.registerAdditionalHitBox(new BossHitBoxEntity((EntityType<? extends LivingEntity>)PomkotsMechs.HITBOX_PMB06.get(), this.m_9236_(), this));
         }

         if (!this.spawnedMonsters.isEmpty()) {
            this.spawnedMonsters.removeIf(uuid -> {
               Entity e = ((ServerLevel)this.m_9236_()).m_8791_(uuid);
               return e == null || !e.m_6084_();
            });
         }

         if (this.m_5448_() != null) {
            this.setTargetEntityID(this.m_5448_().m_19879_());
         }

         if (this.isMoving() && Utils.isBlockDestructionAllowed(this)) {
         }
      }

      super.m_8119_();
   }

   private void gatlingAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.currentActionTick == 2) {
            for (int side = -1; side < 2; side += 2) {
               for (int r = 0; r < 3; r++) {
                  BulletMachineEntity be = new BulletMachineEntity(
                     (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_MACHINE.get(), this.m_9236_(), this, this.getMechData().bulletDamage / 2.0F
                  );
                  be.m_20242_(true);
                  be.setStunPoint(2);
                  Vec3 offset = this.m_20182_();
                  Vec3 muzzlPos = new Vec3(12 * side, 22 + r * 2, 15.0);
                  muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
                  be.m_146884_(offset.m_82549_(muzzlPos));
                  float[] angle = Utils.getShootingAngle(be, target, true);
                  be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().bulletSpeed, 0.0F);
                  this.m_9236_().m_7967_(be);
                  ParticleUtil.spawnAttachedMuzzleFlash((ServerLevel)this.m_9236_(), this, 30.0, new Vec3(12 * side, 22 + r * 2, 17.0));
               }
            }
         }
      }
   }

   private void smallCanonAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "small_canon");
         } else if (action.currentActionTick == 3) {
            for (int side = -1; side < 2; side += 2) {
               BulletGrenadeEntity be = new BulletGrenadeEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE.get(), this.m_9236_(), this, this.getMechData().bulletDamage
               );
               be.m_20242_(true);
               be.setExplosionScale(1);
               Vec3 offset = this.m_20182_();
               Vec3 muzzlPos = new Vec3(side * 2, 30.0, 15.0);
               muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
               be.m_146884_(offset.m_82549_(muzzlPos));
               float[] angle = Utils.getShootingAngle(be, target, true);
               be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().bulletSpeed, 0.0F);
               this.m_9236_().m_7967_(be);
               ParticleUtil.spawnAttachedMuzzleFlash((ServerLevel)this.m_9236_(), this, 50.0, new Vec3(side * 2, 30.0, 17.0));
            }
         }
      }
   }

   private void largeCanonAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "large_canon");
         } else if (action.currentActionTick == 5) {
            BulletGrenadeEntity be = new BulletGrenadeEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE.get(), this.m_9236_(), this, this.getMechData().grenadeDamage
            );
            be.m_20242_(true);
            be.setExplosionScale(5);
            Vec3 offset = this.m_20182_();
            Vec3 muzzlPos = new Vec3(0.0, 30.0, 15.0);
            muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
            be.m_146884_(offset.m_82549_(muzzlPos));
            float[] angle = Utils.getShootingAngle(be, target, true);
            be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().grenadeSpeed, 0.0F);
            this.m_9236_().m_7967_(be);
            ParticleUtil.spawnAttachedMuzzleFlash((ServerLevel)this.m_9236_(), this, 80.0, new Vec3(0.0, 30.0, 17.0));
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
                        this.getMechData().missileSpeed
                     );
                     be.setMaxRotationAnglePerTick(2.0F);
                     be.setSwitchTick(16);
                     be.setMaxLifeTick(150);
                     Vec3 offset = this.m_20182_();
                     Vec3 muzzlPos = new Vec3((10 + col * 2) * side, 40 + row * 2, 10.0);
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

   private void missileHorizontalAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");
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
                     be.setSwitchTick(16);
                     be.setMaxLifeTick(150);
                     Vec3 offset = this.m_20182_();
                     Vec3 muzzlPos = new Vec3((10 + col * 2) * side, 40 + row * 2, 10.0);
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

   private void generateMobs(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         if (action.onStartOfAction()) {
            if (this.spawnedMonsters.isEmpty()) {
               switch (this.spawnIndex % 6) {
                  case 0:
                     this.spawnedCount = 2;
                     this.targetPool = this.generateTargetMonsterListA;
                     this.isFly = false;
                     this.triggerAnim("action_controller", "mob_launch_front");
                     break;
                  case 1:
                     this.spawnedCount = 2;
                     this.targetPool = this.generateTargetMonsterListB;
                     this.isFly = false;
                     this.triggerAnim("action_controller", "mob_launch_front");
                     break;
                  case 2:
                     this.spawnedCount = 2;
                     this.targetPool = this.generateTargetMonsterListD;
                     this.isFly = true;
                     this.triggerAnim("action_controller", "mob_launch_side");
                     break;
                  case 3:
                     this.spawnedCount = 3;
                     this.targetPool = this.generateTargetMonsterListE;
                     this.isFly = false;
                     this.triggerAnim("action_controller", "mob_launch_side");
                     break;
                  case 4:
                     this.spawnedCount = 2;
                     this.targetPool = this.generateTargetMonsterListF;
                     this.isFly = false;
                     this.triggerAnim("action_controller", "mob_launch_side");
                     break;
                  case 5:
                     this.spawnedCount = 2;
                     this.targetPool = this.generateTargetMonsterListC;
                     this.isFly = true;
                     this.triggerAnim("action_controller", "mob_launch_side");
               }

               this.spawnIndex++;
            } else {
               this.spawnedCount = 0;
            }
         } else if (action.currentActionTick % 5 == 0 && this.spawnedCount > 0) {
            Random rand = new Random();
            Level level = this.m_9236_();

            for (int side = -1; side < 2; side += 2) {
               Vec3 muzzlPos;
               if (this.isFly) {
                  muzzlPos = new Vec3(10 * side, 24.0, 5.0);
               } else {
                  muzzlPos = new Vec3(10 * side, 24.0, 10.0);
               }

               muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_())).m_82549_(this.m_20182_());
               BaseSmallMonsterEntity mob = (BaseSmallMonsterEntity)this.getRandomMobType(rand).m_20615_(level);
               if (mob != null) {
                  mob.m_6034_(muzzlPos.f_82479_, muzzlPos.f_82480_, muzzlPos.f_82481_);
                  level.m_7967_(mob);
                  Vec3 dir;
                  if (this.isFly) {
                     dir = new Vec3(side, 0.0, 0.0).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_())).m_82490_(5 + this.spawnedCount);
                  } else {
                     mob.setClosed(true);
                     dir = getDirectionVector(this.m_146908_(), this.m_146909_()).m_82541_().m_82490_(3 + this.spawnedCount);
                  }

                  mob.m_20334_(dir.f_82479_, 1.0, dir.f_82481_);
                  if (this.m_5448_() != null) {
                     mob.m_6710_(this.m_5448_());
                  }

                  this.spawnedMonsters.add(mob.m_20148_());
               }
            }

            this.spawnedCount--;
         }
      }
   }

   public static Vec3 getDirectionVector(float yawDegrees, float pitchDegrees) {
      float yaw = (float)Math.toRadians(yawDegrees);
      float pitch = (float)Math.toRadians(pitchDegrees);
      double x = -Mth.m_14031_(yaw) * Mth.m_14089_(pitch);
      double y = -Mth.m_14031_(pitch);
      double z = Mth.m_14089_(yaw) * Mth.m_14089_(pitch);
      return new Vec3(x, y, z);
   }

   private EntityType<? extends BaseSmallMonsterEntity> getRandomMobType(Random rand) {
      int max = this.targetPool.size();
      return this.targetPool.get(Math.abs(rand.nextInt()) % max);
   }

   private void hadohoAction(BossActionController.BossAction action) {
      LivingEntity target = this.m_5448_();
      if (target != null) {
         this.rotateToTarget(target);
         this.m_20334_(0.0, 0.0, -0.1);
         this.f_19812_ = true;
         this.targetPosHistory.add(target.m_20182_());
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "hadoho");
            this.targetPosHistory.clear();
            this.laserLookAtPos = target.m_20182_();
         } else if (action.onEndOfAction()) {
            this.setLaserLength(0);
         } else if (action.currentActionTick < 32) {
            this.laserLookAtPos = target.m_20182_();
         } else if (action.currentActionTick == 32) {
            this.setLaserLength(1);
         } else if (action.currentActionTick > 35 && action.currentActionTick < 74 && action.currentActionTick % 3 == 0) {
            if (this.targetPosHistory.size() > 7) {
               this.laserLookAtPos = this.targetPosHistory.get(this.targetPosHistory.size() - 6);
            }

            this.fireLaser(this.m_20182_().m_82520_(0.0, this.getLaserHeight(), 0.0), this.laserLookAtPos, this.getMechData().laserDamage / 2.0F, 2);
            this.setLaserLength((int)this.currentLaserLength);
         }

         this.f_21365_.m_24964_(this.laserLookAtPos);
      }
   }

   public float m_20236_(Pose pose) {
      return this.getLaserHeight();
   }

   private float getLaserHeight() {
      return 5.5F;
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
   public void m_7380_(CompoundTag compound) {
      super.m_7380_(compound);
      ListTag list = new ListTag();

      for (UUID uuid : this.spawnedMonsters) {
         list.add(NbtUtils.m_129226_(uuid));
      }

      compound.m_128365_(PomkotsMechs.nbtName("SpawnedMonstersPmb06"), list);
   }

   @Override
   public void m_7378_(CompoundTag compound) {
      super.m_7378_(compound);
      this.spawnedMonsters.clear();
      ListTag list = (ListTag)compound.m_128423_(PomkotsMechs.nbtName("SpawnedMonstersPmb06"));
      if (list != null) {
         for (Tag t : list) {
            this.spawnedMonsters.add(NbtUtils.m_129233_(t));
         }
      }
   }

   @Override
   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(LASER_LENGTH, 0);
      this.f_19804_.m_135372_(TARGET, Integer.MIN_VALUE);
   }

   private void setTargetEntityID(int id) {
      this.f_19804_.m_135381_(TARGET, id);
   }

   public int getTargetEntityID() {
      return (Integer)this.f_19804_.m_135370_(TARGET);
   }

   private void setLaserLength(int value) {
      this.f_19804_.m_135381_(LASER_LENGTH, value);
   }

   public int getLaserLength() {
      return (Integer)this.f_19804_.m_135370_(LASER_LENGTH);
   }

   protected boolean isMoving() {
      Vec3 vel = this.m_20184_();
      return Mth.m_14154_((float)vel.f_82479_) > 0.0F || Mth.m_14154_((float)vel.f_82481_) > 0.0F || Mth.m_14154_((float)vel.f_82480_) > 0.0F;
   }

   public static AABB getFrontDestroyAABB(Entity e) {
      Direction dir = e.m_6350_();
      double w = e.m_20205_() + 10.0F;
      double h = e.m_20206_();
      double half = w / 2.0;
      Vec3 pos = e.m_20182_();
      switch (dir) {
         case NORTH:
            return new AABB(pos.f_82479_ - half, pos.f_82480_, pos.f_82481_ - 2.0, pos.f_82479_ + half, pos.f_82480_ + h, pos.f_82481_ - 0.1);
         case SOUTH:
            return new AABB(pos.f_82479_ - half, pos.f_82480_, pos.f_82481_ + 0.1, pos.f_82479_ + half, pos.f_82480_ + h, pos.f_82481_ + 2.0);
         case WEST:
            return new AABB(pos.f_82479_ - 2.0, pos.f_82480_, pos.f_82481_ - half, pos.f_82479_ - 0.1, pos.f_82480_ + h, pos.f_82481_ + half);
         case EAST:
            return new AABB(pos.f_82479_ + 0.1, pos.f_82480_, pos.f_82481_ - half, pos.f_82479_ + 2.0, pos.f_82480_ + h, pos.f_82481_ + half);
         default:
            return new AABB(pos, pos);
      }
   }

   public static void destroyBlocksInBox(ServerLevel level, AABB aabb) {
      BlockPos.m_121921_(aabb).forEach(pos -> {
         if (!level.m_46859_(pos)) {
            Utils.destroyBlock(level, pos, false);
         }
      });
   }

   protected void handleCollisionWithBlocks(AABB bossBoundingBox) {
      bossBoundingBox.m_165893_(bossBoundingBox.f_82289_ + 13.0);
      bossBoundingBox.m_165887_(bossBoundingBox.f_82289_ + 3.0);
      this.breakBlocksInAABB(bossBoundingBox);
   }

   protected void breakBlocksInAABB(AABB aabb) {
      int minX = Mth.m_14107_(aabb.f_82288_);
      int maxX = Mth.m_14107_(aabb.f_82291_);
      int minZ = Mth.m_14107_(aabb.f_82290_);
      int maxZ = Mth.m_14107_(aabb.f_82293_);
      int minY = Mth.m_14107_(aabb.f_82289_);
      int maxY = Mth.m_14107_(aabb.f_82292_);

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
               BlockPos pos = new BlockPos(x, y, z);
               BlockState blockState = this.m_9236_().m_8055_(pos);
               if (!blockState.m_60795_()) {
                  Utils.destroyBlock(this.m_9236_(), pos, false);
               }
            }
         }
      }
   }

   protected void handleCollisionWithLivingEntities(AABB bossBoundingBox) {
      bossBoundingBox = bossBoundingBox.m_165893_(this.m_20191_().f_82289_ + 3.0);

      for (LivingEntity ent : this.m_9236_().m_6443_(LivingEntity.class, bossBoundingBox.m_82400_(1.0), entity -> !(entity instanceof LivingEntity))) {
         if (bossBoundingBox.m_82381_(ent.m_20191_()) && !this.isSelf(ent)) {
            Vec3 kbVel = ent.m_20182_().m_82505_(this.m_20182_()).m_82541_();
            DamageSource ds = this.m_269291_().m_269264_();
            ent.m_147240_(4.0, kbVel.f_82479_, kbVel.f_82481_);
            ent.m_6469_(ds, 20.0F);
         }
      }
   }
}
