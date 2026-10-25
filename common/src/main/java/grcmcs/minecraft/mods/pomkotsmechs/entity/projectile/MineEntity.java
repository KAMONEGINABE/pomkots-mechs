package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.AABB;
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

public class MineEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 160;
   private int lifeTicks = 0;
   private float damage;
   private LivingEntity shooter = null;
   private int onGroundTicks = 0;
   private float explosionScale = 5.0F;
   private boolean alreadyExplode = false;

   public void setExplosionScale(float explosionScale) {
      this.explosionScale = explosionScale;
   }

   public MineEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      this(entityType, level, null);
   }

   public MineEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter) {
      this(entityType, level, shooter, BattleBalance.MECH_RIFLE_DAMAGE);
   }

   public MineEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, float damage) {
      super(entityType, shooter, world);
      this.setNoGravity(false);
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
      if (!this.onGround()) {
         super.tick();
         this.hasImpulse = true;
      } else {
         this.onGroundTicks++;
         if (this.onGroundTicks > 5 && !this.level().isClientSide) {
            AABB mineAabb = this.getBoundingBox();
            List<Player> players = this.level().getEntitiesOfClass(Player.class, mineAabb, player -> !player.isSpectator());
            if (!players.isEmpty()) {
               this.createExplosion(this.position());
               this.alreadyExplode = true;
               this.discard();
            }
         }
      }

      if (this.lifeTicks++ >= 160) {
         this.discard();
      }
   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
   }

   protected void onHitBlock(BlockHitResult blockHitResult) {
      this.setDeltaMovement(0.0, 0.0, 0.0);
      this.setOnGround(true);
   }

   private void createExplosion(Vec3 pos) {
      Level world = this.level();
      if (!world.isClientSide) {
         if (ProjectileUtil.isDestructionAllowed(this)) {
            Utils.explode(this, pos.x, pos.y + 1.0, pos.z, this.explosionScale, false, ExplosionInteraction.BLOCK, this.level());
         } else {
            Utils.explode(this, pos.x, pos.y + 1.0, pos.z, this.explosionScale, false, ExplosionInteraction.NONE, this.level());
         }
      }
   }

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
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pms.idle")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
