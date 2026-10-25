package grcmcs.minecraft.mods.pomkotsmechs.forge.mixin;

import java.util.List;
import java.util.Set;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class PomkotsForgeMixinPlugin implements IMixinConfigPlugin {
   private static final String CREATE_WORLD_SCREEN_MIXIN = "grcmcs.minecraft.mods.pomkotsmechs.forge.mixin.CreateWorldScreenMixin";

   public void onLoad(String mixinPackage) {
   }

   public String getRefMapperConfig() {
      return null;
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return "grcmcs.minecraft.mods.pomkotsmechs.forge.mixin.CreateWorldScreenMixin".equals(mixinClassName)
         ? LoadingModList.get().getModFileById("lostcities") != null
         : true;
   }

   public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
   }

   public List<String> getMixins() {
      return null;
   }

   public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }
}
