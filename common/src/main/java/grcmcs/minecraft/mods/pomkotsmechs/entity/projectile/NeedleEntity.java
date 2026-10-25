package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.PomkotsCustomThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NeedleEntity extends PomkotsCustomThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 60;
   private int lifeTicks = 0;
   private float damage;
   private LivingEntity shooter = null;
   private int stopTicks = 0;
   private float explosionScale = 5.0F;

   public void setExplosionScale(float explosionScale) {
      this.explosionScale = explosionScale;
   }

   public NeedleEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      this(entityType, level, null);
   }

   public NeedleEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter) {
      this(entityType, level, shooter, BattleBalance.MECH_RIFLE_DAMAGE);
   }

   public NeedleEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, float damage) {
      super(entityType, world, shooter, 60, damage, 0);
      this.m_20242_(true);
      this.f_19811_ = true;
      this.f_19794_ = true;
      this.shooter = shooter;
      this.damage = damage;
   }

   public boolean m_6000_(double a, double b, double c) {
      return true;
   }

   public boolean m_6783_(double e) {
      return true;
   }

   @Override
   public void m_8119_() {
      if (this.stopTicks < 1) {
         HitResult hitResult = ProjectileUtil.raycastBoundingCheck(this);
         if (hitResult.m_6662_() != Type.MISS) {
            this.m_6532_(hitResult);
         }

         super.m_8119_();
         this.f_19812_ = true;
      } else {
         this.m_20334_(0.0, 0.0, 0.0);
         this.stopTicks++;
         if (this.stopTicks > 20) {
            this.createExplosion(this.m_20182_());
            this.m_146870_();
         }
      }

      if (this.lifeTicks++ >= 60) {
         this.m_146870_();
      }
   }

   @Override
   protected void m_5790_(EntityHitResult entityHitResult) {
      this.handleEntityHit(entityHitResult.m_82443_());
   }

   public void handleEntityHit(Entity entity) {
      if (!entity.equals(this.shooter) && !(entity instanceof NeedleEntity)) {
         entity.m_6469_(entity.m_269291_().m_269390_(this, (Entity)(this.m_19749_() != null ? this.m_19749_() : this)), this.damage);
         entity.f_19802_ = 0;
         this.m_146870_();
      }
   }

   @Override
   protected void m_8060_(BlockHitResult blockHitResult) {
      this.stopTicks = 1;
      this.m_20334_(0.0, 0.0, 0.0);
   }

   private void createExplosion(Vec3 pos) {
      Level world = this.m_9236_();
      if (!world.f_46443_) {
         if (ProjectileUtil.isDestructionAllowed(this)) {
            Utils.explode(this, pos.f_82479_, pos.f_82480_ + 2.0, pos.f_82481_, this.explosionScale, false, ExplosionInteraction.BLOCK, this.m_9236_());
         } else {
            Utils.explode(this, pos.f_82479_, pos.f_82480_ + 2.0, pos.f_82481_, this.explosionScale, false, ExplosionInteraction.NONE, this.m_9236_());
         }
      }
   }

   @Override
   public void m_142036_() {
      if (this.m_9236_().f_46443_) {
         ParticleUtil.addSparkParticles(this.m_20182_(), this.m_9236_());
      }
   }

   protected void m_8097_() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.missile.idle")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
