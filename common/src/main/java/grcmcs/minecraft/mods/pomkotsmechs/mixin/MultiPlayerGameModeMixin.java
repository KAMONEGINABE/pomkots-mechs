package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
   @Shadow
   @Final
   private Minecraft f_105189_;

   @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
   private void onStartDestroyBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
      if (this.f_105189_ != null && Utils.isRidingPomkotsMechs(this.f_105189_.f_91074_)) {
         cir.setReturnValue(false);
         cir.cancel();
      }
   }

   @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
   private void onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
      if (this.f_105189_ != null && Utils.isRidingPomkotsMechs(this.f_105189_.f_91074_)) {
         cir.setReturnValue(InteractionResult.FAIL);
         cir.cancel();
      }
   }

   @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
   private void onUseItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
      if (this.f_105189_ != null && Utils.isRidingPomkotsMechs(this.f_105189_.f_91074_)) {
         cir.setReturnValue(InteractionResult.FAIL);
         cir.cancel();
      }
   }

   @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
   private void onInteract(Player player, Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
      if (this.f_105189_ != null && Utils.isRidingPomkotsMechs(this.f_105189_.f_91074_)) {
         cir.setReturnValue(InteractionResult.FAIL);
         cir.cancel();
      }
   }

   @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
   private void onAttack(Player player, Entity target, CallbackInfo ci) {
      if (this.f_105189_ != null && Utils.isRidingPomkotsMechs(this.f_105189_.f_91074_)) {
         ci.cancel();
      }
   }
}
