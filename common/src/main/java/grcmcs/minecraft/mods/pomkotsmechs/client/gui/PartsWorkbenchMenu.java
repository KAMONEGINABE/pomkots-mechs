package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
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
      this.m_38897_(new PartsWorkbenchMenu.ConditionalSlot(this.container, 0, 10, 142, () -> this.currentTab == PartsWorkbenchMenu.Tab.CRAFT) {
         public boolean m_5857_(ItemStack stack) {
            return false;
         }
      });
      this.m_38897_(new PartsWorkbenchMenu.ConditionalSlot(this.container, 1, 10, 142, () -> this.currentTab == PartsWorkbenchMenu.Tab.UPGRADE));
      int offsetX = 284;
      int offsetY = 45;

      for (int row = 0; row < 6; row++) {
         for (int col = 0; col < 6; col++) {
            this.m_38897_(new PartsWorkbenchMenu.ConditionalSlot(playerInv, col + row * 6, offsetX + col * 18, offsetY + row * 18, () -> true));
         }
      }
   }

   public void craftItem(Item target, ServerPlayer player) {
      if (target instanceof BasePartsItem parts && this.m_38853_(0).m_7993_().m_41619_()) {
         if (!this.m_38853_(0).m_7993_().m_41619_()) {
            this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.01}");
            return;
         }

         PomkotsDataPack.PartsData partsData = getPartsData(parts);
         if (partsData == null || partsData.recipes.isEmpty() || partsData.recipes.get(0).isEmpty()) {
            this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.02}");
            return;
         }

         if (isCraftable(player.m_150109_(), partsData.recipes.get(0))) {
            ItemStack crafted = new ItemStack(parts);
            parts.setLevel(crafted, 1);
            consumeMaterials(player.m_150109_(), partsData.recipes.get(0));
            this.m_38853_(0).m_5852_(crafted);
         } else {
            this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.03}");
         }
      }
   }

   public void sendMessage(ServerPlayer player, String msg) {
      player.m_213846_(Utils.string2Component(msg));
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
      ItemStack inputItemStack = this.m_38853_(1).m_7993_();
      if (inputItemStack.m_41619_()) {
         this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.04}");
      } else if (inputItemStack.m_41720_() instanceof BasePartsItem parts) {
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
         if (isCraftable(player.m_150109_(), recipe)) {
            ItemStack newItemStack = inputItemStack.m_41777_();
            parts.setLevel(newItemStack, curLevel + 1);
            consumeMaterials(player.m_150109_(), recipe);
            this.m_38853_(1).m_7993_().m_41774_(1);
            this.m_38853_(1).m_5852_(newItemStack);
         } else {
            this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.03}");
         }
      } else {
         this.sendMessage(player, "{text.pomkotsmechs.messages.partsworkbench.07}");
      }
   }

   public ItemStack m_7648_(Player player, int i) {
      return ItemStack.f_41583_;
   }

   public boolean m_6875_(Player player) {
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

   public static boolean isCraftable(Inventory inv, List<PomkotsDataPack.SerializablePair<String, Integer>> recipe) {
      for (PomkotsDataPack.SerializablePair<String, Integer> entry : recipe) {
         ResourceLocation id = new ResourceLocation(entry.getFirst());
         Item requiredItem = (Item)BuiltInRegistries.f_257033_.m_7745_(id);
         if (requiredItem == Items.f_41852_) {
            return false;
         }

         if (inv.m_18947_(requiredItem) < entry.getSecond()) {
            return false;
         }
      }

      return true;
   }

   public static boolean consumeMaterials(Inventory inv, List<PomkotsDataPack.SerializablePair<String, Integer>> recipe) {
      for (PomkotsDataPack.SerializablePair<String, Integer> entry : recipe) {
         ResourceLocation id = new ResourceLocation(entry.getFirst());
         Item requiredItem = (Item)BuiltInRegistries.f_257033_.m_7745_(id);
         if (requiredItem == Items.f_41852_) {
            return false;
         }

         int remaining = entry.getSecond();

         for (int i = 0; i < inv.m_6643_() && remaining > 0; i++) {
            ItemStack stack = inv.m_8020_(i);
            if (stack.m_150930_(requiredItem)) {
               int take = Math.min(stack.m_41613_(), remaining);
               stack.m_41774_(take);
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

      public boolean m_8010_(Player player) {
         return this.activeCondition.get() && super.m_8010_(player);
      }

      public boolean m_6659_() {
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
