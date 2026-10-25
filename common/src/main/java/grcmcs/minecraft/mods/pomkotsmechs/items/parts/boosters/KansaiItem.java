package grcmcs.minecraft.mods.pomkotsmechs.items.parts.boosters;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.boosters.BoosterItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class KansaiItem extends BasePartsItem.Booster {
   public KansaiItem(Properties properties) {
      super(properties);
   }

   public BoosterItemRenderer newRenderer() {
      return new BoosterItemRenderer();
   }

   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public String getPartsSeriesName() {
      return "kansai";
   }
}
