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
      this.setNoGravity(true);
      this.noCulling = true;
      this.noPhysics = true;
      this.shooter = shooter;
      this.damage = damage;
   }

   public boolean shouldRender(double a, double b, double c) {
      return true;
   }

   public boolean shouldRenderAtSqrDistance(double e) {
      return true;
   }

   @Override
   public void tick() {
      if (this.stopTicks < 1) {
         HitResult hitResult = ProjectileUtil.raycastBoundingCheck(this);
         if (hitResult.getType() != Type.MISS) {
            this.onHit(hitResult);
         }

         super.tick();
         this.hasImpulse = true;
      } else {
         this.setDeltaMovement(0.0, 0.0, 0.0);
         this.stopTicks++;
         if (this.stopTicks > 20) {
            this.createExplosion(this.position());
            this.discard();
         }
      }

      if (this.lifeTicks++ >= 60) {
         this.discard();
      }
   }

   @Override
   protected void onHitEntity(EntityHitResult entityHitResult) {
      this.handleEntityHit(entityHitResult.getEntity());
   }

   public void handleEntityHit(Entity entity) {
      if (!entity.equals(this.shooter) && !(entity instanceof NeedleEntity)) {
         entity.hurt(entity.damageSources().thrown(this, (Entity)(this.getOwner() != null ? this.getOwner() : this)), this.damage);
         entity.invulnerableTime = 0;
         this.discard();
      }
   }

   @Override
   protected void onHitBlock(BlockHitResult blockHitResult) {
      this.stopTicks = 1;
      this.setDeltaMovement(0.0, 0.0, 0.0);
   }

   private void createExplosion(Vec3 pos) {
      Level world = this.level();
      if (!world.isClientSide) {
         if (ProjectileUtil.isDestructionAllowed(this)) {
            Utils.explode(this, pos.x, pos.y + 2.0, pos.z, this.explosionScale, false, ExplosionInteraction.BLOCK, this.level());
         } else {
            Utils.explode(this, pos.x, pos.y + 2.0, pos.z, this.explosionScale, false, ExplosionInteraction.NONE, this.level());
         }
      }
   }

   @Override
   public void onClientRemoval() {
      if (this.level().isClientSide) {
         ParticleUtil.addSparkParticles(this.position(), this.level());
      }
   }

   protected void defineSynchedData() {
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
