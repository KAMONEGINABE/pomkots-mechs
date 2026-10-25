package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.RollerDashGoal;
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

public class Pms06Entity extends BaseSmallMonsterEntity implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 1.0F;

   @Override
   public String getMechName() {
      return "pms06";
   }

   public Pms06Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new RollerDashGoal(this, this.getMechData().speed * 2.0F, 100.0F, 10, 40, 40, 40, 60));
   }

   @Override
   public void doAttack() {
      if (this.isServerSide() && this.getTarget() != null) {
         this.rotateToTarget(this.getTarget());
         int pattern = this.random.nextInt(5);
         if (pattern == 0) {
            for (int j = 0; j < 2; j++) {
               MissileGenericEnemyEntity be = new MissileGenericEnemyEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(),
                  this.level(),
                  this,
                  this.getTarget(),
                  this.getMechData().missileDamage,
                  this.getMechData().missileSpeed
               );
               be.setSwitchTick(32);
               Vec3 offset = this.position();
               Vec3 muzzlPos = new Vec3(2.0, 5.5 - j * 2, 2.0);
               muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
               be.setPos(offset.add(muzzlPos));
               be.shootFromRotation(be, -70.0F, this.getYRot(), this.getFallFlyingTicks(), 1.9F, 0.0F);
               this.level().addFreshEntity(be);
            }

            this.triggerAnim("shoot_controller", "shoot_m");
         } else {
            for (int i = 0; i < 2; i++) {
               BulletMiddleEntity be = new BulletMiddleEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLETMIDDLE.get(), this.level(), this, this.getMechData().bulletDamage
               );
               Vec3 offset = this.position();
               Vec3 muzzlPos = new Vec3(2.0, 3.0 + i, 2.5);
               muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
               be.setPos(offset.add(muzzlPos));
               be.setYRot(this.getYRot());
               be.setXRot(this.getXRot());
               be.yRotO = this.getYRot();
               be.xRotO = this.getXRot();
               float[] angle = Utils.getShootingAngle(be, this.getTarget(), true);
               be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), this.getMechData().bulletSpeed, 0.0F);
               this.level().addFreshEntity(be);
            }

            this.triggerAnim("shoot_controller", "shoot_g");
         }
      }
   }

   @Override
   public int getMaxAttackCooltime() {
      return 20;
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
                        ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms.dash"))
                        : event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms.idle"));
                  }
               }
            )
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "shoot_controller", state -> PlayState.STOP)
               .triggerableAnim("shoot_g", RawAnimation.begin().thenPlay("animation.pms.attack_gun"))
               .triggerableAnim("shoot_m", RawAnimation.begin().thenPlay("animation.pms.attack_missile"))
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

   public static boolean canSpawn(EntityType<Pms06Entity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
      return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
   }
}
