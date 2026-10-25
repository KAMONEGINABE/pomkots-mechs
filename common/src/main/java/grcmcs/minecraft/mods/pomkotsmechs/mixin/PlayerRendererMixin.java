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
      if (player != null && player.getVehicle() instanceof PomkotsVehicleBase vehicle && Utils.shouldRenderCockpit(vehicle)) {
         poseStack.pushPose();
         poseStack.setIdentity();
         poseStack.translate(0.5, -1.8, 0.1);
         poseStack.scale(1.0F, 1.0F, 1.0F);
         poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
         poseStack.mulPose(Axis.XP.rotationDegrees(-player.getXRot() / 2.0F));
         CockpitHudRendererManager.renderCockpit(poseStack, buffer, light, vehicle, player);
         poseStack.popPose();
      }
   }
}
