package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.goal.ContinuousAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pmt01Entity extends BaseTurretEntity {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 1.0F;
   private int curSide = 1;

   @Override
   public String getMechName() {
      return "pmt01";
   }

   public Pmt01Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   protected void registerTargetSelectorGoals() {
      super.registerTargetSelectorGoals();
      this.goalSelector.addGoal(1, new ContinuousAttackGoal(this, 0.8F, 100, 60, 10));
   }

   @Override
   public void tick() {
      super.tick();
      LivingEntity target = this.getTarget();
      if (!this.level().isClientSide && target != null) {
         this.rotateToTarget(target);
      }
   }

   @Override
   public void startActionAnimation() {
      this.triggerAnim("shoot_controller", "shoot");
      this.curSide = 1;
   }

   @Override
   public void stopActionAnimation() {
      this.triggerAnim("shoot_controller", "stop");
   }

   @Override
   public void doAttack() {
      LivingEntity target = this.getTarget();
      if (target != null) {
         BulletGrenadeEntity be = new BulletGrenadeEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE.get(), this.level(), this, this.getMechData().bulletDamage
         );
         be.setNoGravity(true);
         be.setExplosionScale(3);
         be.setStunPoint(2);
         Vec3 offset = this.position();
         Vec3 muzzlPos = new Vec3(1 * this.curSide, 2.0, 4.0);
         this.curSide *= -1;
         muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         be.setPos(offset.add(muzzlPos));
         if (!Utils.isObstructed(this.level(), be, target)) {
            float[] angle = Utils.getShootingAngle(be, target, true);
            be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), this.getMechData().bulletSpeed, 0.0F);
            this.level().addFreshEntity(be);
         }
      }
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "basic_move", 0, event -> event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmt01.idle")))
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "shoot_controller", state -> PlayState.STOP)
               .triggerableAnim("shoot", RawAnimation.begin().thenLoop("animation.pmt01.attack"))
               .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.pmt01.idle"))
               .setSoundKeyframeHandler(this::registerAnimationSoundHandlers)
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
