package grcmcs.minecraft.mods.pomkotsmechs.client.misc;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.PlacementPreviewEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PlacementPreviewEntityManager {
   private static PlacementPreviewEntity preview;

   public static void enable(Level level) {
      if (preview == null) {
         preview = new PlacementPreviewEntity((EntityType<?>)PomkotsMechs.PLACEMENT_PREVIEW.get(), level);
         preview.m_146884_(Minecraft.m_91087_().f_91075_.m_20182_());
         level.m_7967_(preview);
      }
   }

   public static void disable() {
      if (preview != null) {
         preview.m_146870_();
         preview = null;
      }
   }

   public static void tick(Minecraft mc) {
      if (preview != null && mc.f_91074_ != null) {
         if (mc.f_91074_.m_19907_(30.0, 0.0F, true) instanceof BlockHitResult bhr) {
            Vec3 pos = bhr.m_82425_().m_252807_();
            preview.m_6034_(pos.f_82479_, pos.f_82480_, pos.f_82481_);
            preview.m_146922_(mc.f_91074_.m_146908_());
            preview.f_19859_ = preview.m_146908_();
         }
      }
   }
}
