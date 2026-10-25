package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
   @Invoker("setRotation")
   void invokeSetRotation(float var1, float var2);

   @Accessor("xRot")
   void setXRot(float var1);

   @Accessor("yRot")
   void setYRot(float var1);
}
