package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.client.sound.PomkotsBGMManager;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MusicManager.class)
public class MusicManagerMixin {
   @Shadow
   private int f_120180_;

   @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
   private void onTick(CallbackInfo ci) {
      if (PomkotsBGMManager.isSuppressingVanilla()) {
         this.f_120180_ = 72000;
         MusicManager self = (MusicManager)this;
         self.m_120186_();
      }
   }
}
