package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PartsWorkbenchMenu extends AbstractContainerMenu {
   private PartsWorkbenchMenu.Tab currentTab = PartsWorkbenchMenu.Tab.CRAFT;
   private final Container container;

   public PartsWorkbenchMenu(int id, Inventory playerInv) {
      this(id, playerInv, new SimpleContainer(4));
   }

   public PartsWorkbenchMenu(int id, Inventory playerInv, Container inventory) {
      super((MenuType)PomkotsMechs.PARTS_WORKBENCH_GUI.get(), id);
      this.container = inventory;
      this.setupSlotsForCurrentTab(playerInv);
   }

   private void setupSlotsForCurrentTab(Inventory playerInv) {
      this.addSlot(new PartsWorkbenchMenu.ConditionalSlot(this.container, 0, 10, 142, () -> this.currentTab == PartsWorkbenchMenu.Tab.CRAFT) {
         public boolean mayPlace(ItemStack stack) {
            return false;
         }
      });
      this.addSlot(new PartsWorkbenchMenu.ConditionalSlot(this.container, 1, 10, 142, () -> this.currentTab == PartsWorkbenchMenu.Tab.UPGRADE));
      int offsetX = 284;
      int offsetY = 45;

      for (int row = 0; row < 6; row++) {
         for (int col = 0; col < 6; col++) {
            this.addSlot(new PartsWorkbenchMenu.ConditionalSlot(playerInv, col + row * 6, offsetX + col * 18, offsetY + row * 18, () -> true));
         }
      }
   }

   public void craftItem(Item target, ServerPlayer player) {
      if (target instanceof BasePartsItem parts && this.getSlot(0).getItem().isEmpty()) {
         if (!this.getSlot(0).getItem().isEmpty()) {
            this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.01}");
            return;
         }

         PomkotsDataPack.PartsData partsData = getPartsData(parts);
         if (partsData == null || partsData.recipes.isEmpty() || partsData.recipes.get(0).isEmpty()) {
            this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.02}");
            return;
         }

         if (isCraftable(player.getInventory(), partsData.recipes.get(0))) {
            ItemStack crafted = new ItemStack(parts);
            parts.setLevel(crafted, 1);
            consumeMaterials(player.getInventory(), partsData.recipes.get(0));
            this.getSlot(0).set(crafted);
         } else {
            this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.03}");
         }
      }
   }

   public void sendMessage(ServerPlayer player, String msg) {
      player.sendSystemMessage(Utils.string2Component(msg));
   }

   public void setTab(PartsWorkbenchMenu.Tab tab) {
      if (tab != this.currentTab) {
         this.currentTab = tab;
      }
   }

   public PartsWorkbenchMenu.Tab getCurrentTab() {
      return this.currentTab;
   }

   public void upgradeParts(ServerPlayer player) {
      ItemStack inputItemStack = this.getSlot(1).getItem();
      if (inputItemStack.isEmpty()) {
         this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.04}");
      } else if (inputItemStack.getItem() instanceof BasePartsItem parts) {
         PomkotsDataPack.PartsData partsData = getPartsData(parts);
         int curLevel = parts.getLevel(inputItemStack);
         if (partsData == null) {
            this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.05}");
            return;
         }

         if (parts.getMaxLevel() <= curLevel) {
            this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.06}");
            return;
         }

         if (partsData.recipes.isEmpty() || partsData.recipes.size() < curLevel || partsData.recipes.get(curLevel).isEmpty()) {
            this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.02}");
            return;
         }

         List<PomkotsDataPack.SerializablePair<String, Integer>> recipe = partsData.recipes.get(curLevel);
         if (isCraftable(player.getInventory(), recipe)) {
            ItemStack newItemStack = inputItemStack.copy();
            parts.setLevel(newItemStack, curLevel + 1);
            consumeMaterials(player.getInventory(), recipe);
            this.getSlot(1).getItem().shrink(1);
            this.getSlot(1).set(newItemStack);
         } else {
            this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.03}");
         }
      } else {
         this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.07}");
      }
   }

   public ItemStack quickMoveStack(Player player, int i) {
      return ItemStack.EMPTY;
   }

   public boolean stillValid(Player player) {
      return true;
   }

   public static PartsWorkbenchMenu.Tab getTab(int id) {
      PartsWorkbenchMenu.Tab[] types = PartsWorkbenchMenu.Tab.values();

      for (PartsWorkbenchMenu.Tab tab : types) {
         if (tab.getInt() == id) {
            return tab;
         }
      }

      return null;
   }

   public static PomkotsDataPack.PartsData getPartsData(BasePartsItem partsItem) {
      PomkotsDataPack dataPack = PomkotsDataPackManager.getInstance().getDataPack();
      return !(partsItem instanceof BasePartsItem.Arm)
            && !(partsItem instanceof BasePartsItem.Head)
            && !(partsItem instanceof BasePartsItem.Body)
            && !(partsItem instanceof BasePartsItem.Legs)
         ? dataPack.getPartsData(partsItem.getPartsSeriesName())
         : dataPack.getPartsData(partsItem.getPartsSeriesName() + partsItem.getPartsCategory().getString());
   }

   public static final TagKey<Item> FORGE_TITANIUM_INGOT = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ingots/titanium"));

   public static boolean isMatchingMaterial(ItemStack stack, Item requiredItem) {
      if (stack.is(requiredItem)) {
         return true;
      }
      if (requiredItem == PomkotsMechs.P_TITANIUM_INGOT.get()) {
         return stack.is(FORGE_TITANIUM_INGOT);
      }
      return false;
   }

   public static int countMatchingMaterial(Inventory inv, Item requiredItem) {
      int count = 0;
      for (int i = 0; i < inv.getContainerSize(); i++) {
         ItemStack stack = inv.getItem(i);
         if (isMatchingMaterial(stack, requiredItem)) {
            count += stack.getCount();
         }
      }
      return count;
   }

   public static boolean isCraftable(Inventory inv, List<PomkotsDataPack.SerializablePair<String, Integer>> recipe) {
      for (PomkotsDataPack.SerializablePair<String, Integer> entry : recipe) {
         ResourceLocation id = new ResourceLocation(entry.getFirst());
         Item requiredItem = (Item)BuiltInRegistries.ITEM.get(id);
         if (requiredItem == Items.AIR) {
            return false;
         }

         if (countMatchingMaterial(inv, requiredItem) < entry.getSecond()) {
            return false;
         }
      }

      return true;
   }

   public static boolean consumeMaterials(Inventory inv, List<PomkotsDataPack.SerializablePair<String, Integer>> recipe) {
      for (PomkotsDataPack.SerializablePair<String, Integer> entry : recipe) {
         ResourceLocation id = new ResourceLocation(entry.getFirst());
         Item requiredItem = (Item)BuiltInRegistries.ITEM.get(id);
         if (requiredItem == Items.AIR) {
            return false;
         }

         int remaining = entry.getSecond();

         for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (isMatchingMaterial(stack, requiredItem)) {
               int take = Math.min(stack.getCount(), remaining);
               stack.shrink(take);
               remaining -= take;
            }
         }
      }

      return true;
   }

   class ConditionalSlot extends Slot {
      private final Supplier<Boolean> activeCondition;

      public ConditionalSlot(Container container, int index, int x, int y, Supplier<Boolean> condition) {
         super(container, index, x, y);
         this.activeCondition = condition;
      }

      public boolean mayPickup(Player player) {
         return this.activeCondition.get() && super.mayPickup(player);
      }

      public boolean isActive() {
         return this.activeCondition.get();
      }
   }

   public enum Tab {
      CRAFT(0),
      UPGRADE(1);

      private final int id;

      Tab(final int id) {
         this.id = id;
      }

      public int getInt() {
         return this.id;
      }
   }
}
