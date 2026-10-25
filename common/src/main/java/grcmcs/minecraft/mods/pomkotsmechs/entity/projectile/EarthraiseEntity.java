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
      this(entityType, world, Vec3.f_82478_, null, 8);
   }

   public EarthraiseEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, Vec3 vec, LivingEntity shooter, int count) {
      super(entityType, world);
      this.m_20242_(true);
      this.shooter = shooter;
      this.vec = vec;
      Vec3 tmp = vec.m_82541_();
      this.knockbackVec = new Vec3(-tmp.f_82479_, tmp.f_82480_, -tmp.f_82481_);
      this.count = count;
   }

   public float getScaleHeight() {
      return 2.0F;
   }

   public float getScaleWidth() {
      return 2.0F;
   }

   @Override
   public void m_8119_() {
      if (this.f_19803_ && this.m_9236_().m_5776_()) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EARTHRAISE_EVENT.get());
      }

      this.m_20242_(true);
      super.m_8119_();
      if (this.lifeTicks++ >= 20) {
         this.m_146870_();
      } else if (this.lifeTicks == 3) {
         if (!this.m_9236_().f_46443_ && this.count > 0) {
            Vec3 pos = this.m_20182_();
            EarthraiseEntity e = new EarthraiseEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.EARTHRAISE.get(), this.m_9236_(), this.vec, this.shooter, this.count - 1
            );
            e.m_146884_(pos.m_82549_(this.vec));
            e.m_146922_((float)(Math.toDegrees(Math.atan2(this.vec.f_82481_, this.vec.f_82479_)) - 90.0));
            this.m_9236_().m_7967_(e);
         } else {
            this.generateBlockParticles();
         }

         this.atarihantei();
      }
   }

   private void atarihantei() {
      for (Entity ent : this.m_9236_().m_45933_(null, this.m_20191_())) {
         if (!ent.equals(this.shooter) && !(ent instanceof HitBoxEntity) && !(ent instanceof BossHitBoxEntity) && ent instanceof LivingEntity le) {
            if (!(ent instanceof Pmb01Entity) && !(ent.m_20202_() instanceof Pmb01Entity)) {
               le.m_147240_(3.0, this.knockbackVec.f_82479_, this.knockbackVec.f_82481_);
            }

            le.f_19802_ = 20;
            le.m_6469_(this.m_269291_().m_269264_(), 30.0F);
         }
      }

      if (this.isBreakBlocks() && ProjectileUtil.isDestructionAllowed(this)) {
         this.breakBlocks();
      }
   }

   private void breakBlocks() {
      AABB area = this.m_20191_();

      for (BlockPos blockPos : BlockPos.m_121976_(
         (int)area.f_82288_, (int)area.f_82289_, (int)area.f_82290_, (int)area.f_82291_, (int)area.f_82292_ + 10, (int)area.f_82293_
      )) {
         BlockState state = this.m_9236_().m_8055_(blockPos);
         if (!state.m_60795_()) {
            Utils.eraseBlock(this.m_9236_(), blockPos);
         }
      }
   }

   private void generateBlockParticles() {
      if (this.m_9236_().f_46443_) {
         RandomSource random = this.m_9236_().m_213780_();

         for (int i = 0; i < 30; i++) {
            double velocityX = random.m_188500_() * 2.0 - 1.0;
            double velocityY = random.m_188500_() * 2.0 - 1.0;
            double velocityZ = random.m_188500_() * 2.0 - 1.0;
            this.m_9236_()
               .m_6485_(
                  new BlockParticleOption(ParticleTypes.f_123794_, Blocks.f_50069_.m_49966_()),
                  true,
                  this.m_20185_() + velocityX,
                  this.m_20186_() + 8.0,
                  this.m_20189_() + velocityZ,
                  velocityX,
                  velocityY,
                  velocityZ
               );
         }
      }
   }

   protected void m_5790_(EntityHitResult entityHitResult) {
   }

   protected void m_8060_(BlockHitResult blockHitResult) {
   }

   protected void m_8097_() {
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
