package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.HitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.Pmb01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class EarthraiseEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 20;
   private int lifeTicks = 0;
   private int count;
   private LivingEntity shooter;
   private Vec3 vec;
   private Vec3 knockbackVec;
   private boolean breakBlocks = false;
   public static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");

   public void setBreakBlocks(boolean b) {
      this.breakBlocks = b;
   }

   public boolean isBreakBlocks() {
      return this.breakBlocks;
   }

   public EarthraiseEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      this(entityType, world, Vec3.ZERO, null, 8);
   }

   public EarthraiseEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, Vec3 vec, LivingEntity shooter, int count) {
      super(entityType, world);
      this.setNoGravity(true);
      this.shooter = shooter;
      this.vec = vec;
      Vec3 tmp = vec.normalize();
      this.knockbackVec = new Vec3(-tmp.x, tmp.y, -tmp.z);
      this.count = count;
   }

   public float getScaleHeight() {
      return 2.0F;
   }

   public float getScaleWidth() {
      return 2.0F;
   }

   @Override
   public void tick() {
      if (this.firstTick && this.level().isClientSide()) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EARTHRAISE_EVENT.get());
      }

      this.setNoGravity(true);
      super.tick();
      if (this.lifeTicks++ >= 20) {
         this.discard();
      } else if (this.lifeTicks == 3) {
         if (!this.level().isClientSide && this.count > 0) {
            Vec3 pos = this.position();
            EarthraiseEntity e = new EarthraiseEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.EARTHRAISE.get(), this.level(), this.vec, this.shooter, this.count - 1
            );
            e.setPos(pos.add(this.vec));
            e.setYRot((float)(Math.toDegrees(Math.atan2(this.vec.z, this.vec.x)) - 90.0));
            this.level().addFreshEntity(e);
         } else {
            this.generateBlockParticles();
         }

         this.atarihantei();
      }
   }

   private void atarihantei() {
      for (Entity ent : this.level().getEntities(null, this.getBoundingBox())) {
         if (!ent.equals(this.shooter) && !(ent instanceof HitBoxEntity) && !(ent instanceof BossHitBoxEntity) && ent instanceof LivingEntity le) {
            if (!(ent instanceof Pmb01Entity) && !(ent.getVehicle() instanceof Pmb01Entity)) {
               le.knockback(3.0, this.knockbackVec.x, this.knockbackVec.z);
            }

            le.invulnerableTime = 20;
            le.hurt(this.damageSources().generic(), 30.0F);
         }
      }

      if (this.isBreakBlocks() && ProjectileUtil.isDestructionAllowed(this)) {
         this.breakBlocks();
      }
   }

   private void breakBlocks() {
      AABB area = this.getBoundingBox();

      for (BlockPos blockPos : BlockPos.betweenClosed(
         (int)area.minX, (int)area.minY, (int)area.minZ, (int)area.maxX, (int)area.maxY + 10, (int)area.maxZ
      )) {
         BlockState state = this.level().getBlockState(blockPos);
         if (!state.isAir()) {
            Utils.eraseBlock(this.level(), blockPos);
         }
      }
   }

   private void generateBlockParticles() {
      if (this.level().isClientSide) {
         RandomSource random = this.level().getRandom();

         for (int i = 0; i < 30; i++) {
            double velocityX = random.nextDouble() * 2.0 - 1.0;
            double velocityY = random.nextDouble() * 2.0 - 1.0;
            double velocityZ = random.nextDouble() * 2.0 - 1.0;
            this.level()
               .addAlwaysVisibleParticle(
                  new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                  true,
                  this.getX() + velocityX,
                  this.getY() + 8.0,
                  this.getZ() + velocityZ,
                  velocityX,
                  velocityY,
                  velocityZ
               );
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
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.earthraise.new")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
