package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
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

public class RockLargeEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 160;
   private int lifeTicks = 0;
   private float damage = 0.0F;
   private LivingEntity shooter = null;
   private float explosionScale = 10.0F;

   public RockLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      this(entityType, world, null);
   }

   public RockLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
      this(entityType, world, shooter, BattleBalance.MECH_BULLET_GRENADE_DAMAGE);
   }

   public RockLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, float damage) {
      super(entityType, shooter, world);
      this.shooter = shooter;
      this.damage = damage;
   }

   public void setExplosionScale(float scale) {
      this.explosionScale = scale;
   }

   @Override
   public void tick() {
      HitResult hitResult = ProjectileUtil.raycastBoundingCheck(this);
      if (hitResult.getType() != Type.MISS) {
         this.onHit(hitResult);
      }

      super.tick();
      this.hasImpulse = true;
      if (this.lifeTicks++ >= 160) {
         this.discard();
      }
   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.getEntity();
      if (!entity.equals(this.shooter) && !(entity instanceof BossHitBoxEntity)) {
         entity.hurt(entity.damageSources().thrown(this, (Entity)(this.getOwner() != null ? this.getOwner() : this)), this.damage);
         entity.invulnerableTime = 0;
         this.explode();
      }
   }

   protected void onHitBlock(BlockHitResult blockHitResult) {
      this.explode();
   }

   private void explode() {
      if (!this.level().isClientSide) {
         this.explodeVisual();
         this.spawnSmallRocks();
         this.discard();
      }
   }

   private void spawnSmallRocks() {
      int count = 12;
      RandomSource rand = this.random;

      for (int i = 0; i < count; i++) {
         RockSmallEntity rock = new RockSmallEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.ROCK_SMALL.get(), this.level(), this.shooter);
         rock.setPos(this.getX(), this.getY() + 0.5, this.getZ());
         rock.setExplosionScale(this.explosionScale);
         Vec3 dir = new Vec3(rand.nextGaussian(), rand.nextGaussian() * 0.6, rand.nextGaussian()).normalize();
         rock.setDeltaMovement(dir.scale(0.8 + rand.nextFloat()));
         this.level().addFreshEntity(rock);
      }
   }

   private void explodeVisual() {
      Utils.explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, ExplosionInteraction.NONE, this.level());
   }

   private void createExplosion(Vec3 pos) {
      Level world = this.level();
      if (!world.isClientSide) {
         if (Utils.isBlockDestructionAllowed(this.shooter)) {
            Utils.explode(this, pos.x, pos.y, pos.z, this.explosionScale, false, ExplosionInteraction.BLOCK, world);
         } else {
            Utils.explode(this, pos.x, pos.y, pos.z, this.explosionScale, false, ExplosionInteraction.NONE, world);
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
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.rock.idle")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
