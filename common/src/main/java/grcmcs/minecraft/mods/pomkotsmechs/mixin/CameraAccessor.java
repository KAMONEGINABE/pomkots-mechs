package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
    // setYRotへのアクセスを提供
    @Invoker("setRotation")
    void invokeSetRotation(float yaw, float pitch);


    @Accessor("xRot")
    public void setXRot(float xRot);

    @Accessor("yRot")
    public void setYRot(float yRot);

    @Accessor("rotation")
    Quaternionf pomkotsmechs$getRotation();

    @Accessor("forwards")
    Vector3f pomkotsmechs$getForwards();

    @Accessor("up")
    Vector3f pomkotsmechs$getUp();

    @Accessor("left")
    Vector3f pomkotsmechs$getLeft();

}
