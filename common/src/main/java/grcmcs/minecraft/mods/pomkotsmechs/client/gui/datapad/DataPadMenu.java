package grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad;

import dev.architectury.registry.menu.MenuRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechWorkbenchMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.RadarTargetSelectMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaMenuData;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaReceptionistMenu;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaMatchData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DataPadMenu extends AbstractContainerMenu {
   private final SimpleContainer dataPadContainer = new SimpleContainer(1);
   public Player player;
   private boolean stillVallid = true;
   private ItemStack dataPadStack;

   public DataPadMenu(int containerId, Inventory playerInventory) {
      this(containerId, playerInventory, null, null);
   }

   public DataPadMenu(int containerId, Inventory playerInventory, Player p, ItemStack datapad) {
      super((MenuType)PomkotsMechs.POMKOTS_DATAPAD_GUI.get(), containerId);
      if (p == null) {
         this.player = playerInventory.f_35978_;
      } else {
         this.player = p;
      }

      this.dataPadStack = datapad;
      this.m_38897_(new Slot(this.dataPadContainer, 0, 0, 0) {
         public boolean m_6659_() {
            return false;
         }
      });
      if (this.player instanceof ServerPlayer sp && this.dataPadStack != null) {
         for (PomkotsDatapadItem.ArenaRequestInfo req : PomkotsDatapadItem.getArenaRequests(this.dataPadStack)) {
            ArenaMatchData match = ArenaManager.getActiveMatch(req.arenaId(), req.requestUuid(), sp.m_20194_());
            if (match == null) {
               PomkotsDatapadItem.removeArenaRequest(datapad, req.requestUuid());
            }
         }

         this.m_38853_(0).m_5852_(this.dataPadStack);
      }
   }

   public ItemStack getDataPad() {
      return this.m_38853_(0).m_7993_();
   }

   public boolean m_6875_(Player player) {
      return this.stillVallid;
   }

   public ItemStack m_7648_(Player player, int slot) {
      return ItemStack.f_41583_;
   }

   public boolean m_6366_(Player player, int id) {
      if (player instanceof ServerPlayer serverPlayer) {
         switch (id) {
            case 0:
               short mode = MechWorkbenchMenu.MODE_VIEW;
               if (serverPlayer.m_20202_() instanceof Pmvc01Entity mech) {
                  player.m_5893_(new SimpleMenuProvider((i, inventory, p) -> {
                     MechWorkbenchMenu mechMenu = new MechWorkbenchMenu(i, inventory, mech, mech, mode, null, true);
                     mechMenu.setEntityId(mech.m_20148_().hashCode());
                     mechMenu.setTextureColor(mech.getTextureColor());
                     mechMenu.setMode(mode);
                     mechMenu.m_150429_();
                     return mechMenu;
                  }, Component.m_237113_("Mech WorkBench")));
               }
               break;
            case 1:
               if (this.dataPadStack != null) {
                  player.m_5893_(
                     new SimpleMenuProvider((i, inv, p) -> new DataPadKeyCardMenu(i, inv, this.dataPadStack), Component.m_237113_("Pomkots Datapad"))
                  );
               }
               break;
            case 2:
               ArenaMenuData.Entry extendedData = new ArenaMenuData.Entry(
                  "default", 1, this.dataPadStack, ArenaManager.buildRanking(serverPlayer.f_8924_, "default")
               );
               MenuRegistry.openExtendedMenu(
                  serverPlayer,
                  new SimpleMenuProvider((i, container, p) -> new ArenaReceptionistMenu(i, container, extendedData), Component.m_237113_("Arena Reception")),
                  buf -> ArenaMenuData.write(buf, extendedData)
               );
               break;
            case 3:
               player.m_5893_(
                  new SimpleMenuProvider((i, container, p) -> new RadarTargetSelectMenu(i, container, p, true), Component.m_237113_("Pomkots Radar"))
               );
               break;
            case 4:
               serverPlayer.m_6915_();
               serverPlayer.f_8924_.execute(() -> serverPlayer.f_8924_.m_129892_().m_230957_(serverPlayer.m_20203_(), "ftbquests open_book"));
            case 5:
         }
      }

      return true;
   }

   private void sendMessage(String msg, Player player) {
      player.m_213846_(Component.m_237113_(msg));
   }

   public void m_6877_(Player player) {
      super.m_6877_(player);
   }
}
