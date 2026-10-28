package grcmcs.minecraft.mods.pomkotsmechs.client.input;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class MechCameraHandler {
    private static CameraType prevCamera = null;
    private static boolean wasRiding = false;

    public static void tick(Minecraft mc) {
        if (mc.player == null || !PomkotsMechs.CONFIG.forceThirdPersonViewWhenRidingMech) return;

        boolean riding = mc.player.getVehicle() instanceof Pmvc01Entity;

        // 乗った瞬間
        if (riding && !wasRiding) {
            prevCamera = mc.options.getCameraType();
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }

        // 搭乗中：パイロットの体向きをカメラの水平Yawに同期
        if (riding && mc.gameRenderer != null && mc.gameRenderer.getMainCamera() != null) {
            float cameraYaw = mc.gameRenderer.getMainCamera().getYRot();
            mc.player.setYRot(cameraYaw);
            mc.player.yRotO = cameraYaw;
            mc.player.setYHeadRot(cameraYaw);
            mc.player.setYBodyRot(cameraYaw);
        }

        // 降りた瞬間
        if (!riding && wasRiding) {
            if (prevCamera != null) {
                mc.options.setCameraType(prevCamera);
                prevCamera = null;
            } else {
                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }
        }

        wasRiding = riding;
    }
}