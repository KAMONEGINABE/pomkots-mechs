package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.util.CryptoUtil;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FallbackResourceManager.class)
public class FallbackResourceManagerMixin {
   @Inject(method = "getResource", at = @At("HEAD"), cancellable = true)
   private void hook(ResourceLocation loc, CallbackInfoReturnable<Optional<Resource>> cir) {
      System.out.println("FallBack" + loc);
      if (loc.m_135827_().equals("pomkotsmechs")) {
         if (loc.m_135815_().endsWith(".ogg")) {
            if (loc.m_135815_().contains("bgm/")) {
               ResourceLocation encrypted = new ResourceLocation(loc.m_135827_(), loc.m_135815_().replace(".ogg", ".dat"));
               Optional<Resource> res = ((FallbackResourceManager)this).m_213713_(encrypted);
               if (res.isPresent()) {
                  Resource r = res.get();
                  cir.setReturnValue(Optional.of(new Resource(r.m_247173_(), () -> CryptoUtil.decrypt(r.m_215507_()))));
               }
            }
         }
      }
   }
}
