package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.misc.EncryptedPackResources;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {
   @Shadow
   private PackType f_203817_;

   @ModifyVariable(method = "createReload", at = @At("HEAD"), argsOnly = true)
   private List<PackResources> wrapPacks(List<PackResources> original) {
      if (this.f_203817_ != PackType.CLIENT_RESOURCES) {
         return original;
      }

      List<PackResources> wrapped = new ArrayList<>();

      for (PackResources pack : original) {
         PomkotsMechs.LOGGER.info("packid:" + pack.m_5542_());
         PomkotsMechs.LOGGER.info("packid:" + pack.m_5698_(PackType.CLIENT_RESOURCES));
         if (pack instanceof EncryptedPackResources) {
            wrapped.add(pack);
         } else if (pack.m_5542_().contains("pomkotsmechs")) {
            wrapped.add(new EncryptedPackResources(pack));
         } else if (pack.m_5698_(PackType.CLIENT_RESOURCES).contains("pomkotsmechs")) {
            wrapped.add(new EncryptedPackResources(pack));
         } else {
            wrapped.add(pack);
         }
      }

      return wrapped;
   }
}
