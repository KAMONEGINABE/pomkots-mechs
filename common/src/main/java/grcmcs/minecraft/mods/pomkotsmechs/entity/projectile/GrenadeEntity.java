package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.Pmb01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
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

public class GrenadeEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final float DAMAGE = BattleBalance.MECH_GRENADE_DAMAGE;
   private static final int MAX_LIFE_TICKS = 80;
   private int lifeTicks = 0;
   private float explosionScale = 0.0F;
   private float damage;
   private LivingEntity shooter = null;

   public GrenadeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      this(entityType, world, null);
   }

   public GrenadeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
      this(entityType, world, shooter, DAMAGE, 10.0F);
   }

   public GrenadeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, float damage, float explosionScale) {
      super(entityType, shooter, world);
      this.shooter = shooter;
      this.explosionScale = explosionScale;
      this.damage = damage;
   }

   @Override
   public void tick() {
      HitResult hitResult = ProjectileUtil.raycastBoundingCheck(this);
      if (hitResult.getType() != Type.MISS) {
         this.onHit(hitResult);
      }

      super.tick();
      Vec3 vel = this.getDeltaMovement();
      this.setPos(this.getX() + vel.x(), this.getY() + vel.y(), this.getZ() + vel.z());
      if (this.lifeTicks++ >= 80) {
         this.discard();
      }
   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.getEntity();
      if (!entity.equals(this.shooter)) {
         entity.hurt(entity.damageSources().thrown(this, (Entity)(this.getOwner() != null ? this.getOwner() : this)), this.damage);
         entity.invulnerableTime = 0;
         this.createExplosion(entityHitResult.getLocation());
         this.discard();
      }
   }

   protected void onHitBlock(BlockHitResult blockHitResult) {
      this.createExplosion(blockHitResult.getLocation());
      this.discard();
   }

   private void createExplosion(Vec3 pos) {
      Level world = this.level();
      if (!world.isClientSide) {
         if (this.shooter instanceof Pmb01Entity && ProjectileUtil.isDestructionAllowed(this)) {
            Utils.explode(this, pos.x, pos.y, pos.z, this.explosionScale, false, ExplosionInteraction.BLOCK, this.level());
         } else {
            Utils.explode(this, pos.x, pos.y, pos.z, this.explosionScale, false, ExplosionInteraction.NONE, this.level());
         }
      } else {
         this.addParticles(this.position());
      }
   }

   public void onClientRemoval() {
      if (this.level().isClientSide) {
         this.addParticles(this.position());
      }
   }

   private void addParticles(Vec3 offset) {
      Level world = this.level();

      for (int i = 0; i < 5; i++) {
         int rad = Math.abs(i - 3);

         for (int j = 0; j < 5 - rad; j++) {
            for (int k = 0; k < 5 - rad; k++) {
               world.addParticle(
                  ParticleTypes.EXPLOSION,
                  offset.x - (k - (3 - rad / 2)) * 2,
                  offset.y - (i - 3) * 2,
                  offset.z - (j - (3 - rad / 2)) * 2,
                  0.0,
                  0.0,
                  0.0
               );
            }
         }
      }
   }

   protected void defineSynchedData() {
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
