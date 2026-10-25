package grcmcs.minecraft.mods.pomkotsmechs.entity.misc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class PlacementPreviewEntity extends Entity {
   public PlacementPreviewEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.f_19794_ = true;
      this.f_19811_ = true;
   }

   public void m_8119_() {
      super.m_8119_();
   }

   protected void m_8097_() {
   }

   protected void m_7378_(CompoundTag tag) {
   }

   protected void m_7380_(CompoundTag tag) {
   }

   public boolean m_6000_(double x, double y, double z) {
      return true;
   }

   public boolean m_6783_(double d) {
      return true;
   }

   public boolean m_6087_() {
      return false;
   }
}
