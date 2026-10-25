package grcmcs.minecraft.mods.pomkotsmechs.items.parts.generators;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.generators.GeneratorItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class ChibaItem extends BasePartsItem.Generator {
   public ChibaItem(Properties properties) {
      super(properties);
   }

   public GeneratorItemRenderer newRenderer() {
      return new GeneratorItemRenderer();
   }

   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public String getPartsSeriesName() {
      return "chiba";
   }
}
