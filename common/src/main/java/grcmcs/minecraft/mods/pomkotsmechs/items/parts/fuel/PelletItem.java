package grcmcs.minecraft.mods.pomkotsmechs.items.parts.fuel;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.fuel.FuelItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class PelletItem extends BasePartsItem.Fuel {
   public PelletItem(Properties properties) {
      super(properties);
   }

   public FuelItemRenderer newRenderer() {
      return new FuelItemRenderer();
   }

   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public String getPartsSeriesName() {
      return "pellet";
   }
}
