package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.client.misc.FlightSpeedEffects;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class FlightFovMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void pomkotsmechs$flightBoostFov(Camera camera, float partialTick, boolean useFovSetting,
                                             CallbackInfoReturnable<Double> cir) {
        if (useFovSetting) {
            cir.setReturnValue(cir.getReturnValue() + FlightSpeedEffects.boostBlend() * 11.0D);
        }
    }
}
