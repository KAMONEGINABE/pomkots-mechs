package grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PilotMenuProvider implements MenuProvider {
   private final Mob mob;

   public PilotMenuProvider(Mob mob) {
      this.mob = mob;
   }

   public Component m_5446_() {
      return Component.m_237113_("Pilot Config");
   }

   public AbstractContainerMenu m_7208_(int containerId, Inventory inventory, Player player) {
      return new PilotMenu(containerId, inventory, this.mob);
   }
}
