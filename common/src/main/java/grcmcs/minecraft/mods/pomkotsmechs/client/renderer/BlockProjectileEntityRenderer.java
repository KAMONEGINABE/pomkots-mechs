package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BlockProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BlockProjectileEntityRenderer extends EntityRenderer<BlockProjectileEntity> {
   private final ItemRenderer itemRenderer;

   public BlockProjectileEntityRenderer(Context context) {
      super(context);
      this.itemRenderer = context.m_174025_();
   }

   public void render(BlockProjectileEntity entity, float yaw, float partialTicks, PoseStack stack, MultiBufferSource source, int light) {
      stack.m_85836_();
      stack.m_85841_(1.0F, 1.0F, 1.0F);
      this.itemRenderer
         .m_269128_(
            new ItemStack(entity.getBlockState().m_60734_()),
            ItemDisplayContext.NONE,
            light,
            OverlayTexture.f_118083_,
            stack,
            source,
            entity.m_9236_(),
            entity.m_19879_()
         );
      stack.m_85849_();
      super.m_7392_(entity, yaw, partialTicks, stack, source, light);
   }

   public ResourceLocation getTextureLocation(BlockProjectileEntity entity) {
      return InventoryMenu.f_39692_;
   }
}
