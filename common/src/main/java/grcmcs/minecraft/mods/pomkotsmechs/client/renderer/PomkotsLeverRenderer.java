package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsLeverBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsLeverModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PomkotsLeverRenderer extends GeoBlockRenderer<PomkotsLeverBlockEntity> {
   public PomkotsLeverRenderer(Context ctx) {
      super(new PomkotsLeverModel());
   }
}
