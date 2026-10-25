package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.goal.ContinuousAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeLargeEntity;
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

public class Pmt03Entity extends BaseTurretEntity {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 1.0F;

   @Override
   public String getMechName() {
      return "pmt03";
   }

   public Pmt03Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   protected void registerTargetSelectorGoals() {
      super.registerTargetSelectorGoals();
      this.goalSelector.addGoal(1, new ContinuousAttackGoal(this, 0.8F, 41, 60, 20));
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
   }

   @Override
   public void stopActionAnimation() {
      this.triggerAnim("shoot_controller", "stop");
   }

   @Override
   public void doAttack() {
      LivingEntity target = this.getTarget();
      if (target != null) {
         BulletGrenadeLargeEntity be = new BulletGrenadeLargeEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE_LARGE.get(), this.level(), this, this.getMechData().grenadeDamage
         );
         be.setExplosionScale((int)this.getMechData().grenadeExplosionScale);
         be.setNoGravity(true);
         Vec3 offset = this.position();
         Vec3 muzzlPos = new Vec3(0.0, 2.0, 4.0);
         muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         be.setPos(offset.add(muzzlPos));
         if (!Utils.isObstructed(this.level(), be, target)) {
            float[] angle = Utils.getShootingAngle(be, target, true);
            be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), this.getMechData().grenadeSpeed, 0.0F);
            this.level().addFreshEntity(be);
            this.triggerAnim("shoot_controller", "shoot");
         }
      }
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "basic_move", 0, event -> event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmt03.idle")))
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "shoot_controller", state -> PlayState.STOP)
               .triggerableAnim("shoot", RawAnimation.begin().thenPlay("animation.pmt03.attack"))
               .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.pmt03.idle"))
               .setSoundKeyframeHandler(this::registerAnimationSoundHandlers)
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
