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
      this.m_38897_(new Slot(this.dataPadContainer, 0, 0, 0) {
         public boolean m_6659_() {
            return false;
         }
      });
      if (playerInv.f_35978_ instanceof ServerPlayer sp) {
         List<ItemStack> dataPads = PomkotsDatapadItem.getPlayersDataPads(sp);
         ItemStack pad;
         if (dataPads.isEmpty()) {
            pad = ItemStack.f_41583_;
         } else {
            pad = dataPads.get(0);
         }

         this.m_38853_(0).m_5852_(pad);
      }

      this.addPlayerInventory(playerInv);
   }

   public ItemStack getDataPad() {
      return this.m_38853_(0).m_7993_();
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

   public void summon(ServerPlayer player, UUID mechUuid) {
      if (this.inventory instanceof MechSalvagerBlockEntity msbe) {
         msbe.summon(player, mechUuid);
      }
   }

   public ItemStack m_7648_(Player player, int i) {
      return ItemStack.f_41583_;
   }

   public boolean m_6875_(Player player) {
      return true;
   }
}
