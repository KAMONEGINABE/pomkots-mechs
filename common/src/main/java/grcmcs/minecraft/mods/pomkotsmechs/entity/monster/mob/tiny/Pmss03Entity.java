package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.goal.MeleeAttackGoal2;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pmss03Entity extends BaseTinyMonsterEntity implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 1.0F;
   private static final Vec3 ATTACK_OFFSET = new Vec3(0.0, 0.0, 2.0);
   private static final double ATTACK_RADIUS = 2.5;
   private static final EntityDataAccessor<Integer> FUSE = SynchedEntityData.m_135353_(Pmss03Entity.class, EntityDataSerializers.f_135028_);

   @Override
   public String getMechName() {
      return "pmss03";
   }

   public Pmss03Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   public void m_8119_() {
      super.m_8119_();
      if (this.isAttacking()) {
         this.m_20334_(0.0, this.m_20184_().f_82480_, 0.0);
      }

      int fuse = this.getFuse();
      if (fuse >= 0) {
         this.setFuse(--fuse);
         if (fuse == 0) {
            this.explode();
         }
      }
   }

   @Override
   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(FUSE, -1);
   }

   public void startSelfDestruct() {
      this.f_19804_.m_135381_(FUSE, 40);
   }

   public void stopSelfDestruct() {
      this.f_19804_.m_135381_(FUSE, -1);
      this.attackingTime = 0;
   }

   public int getFuse() {
      return (Integer)this.f_19804_.m_135370_(FUSE);
   }

   public void setFuse(int fuse) {
      this.f_19804_.m_135381_(FUSE, fuse);
   }

   public boolean isSelfDestructing() {
      return this.getFuse() >= 0;
   }

   private void explode() {
      if (!this.m_9236_().f_46443_) {
         Utils.explode(
            this, this.m_20185_(), this.m_20186_(), this.m_20189_(), this.getMechData().grenadeExplosionScale, ExplosionInteraction.MOB, this.m_9236_()
         );
         this.m_146870_();
      }
   }

   @Override
   protected void startAttack() {
      super.startAttack();
      this.startSelfDestruct();
      this.triggerAnim("shoot_controller", "attack_charge");
   }

   @Override
   protected void tickAttack() {
      super.tickAttack();
   }

   @Override
   public int getMaxAttackCooltime() {
      return 80;
   }

   @Override
   public int getMaxAttackTicks() {
      return 50;
   }

   @Override
   public boolean m_6469_(DamageSource source, float amount) {
      if (this.isSelfDestructing()) {
         this.stopSelfDestruct();
      }

      return super.m_6469_(source, amount);
   }

   @Override
   protected void m_8099_() {
      super.m_8099_();
      this.f_21345_.m_25352_(1, new MeleeAttackGoal2(this, this.getMechData().speed, false));
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
               .triggerableAnim("attack_charge", RawAnimation.begin().thenPlay("animation.pms.attack_charge"))
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
