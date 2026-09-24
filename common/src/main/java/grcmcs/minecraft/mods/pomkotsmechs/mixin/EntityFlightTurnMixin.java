package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.client.misc.FlightClientController;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityFlightTurnMixin {
    private boolean pomkotsmechs$replayingLimitedTurn;

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void pomkotsmechs$captureFlightTurn(double yawInput, double pitchInput, CallbackInfo ci) {
        Object self = this;
        if (self instanceof LocalPlayer player && !pomkotsmechs$replayingLimitedTurn) {
            FlightClientController.TurnInput limited =
                    FlightClientController.limitArcadeTurn(player, yawInput, pitchInput);
            if (limited.yaw() != yawInput || limited.pitch() != pitchInput) {
                pomkotsmechs$replayingLimitedTurn = true;
                try {
                    ((Entity) self).turn(limited.yaw(), limited.pitch());
                } finally {
                    pomkotsmechs$replayingLimitedTurn = false;
                }
                ci.cancel();
                return;
            }
            FlightClientController.captureTurn(player, yawInput, pitchInput);
        }
    }
}
