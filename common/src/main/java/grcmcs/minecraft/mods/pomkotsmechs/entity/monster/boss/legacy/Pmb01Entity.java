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
   private final ServerBossEvent bossInfo = new ServerBossEvent(this.m_5446_(), BossBarColor.RED, BossBarOverlay.PROGRESS);
   private Vec3 clientSeatPos = Vec3.f_82478_;
   private DriverInput driverInput = null;
   private static final EntityDataAccessor<Boolean> MODE = SynchedEntityData.m_135353_(Pmb01Entity.class, EntityDataSerializers.f_135035_);

   @Override
   public String getMechName() {
      return "pmb01";
   }

   public Pmb01Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.actionController = new BossActionController();
      this.m_274367_(this.getMechData().maxStepUp);
      this.m_7910_(this.getMechData().speed);
      this.m_21530_();
      this.m_20242_(false);
      this.m_146922_(0.0F);
      this.f_19811_ = true;
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
      this.f_21346_.m_25352_(1, new NearestAttackableTargetGoal(this, Player.class, false, false));
      this.f_21345_.m_25352_(1, new AttackBreakBossGoal(this.actionController.getAction("lbrk"), this, 0.8F));
      this.f_21345_.m_25352_(1, new AttackMeleeBossGoal(this.actionController.getAction("punch"), this, 0.8F));
      this.f_21345_.m_25352_(1, new AttackMeleeBossGoal(this.actionController.getAction("upper"), this, 0.8F));
      this.f_21345_.m_25352_(1, new AttackMeleeBossGoal(this.actionController.getAction("jump"), this, 0.8F));
      this.f_21345_.m_25352_(1, new AttackLongRangeBossGoal(this.actionController.getAction("msl"), this, 0.8F));
      this.f_21345_.m_25352_(1, new AttackLongRangeBossGoal(this.actionController.getAction("lmsl"), this, 0.8F));
      this.f_21345_.m_25352_(1, new AttackLongRangeBossGoal(this.actionController.getAction("gat"), this, 0.8F, true));
      this.f_21345_.m_25352_(1, new AttackLongRangeBossGoal(this.actionController.getAction("grn"), this, 0.8F));
      this.f_21345_.m_25352_(2, new WalkBossGoal(this, 0.6F));
      this.f_21345_.m_25352_(3, new RandomLookAroundGoal(this));
   }

   private void attackPunch() {
      if (this.isServerSide()) {
         Vec3 offset = this.m_20182_();

         for (int i = 0; i < 12; i++) {
            Vec3 muzzlPos = new Vec3(0.0, 0.0, 4.0);
            muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-90 + i * 15));
            muzzlPos = offset.m_82549_(muzzlPos);
            SlashEntity be = new SlashEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOADSLASH.get(), this.m_9236_());
            be.m_146884_(offset);
            be.m_37251_(be, 0.0F, -180 + i * 30, this.m_21256_(), 1.5F, 0.0F);
            this.m_9236_().m_7967_(be);
         }
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EXPLOADSPARK_EVENT.get());
      }
   }

   private void attackUpper() {
      if (this.isServerSide()) {
         Vec3 offset = this.m_20182_();
         Vec3 muzzlPos = new Vec3(0.0, 0.0, 3.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         Vec3 vec = muzzlPos.m_82541_().m_82542_(6.0, 6.0, 6.0);
         muzzlPos = offset.m_82549_(muzzlPos);
         EarthraiseEntity be = new EarthraiseEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EARTHRAISE.get(), this.m_9236_(), vec, this, 8);
         be.m_146884_(muzzlPos);
         be.m_146922_((float)(Math.toDegrees(Math.atan2(vec.f_82481_, vec.f_82479_)) - 90.0));
         this.m_9236_().m_7967_(be);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get());
      }
   }

   private void attackJump() {
      if (this.isServerSide()) {
         Vec3 offset = this.m_20182_();
         Vec3 muzzlPos = new Vec3(0.0, 0.0, -3.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         muzzlPos = offset.m_82549_(muzzlPos);
         EarthbreakEntity be = new EarthbreakEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EARTHBREAK.get(), this.m_9236_(), this);
         be.m_146884_(muzzlPos);
         be.m_37251_(be, 0.0F, 0.0F, this.m_21256_(), 0.0F, 0.0F);
         this.m_9236_().m_7967_(be);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get());
      }
   }

   private void attackMissile() {
      if (this.isServerSide()) {
         Vec3 offset = this.m_20182_();
         Vec3 muzzlPos = new Vec3(-10.0, 15.0, 0.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         muzzlPos = offset.m_82549_(muzzlPos);

         for (int i = 0; i < 5; i++) {
            MissileEnemyEntity be = new MissileEnemyEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.m_9236_(), this, this.m_5448_(), 15.0F, 1.5F
            );
            be.m_146884_(muzzlPos.m_82520_(0.0, i, 0.0));
            be.m_37251_(be, -i * 2, this.m_146908_() + 20.0F, this.m_21256_(), 1.5F, 0.0F);
            this.m_9236_().m_7967_(be);
         }

         muzzlPos = new Vec3(10.0, 15.0, 0.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         muzzlPos = offset.m_82549_(muzzlPos);

         for (int i = 0; i < 5; i++) {
            MissileEnemyEntity be = new MissileEnemyEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.m_9236_(), this, this.m_5448_(), 15.0F, 1.5F
            );
            be.m_146884_(muzzlPos.m_82520_(0.0, i, 0.0));
            be.m_37251_(be, -i * 2, this.m_146908_() - 20.0F, this.m_21256_(), 1.5F, 0.0F);
            this.m_9236_().m_7967_(be);
         }
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());
      }
   }

   private void attackLargeMissile() {
      if (this.isServerSide()) {
         Vec3 offset = this.m_20182_();
         Vec3 muzzlPos = new Vec3(0.0, 15.0, 1.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         muzzlPos = offset.m_82549_(muzzlPos);
         MissileEnemyLargeEntity be = new MissileEnemyLargeEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY_LARGE.get(), this.m_9236_(), this
         );
         be.m_146884_(muzzlPos);
         be.m_37251_(be, -89.0F, this.m_146908_(), this.m_21256_(), 1.5F, 0.0F);
         this.m_9236_().m_7967_(be);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());
      }
   }

   private void attackGatling() {
      if (this.isServerSide()) {
         LivingEntity target = this.m_5448_();
         if (target != null && !this.m_20160_()) {
            this.rotateToTarget(target);
         }

         GrenadeEntity be = new GrenadeEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.GRENADE.get(), this.m_9236_(), this, 2.0F, 10.0F);
         Vec3 offset = this.m_20182_();
         Vec3 muzzlPos = new Vec3(-8.0, 8.0, -1.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         be.m_146884_(offset.m_82549_(muzzlPos));
         be.m_146922_(this.m_146908_());
         be.m_146926_(this.m_146909_());
         be.f_19859_ = this.m_146908_();
         be.f_19860_ = this.m_146909_();
         float[] angle = this.getShootingAngle(be, target);
         be.m_37251_(be, angle[0], angle[1], this.m_21256_(), 2.0F, 0.0F);
         this.m_9236_().m_7967_(be);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_HIT_EVENT.get());
      }
   }

   private void attackGrenade() {
      if (this.isServerSide()) {
         LivingEntity target = this.m_5448_();
         if (target != null && !this.m_20160_()) {
            this.rotateToTarget(target);
         }

         GrenadeLargeEntity be = new GrenadeLargeEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.GRENADELARGE.get(), this.m_9236_(), this, 8.0F);
         Vec3 offset = this.m_20182_();
         Vec3 muzzlPos = new Vec3(8.0, 8.0, -1.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         be.m_146884_(offset.m_82549_(muzzlPos));
         be.m_146922_(this.m_146908_());
         be.m_146926_(this.m_146909_());
         be.f_19859_ = this.m_146908_();
         be.f_19860_ = this.m_146909_();
         float[] angle = this.getShootingAngle(be, target);
         be.m_37251_(be, angle[0], angle[1], this.m_21256_(), 2.0F, 0.0F);
         this.m_9236_().m_7967_(be);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GRENADE_EVENT.get());
      }
   }

   private float[] getShootingAngle(Entity bullet, LivingEntity target) {
      double xRot = 0.0;
      double yRot = 0.0;
      if (target != null && this.m_6688_() == null) {
         Vec3 positionA = bullet.m_20182_();
         Vec3 positionB = target.m_20191_().m_82399_();
         double deltaX = positionB.f_82479_ - positionA.f_82479_;
         double deltaY = target.m_20191_().f_82289_ - positionA.f_82480_;
         double deltaZ = positionB.f_82481_ - positionA.f_82481_;
         yRot = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0;
         double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
         xRot = -Math.toDegrees(Math.atan2(deltaY, distanceXZ));
      } else if (this.m_6688_() == null) {
         Vec3 lookAngle = this.m_20154_();
         xRot = -Math.toDegrees(Math.asin(lookAngle.f_82480_)) - 7.5;
         yRot = Math.toDegrees(Math.atan2(lookAngle.f_82481_, lookAngle.f_82479_)) - 90.0;
      } else {
         Vec3 lookAngle = this.m_6688_().m_20154_();
         xRot = -Math.toDegrees(Math.asin(lookAngle.f_82480_)) - 20.0;
         yRot = Math.toDegrees(Math.atan2(lookAngle.f_82481_, lookAngle.f_82479_)) - 90.0;
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
   public void m_8119_() {
      if (!this.m_9236_().f_46443_) {
         Vec3 offset = this.m_20182_();
         if (this.f_19803_) {
            this.hitBox1 = new HitBoxEntity((EntityType<? extends LivingEntity>)PomkotsMechs.HITBOX1.get(), this.m_9236_(), this);
            this.hitBox1.m_6034_(offset.f_82479_, offset.f_82480_ + 9.0, offset.f_82481_);
            this.hitBox1.setBreakCallback(Void -> {
               if (this.isServerSide()) {
                  this.triggerAnim("break_controller", "break");
               }
            });
            this.m_9236_().m_7967_(this.hitBox1);
            this.hitBox2 = new HitBoxLegsEntity((EntityType<? extends LivingEntity>)PomkotsMechs.HITBOX2.get(), this.m_9236_(), this);
            this.hitBox2.m_6034_(offset.f_82479_, offset.f_82480_, offset.f_82481_);
            this.hitBox2.setBreakCallback(Void -> {
               if (this.isServerSide()) {
                  this.setBreakFlag(true);
               }
            });
            this.m_9236_().m_7967_(this.hitBox2);
         } else {
            if (this.actionController.getAction("lbrk").isInAction()) {
               this.hitBox1.m_6034_(offset.f_82479_, offset.f_82480_, offset.f_82481_);
            } else {
               this.hitBox1.m_6034_(offset.f_82479_, offset.f_82480_ + 9.0, offset.f_82481_);
            }

            this.hitBox2.m_6034_(offset.f_82479_, offset.f_82480_, offset.f_82481_);
         }
      }

      super.m_8119_();
      this.actionController.tick();
      if (this.m_6084_() && this.m_20160_()) {
         if (this.rideCoolTick == 0) {
            this.applyPlayerControll();
         }
      } else if (!this.m_21525_()) {
         this.rotateToTarget(this.m_5448_());
      }
   }

   public void m_8107_() {
      if (this.waitTicks > 0) {
         this.waitTicks--;
         this.m_20256_(Vec3.f_82478_);
         this.m_21557_(true);
      } else {
         if (this.m_21525_()) {
            this.m_21557_(false);
         }

         super.m_8107_();
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
   public boolean m_6469_(DamageSource source, float amount) {
      if (source.m_276093_(DamageTypes.f_268565_)) {
         amount *= this.getMechData().explosionDamageModifier;
      }

      amount *= this.getMechData().baseDamageModifier;
      ParticleUtil.addParticles(source, this);
      return super.m_6469_(source, amount);
   }

   public void m_6457_(ServerPlayer player) {
      super.m_6457_(player);
      this.bossInfo.m_6543_(player);
   }

   public void m_6452_(ServerPlayer player) {
      super.m_6452_(player);
      this.bossInfo.m_6539_(player);
   }

   public void m_8024_() {
      super.m_8024_();
      this.bossInfo.m_142711_(this.m_21223_() / this.m_21233_());
   }

   @Override
   public void rotateToTarget(LivingEntity target) {
      if (target != null) {
         Vec3 tgtPos = target.m_20182_();
         Vec3 slfPos = this.m_20182_();
         Vec3 v = slfPos.m_82505_(tgtPos);
         this.m_146922_((float)Math.toDegrees(Mth.m_14136_(v.f_82481_, v.f_82479_)) - 90.0F);
         this.m_5618_(this.m_146908_());
         this.m_5616_(this.m_146908_());
         this.f_19859_ = this.m_146908_();
         this.f_20884_ = this.m_146908_();
         this.f_20886_ = this.m_146908_();
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

   public double m_6048_() {
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

   public void m_7355_(BlockPos blockPos, BlockState blockState) {
      if (this.isClientSide() && this.f_19797_ % 10 == 0) {
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
   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(MODE, true);
   }

   private void setMainMode(boolean value) {
      this.f_19804_.m_135381_(MODE, value);
   }

   public boolean isMainMode() {
      return (Boolean)this.f_19804_.m_135370_(MODE);
   }

   public boolean m_142535_(float f1, float f2, DamageSource damageSource) {
      return false;
   }
}
