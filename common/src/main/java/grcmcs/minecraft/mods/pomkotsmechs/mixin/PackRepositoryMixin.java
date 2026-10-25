package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.google.common.collect.ImmutableMap;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.misc.EncryptedPackResources;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.Pack.Position;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
   @Inject(method = "discoverAvailable", at = @At("RETURN"), cancellable = true)
   private void wrapModPacks(CallbackInfoReturnable<Map<String, Pack>> cir) {
      Map<String, Pack> original = (Map<String, Pack>)cir.getReturnValue();
      Map<String, Pack> modified = new HashMap<>(original);
      modified.replaceAll(
         (id, pack) -> {
            if (!id.contains("pomkotsmechs")) {
               return (Pack)pack;
            }

            Pack wrapped = Pack.readMetaAndCreate(
               pack.getId(),
               pack.getTitle(),
               true,
               path -> new EncryptedPackResources(new PathPackResources("pomkotsmechs", PomkotsMechs.getModRootPath(), true)),
               PackType.CLIENT_RESOURCES,
               Position.TOP,
               PackSource.BUILT_IN
            );
            return wrapped != null ? wrapped : pack;
         }
      );
      cir.setReturnValue(ImmutableMap.copyOf(modified));
   }
}
