package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.util.CryptoUtil;
import java.io.InputStream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PathPackResources.class)
public abstract class PathPackResourcesMixin implements PackResources {
   @Inject(method = "getResource", at = @At("RETURN"), cancellable = true)
   private void interceptOggResource(PackType type, ResourceLocation location, CallbackInfoReturnable<IoSupplier<InputStream>> cir) {
      if (this.packId().contains("pomkotsmechs")) {
         System.out.println(location);
         if (location.getPath().endsWith(".ogg")) {
            IoSupplier<InputStream> original = (IoSupplier<InputStream>)cir.getReturnValue();
            if (original != null) {
               cir.setReturnValue((IoSupplier)() -> CryptoUtil.decrypt((InputStream)original.get()));
            }
         }
      }
   }
}
