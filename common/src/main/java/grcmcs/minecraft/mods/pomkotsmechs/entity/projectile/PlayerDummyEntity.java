package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
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

public class PlayerDummyEntity extends PathfinderMob implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   public static final float DEFAULT_SCALE = 1.0F;
   public static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");

   public PlayerDummyEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
      super(entityType, world);
      this.setMaxUpStep(1.0F);
   }

   public boolean hurt(DamageSource s, float d) {
      return s.getMsgId().equals("inWall") ? false : super.hurt(s, d);
   }

   public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   public void tick() {
      if (this.getVehicle() == null) {
         this.kill();
      }

      super.tick();
   }

   public void travel(Vec3 pos) {
      super.travel(pos);
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "rotation", 0, event -> event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.ossan.sit")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
