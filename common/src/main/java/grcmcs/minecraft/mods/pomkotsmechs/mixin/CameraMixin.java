package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv03Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
   @Shadow
   protected abstract void m_90581_(Vec3 var1);

   @Inject(method = "setup", at = @At("TAIL"), cancellable = true)
   private void adjustCameraPosition(BlockGetter blockGetter, Entity viewEntity, boolean thirdPerson, boolean inverse, float partialTick, CallbackInfo ci) {
      if (viewEntity instanceof Player player) {
         if (player.m_20202_() instanceof Pmvc01Entity pmg && !thirdPerson) {
            Vec3 bonePos = pmg.getMainCameraPosition();
            this.m_90581_(pmg.m_20318_(partialTick).m_82549_(bonePos));
         } else if (player.m_20202_() instanceof Pmv03Entity pmg && !thirdPerson) {
            Vec3 bonePos = pmg.getMainCameraPosition();
            this.m_90581_(pmg.m_20318_(partialTick).m_82549_(bonePos));
         }
      }
   }
}
