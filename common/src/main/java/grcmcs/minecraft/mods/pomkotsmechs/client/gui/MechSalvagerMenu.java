package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.MechSalvagerBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MechSalvagerMenu extends AbstractContainerMenu {
   private final Container inventory;
   private final ContainerLevelAccess access;
   private final SimpleContainer dataPadContainer = new SimpleContainer(1);

   public MechSalvagerMenu(int id, Inventory playerInv) {
      this(id, playerInv, new SimpleContainer(2), null);
   }

   public MechSalvagerMenu(int id, Inventory playerInv, Container inventory, ContainerLevelAccess access) {
      super((MenuType)PomkotsMechs.MECH_SALVAGER_GUI.get(), id);
      this.inventory = inventory;
      this.access = access;
      this.addSlot(new Slot(this.dataPadContainer, 0, 0, 0) {
         public boolean isActive() {
            return false;
         }
      });
      if (playerInv.player instanceof ServerPlayer sp) {
         List<ItemStack> dataPads = PomkotsDatapadItem.getPlayersDataPads(sp);
         ItemStack pad;
         if (dataPads.isEmpty()) {
            pad = ItemStack.EMPTY;
         } else {
            pad = dataPads.get(0);
         }

         this.getSlot(0).set(pad);
      }

      this.addPlayerInventory(playerInv);
   }

   public ItemStack getDataPad() {
      return this.getSlot(0).getItem();
   }

   private void addPlayerInventory(Inventory inventory) {
      int offsetX = 270;
      int offsetY = 66;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            if (inventory.getItem(col + row * 9 + 9).is((Item)PomkotsMechs.POMKOTS_DATAPAD_ITEM.get())) {
               this.addSlot(new Slot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18) {
                  public boolean mayPickup(Player player) {
                     return false;
                  }
               });
            } else {
               this.addSlot(new Slot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18));
            }
         }
      }

      for (int i = 0; i < 9; i++) {
         if (inventory.getItem(i).is((Item)PomkotsMechs.POMKOTS_DATAPAD_ITEM.get())) {
            this.addSlot(new Slot(inventory, i, offsetX + i * 18, offsetY + 54 + 4) {
               public boolean mayPickup(Player player) {
                  return false;
               }
            });
         } else {
            this.addSlot(new Slot(inventory, i, offsetX + i * 18, offsetY + 54 + 4));
         }
      }
   }

   public void summon(ServerPlayer player, UUID mechUuid) {
      if (this.inventory instanceof MechSalvagerBlockEntity msbe) {
         msbe.summon(player, mechUuid);
      }
   }

   public ItemStack quickMoveStack(Player player, int i) {
      return ItemStack.EMPTY;
   }

   public boolean stillValid(Player player) {
      return true;
   }
}
