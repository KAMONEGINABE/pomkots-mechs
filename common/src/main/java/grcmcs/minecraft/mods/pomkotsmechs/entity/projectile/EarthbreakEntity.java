package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.HitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.Pmb01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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

public class EarthbreakEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 20;
   private int lifeTicks = 0;
   private LivingEntity shooter;
   private boolean breakBlocks = false;

   public void setBreakBlocks(boolean b) {
      this.breakBlocks = b;
   }

   public boolean isBreakBlocks() {
      return this.breakBlocks;
   }

   public EarthbreakEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      super(entityType, world);
      this.setNoGravity(true);
      this.shooter = null;
   }

   public EarthbreakEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
      super(entityType, world);
      this.setNoGravity(true);
      this.shooter = shooter;
   }

   @Override
   public void tick() {
      if (this.firstTick && this.level().isClientSide()) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EARTHBREAK_EVENT.get());
      }

      this.setNoGravity(true);
      super.tick();
      if (this.lifeTicks++ >= 20) {
         this.discard();
      } else if (this.lifeTicks == 1) {
         this.atarihantei();
      }
   }

   private void atarihantei() {
      for (Entity ent : this.level().getEntities(null, this.getBoundingBox())) {
         if (!ent.equals(this.shooter) && !(ent instanceof HitBoxEntity) && !(ent instanceof BossHitBoxEntity) && ent instanceof LivingEntity le) {
            if (!(ent instanceof Pmb01Entity) && !(ent.getVehicle() instanceof Pmb01Entity)) {
               le.addDeltaMovement(new Vec3(0.0, 2.0, 0.0));
            }

            le.invulnerableTime = 20;
            le.hurt(this.damageSources().generic(), 60.0F);
         }
      }

      if (this.isBreakBlocks() && ProjectileUtil.isDestructionAllowed(this)) {
         this.breakBlocks(35);
      }
   }

   private void breakBlocks(int radius) {
      BlockPos pos = this.blockPosition();
      int height = 15;

      for (int y = 0; y < height; y++) {
         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               if (x * x + z * z <= radius * radius) {
                  BlockPos blockPos = pos.offset(x, y, z);
                  BlockState state = this.level().getBlockState(blockPos);
                  if (!state.isAir()) {
                     Utils.eraseBlock(this.level(), blockPos);
                  }
               }
            }
         }
      }
   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
   }

   protected void onHitBlock(BlockHitResult blockHitResult) {
   }

   @Override
   public boolean shouldBeSaved() {
      return false;
   }

   @Override
   public boolean ignoreExplosion() {
      return true;
   }

   protected void defineSynchedData() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.earthbreak.new")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
