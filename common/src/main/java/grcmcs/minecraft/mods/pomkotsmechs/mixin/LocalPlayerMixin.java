package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
   @Inject(method = "swing", at = @At("HEAD"), cancellable = true)
   private void onSwing(InteractionHand hand, CallbackInfo ci) {
      LocalPlayer player = (LocalPlayer)(Object)this;
      if (Utils.isRidingPomkotsMechs(player)) {
         ci.cancel();
      }
   }
}
