package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.MechTraderEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.TraderInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MechTraderMenu extends AbstractContainerMenu {
   private final MechTraderEntity trader;
   private MechWorkbenchMenu.ShortDataSlot entityId = new MechWorkbenchMenu.ShortDataSlot();

   public MechTraderMenu(int containerId, Inventory playerInventory) {
      this(containerId, playerInventory, null);
   }

   public MechTraderMenu(int containerId, Inventory playerInventory, MechTraderEntity trader) {
      super((MenuType)PomkotsMechs.MECH_TRADER_GUI.get(), containerId);
      this.trader = trader;
      this.addTraderSlots(trader);
      this.addPlayerInventory(playerInventory);
      this.entityId = (MechWorkbenchMenu.ShortDataSlot)this.m_38895_(this.entityId);
   }

   public short getEntityId() {
      return this.entityId.getShort();
   }

   public void setEntityId(int id) {
      this.m_7511_(0, (short)(id & 65535));
   }

   private void addTraderSlots(MechTraderEntity trader) {
      Container traderInv;
      if (trader == null) {
         traderInv = new TraderInventory();
      } else {
         traderInv = trader.getInventory();
      }

      int offsetX = 30;
      int offsetY = 53;

      for (MechTraderEntity.TraderCategory category : MechTraderEntity.TraderCategory.values()) {
         int col = category.ordinal();

         for (int row = 0; row < 8; row++) {
            int offerSlot = TraderInventory.getOfferSlot(category, row);
            int priceSlot = TraderInventory.getPriceSlot(category, row);
            int y = offsetY + row * 22;
            int offerX = offsetX + col * 65;
            int priceX = offerX + 30;
            this.m_38897_(new Slot(traderInv, offerSlot, offerX, y) {
               public boolean m_5857_(ItemStack stack) {
                  return false;
               }

               public boolean m_8010_(Player player) {
                  return false;
               }

               public boolean m_6659_() {
                  return !this.m_7993_().m_41619_();
               }
            });
            this.m_38897_(new Slot(traderInv, priceSlot, priceX, y) {
               public boolean m_5857_(ItemStack stack) {
                  return false;
               }

               public boolean m_8010_(Player player) {
                  return false;
               }

               public boolean m_6659_() {
                  return !this.m_7993_().m_41619_();
               }
            });
         }
      }
   }

   private void addPlayerInventory(Inventory inventory) {
      int offsetX = 312;
      int offsetY = 53;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.m_38897_(new Slot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.m_38897_(new Slot(inventory, i, offsetX + i * 18, offsetY + 54 + 4));
      }
   }

   public ItemStack m_7648_(Player player, int i) {
      return ItemStack.f_41583_;
   }

   public boolean m_6875_(Player player) {
      return this.trader.m_6084_() && this.trader.m_20270_(player) < 8.0F && !this.trader.isLeaving();
   }

   public boolean m_6366_(Player player, int id) {
      if (player instanceof ServerPlayer sp) {
         this.buy(sp, id);
      }

      return true;
   }

   public void buy(ServerPlayer player, int slot) {
      if (!player.m_9236_().m_5776_() && this.trader != null) {
         this.trader.buy(player, slot);
      }
   }
}
