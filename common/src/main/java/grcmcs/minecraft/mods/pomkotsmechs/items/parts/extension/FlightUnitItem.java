package grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension.ExtensionItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class FlightUnitItem extends BasePartsItem.Extension {
    public FlightUnitItem(Properties properties) {
        super(properties);
    }

    @Override
    public ExtensionItemRenderer newRenderer() {
        return new ExtensionItemRenderer();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public String getPartsSeriesName() {
        return "flightunit";
    }
}
