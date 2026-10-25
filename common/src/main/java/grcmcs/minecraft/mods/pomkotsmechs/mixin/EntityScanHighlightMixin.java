package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.scan.ClientScanManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityScanHighlightMixin {
   @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
   private void pomkotsmechs$showScanHighlight(CallbackInfoReturnable<Boolean> cir) {
      Entity self = (Entity)this;
      if (self.m_9236_().f_46443_) {
         if (ClientScanManager.isEntityHighlighted(self.m_19879_())) {
            if (self instanceof Pmvc01Entity mech && mech.getDrivingPassenger() == Minecraft.m_91087_().f_91074_) {
               return;
            }

            cir.setReturnValue(true);
         }
      }
   }
}
