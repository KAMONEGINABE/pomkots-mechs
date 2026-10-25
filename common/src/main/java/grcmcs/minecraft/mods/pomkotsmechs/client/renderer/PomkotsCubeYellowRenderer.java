package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockYellowEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsCubeYellowModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PomkotsCubeYellowRenderer extends GeoBlockRenderer<PomkotsCubeBlockYellowEntity> {
   public PomkotsCubeYellowRenderer(Context ctx) {
      super(new PomkotsCubeYellowModel());
   }
}
