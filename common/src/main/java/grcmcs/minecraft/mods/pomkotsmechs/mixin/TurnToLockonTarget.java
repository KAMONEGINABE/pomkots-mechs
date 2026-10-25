package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.client.input.TargetLocker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MouseHandler.class)
public class TurnToLockonTarget {
   @Shadow
   @Final
   private Minecraft f_91503_;

   @Inject(
      method = "turnPlayer",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"),
      cancellable = true,
      locals = LocalCapture.CAPTURE_FAILHARD
   )
   private void lockon$onTurn(CallbackInfo ci, double d0, double d1, double d4, double d5, double d6, double d2, double d3, int i) {
      if (TargetLocker.getInstance().turn(this.f_91503_.f_91074_)) {
         ci.cancel();
      }
   }
}
