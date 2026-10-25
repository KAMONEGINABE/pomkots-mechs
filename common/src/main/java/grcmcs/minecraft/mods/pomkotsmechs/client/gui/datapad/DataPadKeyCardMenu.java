package grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.KeycardItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DataPadKeyCardMenu extends AbstractContainerMenu {
   private final Container keyCardContainer;
   private final ItemStack datapad;

   public DataPadKeyCardMenu(int containerId, Inventory playerInventory) {
      this(containerId, playerInventory, (ItemStack)null);
   }

   public DataPadKeyCardMenu(int containerId, Inventory playerInventory, ItemStack datapad) {
      super((MenuType)PomkotsMechs.POMKOTS_DATAPAD_KEYCARD_GUI.get(), containerId);
      this.datapad = datapad;
      if (this.datapad != null) {
         this.keyCardContainer = PomkotsDatapadItem.createKeyCardContainer(datapad);
      } else {
         this.keyCardContainer = new SimpleContainer(8);
      }

      this.addDataPadInventory(this.keyCardContainer);
      this.addPlayerInventory(playerInventory);
   }

   private void addDataPadInventory(Container container) {
      int offsetX = 61;
      int offsetY = 70;
      int slot = 0;

      for (int row = 0; row < container.m_6643_(); row++) {
         this.m_38897_(new Slot(container, row, offsetX, offsetY + row * 18) {
            public boolean m_5857_(ItemStack stack) {
               return stack.m_41720_() instanceof KeycardItem;
            }
         });
      }
   }

   private void addPlayerInventory(Inventory inventory) {
      int offsetX = 270;
      int offsetY = 66;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            if (inventory.m_8020_(col + row * 9 + 9).m_150930_((Item)PomkotsMechs.POMKOTS_DATAPAD_ITEM.get())) {
               this.m_38897_(new Slot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18) {
                  public boolean m_8010_(Player player) {
                     return false;
                  }
               });
            } else {
               this.m_38897_(new Slot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18));
            }
         }
      }

      for (int i = 0; i < 9; i++) {
         if (inventory.m_8020_(i).m_150930_((Item)PomkotsMechs.POMKOTS_DATAPAD_ITEM.get())) {
            this.m_38897_(new Slot(inventory, i, offsetX + i * 18, offsetY + 54 + 4) {
               public boolean m_8010_(Player player) {
                  return false;
               }
            });
         } else {
            this.m_38897_(new Slot(inventory, i, offsetX + i * 18, offsetY + 54 + 4));
         }
      }
   }

   public ItemStack m_7648_(Player player, int index) {
      ItemStack result = ItemStack.f_41583_;
      Slot slot = (Slot)this.f_38839_.get(index);
      if (!slot.m_6657_()) {
         return ItemStack.f_41583_;
      }

      ItemStack stack = slot.m_7993_();
      result = stack.m_41777_();
      if (index < 8) {
         if (!this.m_38903_(stack, 8, this.f_38839_.size(), true)) {
            return ItemStack.f_41583_;
         }
      } else {
         if (!(stack.m_41720_() instanceof KeycardItem)) {
            return ItemStack.f_41583_;
         }

         if (!this.m_38903_(stack, 0, 8, false)) {
            return ItemStack.f_41583_;
         }
      }

      if (stack.m_41619_()) {
         slot.m_5852_(ItemStack.f_41583_);
      } else {
         slot.m_6654_();
      }

      return result;
   }

   public void m_6877_(Player player) {
      super.m_6877_(player);
      if (!player.m_9236_().m_5776_()) {
         PomkotsDatapadItem.saveKeyCardContainer(this.datapad, this.keyCardContainer);
      }
   }

   public boolean m_6875_(Player player) {
      return true;
   }
}
