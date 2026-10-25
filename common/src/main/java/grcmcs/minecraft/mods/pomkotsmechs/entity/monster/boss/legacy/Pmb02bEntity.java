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
      this.m_21153_(100.0F);
      this.m_274367_(1.0F);
      this.m_20242_(true);
      this.m_21530_();
      this.f_19811_ = true;
   }

   public boolean m_142535_(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   public void m_8119_() {
      this.m_20242_(true);
      super.m_8119_();
      GameType gameMode = null;
      if (this.m_9236_().f_46443_) {
         gameMode = Minecraft.m_91087_().f_91072_.m_105295_();
      } else {
         List<ServerPlayer> list = this.m_9236_().m_7654_().m_6846_().m_11314_();
         if (list != null && !list.isEmpty()) {
            gameMode = ((ServerPlayer)this.m_9236_().m_7654_().m_6846_().m_11314_().get(0)).f_8941_.m_9290_();
         } else {
            gameMode = GameType.CREATIVE;
         }
      }

      if (gameMode.equals(GameType.SURVIVAL)) {
         this.lifeTick++;
         if (this.lifeTick < 10) {
            this.m_20219_(this.m_20182_().m_82549_(new Vec3(0.0, 1.0, 0.0)));
         }

         int cnt = this.lifeTick % 300;
         if (cnt == 100) {
            for (int i = 0; i < 5; i++) {
               MissileEnemyEntity be = new MissileEnemyEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.m_9236_(), this, 20.0F, 1.5F
               );
               be.m_146884_(this.m_20182_().m_82549_(new Vec3(-10.0, i, 0.0)));
               be.m_37251_(be, 0.0F, this.m_146908_() + 20.0F, this.m_21256_(), 1.5F, 0.0F);
               this.m_9236_().m_7967_(be);
            }

            for (int i = 0; i < 5; i++) {
               MissileEnemyEntity be = new MissileEnemyEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.m_9236_(), this, 20.0F, 1.5F
               );
               be.m_146884_(this.m_20182_().m_82549_(new Vec3(10.0, i, 0.0)));
               be.m_37251_(be, 0.0F, this.m_146908_() - 20.0F, this.m_21256_(), 1.5F, 0.0F);
               this.m_9236_().m_7967_(be);
            }
         }

         if (cnt == 200) {
            for (int i = 0; i < 5; i++) {
               MissileEnemyEntity be = new MissileEnemyEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.m_9236_(), this, 20.0F, 1.5F
               );
               be.m_146884_(this.m_20182_().m_82549_(new Vec3(3 - i, 0.0, 10.0)));
               be.m_37251_(be, 10.0F, this.m_146908_(), this.m_21256_(), 1.5F, 0.0F);
               this.m_9236_().m_7967_(be);
            }

            for (int i = 0; i < 5; i++) {
               MissileEnemyEntity be = new MissileEnemyEntity(
                  (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.m_9236_(), this, 20.0F, 1.5F
               );
               be.m_146884_(this.m_20182_().m_82549_(new Vec3(3 - i, -1.0, 10.0)));
               be.m_37251_(be, -10.0F, this.m_146908_(), this.m_21256_(), 1.5F, 0.0F);
               this.m_9236_().m_7967_(be);
            }
         }

         if (cnt == 299) {
            int mNum = 20;

            for (int i = 0; i < mNum; i++) {
               for (int j = 0; j < mNum; j++) {
                  MissileEnemyEntity be = new MissileEnemyEntity(
                     (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_ENEMY.get(), this.m_9236_(), this, 20.0F, 1.5F
                  );
                  be.m_146884_(this.m_20182_());
                  be.m_37251_(be, -i * (90 / mNum), -90 + j * (180 / mNum), this.m_21256_(), 1.5F, 0.0F);
                  this.m_9236_().m_7967_(be);
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

   public boolean m_6000_(double cameraX, double cameraY, double cameraZ) {
      double maxDistance = 1000.0;
      return true;
   }

   public boolean m_6783_(double d) {
      return true;
   }

   public boolean m_21532_() {
      return true;
   }

   public void m_6043_() {
   }

   public AABB m_6921_() {
      return this.m_20191_().m_82400_(5.0);
   }
}
