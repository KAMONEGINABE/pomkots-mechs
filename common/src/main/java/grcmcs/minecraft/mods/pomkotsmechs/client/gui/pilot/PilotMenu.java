package grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.pilot.PilotLicenseItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.pilot.PilotRoleItem;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PilotMenu extends AbstractContainerMenu {
   private final Mob mob;

   public PilotMenu(int containerId, Inventory playerInventory) {
      this(containerId, playerInventory, null);
   }

   public PilotMenu(int containerId, Inventory playerInventory, Mob mob) {
      super((MenuType)PomkotsMechs.PILOT_CONFIG_GUI.get(), containerId);
      this.mob = mob;
      Container equipment = null;
      if (mob == null) {
         equipment = new SimpleContainer(6);
      } else {
         equipment = new MobEquipmentContainer(mob);
      }

      int offsetY = 50;
      int height = 18;
      this.addSlot(new Slot(equipment, 0, 70, offsetY + height * 2));
      this.addSlot(new Slot(equipment, 1, 70, offsetY + height * 3));
      this.addSlot(new Slot(equipment, 2, 70, offsetY + height * 4));
      this.addSlot(new Slot(equipment, 3, 70, offsetY + height * 5));
      this.addSlot(new Slot(equipment, 4, 70, offsetY) {
         public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof PilotLicenseItem;
         }
      });
      this.addSlot(new Slot(equipment, 5, 70, offsetY + height) {
         public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof PilotRoleItem;
         }
      });
      this.addPlayerInventory(playerInventory);
   }

   private void addPlayerInventory(Inventory inventory) {
      int offsetX = 262;
      int offsetY = 53;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.addSlot(new Slot(inventory, i, offsetX + i * 18, offsetY + 54 + 4));
      }
   }

   public ItemStack quickMoveStack(Player player, int i) {
      return ItemStack.EMPTY;
   }

   public boolean stillValid(Player player) {
      return this.mob != null && this.mob.isAlive();
   }

   public boolean clickMenuButton(Player player, int id) {
      if (player instanceof ServerPlayer sp && this.mob != null) {
         Pmvc01Entity mech = findNearestMech(this.mob);
         if (mech != null && !mech.isBroken()) {
            this.mob.startRiding(mech);
         }
      }

      return true;
   }

   private static Pmvc01Entity findNearestMech(Mob mob) {
      List<Pmvc01Entity> mechs = mob.level().getEntitiesOfClass(Pmvc01Entity.class, mob.getBoundingBox().inflate(32.0));
      double nearest = Double.MAX_VALUE;
      Pmvc01Entity result = null;

      for (Pmvc01Entity mech : mechs) {
         if (mech.getDrivingPassenger() == null) {
            double dist = mob.distanceToSqr(mech);
            if (dist < nearest) {
               nearest = dist;
               result = mech;
            }
         }
      }

      return result;
   }
}
