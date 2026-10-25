package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Attributes.class)
public abstract class AttributesMixin {
   private static final double MAX_HEALTH_LIMIT = 100000.0;

   @ModifyArg(
      method = "<clinit>",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/RangedAttribute;<init>(Ljava/lang/String;DDD)V", ordinal = 0),
      index = 3
   )
   private static double pomkotsmechs$increaseMaxHealthLimit(double original) {
      return 100000.0;
   }
}
