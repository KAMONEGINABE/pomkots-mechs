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
      this.f_21345_.m_25352_(1, new ContinuousAttackGoal(this, 0.8F, 41, 60, 20));
   }

   @Override
   public void m_8119_() {
      super.m_8119_();
      LivingEntity target = this.m_5448_();
      if (!this.m_9236_().f_46443_ && target != null) {
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
      LivingEntity target = this.m_5448_();
      if (target != null) {
         BulletGrenadeLargeEntity be = new BulletGrenadeLargeEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE_LARGE.get(), this.m_9236_(), this, this.getMechData().grenadeDamage
         );
         be.setExplosionScale((int)this.getMechData().grenadeExplosionScale);
         be.m_20242_(true);
         Vec3 offset = this.m_20182_();
         Vec3 muzzlPos = new Vec3(0.0, 2.0, 4.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         be.m_146884_(offset.m_82549_(muzzlPos));
         if (!Utils.isObstructed(this.m_9236_(), be, target)) {
            float[] angle = Utils.getShootingAngle(be, target, true);
            be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().grenadeSpeed, 0.0F);
            this.m_9236_().m_7967_(be);
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
