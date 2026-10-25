package grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension.ExtensionItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class CircuitSoftLockItem extends BasePartsItem.Extension {
   public CircuitSoftLockItem(Properties properties) {
      super(properties);
   }

   public ExtensionItemRenderer newRenderer() {
      return new ExtensionItemRenderer();
   }

   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public String getPartsSeriesName() {
      return "circuitsoftlock";
   }
}
