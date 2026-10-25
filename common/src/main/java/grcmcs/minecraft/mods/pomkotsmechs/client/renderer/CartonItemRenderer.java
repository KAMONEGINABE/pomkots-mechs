package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.CartonItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.CartonItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class CartonItemRenderer extends GeoItemRenderer<CartonItem> {
   public CartonItemRenderer() {
      super(new CartonItemModel());
   }

   public void renderByItem(
      ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay
   ) {
      poseStack.pushPose();
      super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
      poseStack.popPose();
   }
}
