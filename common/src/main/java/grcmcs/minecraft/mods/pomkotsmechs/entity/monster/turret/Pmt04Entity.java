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
      this.f_21345_.m_25352_(1, new ContinuousAttackGoal(this, 0.8F, 21, 60, 20));
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
      LivingEntity target = this.m_5448_();
      if (target != null) {
         Vec3 offset = this.m_20182_();
         float slotX = this.curSlot % 6 * -1 + 3;
         this.curSlot++;
         Vec3 muzzlPos = new Vec3(slotX, 2.0, 0.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         MissileGenericLargeEntity be = new MissileGenericLargeEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC_LARGE.get(),
            this.m_9236_(),
            this,
            target,
            this.getMechData().missileDamage,
            this.getMechData().missileSpeed
         );
         be.m_146884_(offset.m_82549_(muzzlPos));
         be.m_37251_(be, -90.0F, this.m_146908_(), this.m_21256_(), this.getMechData().missileSpeed, 0.0F);
         this.m_9236_().m_7967_(be);
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
