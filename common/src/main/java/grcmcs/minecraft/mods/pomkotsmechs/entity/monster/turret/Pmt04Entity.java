package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.goal.ContinuousAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericLargeEntity;
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

public class Pmt04Entity extends BaseTurretEntity {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 1.0F;
   private int curSlot = 0;

   @Override
   public String getMechName() {
      return "pmt04";
   }

   public Pmt04Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.alwaysLookAtTarget = false;
   }

   @Override
   protected void registerTargetSelectorGoals() {
      super.registerTargetSelectorGoals();
      this.goalSelector.addGoal(1, new ContinuousAttackGoal(this, 0.8F, 21, 60, 20));
   }

   @Override
   public void startActionAnimation() {
      this.triggerAnim("shoot_controller", "shoot");
      this.curSlot = 0;
   }

   @Override
   public void stopActionAnimation() {
      this.triggerAnim("shoot_controller", "stop");
   }

   @Override
   public void doAttack() {
      LivingEntity target = this.getTarget();
      if (target != null) {
         Vec3 offset = this.position();
         float slotX = this.curSlot % 6 * -1 + 3;
         this.curSlot++;
         Vec3 muzzlPos = new Vec3(slotX, 2.0, 0.0);
         muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         MissileGenericLargeEntity be = new MissileGenericLargeEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC_LARGE.get(),
            this.level(),
            this,
            target,
            this.getMechData().missileDamage,
            this.getMechData().missileSpeed
         );
         be.setPos(offset.add(muzzlPos));
         be.shootFromRotation(be, -90.0F, this.getYRot(), this.getFallFlyingTicks(), this.getMechData().missileSpeed, 0.0F);
         this.level().addFreshEntity(be);
      }
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "basic_move", 0, event -> event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmt04.idle")))
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "shoot_controller", state -> PlayState.STOP)
               .triggerableAnim("shoot", RawAnimation.begin().thenPlay("animation.pmt04.attack"))
               .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.pmt04.idle"))
               .setSoundKeyframeHandler(this::registerAnimationSoundHandlers)
         }
      );
   }

   @Override
   public int getMaxAttackCooltime() {
      return 2;
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
