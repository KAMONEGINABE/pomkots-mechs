package grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension.BuilderUnitItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class BuilderUnitItem extends BasePartsItem.Extension {
   public BuilderUnitItem(Properties properties) {
      super(properties);
   }

   public BuilderUnitItemRenderer newRenderer() {
      return new BuilderUnitItemRenderer();
   }

   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public String getPartsSeriesName() {
      return "builderunit";
   }
}
