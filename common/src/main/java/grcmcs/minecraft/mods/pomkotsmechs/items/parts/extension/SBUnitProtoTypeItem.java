package grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension.SBUnitProtoTypeItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class SBUnitProtoTypeItem extends BasePartsItem.Extension {
   public SBUnitProtoTypeItem(Properties properties) {
      super(properties);
   }

   public SBUnitProtoTypeItemRenderer newRenderer() {
      return new SBUnitProtoTypeItemRenderer();
   }

   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public String getPartsSeriesName() {
      return "protosbunit";
   }
}
