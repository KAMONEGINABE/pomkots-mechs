package grcmcs.minecraft.mods.pomkotsmechs.client.input;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;

public class MechCameraHandler {
    private static CameraType prevCamera = null;
    private static boolean wasRiding = false;
    private static boolean leawindChecked = false;
    private static Field leawindAimingField = null;
    private static boolean wasAimingBeforeMount = false;

    private static void initLeawindCompat() {
        if (leawindChecked) return;
        leawindChecked = true;
        try {
            Class<?> clazz = Class.forName("com.github.leawind.thirdperson.ThirdPersonStatus");
            leawindAimingField = clazz.getField("isToggleToAiming");
        } catch (Throwable ignored) {
            leawindAimingField = null;
        }
    }

    private static void setLeawindAiming(boolean aiming) {
        initLeawindCompat();
        if (leawindAimingField != null) {
            try {
                leawindAimingField.setBoolean(null, aiming);
            } catch (Throwable ignored) {}
        }
    }

    private static boolean getLeawindAiming() {
        initLeawindCompat();
        if (leawindAimingField != null) {
            try {
                return leawindAimingField.getBoolean(null);
            } catch (Throwable ignored) {}
        }
        return false;
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null) return;

        boolean riding = mc.player.getVehicle() instanceof Pmvc01Entity;

        // 乗った瞬間
        if (riding && !wasRiding) {
            if (PomkotsMechs.CONFIG.forceThirdPersonViewWhenRidingMech) {
                prevCamera = mc.options.getCameraType();
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            }
            wasAimingBeforeMount = getLeawindAiming();
            setLeawindAiming(true);
        }

        // 搭乗中：LeawindのAimingモードを維持
        if (riding) {
            setLeawindAiming(true);
        }

        // 降りた瞬間
        if (!riding && wasRiding) {
            setLeawindAiming(wasAimingBeforeMount);
            if (PomkotsMechs.CONFIG.forceThirdPersonViewWhenRidingMech) {
                if (prevCamera != null) {
                    mc.options.setCameraType(prevCamera);
                    prevCamera = null;
                } else {
                    mc.options.setCameraType(CameraType.FIRST_PERSON);
                }
            }
        }

        wasRiding = riding;
    }
}
