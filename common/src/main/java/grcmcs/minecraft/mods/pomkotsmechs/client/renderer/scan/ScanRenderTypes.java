package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.scan;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.OptionalDouble;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.LineStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;

public final class ScanRenderTypes extends RenderType {
   public static final RenderType SCAN_DOME = RenderType.create(
      "pomkotsmechs_scan_dome",
      DefaultVertexFormat.POSITION_COLOR,
      Mode.QUADS,
      65536,
      false,
      true,
      CompositeState.builder().setShaderState(POSITION_COLOR_SHADER).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).setDepthTestState(LEQUAL_DEPTH_TEST).setWriteMaskState(COLOR_WRITE).createCompositeState(false)
   );
   public static final RenderType SCAN_HIGHLIGHT_LINES = RenderType.create(
      "pomkotsmechs_scan_highlight_lines",
      DefaultVertexFormat.POSITION_COLOR_NORMAL,
      Mode.LINES,
      256,
      false,
      false,
      CompositeState.builder()
         .setShaderState(RENDERTYPE_LINES_SHADER)
         .setLineState(new LineStateShard(OptionalDouble.of(2.0)))
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
         .setDepthTestState(NO_DEPTH_TEST)
         .setCullState(NO_CULL)
         .setWriteMaskState(COLOR_WRITE)
         .createCompositeState(false)
   );

   private ScanRenderTypes(
      String name, VertexFormat format, Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState
   ) {
      super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
   }
}
