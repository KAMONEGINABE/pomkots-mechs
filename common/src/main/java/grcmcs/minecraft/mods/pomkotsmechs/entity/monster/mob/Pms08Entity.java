package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.RollerDashGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pms08Entity extends BaseSmallMonsterEntity implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 1.0F;
   private boolean hasBeenKilled = true;

   @Override
   public String getMechName() {
      return "pms08";
   }

   public Pms08Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new RollerDashGoal(this, this.getMechData().speed * 2.0F, 5.0F, 20, 3, 3, 30, 20));
   }

   @Override
   public void doAttack() {
      if (this.isServerSide() && this.getTarget() != null) {
         Level level = this.level();
         ExplosionEntity e = new ExplosionEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOSION.get(), level, (int)this.getMechData().grenadeExplosionScale
         );
         e.setPos(this.position());
         level.addFreshEntity(e);
         this.hasBeenKilled = false;
         this.kill();
      }
   }

   @Override
   protected void tickDeath() {
      this.deathTime++;
      if (this.deathTime == 10) {
         this.remove(RemovalReason.KILLED);
         if (this.isServerSide() && this.hasBeenKilled) {
            Level level = this.level();
            ExplosionEntity e = new ExplosionEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOSION.get(), level);
            e.setPos(this.position());
            level.addFreshEntity(e);
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
               .triggerableAnim("shoot", RawAnimation.begin().thenPlay("animation.pms.attack"))
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

   public static boolean canSpawn(EntityType<Pms08Entity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
      return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
   }
}
