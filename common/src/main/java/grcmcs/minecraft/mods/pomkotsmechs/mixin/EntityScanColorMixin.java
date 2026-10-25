package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.misc.scan.ClientScanManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityScanColorMixin {
   @Inject(method = "getTeamColor()I", at = @At("HEAD"), cancellable = true)
   private void pomkotsmechs$scanOutlineColor(CallbackInfoReturnable<Integer> cir) {
      Entity self = (Entity)(Object)this;
      if (self.level().isClientSide) {
         if (ClientScanManager.isEntityHighlighted(self.getId())) {
            cir.setReturnValue(4259712);
         }
      }
   }
}
