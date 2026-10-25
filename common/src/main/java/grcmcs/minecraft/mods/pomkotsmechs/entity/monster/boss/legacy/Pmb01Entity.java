package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonsterPercistant;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.goal.AttackBreakBossGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.goal.AttackLongRangeBossGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.goal.AttackMeleeBossGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.goal.WalkBossGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthbreakEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthraiseEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeLargeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileEnemyEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileEnemyLargeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.SlashEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pmb01Entity extends GenericPomkotsMonsterPercistant implements GeoEntity, GeoAnimatable, PomkotsControllable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 2.0F;
   public static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");
   private final BossActionController actionController;
   private HitBoxEntity hitBox1 = null;
   private HitBoxEntity hitBox2 = null;
   private boolean breakFlag = false;
   private int waitTicks = 60;
   private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), BossBarColor.RED, BossBarOverlay.PROGRESS);
   private Vec3 clientSeatPos = Vec3.ZERO;
   private DriverInput driverInput = null;
   private static final EntityDataAccessor<Boolean> MODE = SynchedEntityData.defineId(Pmb01Entity.class, EntityDataSerializers.BOOLEAN);

   @Override
   public String getMechName() {
      return "pmb01";
   }

   public Pmb01Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.actionController = new BossActionController();
      this.setMaxUpStep(this.getMechData().maxStepUp);
      this.setSpeed(this.getMechData().speed);
      this.setPersistenceRequired();
      this.setNoGravity(false);
      this.setYRot(0.0F);
      this.noCulling = true;
      this.actionController.registerAction("punch", new BossActionController.BossAction(180, 20, 15, Void -> {
         if (this.isServerSide()) {
            this.triggerAnim("action_controller", "punch");
         }
      }, Void -> this.attackPunch()));
      this.actionController.registerAction("upper", new BossActionController.BossAction(60, 20, 12, Void -> {
         if (this.isServerSide()) {
            this.triggerAnim("action_controller", "upper");
         }
      }, Void -> this.attackUpper()));
      this.actionController.registerAction("jump", new BossActionController.BossAction(60, 25, 20, Void -> {
         if (this.isServerSide()) {
            this.triggerAnim("action_controller", "jump");
         }
      }, Void -> this.attackJump()));
      this.actionController.registerAction("msl", new BossActionController.BossAction(180, 20, 15, Void -> {
         if (this.isServerSide()) {
            this.triggerAnim("action_controller", "msl");
         }
      }, Void -> this.attackMissile()));
      this.actionController.registerAction("lmsl", new BossActionController.BossAction(600, 20, 15, Void -> {
         if (this.isServerSide()) {
            this.triggerAnim("action_controller", "lmsl");
         }
      }, Void -> this.attackLargeMissile()));
      this.actionController.registerAction("gat", new BossActionController.BossAction(180, 5, 1, 10, Void -> {
         if (this.isServerSide()) {
            this.triggerAnim("action_controller", "gat");
         }
      }, Void -> this.attackGatling()));
      this.actionController.registerAction("grn", new BossActionController.BossAction(180, 20, 12, Void -> {
         if (this.isServerSide()) {
            this.triggerAnim("action_controller", "grn");
         }
      }, Void -> this.attackGrenade()));
      this.actionController.registerAction("lbrk", new BossActionController.BossAction(180, 95, 12, Void -> {
         if (this.isServerSide()) {
            this.triggerAnim("action_controller", "lbrk");
         }
      }, Void -> {}));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, false, false));
      this.goalSelector.addGoal(1, new AttackBreakBossGoal(this.actionController.getAction("lbrk"), this, 0.8F));
      this.goalSelector.addGoal(1, new AttackMeleeBossGoal(this.actionController.getAction("punch"), this, 0.8F));
      this.goalSelector.addGoal(1, new AttackMeleeBossGoal(this.actionController.getAction("upper"), this, 0.8F));
      this.goalSelector.addGoal(1, new AttackMeleeBossGoal(this.actionController.getAction("jump"), this, 0.8F));
      this.goalSelector.addGoal(1, new AttackLongRangeBossGoal(this.actionController.getAction("msl"), this, 0.8F));
      this.goalSelector.addGoal(1, new AttackLongRangeBossGoal(this.actionController.getAction("lmsl"), this, 0.8F));
      this.goalSelector.addGoal(1, new AttackLongRangeBossGoal(this.actionController.getAction("gat"), this, 0.8F, true));
      this.goalSelector.addGoal(1, new AttackLongRangeBossGoal(this.actionController.getAction("grn"), this, 0.8F));
      this.goalSelector.addGoal(2, new WalkBossGoal(this, 0.6F));
      this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
   }

   private void attackPunch() {
      if (this.isServerSide()) {
         Vec3 offset = this.position();

         for (int i = 0; i < 12; i++) {
            Vec3 muzzlPos = new Vec3(0.0, 0.0, 4.0);
            muzzlPos = muzzlPos.yRot((float)Math.toRadians(-90 + i * 15));
            muzzlPos = offset.add(muzzlPos);
            SlashEntity be = new SlashEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOADSLASH.get(), this.level());
            be.setPos(offset);
            be.shootFromRotation(be, 0.0F, -180 + i * 30, this.getFallFlyingTicks(), 1.5F, 0.0F);
            this.level().addFreshEntity(be);
         }
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EXPLOADSPARK_EVENT.get());
      }
   }

   private void attackUpper() {
      if (this.isServerSide()) {
         Vec3 offset = this.position();
         Vec3 muzzlPos = new Vec3(0.0, 0.0, 3.0);
         muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         Vec3 vec = muzzlPos.normalize().multiply(6.0, 6.0, 6.0);
         muzzlPos = offset.add(muzzlPos);
         EarthraiseEntity be = new EarthraiseEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EARTHRAISE.get(), this.level(), vec, this, 8);
         be.setPos(muzzlPos);
         be.setYRot((float)(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0));
         this.level().addFreshEntity(be);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get());
      }
   }

   private void attackJump() {
      if (this.isServerSide()) {
         Vec3 offset = this.position();
         Vec3 muzzlPos = new Vec3(0.0, 0.0, -3.0);
         muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         muzzlPos = offset.add(muzzlPos);
         EarthbreakEntity be = new EarthbreakEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EARTHBREAK.get(), this.level(), this);
         be.setPos(muzzlPos);
         be.shootFromRotation(be, 0.0F, 0.0F, this.getFallFlyingTicks(), 0.0F, 0.0F);
         this.level().addFreshEntity(be);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get());
      }
   }

   private void attackMissile() {
      if (this.isServerSide()) {
         Vec3 offset = this.position();
         Vec3 muzzlPos = new Vec3(-10.0, 15.0, 0.0);
         muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         muzzlPos = offset.add(muzzlPos);

         for (int i = 0; i < 5; i++) {
            MissileEnemyEntity be = new MissileEnemyEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this, this.getTarget(), 15.0F, 1.5F
            );
            be.setPos(muzzlPos.add(0.0, i, 0.0));
            be.shootFromRotation(be, -i * 2, this.getYRot() + 20.0F, this.getFallFlyingTicks(), 1.5F, 0.0F);
            this.level().addFreshEntity(be);
         }

         muzzlPos = new Vec3(10.0, 15.0, 0.0);
         muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         muzzlPos = offset.add(muzzlPos);

         for (int i = 0; i < 5; i++) {
            MissileEnemyEntity be = new MissileEnemyEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this, this.getTarget(), 15.0F, 1.5F
            );
            be.setPos(muzzlPos.add(0.0, i, 0.0));
            be.shootFromRotation(be, -i * 2, this.getYRot() - 20.0F, this.getFallFlyingTicks(), 1.5F, 0.0F);
            this.level().addFreshEntity(be);
         }
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());
      }
   }

   private void attackLargeMissile() {
      if (this.isServerSide()) {
         Vec3 offset = this.position();
         Vec3 muzzlPos = new Vec3(0.0, 15.0, 1.0);
         muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         muzzlPos = offset.add(muzzlPos);
         MissileEnemyLargeEntity be = new MissileEnemyLargeEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY_LARGE.get(), this.level(), this
         );
         be.setPos(muzzlPos);
         be.shootFromRotation(be, -89.0F, this.getYRot(), this.getFallFlyingTicks(), 1.5F, 0.0F);
         this.level().addFreshEntity(be);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());
      }
   }

   private void attackGatling() {
      if (this.isServerSide()) {
         LivingEntity target = this.getTarget();
         if (target != null && !this.isVehicle()) {
            this.rotateToTarget(target);
         }

         GrenadeEntity be = new GrenadeEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.GRENADE.get(), this.level(), this, 2.0F, 10.0F);
         Vec3 offset = this.position();
         Vec3 muzzlPos = new Vec3(-8.0, 8.0, -1.0);
         muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         be.setPos(offset.add(muzzlPos));
         be.setYRot(this.getYRot());
         be.setXRot(this.getXRot());
         be.yRotO = this.getYRot();
         be.xRotO = this.getXRot();
         float[] angle = this.getShootingAngle(be, target);
         be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), 2.0F, 0.0F);
         this.level().addFreshEntity(be);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_HIT_EVENT.get());
      }
   }

   private void attackGrenade() {
      if (this.isServerSide()) {
         LivingEntity target = this.getTarget();
         if (target != null && !this.isVehicle()) {
            this.rotateToTarget(target);
         }

         GrenadeLargeEntity be = new GrenadeLargeEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.GRENADELARGE.get(), this.level(), this, 8.0F);
         Vec3 offset = this.position();
         Vec3 muzzlPos = new Vec3(8.0, 8.0, -1.0);
         muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         be.setPos(offset.add(muzzlPos));
         be.setYRot(this.getYRot());
         be.setXRot(this.getXRot());
         be.yRotO = this.getYRot();
         be.xRotO = this.getXRot();
         float[] angle = this.getShootingAngle(be, target);
         be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), 2.0F, 0.0F);
         this.level().addFreshEntity(be);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GRENADE_EVENT.get());
      }
   }

   private float[] getShootingAngle(Entity bullet, LivingEntity target) {
      double xRot = 0.0;
      double yRot = 0.0;
      if (target != null && this.getControllingPassenger() == null) {
         Vec3 positionA = bullet.position();
         Vec3 positionB = target.getBoundingBox().getCenter();
         double deltaX = positionB.x - positionA.x;
         double deltaY = target.getBoundingBox().minY - positionA.y;
         double deltaZ = positionB.z - positionA.z;
         yRot = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0;
         double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
         xRot = -Math.toDegrees(Math.atan2(deltaY, distanceXZ));
      } else if (this.getControllingPassenger() == null) {
         Vec3 lookAngle = this.getLookAngle();
         xRot = -Math.toDegrees(Math.asin(lookAngle.y)) - 7.5;
         yRot = Math.toDegrees(Math.atan2(lookAngle.z, lookAngle.x)) - 90.0;
      } else {
         Vec3 lookAngle = this.getControllingPassenger().getLookAngle();
         xRot = -Math.toDegrees(Math.asin(lookAngle.y)) - 20.0;
         yRot = Math.toDegrees(Math.atan2(lookAngle.z, lookAngle.x)) - 90.0;
      }

      return new float[]{(float)xRot, (float)yRot};
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               "basic_move",
               0,
               event -> event.isMoving()
                  ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.walk"))
                  : event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.idle"))
            )
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "action_controller", state -> PlayState.STOP)
               .triggerableAnim("lbrk", RawAnimation.begin().thenPlay("animation.pmb01.hurt").thenPlay("animation.pmb01.down"))
               .triggerableAnim("punch", RawAnimation.begin().thenPlay("animation.pmb01.attackpunch"))
               .triggerableAnim("upper", RawAnimation.begin().thenPlay("animation.pmb01.attackupper"))
               .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.pmb01.attackjump"))
               .triggerableAnim("msl", RawAnimation.begin().thenPlay("animation.pmb01.attackmissile"))
               .triggerableAnim("lmsl", RawAnimation.begin().thenPlay("animation.pmb01.attacklargemissile"))
               .triggerableAnim("gat", RawAnimation.begin().thenPlayXTimes("animation.pmb01.attackgatling", 10))
               .triggerableAnim("grn", RawAnimation.begin().thenPlay("animation.pmb01.attackgrenade"))
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "break_controller", state -> PlayState.STOP)
               .triggerableAnim("break", RawAnimation.begin().thenPlayAndHold("animation.pmb01.bodyfrontbreak"))
         }
      );
   }

   private void setBreakFlag(boolean bool) {
      this.breakFlag = bool;
   }

   public boolean consumeBreakFlag() {
      if (this.breakFlag) {
         this.breakFlag = false;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void tick() {
      if (!this.level().isClientSide) {
         Vec3 offset = this.position();
         if (this.firstTick) {
            this.hitBox1 = new HitBoxEntity((EntityType<? extends LivingEntity>)PomkotsMechs.HITBOX1.get(), this.level(), this);
            this.hitBox1.setPos(offset.x, offset.y + 9.0, offset.z);
            this.hitBox1.setBreakCallback(Void -> {
               if (this.isServerSide()) {
                  this.triggerAnim("break_controller", "break");
               }
            });
            this.level().addFreshEntity(this.hitBox1);
            this.hitBox2 = new HitBoxLegsEntity((EntityType<? extends LivingEntity>)PomkotsMechs.HITBOX2.get(), this.level(), this);
            this.hitBox2.setPos(offset.x, offset.y, offset.z);
            this.hitBox2.setBreakCallback(Void -> {
               if (this.isServerSide()) {
                  this.setBreakFlag(true);
               }
            });
            this.level().addFreshEntity(this.hitBox2);
         } else {
            if (this.actionController.getAction("lbrk").isInAction()) {
               this.hitBox1.setPos(offset.x, offset.y, offset.z);
            } else {
               this.hitBox1.setPos(offset.x, offset.y + 9.0, offset.z);
            }

            this.hitBox2.setPos(offset.x, offset.y, offset.z);
         }
      }

      super.tick();
      this.actionController.tick();
      if (this.isAlive() && this.isVehicle()) {
         if (this.rideCoolTick == 0) {
            this.applyPlayerControll();
         }
      } else if (!this.isNoAi()) {
         this.rotateToTarget(this.getTarget());
      }
   }

   public void aiStep() {
      if (this.waitTicks > 0) {
         this.waitTicks--;
         this.setDeltaMovement(Vec3.ZERO);
         this.setNoAi(true);
      } else {
         if (this.isNoAi()) {
            this.setNoAi(false);
         }

         super.aiStep();
      }
   }

   private void applyPlayerControll() {
      if (this.driverInput != null && this.isServerSide()) {
         if (this.driverInput.isWeaponRightHandPressed()) {
            if (this.isMainMode()) {
               this.actionController.getAction("punch").tryAction();
            } else {
               this.actionController.getAction("gat").tryAction();
            }
         } else if (this.driverInput.isWeaponLeftHandPressed()) {
            if (this.isMainMode()) {
               this.actionController.getAction("upper").tryAction();
            } else {
               this.actionController.getAction("grn").tryAction();
            }
         } else if (this.driverInput.isWeaponRightShoulderPressed()) {
            if (this.isMainMode()) {
               this.actionController.getAction("jump").tryAction();
            } else {
               this.actionController.getAction("msl").tryAction();
            }
         } else if (this.driverInput.isWeaponLeftShoulderPressed()) {
            if (this.isMainMode()) {
               this.actionController.getAction("jump").tryAction();
            } else {
               this.actionController.getAction("lmsl").tryAction();
            }
         }
      }
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      if (source.is(DamageTypes.EXPLOSION)) {
         amount *= this.getMechData().explosionDamageModifier;
      }

      amount *= this.getMechData().baseDamageModifier;
      ParticleUtil.addParticles(source, this);
      return super.hurt(source, amount);
   }

   public void startSeenByPlayer(ServerPlayer player) {
      super.startSeenByPlayer(player);
      this.bossInfo.addPlayer(player);
   }

   public void stopSeenByPlayer(ServerPlayer player) {
      super.stopSeenByPlayer(player);
      this.bossInfo.removePlayer(player);
   }

   public void customServerAiStep() {
      super.customServerAiStep();
      this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
   }

   @Override
   public void rotateToTarget(LivingEntity target) {
      if (target != null) {
         Vec3 tgtPos = target.position();
         Vec3 slfPos = this.position();
         Vec3 v = slfPos.vectorTo(tgtPos);
         this.setYRot((float)Math.toDegrees(Mth.atan2(v.z, v.x)) - 90.0F);
         this.setYBodyRot(this.getYRot());
         this.setYHeadRot(this.getYRot());
         this.yRotO = this.getYRot();
         this.yBodyRotO = this.getYRot();
         this.yHeadRotO = this.getYRot();
      }
   }

   @Override
   public boolean tryAttack() {
      return false;
   }

   @Override
   public void doAttack() {
   }

   @Override
   public int getMaxAttackCooltime() {
      return 100;
   }

   public double getPassengersRidingOffset() {
      return 19.0;
   }

   public void setClientSeatPos(Vec3 seatPos) {
      this.clientSeatPos = seatPos;
   }

   public Vec3 getClientSeatPos() {
      return this.clientSeatPos;
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public void playStepSound(BlockPos blockPos, BlockState blockState) {
      if (this.isClientSide() && this.tickCount % 10 == 0) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_WALK_EVENT.get());
      }
   }

   @Override
   public void setDriverInput(DriverInput di) {
      this.driverInput = new DriverInput(di.getStatus(), this.driverInput);
      if (this.isServerSide() && di.isModeChangePressed()) {
         this.setMainMode(!this.isMainMode());
      }
   }

   @Override
   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(MODE, true);
   }

   private void setMainMode(boolean value) {
      this.entityData.set(MODE, value);
   }

   public boolean isMainMode() {
      return (Boolean)this.entityData.get(MODE);
   }

   public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
      return false;
   }
}
