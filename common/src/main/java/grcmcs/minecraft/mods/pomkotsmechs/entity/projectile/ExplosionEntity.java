package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ExplosionEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 50;
   private int lifeTicks = 0;
   private int explosionScale;

   public ExplosionEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      this(entityType, world, 0);
   }

   public ExplosionEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, int explosionScale) {
      super(entityType, world);
      this.setNoGravity(true);
      this.explosionScale = explosionScale;
   }

   @Override
   public void tick() {
      this.setNoGravity(true);
      super.tick();
      Level level = this.level();
      if (this.lifeTicks++ >= 50) {
         this.discard();
      } else if (this.lifeTicks == 5) {
         this.generateLava();
         if (level.isClientSide) {
            this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EXPLOSION_EVENT.get());
         }
      } else if (this.lifeTicks == 4 && !level.isClientSide && this.explosionScale > 0) {
         Vec3 pos = this.position();
         if (PomkotsMechs.CONFIG.enableEntityBlockDestruction) {
            Utils.explode(this, pos.x, pos.y, pos.z, this.explosionScale, false, ExplosionInteraction.BLOCK, this.level());
         } else {
            Utils.explode(this, pos.x, pos.y, pos.z, this.explosionScale, false, ExplosionInteraction.NONE, this.level());
         }
      }
   }

   private void generateLava() {
      if (this.level().isClientSide) {
         RandomSource random = this.level().getRandom();

         for (int i = 0; i < 20; i++) {
            double velocityX = random.nextDouble() * 2.0 - 1.0;
            double velocityY = random.nextDouble() * 2.0 - 1.0;
            double velocityZ = random.nextDouble() * 2.0 - 1.0;
            this.level().addParticle(ParticleTypes.LAVA, this.getX(), this.getY(), this.getZ(), velocityX, velocityY, velocityZ);
         }
      }
   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
   }

   protected void onHitBlock(BlockHitResult blockHitResult) {
   }

   protected void defineSynchedData() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.explosion.new")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
