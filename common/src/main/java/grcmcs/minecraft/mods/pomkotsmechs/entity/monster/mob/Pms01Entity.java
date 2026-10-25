package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.ChargingMobGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
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

public class Pms01Entity extends BaseSmallMonsterEntity implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 1.0F;
   private int attackingTime = 0;

   public boolean isAttacking() {
      return this.attackingTime > 0;
   }

   @Override
   public String getMechName() {
      return "pms01";
   }

   public Pms01Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   public void m_8119_() {
      super.m_8119_();
      if (this.attackingTime > 0) {
         this.attackingTime--;
      }

      if (this.isServerSide()) {
         if (this.attackingTime == 15) {
            this.rotateToTarget(this.m_5448_());
            this.updateHomingMovement();
         } else if (this.attackingTime > 0 && this.attackingTime < 15) {
            this.attack();
         }
      }
   }

   private void attack() {
      Vec3 kbVel = new Vec3(0.0, 0.0, -1.0).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));

      for (Entity ent : this.m_9236_().m_45933_(null, this.m_20191_().m_82400_(2.0))) {
         if (ent instanceof Player || ent instanceof PomkotsVehicleBase) {
            LivingEntity le = (LivingEntity)ent;
            if (this.isServerSide()) {
               le.m_147240_(2.0, kbVel.f_82479_, kbVel.f_82481_);
               le.m_6469_(this.m_269291_().m_269264_(), this.getMechData().meleeDamage);
            } else {
               this.addHitParticles(le);
            }
         }
      }
   }

   protected void updateHomingMovement() {
      Vec3 currentPosition = this.m_20182_();
      LivingEntity target = this.m_5448_();
      if (target != null) {
         Vec3 targetPosition = target.m_20182_();
         Vec3 directionToTarget = targetPosition.m_82546_(currentPosition).m_82541_();
         this.m_20256_(directionToTarget.m_82490_(10.0));
      }
   }

   @Override
   public void doAttack() {
      this.rotateToTarget(this.m_5448_());
      this.attackingTime = 20;
      if (this.isServerSide()) {
         this.m_20256_(new Vec3(0.0, 2.0, 0.0));
         this.triggerAnim("shoot_controller", "shoot");
      }
   }

   @Override
   public int getMaxAttackCooltime() {
      return 80;
   }

   @Override
   protected void m_8099_() {
      super.m_8099_();
      this.f_21345_.m_25352_(1, new ChargingMobGoal(this, this.getMechData().speed));
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

   public static boolean canSpawn(EntityType<Pms01Entity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
      return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
   }
}
