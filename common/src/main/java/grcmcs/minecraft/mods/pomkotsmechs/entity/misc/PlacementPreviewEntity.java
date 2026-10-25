package grcmcs.minecraft.mods.pomkotsmechs.entity.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class PlacementPreviewEntity extends Entity {
   public PlacementPreviewEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
      this.noCulling = true;
   }

   public void tick() {
      super.tick();
   }

   protected void defineSynchedData() {
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
   }

   public boolean shouldRender(double x, double y, double z) {
      return true;
   }

   public boolean shouldRenderAtSqrDistance(double d) {
      return true;
   }

   public boolean isPickable() {
      return false;
   }
}
