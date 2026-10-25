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
   public void tick() {
      super.tick();
      if (this.isAttacking()) {
         this.setDeltaMovement(0.0, this.getDeltaMovement().y, 0.0);
      }
   }

   public void performRangedAttack(LivingEntity livingEntity, float f) {
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
         this.rotateToTarget(this.getTarget());
         this.choiMove();
      }

      if (this.attackingTime == 10) {
         this.actualAttack();
      }
   }

   private void actualAttack() {
      LivingEntity target = this.getTarget();
      if (target != null && target.isAlive()) {
         BulletRifleEntity be = new BulletRifleEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_RIFLE.get(), this.level(), this, this.getMechData().bulletDamage
         );
         be.setNoGravity(true);
         Vec3 offset = this.position();
         Vec3 muzzlPos = new Vec3(-0.5, 1.5, 2.0);
         muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         be.setPos(offset.add(muzzlPos));
         float[] angle = Utils.getShootingAngle(be, target, true, false);
         be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), this.getMechData().bulletSpeed, 0.0F);
         this.level().addFreshEntity(be);
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
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new RangedAttackGoal(this, this.getMechData().speed, 60, 80, 20.0F));
      this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, this.getMechData().speed));
      this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
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
