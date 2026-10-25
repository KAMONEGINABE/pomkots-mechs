package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import grcmcs.minecraft.mods.pomkotsmechs.client.hud.PomkotsHud;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class ArenaCameraClient {
   private static CameraType previousCameraType;

   public static void startOpeningCamera() {
      PomkotsHud.setActive(false);
      Minecraft mc = Minecraft.m_91087_();
      previousCameraType = mc.f_91066_.m_92176_();
      mc.f_91066_.m_92157_(CameraType.FIRST_PERSON);
   }

   public static void endOpeningCamera() {
      PomkotsHud.setActive(true);
      Minecraft mc = Minecraft.m_91087_();
      if (previousCameraType != null) {
         mc.f_91066_.m_92157_(previousCameraType);
         previousCameraType = null;
      } else {
         mc.f_91066_.m_92157_(CameraType.THIRD_PERSON_FRONT);
      }
   }
}
