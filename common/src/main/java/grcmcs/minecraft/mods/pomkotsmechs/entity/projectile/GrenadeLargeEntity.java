package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
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

public class GrenadeLargeEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final float DAMAGE = 10.0F;
   private static final int MAX_LIFE_TICKS = 80;
   private int lifeTicks = 0;
   private float explosionScale = 0.0F;
   private LivingEntity shooter = null;

   public GrenadeLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      this(entityType, world, null);
   }

   public GrenadeLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
      this(entityType, world, shooter, 10.0F);
   }

   public GrenadeLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, float exprosionScale) {
      super(entityType, shooter, world);
      this.shooter = shooter;
      this.explosionScale = exprosionScale;
   }

   @Override
   public void m_8119_() {
      HitResult hitResult = ProjectileUtil.raycastBoundingCheck(this);
      if (hitResult.m_6662_() != Type.MISS) {
         this.m_6532_(hitResult);
      }

      super.m_8119_();
      Vec3 vel = this.m_20184_();
      this.m_6034_(this.m_20185_() + vel.m_7096_(), this.m_20186_() + vel.m_7098_(), this.m_20189_() + vel.m_7094_());
      if (this.lifeTicks++ >= 80) {
         this.m_146870_();
      }
   }

   protected void m_5790_(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.m_82443_();
      if (!entity.equals(this.shooter)) {
         entity.m_6469_(entity.m_269291_().m_269390_(this, (Entity)(this.m_19749_() != null ? this.m_19749_() : this)), 10.0F);
         entity.f_19802_ = 0;
         this.createExplosion(entityHitResult.m_82450_());
         this.m_146870_();
      }
   }

   protected void m_8060_(BlockHitResult blockHitResult) {
      this.createExplosion(blockHitResult.m_82450_());
      this.m_146870_();
   }

   private void createExplosion(Vec3 pos) {
      Level world = this.m_9236_();
      if (!world.f_46443_) {
         if (ProjectileUtil.isDestructionAllowed(this)) {
            Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, this.explosionScale, false, ExplosionInteraction.BLOCK, this.m_9236_());
         } else {
            Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, this.explosionScale, false, ExplosionInteraction.NONE, this.m_9236_());
         }

         ExplosionEntity e = new ExplosionEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOSION.get(), world);
         e.m_146884_(this.m_20182_());
         world.m_7967_(e);
      }
   }

   protected void m_8097_() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.grenade.idle")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
