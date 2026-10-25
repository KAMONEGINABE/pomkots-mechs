package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BlockMassEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockMassEntityRenderer extends EntityRenderer<BlockMassEntity> {
   private final BlockRenderDispatcher blockRenderer;

   public BlockMassEntityRenderer(Context context) {
      super(context);
      this.blockRenderer = context.m_234597_();
   }

   public void render(BlockMassEntity entity, float yaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
      BlockState[][][] states = entity.getBlockStates();
      Vec3 interpolatedPos = entity.m_20318_(partialTicks).m_82520_(-entity.f_19854_, -entity.f_19855_, -entity.f_19856_);

      for (int x = 0; x < states.length; x++) {
         for (int y = 0; y < states[x].length; y++) {
            for (int z = 0; z < states[x][y].length; z++) {
               BlockState state = states[x][y][z];
               if (state != null) {
                  stack.m_85836_();
                  stack.m_252880_(x - 2.0F, y, z - 2.0F);
                  BakedModel model = this.blockRenderer.m_110910_(state);
                  this.blockRenderer
                     .m_110937_()
                     .m_111067_(stack.m_85850_(), buffer.m_6299_(RenderType.m_110451_()), state, model, 1.0F, 1.0F, 1.0F, light, OverlayTexture.f_118083_);
                  stack.m_85849_();
               }
            }
         }
      }
   }

   public ResourceLocation getTextureLocation(BlockMassEntity entity) {
      return null;
   }
}
