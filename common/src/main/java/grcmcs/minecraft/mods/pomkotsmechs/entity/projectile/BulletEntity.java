package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
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

public class BulletEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final float DAMAGE = BattleBalance.MECH_GATLING_DAMAGE;
   private static final int MAX_LIFE_TICKS = 30;
   private int lifeTicks = 0;
   private LivingEntity shooter = null;

   public BulletEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      this(entityType, world, null);
   }

   public BulletEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
      super(entityType, shooter, world);
      this.m_20242_(true);
      this.shooter = shooter;
   }

   @Override
   public void m_8119_() {
      this.m_20242_(true);
      HitResult hitResult = ProjectileUtil.raycastBoundingCheck(this);
      if (hitResult.m_6662_() != Type.MISS) {
         this.m_6532_(hitResult);
      }

      super.m_8119_();
      Vec3 vel = this.m_20184_();
      this.m_6034_(this.m_20185_() + vel.m_7096_(), this.m_20186_() + vel.m_7098_(), this.m_20189_() + vel.m_7094_());
      if (this.lifeTicks++ >= 30) {
         this.m_146870_();
      }
   }

   protected void m_5790_(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.m_82443_();
      if (!entity.equals(this.shooter)) {
         float damage = DAMAGE;
         if (this.lifeTicks < 10) {
            damage += (10.0F - this.lifeTicks) * DAMAGE / 20.0F;
         }

         entity.m_6469_(entity.m_269291_().m_269390_(this, (Entity)(this.m_19749_() != null ? this.m_19749_() : this)), damage);
         entity.f_19802_ = 0;
         this.m_146870_();
      }
   }

   protected void m_8060_(BlockHitResult blockHitResult) {
      this.m_146870_();
   }

   protected void m_8097_() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.bullet.idle")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
