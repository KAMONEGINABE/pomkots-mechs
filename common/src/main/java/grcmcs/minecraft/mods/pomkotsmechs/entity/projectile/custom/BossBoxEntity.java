package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BossBoxEntity extends ThrowableProjectile implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 100;
   private int lifeTicks = 0;
   public float scale = 2.0F;

   public BossBoxEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      super(entityType, level);
      this.m_20242_(true);
      this.f_19811_ = true;
      this.f_19794_ = true;
   }

   public boolean m_6000_(double a, double b, double c) {
      return true;
   }

   public boolean m_6783_(double e) {
      return true;
   }

   public void m_8119_() {
      this.m_20242_(true);
      super.m_8119_();
      if (this.lifeTicks++ >= 100) {
         this.m_146870_();
      }
   }

   protected void m_8097_() {
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
                  this,
                  "rotation",
                  0,
                  event -> {
                     if (this.lifeTicks < 10) {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.bossbox.idle"));
                     } else {
                        return this.lifeTicks == 10
                           ? event.setAndContinue(RawAnimation.begin().thenPlay("animation.bossbox.start").thenPlayAndHold("animation.bossbox.open"))
                           : PlayState.CONTINUE;
                     }
                  }
               )
               .setSoundKeyframeHandler(this::registerAnimationSoundHandlers)
         }
      );
   }

   protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
      if ("raise1".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get());
         this.spawnDirtParticlesSquare(this.m_9236_(), (int)(1.0F * this.scale), this.m_20183_());
      } else if ("raise2".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EARTHBREAK_EVENT.get());
         this.spawnDirtParticlesSquare(this.m_9236_(), (int)(10.0F * this.scale), this.m_20183_());
      } else if ("close".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BOSSBOXOPEN.get());
      }
   }

   public void spawnDirtParticlesSquare(Level level, int ypos, BlockPos centerPos) {
      BlockState dirt = Blocks.f_50493_.m_49966_();
      RandomSource random = level.m_213780_();
      int range = (int)(7.0F * this.scale);

      for (int dx = -range; dx < range; dx++) {
         for (int dz = -range; dz < range; dz++) {
            double px = centerPos.m_123341_() + 0.5 + dx;
            double py = centerPos.m_123342_() + 1.0 + random.m_188500_() * 2.0 + ypos;
            double pz = centerPos.m_123343_() + 0.5 + dz;
            double vx = (random.m_188500_() - 0.5) * 0.1;
            double vy = random.m_188500_() * 0.1;
            double vz = (random.m_188500_() - 0.5) * 0.1;
            this.m_9236_().m_7106_(new BlockParticleOption(ParticleTypes.f_123794_, dirt), px, py, pz, vx, vy, vz);
         }
      }
   }

   protected void playSoundEffect(SoundEvent event) {
      this.m_9236_().m_7785_(this.m_20185_(), this.m_20186_(), this.m_20189_(), event, SoundSource.PLAYERS, 1.0F, 1.0F, false);
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
