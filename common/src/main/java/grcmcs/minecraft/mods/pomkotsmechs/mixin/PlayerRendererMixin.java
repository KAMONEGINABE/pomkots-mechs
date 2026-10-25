package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.cockpit.CockpitHudRendererManager;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
   @Inject(method = "renderRightHand", at = @At("HEAD"))
   private void onRenderRightHand(PoseStack poseStack, MultiBufferSource buffer, int light, AbstractClientPlayer player, CallbackInfo ci) {
      if (player != null && player.m_20202_() instanceof PomkotsVehicleBase vehicle && Utils.shouldRenderCockpit(vehicle)) {
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
