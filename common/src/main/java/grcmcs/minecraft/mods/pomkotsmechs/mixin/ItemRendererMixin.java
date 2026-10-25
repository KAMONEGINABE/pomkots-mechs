package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.cockpit.CockpitHudRendererManager;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
   @Inject(method = "render", at = @At("HEAD"))
   private void render(
      ItemStack itemStack,
      ItemDisplayContext itemDisplayContext,
      boolean bl,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int light,
      int z,
      BakedModel bakedModel,
      CallbackInfo ci
   ) {
      Player player = Minecraft.m_91087_().f_91074_;
      if (player != null && player.m_20202_() instanceof PomkotsVehicleBase vehicle && itemDisplayContext.m_269069_() && Utils.shouldRenderCockpit(vehicle)) {
         poseStack.m_85836_();
         poseStack.m_166856_();
         poseStack.m_85837_(0.5, -1.8, 0.1);
         poseStack.m_85841_(1.0F, 1.0F, 1.0F);
         poseStack.m_252781_(Axis.f_252436_.m_252977_(180.0F));
         poseStack.m_252781_(Axis.f_252529_.m_252977_(-player.m_146909_() / 2.0F));
         CockpitHudRendererManager.renderCockpit(poseStack, buffer, light, vehicle, player);
         poseStack.m_85849_();
      }
   }
}
