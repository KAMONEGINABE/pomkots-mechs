package grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension.ExtensionItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class CircuitHardLockItem extends BasePartsItem.Extension {
   public CircuitHardLockItem(Properties properties) {
      super(properties);
   }

   public ExtensionItemRenderer newRenderer() {
      return new ExtensionItemRenderer();
   }

   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public String getPartsSeriesName() {
      return "circuithardlock";
   }
}
