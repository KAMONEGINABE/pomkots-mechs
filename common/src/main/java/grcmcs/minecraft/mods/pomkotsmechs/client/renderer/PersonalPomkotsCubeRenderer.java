package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.block.PersonalPomkotsCubeBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PersonalPomkotsCubeModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PersonalPomkotsCubeRenderer extends GeoBlockRenderer<PersonalPomkotsCubeBlockEntity> {
   public PersonalPomkotsCubeRenderer(Context context) {
      super(new PersonalPomkotsCubeModel());
   }

   public int getViewDistance() {
      return 128;
   }
}
