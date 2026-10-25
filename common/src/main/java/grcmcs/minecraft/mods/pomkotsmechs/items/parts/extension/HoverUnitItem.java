package grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension.HoverUnitItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class HoverUnitItem extends BasePartsItem.Extension {
   public HoverUnitItem(Properties properties) {
      super(properties);
   }

   public HoverUnitItemRenderer newRenderer() {
      return new HoverUnitItemRenderer();
   }

   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public String getPartsSeriesName() {
      return "hoverunit";
   }
}
