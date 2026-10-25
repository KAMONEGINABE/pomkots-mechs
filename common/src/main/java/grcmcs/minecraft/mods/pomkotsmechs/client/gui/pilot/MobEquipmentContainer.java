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

   public int getContainerSize() {
      return SLOTS.length;
   }

   public boolean isEmpty() {
      for (EquipmentSlot slot : SLOTS) {
         if (!this.mob.getItemBySlot(slot).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int index) {
      return this.mob.getItemBySlot(SLOTS[index]);
   }

   public ItemStack removeItem(int index, int count) {
      ItemStack stack = this.mob.getItemBySlot(SLOTS[index]);
      ItemStack result = stack.split(count);
      this.mob.setItemSlot(SLOTS[index], stack);
      return result;
   }

   public ItemStack removeItemNoUpdate(int index) {
      ItemStack stack = this.mob.getItemBySlot(SLOTS[index]);
      this.mob.setItemSlot(SLOTS[index], ItemStack.EMPTY);
      return stack;
   }

   public void setItem(int index, ItemStack stack) {
      this.mob.setItemSlot(SLOTS[index], stack);
   }

   public void setChanged() {
   }

   public boolean stillValid(Player player) {
      return this.mob.isAlive();
   }

   public void clearContent() {
      for (EquipmentSlot slot : SLOTS) {
         this.mob.setItemSlot(slot, ItemStack.EMPTY);
      }
   }
}
