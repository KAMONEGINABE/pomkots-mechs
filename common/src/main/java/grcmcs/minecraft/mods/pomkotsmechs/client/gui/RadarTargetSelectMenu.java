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
      ItemStack mainItem = inv.f_35978_.m_21205_();
      ItemStack offItem = inv.f_35978_.m_21206_();
      List<ItemStack> fromInv = PomkotsDatapadItem.getPlayersDataPads(inv.f_35978_);
      if (mainItem.m_41720_() instanceof PomkotsRadarItem || mainItem.m_41720_() instanceof PomkotsDatapadItem) {
         this.dataPadStack = mainItem;
      } else if (offItem.m_41720_() instanceof PomkotsRadarItem || offItem.m_41720_() instanceof PomkotsDatapadItem) {
         this.dataPadStack = offItem;
      } else if (!fromInv.isEmpty()) {
         this.dataPadStack = fromInv.get(0);
      } else {
         this.dataPadStack = ItemStack.f_41583_;
      }
   }

   public void setSelectedTarget(int idx) {
      ItemStack stack = this.getRadarStack();
      if (stack.m_41720_() instanceof PomkotsRadarItem) {
         PomkotsRadarItem.setSelectedIndex(stack, idx);
      } else if (stack.m_41720_() instanceof PomkotsDatapadItem) {
         PomkotsDatapadItem.setSelectedIndex(stack, idx);
      }
   }

   public ItemStack getRadarStack() {
      return this.dataPadStack;
   }

   public boolean m_6875_(Player player) {
      return this.stillValid;
   }

   public ItemStack m_7648_(Player player, int i) {
      return ItemStack.f_41583_;
   }

   public void m_6877_(Player player) {
      super.m_6877_(player);
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
         && this.getDatapadItemStackForMenu().m_41720_() instanceof PomkotsDatapadItem) {
         serverPlayer.f_8924_
            .execute(
               () -> serverPlayer.m_5893_(
                  new SimpleMenuProvider((id, inv, p) -> new DataPadMenu(id, inv, p, this.getDatapadItemStackForMenu()), Component.m_237113_("Pomkots Datapad"))
               )
            );
      }
   }
}
