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
      this.m_20242_(true);
      this.shooter = null;
   }

   public EarthbreakEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
      super(entityType, world);
      this.m_20242_(true);
      this.shooter = shooter;
   }

   @Override
   public void m_8119_() {
      if (this.f_19803_ && this.m_9236_().m_5776_()) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EARTHBREAK_EVENT.get());
      }

      this.m_20242_(true);
      super.m_8119_();
      if (this.lifeTicks++ >= 20) {
         this.m_146870_();
      } else if (this.lifeTicks == 1) {
         this.atarihantei();
      }
   }

   private void atarihantei() {
      for (Entity ent : this.m_9236_().m_45933_(null, this.m_20191_())) {
         if (!ent.equals(this.shooter) && !(ent instanceof HitBoxEntity) && !(ent instanceof BossHitBoxEntity) && ent instanceof LivingEntity le) {
            if (!(ent instanceof Pmb01Entity) && !(ent.m_20202_() instanceof Pmb01Entity)) {
               le.m_246865_(new Vec3(0.0, 2.0, 0.0));
            }

            le.f_19802_ = 20;
            le.m_6469_(this.m_269291_().m_269264_(), 60.0F);
         }
      }

      if (this.isBreakBlocks() && ProjectileUtil.isDestructionAllowed(this)) {
         this.breakBlocks(35);
      }
   }

   private void breakBlocks(int radius) {
      BlockPos pos = this.m_20183_();
      int height = 15;

      for (int y = 0; y < height; y++) {
         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               if (x * x + z * z <= radius * radius) {
                  BlockPos blockPos = pos.m_7918_(x, y, z);
                  BlockState state = this.m_9236_().m_8055_(blockPos);
                  if (!state.m_60795_()) {
                     Utils.eraseBlock(this.m_9236_(), blockPos);
                  }
               }
            }
         }
      }
   }

   protected void m_5790_(EntityHitResult entityHitResult) {
   }

   protected void m_8060_(BlockHitResult blockHitResult) {
   }

   @Override
   public boolean m_142391_() {
      return false;
   }

   @Override
   public boolean m_6128_() {
      return true;
   }

   protected void m_8097_() {
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
