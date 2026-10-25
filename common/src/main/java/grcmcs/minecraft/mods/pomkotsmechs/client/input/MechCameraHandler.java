package grcmcs.minecraft.mods.pomkotsmechs.client.input;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class MechCameraHandler {
   private static CameraType prevCamera = null;
   private static boolean wasRiding = false;

   public static void tick(Minecraft mc) {
      if (mc.f_91074_ != null && PomkotsMechs.CONFIG.forceThirdPersonViewWhenRidingMech) {
         boolean riding = mc.f_91074_.m_20202_() instanceof Pmvc01Entity;
         if (riding && !wasRiding) {
            prevCamera = mc.f_91066_.m_92176_();
            mc.f_91066_.m_92157_(CameraType.THIRD_PERSON_BACK);
         }

         if (!riding && wasRiding) {
            if (prevCamera != null) {
               mc.f_91066_.m_92157_(prevCamera);
               prevCamera = null;
            } else {
               mc.f_91066_.m_92157_(CameraType.FIRST_PERSON);
            }
         }

         wasRiding = riding;
      }
   }
}
