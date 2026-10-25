package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletRifleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pmss02Entity extends BaseTinyMonsterEntity implements GeoEntity, GeoAnimatable, RangedAttackMob {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 1.0F;

   @Override
   public String getMechName() {
      return "pmss02";
   }

   public Pmss02Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   public void m_8119_() {
      super.m_8119_();
      if (this.isAttacking()) {
         this.m_20334_(0.0, this.m_20184_().f_82480_, 0.0);
      }
   }

   public void m_6504_(LivingEntity livingEntity, float f) {
      this.startAttack();
   }

   @Override
   protected void startAttack() {
      super.startAttack();
      this.triggerAnim("shoot_controller", "shoot");
   }

   @Override
   protected void tickAttack() {
      super.tickAttack();
      if (this.isAttacking()) {
         this.rotateToTarget(this.m_5448_());
         this.choiMove();
      }

      if (this.attackingTime == 10) {
         this.actualAttack();
      }
   }

   private void actualAttack() {
      LivingEntity target = this.m_5448_();
      if (target != null && target.m_6084_()) {
         BulletRifleEntity be = new BulletRifleEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_RIFLE.get(), this.m_9236_(), this, this.getMechData().bulletDamage
         );
         be.m_20242_(true);
         Vec3 offset = this.m_20182_();
         Vec3 muzzlPos = new Vec3(-0.5, 1.5, 2.0);
         muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         be.m_146884_(offset.m_82549_(muzzlPos));
         float[] angle = Utils.getShootingAngle(be, target, true, false);
         be.m_37251_(be, angle[0], angle[1], this.m_21256_(), this.getMechData().bulletSpeed, 0.0F);
         this.m_9236_().m_7967_(be);
      }
   }

   @Override
   public int getMaxAttackCooltime() {
      return 40;
   }

   @Override
   public int getMaxAttackTicks() {
      return 20;
   }

   @Override
   protected void m_8099_() {
      super.m_8099_();
      this.f_21345_.m_25352_(1, new RangedAttackGoal(this, this.getMechData().speed, 60, 80, 20.0F));
      this.f_21345_.m_25352_(2, new WaterAvoidingRandomStrollGoal(this, this.getMechData().speed));
      this.f_21345_.m_25352_(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.f_21345_.m_25352_(4, new RandomLookAroundGoal(this));
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
                           ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms.walk"))
                           : event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms.idle"));
                     }
                  }
               )
               .setSoundKeyframeHandler(this::playSounds)
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "shoot_controller", state -> PlayState.STOP)
               .triggerableAnim("shoot", RawAnimation.begin().thenPlay("animation.pms.attack_charge").thenPlay("animation.pms.attack"))
               .triggerableAnim("close", RawAnimation.begin().thenPlayAndHold("animation.pms.close"))
               .triggerableAnim("open", RawAnimation.begin().thenPlay("animation.pms.boot"))
               .setSoundKeyframeHandler(this::playSounds)
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
}
