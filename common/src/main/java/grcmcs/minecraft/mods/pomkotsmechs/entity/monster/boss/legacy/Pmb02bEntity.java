package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileEnemyEntity;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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

public class Pmb02bEntity extends PathfinderMob implements GeoEntity, GeoAnimatable, Enemy {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 1.0F;
   public static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");
   private int lifeTick = 0;

   public Pmb02bEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
      super(entityType, world);
      this.setHealth(100.0F);
      this.setMaxUpStep(1.0F);
      this.setNoGravity(true);
      this.setPersistenceRequired();
      this.noCulling = true;
   }

   public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   public void tick() {
      this.setNoGravity(true);
      super.tick();
      GameType gameMode = null;
      if (this.level().isClientSide) {
         gameMode = Minecraft.getInstance().gameMode.getPlayerMode();
      } else {
         List<ServerPlayer> list = this.level().getServer().getPlayerList().getPlayers();
         if (list != null && !list.isEmpty()) {
            gameMode = ((ServerPlayer)this.level().getServer().getPlayerList().getPlayers().get(0)).gameMode.getGameModeForPlayer();
         } else {
            gameMode = GameType.CREATIVE;
         }
      }

      if (gameMode.equals(GameType.SURVIVAL)) {
         this.lifeTick++;
         if (this.lifeTick < 10) {
            this.moveTo(this.position().add(new Vec3(0.0, 1.0, 0.0)));
         }

         int cnt = this.lifeTick % 300;
         if (cnt == 100) {
            for (int i = 0; i < 5; i++) {
               MissileEnemyEntity be = new MissileEnemyEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this, 20.0F, 1.5F
               );
               be.setPos(this.position().add(new Vec3(-10.0, i, 0.0)));
               be.shootFromRotation(be, 0.0F, this.getYRot() + 20.0F, this.getFallFlyingTicks(), 1.5F, 0.0F);
               this.level().addFreshEntity(be);
            }

            for (int i = 0; i < 5; i++) {
               MissileEnemyEntity be = new MissileEnemyEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this, 20.0F, 1.5F
               );
               be.setPos(this.position().add(new Vec3(10.0, i, 0.0)));
               be.shootFromRotation(be, 0.0F, this.getYRot() - 20.0F, this.getFallFlyingTicks(), 1.5F, 0.0F);
               this.level().addFreshEntity(be);
            }
         }

         if (cnt == 200) {
            for (int i = 0; i < 5; i++) {
               MissileEnemyEntity be = new MissileEnemyEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this, 20.0F, 1.5F
               );
               be.setPos(this.position().add(new Vec3(3 - i, 0.0, 10.0)));
               be.shootFromRotation(be, 10.0F, this.getYRot(), this.getFallFlyingTicks(), 1.5F, 0.0F);
               this.level().addFreshEntity(be);
            }

            for (int i = 0; i < 5; i++) {
               MissileEnemyEntity be = new MissileEnemyEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this, 20.0F, 1.5F
               );
               be.setPos(this.position().add(new Vec3(3 - i, -1.0, 10.0)));
               be.shootFromRotation(be, -10.0F, this.getYRot(), this.getFallFlyingTicks(), 1.5F, 0.0F);
               this.level().addFreshEntity(be);
            }
         }

         if (cnt == 299) {
            int mNum = 20;

            for (int i = 0; i < mNum; i++) {
               for (int j = 0; j < mNum; j++) {
                  MissileEnemyEntity be = new MissileEnemyEntity(
                     (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this, 20.0F, 1.5F
                  );
                  be.setPos(this.position());
                  be.shootFromRotation(be, -i * (90 / mNum), -90 + j * (180 / mNum), this.getFallFlyingTicks(), 1.5F, 0.0F);
                  this.level().addFreshEntity(be);
               }
            }
         }
      }
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb02.idle")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
      double maxDistance = 1000.0;
      return true;
   }

   public boolean shouldRenderAtSqrDistance(double d) {
      return true;
   }

   public boolean isPersistenceRequired() {
      return true;
   }

   public void checkDespawn() {
   }

   public AABB getBoundingBoxForCulling() {
      return this.getBoundingBox().inflate(5.0);
   }
}
