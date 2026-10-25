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
   private Vec3 laserLookAtPos = Vec3.ZERO;
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
                  if (!this.isNoGravity()) {
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
                  return !this.isNoGravity()
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
   private static final EntityDataAccessor<Integer> LASER_LENGTH = SynchedEntityData.defineId(Pmb06Entity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(Pmb06Entity.class, EntityDataSerializers.INT);

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
      this.setNoGravity(false);
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
   public void tick() {
      if (this.isServerSide()) {
         if (this.firstTick) {
            this.registerAdditionalHitBox(new BossHitBoxEntity((EntityType<? extends LivingEntity>)PomkotsMechs.HITBOX_PMB06.get(), this.level(), this));
         }

         if (!this.spawnedMonsters.isEmpty()) {
            this.spawnedMonsters.removeIf(uuid -> {
               Entity e = ((ServerLevel)this.level()).getEntity(uuid);
               return e == null || !e.isAlive();
            });
         }

         if (this.getTarget() != null) {
            this.setTargetEntityID(this.getTarget().getId());
         }

         if (this.isMoving() && Utils.isBlockDestructionAllowed(this)) {
         }
      }

      super.tick();
   }

   private void gatlingAction(BossActionController.BossAction action) {
      LivingEntity target = this.getTarget();
      if (target != null) {
         if (action.currentActionTick == 2) {
            for (int side = -1; side < 2; side += 2) {
               for (int r = 0; r < 3; r++) {
                  BulletMachineEntity be = new BulletMachineEntity(
                     (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_MACHINE.get(), this.level(), this, this.getMechData().bulletDamage / 2.0F
                  );
                  be.setNoGravity(true);
                  be.setStunPoint(2);
                  Vec3 offset = this.position();
                  Vec3 muzzlPos = new Vec3(12 * side, 22 + r * 2, 15.0);
                  muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
                  be.setPos(offset.add(muzzlPos));
                  float[] angle = Utils.getShootingAngle(be, target, true);
                  be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), this.getMechData().bulletSpeed, 0.0F);
                  this.level().addFreshEntity(be);
                  ParticleUtil.spawnAttachedMuzzleFlash((ServerLevel)this.level(), this, 30.0, new Vec3(12 * side, 22 + r * 2, 17.0));
               }
            }
         }
      }
   }

   private void smallCanonAction(BossActionController.BossAction action) {
      LivingEntity target = this.getTarget();
      if (target != null) {
         if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "small_canon");
         } else if (action.currentActionTick == 3) {
            for (int side = -1; side < 2; side += 2) {
               BulletGrenadeEntity be = new BulletGrenadeEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE.get(), this.level(), this, this.getMechData().bulletDamage
               );
               be.setNoGravity(true);
               be.setExplosionScale(1);
               Vec3 offset = this.position();
               Vec3 muzzlPos = new Vec3(side * 2, 30.0, 15.0);
               muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
               be.setPos(offset.add(muzzlPos));
               float[] angle = Utils.getShootingAngle(be, target, true);
               be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), this.getMechData().bulletSpeed, 0.0F);
               this.level().addFreshEntity(be);
               ParticleUtil.spawnAttachedMuzzleFlash((ServerLevel)this.level(), this, 50.0, new Vec3(side * 2, 30.0, 17.0));
            }
         }
      }
   }

   private void largeCanonAction(BossActionController.BossAction action) {
      LivingEntity target = this.getTarget();
      if (target != null) {
         if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "large_canon");
         } else if (action.currentActionTick == 5) {
            BulletGrenadeEntity be = new BulletGrenadeEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE.get(), this.level(), this, this.getMechData().grenadeDamage
            );
            be.setNoGravity(true);
            be.setExplosionScale(5);
            Vec3 offset = this.position();
            Vec3 muzzlPos = new Vec3(0.0, 30.0, 15.0);
            muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
            be.setPos(offset.add(muzzlPos));
            float[] angle = Utils.getShootingAngle(be, target, true);
            be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), this.getMechData().grenadeSpeed, 0.0F);
            this.level().addFreshEntity(be);
            ParticleUtil.spawnAttachedMuzzleFlash((ServerLevel)this.level(), this, 80.0, new Vec3(0.0, 30.0, 17.0));
         }
      }
   }

   private void missileVerticalAction(BossActionController.BossAction action) {
      LivingEntity target = this.getTarget();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");
         } else if (action.currentActionTick == 2) {
            for (int row = 0; row < 2; row++) {
               for (int col = 0; col < 2; col++) {
                  for (int side = -1; side < 2; side += 2) {
                     MissileGenericEntity be = new MissileGenericEntity(
                        (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                        this.level(),
                        this,
                        target,
                        this.getMechData().missileDamage,
                        this.getMechData().missileSpeed
                     );
                     be.setMaxRotationAnglePerTick(2.0F);
                     be.setSwitchTick(16);
                     be.setMaxLifeTick(150);
                     Vec3 offset = this.position();
                     Vec3 muzzlPos = new Vec3((10 + col * 2) * side, 40 + row * 2, 10.0);
                     muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
                     be.setPos(offset.add(muzzlPos));
                     float[] angle = Utils.getShootingAngle(be, target, true);
                     be.shootFromRotation(be, -80.0F, angle[1], this.getFallFlyingTicks(), this.getMechData().missileSpeed, 0.0F);
                     this.level().addFreshEntity(be);
                  }
               }
            }
         }
      }
   }

   private void missileHorizontalAction(BossActionController.BossAction action) {
      LivingEntity target = this.getTarget();
      if (target != null) {
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");
         } else if (action.currentActionTick == 10) {
            for (int row = 0; row < 2; row++) {
               for (int col = 0; col < 2; col++) {
                  for (int side = -1; side < 2; side += 2) {
                     MissileGenericEnemyEntity be = new MissileGenericEnemyEntity(
                        (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                        this.level(),
                        this,
                        target,
                        this.getMechData().missileDamage,
                        this.getMechData().missileSpeed
                     );
                     be.setMaxRotationAnglePerTick(2.0F);
                     be.setSwitchTick(16);
                     be.setMaxLifeTick(150);
                     Vec3 offset = this.position();
                     Vec3 muzzlPos = new Vec3((10 + col * 2) * side, 40 + row * 2, 10.0);
                     muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
                     be.setPos(offset.add(muzzlPos));
                     float[] angle = Utils.getShootingAngle(be, target, true);
                     be.shootFromRotation(be, angle[0], angle[1] - side * 30, this.getFallFlyingTicks(), this.getMechData().missileSpeed, 0.0F);
                     this.level().addFreshEntity(be);
                  }
               }
            }
         }
      }
   }

   private void generateMobs(BossActionController.BossAction action) {
      LivingEntity target = this.getTarget();
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
            Level level = this.level();

            for (int side = -1; side < 2; side += 2) {
               Vec3 muzzlPos;
               if (this.isFly) {
                  muzzlPos = new Vec3(10 * side, 24.0, 5.0);
               } else {
                  muzzlPos = new Vec3(10 * side, 24.0, 10.0);
               }

               muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
               BaseSmallMonsterEntity mob = (BaseSmallMonsterEntity)this.getRandomMobType(rand).create(level);
               if (mob != null) {
                  mob.setPos(muzzlPos.x, muzzlPos.y, muzzlPos.z);
                  level.addFreshEntity(mob);
                  Vec3 dir;
                  if (this.isFly) {
                     dir = new Vec3(side, 0.0, 0.0).yRot((float)Math.toRadians(-1.0 * this.getYRot())).scale(5 + this.spawnedCount);
                  } else {
                     mob.setClosed(true);
                     dir = getDirectionVector(this.getYRot(), this.getXRot()).normalize().scale(3 + this.spawnedCount);
                  }

                  mob.setDeltaMovement(dir.x, 1.0, dir.z);
                  if (this.getTarget() != null) {
                     mob.setTarget(this.getTarget());
                  }

                  this.spawnedMonsters.add(mob.getUUID());
               }
            }

            this.spawnedCount--;
         }
      }
   }

   public static Vec3 getDirectionVector(float yawDegrees, float pitchDegrees) {
      float yaw = (float)Math.toRadians(yawDegrees);
      float pitch = (float)Math.toRadians(pitchDegrees);
      double x = -Mth.sin(yaw) * Mth.cos(pitch);
      double y = -Mth.sin(pitch);
      double z = Mth.cos(yaw) * Mth.cos(pitch);
      return new Vec3(x, y, z);
   }

   private EntityType<? extends BaseSmallMonsterEntity> getRandomMobType(Random rand) {
      int max = this.targetPool.size();
      return this.targetPool.get(Math.abs(rand.nextInt()) % max);
   }

   private void hadohoAction(BossActionController.BossAction action) {
      LivingEntity target = this.getTarget();
      if (target != null) {
         this.rotateToTarget(target);
         this.setDeltaMovement(0.0, 0.0, -0.1);
         this.hasImpulse = true;
         this.targetPosHistory.add(target.position());
         if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "hadoho");
            this.targetPosHistory.clear();
            this.laserLookAtPos = target.position();
         } else if (action.onEndOfAction()) {
            this.setLaserLength(0);
         } else if (action.currentActionTick < 32) {
            this.laserLookAtPos = target.position();
         } else if (action.currentActionTick == 32) {
            this.setLaserLength(1);
         } else if (action.currentActionTick > 35 && action.currentActionTick < 74 && action.currentActionTick % 3 == 0) {
            if (this.targetPosHistory.size() > 7) {
               this.laserLookAtPos = this.targetPosHistory.get(this.targetPosHistory.size() - 6);
            }

            this.fireLaser(this.position().add(0.0, this.getLaserHeight(), 0.0), this.laserLookAtPos, this.getMechData().laserDamage / 2.0F, 2);
            this.setLaserLength((int)this.currentLaserLength);
         }

         this.lookControl.setLookAt(this.laserLookAtPos);
      }
   }

   public float getEyeHeight(Pose pose) {
      return this.getLaserHeight();
   }

   private float getLaserHeight() {
      return 5.5F;
   }

   private void fireLaser(Vec3 startPos, Vec3 targetPos, float damage, int explosionScale) {
      this.laserStart = startPos;
      this.laserDirection = targetPos.subtract(this.laserStart).normalize();
      this.calculateLaserEnd();
      this.damageEntitiesInLaserPath(damage);
      if (this.laserHitBlockPos != Vec3.ZERO) {
         if (explosionScale > 5) {
            ExplosionEntity e = new ExplosionEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOSION.get(), this.level(), explosionScale);
            e.setPos(this.laserHitBlockPos);
            this.level().addFreshEntity(e);
         } else if (explosionScale > 0) {
            Vec3 pos = this.laserHitBlockPos;
            Utils.explode(this, pos.x, pos.y, pos.z, explosionScale, false, ExplosionInteraction.BLOCK, this.level());
         }
      }
   }

   private void calculateLaserEnd() {
      Vec3 start = this.laserStart;
      Vec3 end = start.add(this.laserDirection.scale(this.LASER_MAX_RANGE));
      BlockHitResult hitResult = this.level().clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, this));
      if (hitResult.getType() != Type.MISS) {
         this.currentLaserLength = start.distanceTo(hitResult.getLocation());
         this.laserHitBlockPos = hitResult.getLocation();
         this.laserEnd = hitResult.getLocation();
      } else {
         this.currentLaserLength = this.LASER_MAX_RANGE;
         this.laserHitBlockPos = Vec3.ZERO;
         this.laserEnd = end;
      }
   }

   private void damageEntitiesInLaserPath(float damage) {
      Vec3 start = this.laserStart;
      Vec3 end = start.add(this.laserDirection.scale(this.currentLaserLength));
      AABB searchBox = new AABB(start, end).inflate(this.LASER_WIDTH);

      for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, searchBox, entityx -> entityx != this && this.canAttack(entityx))) {
         if (!this.isSelf(entity) && this.isEntityInLaserPath(entity)) {
            entity.hurt(this.damageSources().mobAttack(this), damage);
         }
      }
   }

   private boolean isEntityInLaserPath(LivingEntity entity) {
      Vec3 entityPos = entity.position().add(0.0, entity.getBbHeight() * 0.5, 0.0);
      Vec3 toEntity = entityPos.subtract(this.laserStart);
      double projectionLength = toEntity.dot(this.laserDirection);
      if (!(projectionLength < 0.0) && !(projectionLength > this.currentLaserLength)) {
         Vec3 closestPoint = this.laserStart.add(this.laserDirection.scale(projectionLength));
         double distance = entityPos.distanceTo(closestPoint);
         return distance <= this.LASER_WIDTH + entity.getBbWidth() * 0.5;
      } else {
         return false;
      }
   }

   private void onGroundAction(BossActionController.BossAction action) {
      if (action.onStartOfAction()) {
         this.triggerAnim("action_controller", "onground");
         Level level = this.level();
         BlockPos landingPos = this.blockPosition();
         AABB damageArea = new AABB(landingPos).inflate(10.0);

         for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, damageArea)) {
            if (!this.isSelf(entity)) {
               entity.hurt(this.damageSources().generic(), 30.0F);
               Vec3 knockback = entity.position().subtract(this.position()).normalize().scale(1.5);
               entity.knockback(2.0, knockback.x, knockback.z);
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
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      ListTag list = new ListTag();

      for (UUID uuid : this.spawnedMonsters) {
         list.add(NbtUtils.createUUID(uuid));
      }

      compound.put(PomkotsMechs.nbtName("SpawnedMonstersPmb06"), list);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      this.spawnedMonsters.clear();
      ListTag list = (ListTag)compound.get(PomkotsMechs.nbtName("SpawnedMonstersPmb06"));
      if (list != null) {
         for (Tag t : list) {
            this.spawnedMonsters.add(NbtUtils.loadUUID(t));
         }
      }
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(LASER_LENGTH, 0);
      this.entityData.define(TARGET, Integer.MIN_VALUE);
   }

   private void setTargetEntityID(int id) {
      this.entityData.set(TARGET, id);
   }

   public int getTargetEntityID() {
      return (Integer)this.entityData.get(TARGET);
   }

   private void setLaserLength(int value) {
      this.entityData.set(LASER_LENGTH, value);
   }

   public int getLaserLength() {
      return (Integer)this.entityData.get(LASER_LENGTH);
   }

   protected boolean isMoving() {
      Vec3 vel = this.getDeltaMovement();
      return Mth.abs((float)vel.x) > 0.0F || Mth.abs((float)vel.z) > 0.0F || Mth.abs((float)vel.y) > 0.0F;
   }

   public static AABB getFrontDestroyAABB(Entity e) {
      Direction dir = e.getDirection();
      double w = e.getBbWidth() + 10.0F;
      double h = e.getBbHeight();
      double half = w / 2.0;
      Vec3 pos = e.position();
      switch (dir) {
         case NORTH:
            return new AABB(pos.x - half, pos.y, pos.z - 2.0, pos.x + half, pos.y + h, pos.z - 0.1);
         case SOUTH:
            return new AABB(pos.x - half, pos.y, pos.z + 0.1, pos.x + half, pos.y + h, pos.z + 2.0);
         case WEST:
            return new AABB(pos.x - 2.0, pos.y, pos.z - half, pos.x - 0.1, pos.y + h, pos.z + half);
         case EAST:
            return new AABB(pos.x + 0.1, pos.y, pos.z - half, pos.x + 2.0, pos.y + h, pos.z + half);
         default:
            return new AABB(pos, pos);
      }
   }

   public static void destroyBlocksInBox(ServerLevel level, AABB aabb) {
      BlockPos.betweenClosedStream(aabb).forEach(pos -> {
         if (!level.isEmptyBlock(pos)) {
            Utils.destroyBlock(level, pos, false);
         }
      });
   }

   protected void handleCollisionWithBlocks(AABB bossBoundingBox) {
      bossBoundingBox.setMaxY(bossBoundingBox.minY + 13.0);
      bossBoundingBox.setMinY(bossBoundingBox.minY + 3.0);
      this.breakBlocksInAABB(bossBoundingBox);
   }

   protected void breakBlocksInAABB(AABB aabb) {
      int minX = Mth.floor(aabb.minX);
      int maxX = Mth.floor(aabb.maxX);
      int minZ = Mth.floor(aabb.minZ);
      int maxZ = Mth.floor(aabb.maxZ);
      int minY = Mth.floor(aabb.minY);
      int maxY = Mth.floor(aabb.maxY);

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
               BlockPos pos = new BlockPos(x, y, z);
               BlockState blockState = this.level().getBlockState(pos);
               if (!blockState.isAir()) {
                  Utils.destroyBlock(this.level(), pos, false);
               }
            }
         }
      }
   }

   protected void handleCollisionWithLivingEntities(AABB bossBoundingBox) {
      bossBoundingBox = bossBoundingBox.setMaxY(this.getBoundingBox().minY + 3.0);

      for (LivingEntity ent : this.level().getEntitiesOfClass(LivingEntity.class, bossBoundingBox.inflate(1.0), entity -> !(entity instanceof LivingEntity))) {
         if (bossBoundingBox.intersects(ent.getBoundingBox()) && !this.isSelf(ent)) {
            Vec3 kbVel = ent.position().vectorTo(this.position()).normalize();
            DamageSource ds = this.damageSources().generic();
            ent.knockback(4.0, kbVel.x, kbVel.z);
            ent.hurt(ds, 20.0F);
         }
      }
   }
}
