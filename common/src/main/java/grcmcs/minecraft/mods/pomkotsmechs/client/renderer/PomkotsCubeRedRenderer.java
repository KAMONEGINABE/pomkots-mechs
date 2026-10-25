package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockRedEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsCubeRedModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PomkotsCubeRedRenderer extends GeoBlockRenderer<PomkotsCubeBlockRedEntity> {
   public PomkotsCubeRedRenderer(Context ctx) {
      super(new PomkotsCubeRedModel());
   }
}
