package grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.magazine.MagazineItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class MagazineShotGunItem extends BasePartsItem.Magazine {
   public MagazineShotGunItem(Properties properties) {
      super(properties);
   }

   public MagazineItemRenderer newRenderer() {
      return new MagazineItemRenderer();
   }

   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public String getPartsSeriesName() {
      return "magazineshotgun";
   }
}
