package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadMenu;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.PomkotsRadarItem;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class RadarTargetSelectMenu extends AbstractContainerMenu {
   private ItemStack dataPadStack = null;
   private boolean openedFromDatapad = false;
   private boolean stillValid = true;

   public RadarTargetSelectMenu(int syncId, Inventory inv) {
      this(syncId, inv, null);
   }

   public RadarTargetSelectMenu(int syncId, Inventory inv, Player p) {
      this(syncId, inv, p, false);
   }

   public RadarTargetSelectMenu(int syncId, Inventory inv, Player p, boolean openedFromDatapad) {
      super((MenuType)PomkotsMechs.POMKOTS_RADAR_GUI.get(), syncId);
      this.openedFromDatapad = openedFromDatapad;
      ItemStack mainItem = inv.player.getMainHandItem();
      ItemStack offItem = inv.player.getOffhandItem();
      List<ItemStack> fromInv = PomkotsDatapadItem.getPlayersDataPads(inv.player);
      if (mainItem.getItem() instanceof PomkotsRadarItem || mainItem.getItem() instanceof PomkotsDatapadItem) {
         this.dataPadStack = mainItem;
      } else if (offItem.getItem() instanceof PomkotsRadarItem || offItem.getItem() instanceof PomkotsDatapadItem) {
         this.dataPadStack = offItem;
      } else if (!fromInv.isEmpty()) {
         this.dataPadStack = fromInv.get(0);
      } else {
         this.dataPadStack = ItemStack.EMPTY;
      }
   }

   public void setSelectedTarget(int idx) {
      ItemStack stack = this.getRadarStack();
      if (stack.getItem() instanceof PomkotsRadarItem) {
         PomkotsRadarItem.setSelectedIndex(stack, idx);
      } else if (stack.getItem() instanceof PomkotsDatapadItem) {
         PomkotsDatapadItem.setSelectedIndex(stack, idx);
      }
   }

   public ItemStack getRadarStack() {
      return this.dataPadStack;
   }

   public boolean stillValid(Player player) {
      return this.stillValid;
   }

   public ItemStack quickMoveStack(Player player, int i) {
      return ItemStack.EMPTY;
   }

   public void removed(Player player) {
      super.removed(player);
   }

   private boolean isOpenedFromDatapad() {
      return this.openedFromDatapad;
   }

   private ItemStack getDatapadItemStackForMenu() {
      return this.dataPadStack;
   }

   private void reopenDatapadMenu(Player player) {
      if (player instanceof ServerPlayer serverPlayer
         && this.isOpenedFromDatapad()
         && this.getDatapadItemStackForMenu() != null
         && this.getDatapadItemStackForMenu().getItem() instanceof PomkotsDatapadItem) {
         serverPlayer.server
            .execute(
               () -> serverPlayer.openMenu(
                  new SimpleMenuProvider((id, inv, p) -> new DataPadMenu(id, inv, p, this.getDatapadItemStackForMenu()), Component.literal("Pomkots Datapad"))
               )
            );
      }
   }
}
