package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockPurpleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PomkotsCubePurpleModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PomkotsCubePurpleRenderer extends GeoBlockRenderer<PomkotsCubeBlockPurpleEntity> {
   public PomkotsCubePurpleRenderer(Context ctx) {
      super(new PomkotsCubePurpleModel());
   }
}
