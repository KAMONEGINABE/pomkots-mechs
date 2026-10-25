package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AlertRedEntity extends AlertEntity {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

   public AlertRedEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
      super(entityType, level);
   }

   @Override
   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }
}
