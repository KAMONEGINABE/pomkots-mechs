package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsCubeModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PomkotsCubeRenderer extends GeoBlockRenderer<PomkotsCubeBlockEntity> {
   public PomkotsCubeRenderer(Context ctx) {
      super(new PomkotsCubeModel());
   }

   public int getViewDistance() {
      return 128;
   }
}
