package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.scan;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.OptionalDouble;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.LineStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;

public final class ScanRenderTypes extends RenderType {
   public static final RenderType SCAN_DOME = RenderType.m_173215_(
      "pomkotsmechs_scan_dome",
      DefaultVertexFormat.f_85815_,
      Mode.QUADS,
      65536,
      false,
      true,
      CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110139_).m_110661_(f_110110_).m_110663_(f_110113_).m_110687_(f_110115_).m_110691_(false)
   );
   public static final RenderType SCAN_HIGHLIGHT_LINES = RenderType.m_173215_(
      "pomkotsmechs_scan_highlight_lines",
      DefaultVertexFormat.f_166851_,
      Mode.LINES,
      256,
      false,
      false,
      CompositeState.m_110628_()
         .m_173292_(f_173095_)
         .m_110673_(new LineStateShard(OptionalDouble.of(2.0)))
         .m_110669_(f_110119_)
         .m_110685_(f_110139_)
         .m_110663_(f_110111_)
         .m_110661_(f_110110_)
         .m_110687_(f_110115_)
         .m_110691_(false)
   );

   private ScanRenderTypes(
      String name, VertexFormat format, Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState
   ) {
      super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
   }
}
