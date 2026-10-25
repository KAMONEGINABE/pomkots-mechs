package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;

public class ModRenderTypes {
   public static final RenderType ENTITY_CULL = RenderType.m_173215_(
      "entity_cull",
      DefaultVertexFormat.f_85812_,
      Mode.QUADS,
      256,
      true,
      false,
      CompositeState.m_110628_()
         .m_173292_(new ShaderStateShard(GameRenderer::m_172667_))
         .m_110661_(RenderStateShard.f_110158_)
         .m_110671_(RenderStateShard.f_110152_)
         .m_110677_(RenderStateShard.f_110154_)
         .m_110691_(true)
   );
}
