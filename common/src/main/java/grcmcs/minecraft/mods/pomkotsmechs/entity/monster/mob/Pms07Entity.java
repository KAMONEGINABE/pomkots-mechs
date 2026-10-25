package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.RollerDashGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pms07Entity extends BaseSmallMonsterEntity implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 1.0F;
   private int attackDelay = 0;
   private AABB attackAabb = null;

   @Override
   public String getMechName() {
      return "pms07";
   }

   public Pms07Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new RollerDashGoal(this, this.getMechData().speed * 2.0F, 12.0F, 20, 5, 5, 30, 20));
   }

   @Override
   public void tick() {
      super.tick();
      if (this.isServerSide() && this.attackDelay > 0 && this.attackAabb != null) {
         this.attackDelay--;
         if (this.attackDelay == 0) {
            this.rotateToTarget(this.getTarget());
            Level world = this.level();
            Vec3 kbVel = new Vec3(0.0, 0.0, -1.0).yRot((float)Math.toRadians(-1.0 * this.getYRot()));

            for (Entity ent : world.getEntities(null, this.attackAabb)) {
               if (!this.equals(ent) && ent instanceof LivingEntity le) {
                  le.knockback(3.0, kbVel.x, kbVel.z);
                  DamageSource ds = this.damageSources().generic();
                  le.hurt(ds, this.getMechData().meleeDamage);
               }
            }
         }
      }
   }

   @Override
   public void doAttack() {
      if (this.isServerSide() && this.getTarget() != null) {
         int pattern = this.random.nextInt(2);
         Vec3 aabb1;
         Vec3 aabb2;
         if (pattern == 0) {
            aabb1 = new Vec3(6.5, 4.0, 18.0).yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
            aabb2 = new Vec3(-2.0, -4.0, -4.0).yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
            this.triggerAnim("shoot_controller", "pile");
         } else {
            aabb1 = new Vec3(18.0, 4.0, 18.0).yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
            aabb2 = new Vec3(-18.0, -4.0, -18.0).yRot((float)Math.toRadians(-1.0 * this.getYRot())).add(this.position());
            this.triggerAnim("shoot_controller", "saber");
         }

         this.attackDelay = 5;
         this.attackAabb = new AABB(aabb1, aabb2);
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
               .triggerableAnim("pile", RawAnimation.begin().thenPlay("animation.pms.attack_pile"))
               .triggerableAnim("saber", RawAnimation.begin().thenPlay("animation.pms.attack_blade"))
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

   public static boolean canSpawn(EntityType<Pms07Entity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
      return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
   }
}
