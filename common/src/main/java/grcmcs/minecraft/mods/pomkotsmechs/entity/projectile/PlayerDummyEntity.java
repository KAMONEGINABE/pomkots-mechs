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
      this.m_274367_(1.0F);
   }

   public boolean m_6469_(DamageSource s, float d) {
      return s.m_19385_().equals("inWall") ? false : super.m_6469_(s, d);
   }

   public boolean m_142535_(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   public void m_8119_() {
      if (this.m_20202_() == null) {
         this.m_6074_();
      }

      super.m_8119_();
   }

   public void m_7023_(Vec3 pos) {
      super.m_7023_(pos);
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
