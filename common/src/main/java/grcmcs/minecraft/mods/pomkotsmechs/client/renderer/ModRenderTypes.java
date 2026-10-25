package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;

public class ModRenderTypes {
   public static final RenderType ENTITY_CULL = RenderType.create(
      "entity_cull",
      DefaultVertexFormat.NEW_ENTITY,
      Mode.QUADS,
      256,
      true,
      false,
      CompositeState.builder()
         .setShaderState(new ShaderStateShard(GameRenderer::getRendertypeEntityCutoutNoCullShader))
         .setCullState(RenderStateShard.CULL)
         .setLightmapState(RenderStateShard.LIGHTMAP)
         .setOverlayState(RenderStateShard.OVERLAY)
         .createCompositeState(true)
   );
}
