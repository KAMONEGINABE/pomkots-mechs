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
         preview.setPos(Minecraft.getInstance().cameraEntity.position());
         level.addFreshEntity(preview);
      }
   }

   public static void disable() {
      if (preview != null) {
         preview.discard();
         preview = null;
      }
   }

   public static void tick(Minecraft mc) {
      if (preview != null && mc.player != null) {
         if (mc.player.pick(30.0, 0.0F, true) instanceof BlockHitResult bhr) {
            Vec3 pos = bhr.getBlockPos().getCenter();
            preview.setPos(pos.x, pos.y, pos.z);
            preview.setYRot(mc.player.getYRot());
            preview.yRotO = preview.getYRot();
         }
      }
   }
}
