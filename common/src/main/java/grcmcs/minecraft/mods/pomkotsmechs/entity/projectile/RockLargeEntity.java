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
   public void m_8119_() {
      HitResult hitResult = ProjectileUtil.raycastBoundingCheck(this);
      if (hitResult.m_6662_() != Type.MISS) {
         this.m_6532_(hitResult);
      }

      super.m_8119_();
      this.f_19812_ = true;
      if (this.lifeTicks++ >= 160) {
         this.m_146870_();
      }
   }

   protected void m_5790_(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.m_82443_();
      if (!entity.equals(this.shooter) && !(entity instanceof BossHitBoxEntity)) {
         entity.m_6469_(entity.m_269291_().m_269390_(this, (Entity)(this.m_19749_() != null ? this.m_19749_() : this)), this.damage);
         entity.f_19802_ = 0;
         this.explode();
      }
   }

   protected void m_8060_(BlockHitResult blockHitResult) {
      this.explode();
   }

   private void explode() {
      if (!this.m_9236_().f_46443_) {
         this.explodeVisual();
         this.spawnSmallRocks();
         this.m_146870_();
      }
   }

   private void spawnSmallRocks() {
      int count = 12;
      RandomSource rand = this.f_19796_;

      for (int i = 0; i < count; i++) {
         RockSmallEntity rock = new RockSmallEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.ROCK_SMALL.get(), this.m_9236_(), this.shooter);
         rock.m_6034_(this.m_20185_(), this.m_20186_() + 0.5, this.m_20189_());
         rock.setExplosionScale(this.explosionScale);
         Vec3 dir = new Vec3(rand.m_188583_(), rand.m_188583_() * 0.6, rand.m_188583_()).m_82541_();
         rock.m_20256_(dir.m_82490_(0.8 + rand.m_188501_()));
         this.m_9236_().m_7967_(rock);
      }
   }

   private void explodeVisual() {
      Utils.explode(this, this.m_20185_(), this.m_20186_(), this.m_20189_(), 2.0F, ExplosionInteraction.NONE, this.m_9236_());
   }

   private void createExplosion(Vec3 pos) {
      Level world = this.m_9236_();
      if (!world.f_46443_) {
         if (Utils.isBlockDestructionAllowed(this.shooter)) {
            Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, this.explosionScale, false, ExplosionInteraction.BLOCK, world);
         } else {
            Utils.explode(this, pos.f_82479_, pos.f_82480_, pos.f_82481_, this.explosionScale, false, ExplosionInteraction.NONE, world);
         }
      } else {
         this.addParticles(this.m_20182_());
      }
   }

   public void m_142036_() {
      if (this.m_9236_().f_46443_) {
         this.addParticles(this.m_20182_());
      }
   }

   private void addParticles(Vec3 offset) {
      Level world = this.m_9236_();

      for (int i = 0; i < 5; i++) {
         int rad = Math.abs(i - 3);

         for (int j = 0; j < 5 - rad; j++) {
            for (int k = 0; k < 5 - rad; k++) {
               world.m_7106_(
                  ParticleTypes.f_123813_,
                  offset.f_82479_ - (k - (3 - rad / 2)) * 2,
                  offset.f_82480_ - (i - 3) * 2,
                  offset.f_82481_ - (j - (3 - rad / 2)) * 2,
                  0.0,
                  0.0,
                  0.0
               );
            }
         }
      }
   }

   protected void m_8097_() {
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
