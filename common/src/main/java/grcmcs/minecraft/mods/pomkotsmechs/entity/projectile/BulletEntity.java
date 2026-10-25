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
      this.setNoGravity(true);
      this.shooter = shooter;
   }

   @Override
   public void tick() {
      this.setNoGravity(true);
      HitResult hitResult = ProjectileUtil.raycastBoundingCheck(this);
      if (hitResult.getType() != Type.MISS) {
         this.onHit(hitResult);
      }

      super.tick();
      Vec3 vel = this.getDeltaMovement();
      this.setPos(this.getX() + vel.x(), this.getY() + vel.y(), this.getZ() + vel.z());
      if (this.lifeTicks++ >= 30) {
         this.discard();
      }
   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.getEntity();
      if (!entity.equals(this.shooter)) {
         float damage = DAMAGE;
         if (this.lifeTicks < 10) {
            damage += (10.0F - this.lifeTicks) * DAMAGE / 20.0F;
         }

         entity.hurt(entity.damageSources().thrown(this, (Entity)(this.getOwner() != null ? this.getOwner() : this)), damage);
         entity.invulnerableTime = 0;
         this.discard();
      }
   }

   protected void onHitBlock(BlockHitResult blockHitResult) {
      this.discard();
   }

   protected void defineSynchedData() {
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
