package grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MobEquipmentContainer implements Container {
   private final Mob mob;
   private static final EquipmentSlot[] SLOTS = new EquipmentSlot[]{
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
   };

   public MobEquipmentContainer(Mob mob) {
      this.mob = mob;
   }

   public int m_6643_() {
      return SLOTS.length;
   }

   public boolean m_7983_() {
      for (EquipmentSlot slot : SLOTS) {
         if (!this.mob.m_6844_(slot).m_41619_()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack m_8020_(int index) {
      return this.mob.m_6844_(SLOTS[index]);
   }

   public ItemStack m_7407_(int index, int count) {
      ItemStack stack = this.mob.m_6844_(SLOTS[index]);
      ItemStack result = stack.m_41620_(count);
      this.mob.m_8061_(SLOTS[index], stack);
      return result;
   }

   public ItemStack m_8016_(int index) {
      ItemStack stack = this.mob.m_6844_(SLOTS[index]);
      this.mob.m_8061_(SLOTS[index], ItemStack.f_41583_);
      return stack;
   }

   public void m_6836_(int index, ItemStack stack) {
      this.mob.m_8061_(SLOTS[index], stack);
   }

   public void m_6596_() {
   }

   public boolean m_6542_(Player player) {
      return this.mob.m_6084_();
   }

   public void m_6211_() {
      for (EquipmentSlot slot : SLOTS) {
         this.mob.m_8061_(slot, ItemStack.f_41583_);
      }
   }
}
