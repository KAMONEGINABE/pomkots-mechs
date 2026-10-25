package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.FlyingMobGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.SimpleMobAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletMiddleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEnemyEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pms02Entity extends BaseSmallMonsterEntity implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 1.0F;

   @Override
   public String getMechName() {
      return "pms02";
   }

   public Pms02Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.m_20242_(true);
   }

   @Override
   public void m_8119_() {
      this.m_20242_(true);
      super.m_8119_();
   }

   @Override
   protected void m_8099_() {
      super.m_8099_();
      this.f_21345_.m_25352_(1, new FlyingMobGoal(this, this.getMechData().speed, 20.0, 100.0, 10.0, 30.0));
      this.f_21345_.m_25352_(1, new SimpleMobAttackGoal(this, 0.5F));
   }

   @Override
   public void doAttack() {
      if (this.isServerSide() && this.m_5448_() != null) {
         this.rotateToTarget(this.m_5448_());
         int pattern = this.f_19796_.m_188503_(3);
         if (pattern != 0) {
            for (int i = 0; i < 2; i++) {
               BulletMiddleEntity be = new BulletMiddleEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLETMIDDLE.get(), this.m_9236_(), this, this.getMechData().bulletDamage
               );
               Vec3 offset = this.m_20182_();
               Vec3 muzzlPos = new Vec3(1 - i * 2, -2.0, 0.0);
               muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
               be.m_146884_(offset.m_82549_(muzzlPos));
               be.m_146922_(this.m_146908_());
               be.m_146926_(this.m_146909_());
               be.f_19859_ = this.m_146908_();
               be.f_19860_ = this.m_146909_();
               float[] angle = Utils.getShootingAngle(be, this.m_5448_(), true);
               be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().bulletSpeed, 0.0F);
               this.m_9236_().m_7967_(be);
            }
         } else {
            for (int i = 0; i < 2; i++) {
               for (int j = 0; j < 2; j++) {
                  MissileGenericEnemyEntity be = new MissileGenericEnemyEntity(
                     (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(),
                     this.m_9236_(),
                     this,
                     this.m_5448_(),
                     this.getMechData().missileDamage,
                     this.getMechData().missileSpeed
                  );
                  Vec3 offset = this.m_20182_();
                  Vec3 muzzlPos = new Vec3(1 - i * 2, 4.4F, 0.0F - j * 2.0F);
                  muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
                  be.m_146884_(offset.m_82549_(muzzlPos));
                  be.m_37251_(be, -20.0F, this.m_146908_() - 30.0F + i * 60, this.m_21256_(), 1.9F, 0.0F);
                  this.m_9236_().m_7967_(be);
               }
            }
         }

         this.triggerAnim("shoot_controller", "shoot");
      }
   }

   @Override
   public int getMaxAttackCooltime() {
      return 30;
   }

   protected float m_274460_() {
      return 0.6F;
   }

   public boolean m_20096_() {
      return false;
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               "basic_move",
               0,
               event -> {
                  if (this.isClosed()) {
                     return PlayState.STOP;
                  } else {
                     return event.isMoving()
                        ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms.idle"))
                        : event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms.idle"));
                  }
               }
            )
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "shoot_controller", state -> PlayState.STOP)
               .triggerableAnim("shoot", RawAnimation.begin().thenPlay("animation.pms.idle"))
               .triggerableAnim("close", RawAnimation.begin().thenPlayAndHold("animation.pms.close"))
               .triggerableAnim("open", RawAnimation.begin().thenPlay("animation.pms.boot"))
         }
      );
   }

   @Override
   protected void fireOpenAnimation() {
      this.triggerAnim("shoot_controller", "open");
   }

   @Override
   protected void fireCloseAnimation() {
      this.triggerAnim("shoot_controller", "close");
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public static boolean canSpawn(EntityType<Pms02Entity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
      return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
   }
}
