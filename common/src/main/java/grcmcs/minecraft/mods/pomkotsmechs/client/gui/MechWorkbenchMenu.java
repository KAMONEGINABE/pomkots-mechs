package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.MechWorkbenchBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.RepairKitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItemStackHelper;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitPrefix;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

public class MechWorkbenchMenu extends AbstractContainerMenu {
   public static short MODE_VIEW = 0;
   public static short MODE_ASSEMBLE = 1;
   private static final int MECH_START = 18;
   private static final int MECH_END = 54;
   private static final int PLAYER_START = 54;
   private static final int PLAYER_END = 90;
   private static final int CIRCUIT_START = 90;
   private static final int CIRCUIT_END = 108;
   private MechWorkbenchMenu.ShortDataSlot entityId = new MechWorkbenchMenu.ShortDataSlot();
   private MechWorkbenchMenu.ShortDataSlot textureColor = new MechWorkbenchMenu.ShortDataSlot();
   private MechWorkbenchMenu.ShortDataSlot menuMode = new MechWorkbenchMenu.ShortDataSlot();
   private final DataSlot inventoryRevision = DataSlot.standalone();
   private MechWorkbenchBlockEntity accessor = null;
   private boolean openedFromDatapad = false;
   private Pmvc01Entity mech;
   private Runnable inventoryChangedListener;
   private NonNullList<ItemStack> lastSlots = NonNullList.create();
   public static final int CMD_EQUIP = 1;
   public static final int CMD_UNEQUIP = 0;

   public MechWorkbenchMenu(int id, Inventory playerInventory) {
      this(id, playerInventory, new SimpleContainer(82), null, 0, null);
   }

   public MechWorkbenchMenu(int id, Inventory playerInventory, Container mechInventory, Pmvc01Entity mech, int mode, MechWorkbenchBlockEntity consoleAccessor) {
      this(id, playerInventory, mechInventory, mech, mode, consoleAccessor, false);
   }

   public MechWorkbenchMenu(
      int id,
      Inventory playerInventory,
      Container mechInventory,
      Pmvc01Entity mech,
      int mode,
      MechWorkbenchBlockEntity consoleAccessor,
      boolean openedFromDataPad
   ) {
      super((MenuType)PomkotsMechs.MECH_WORKBENCH_GUI.get(), id);
      this.entityId = (MechWorkbenchMenu.ShortDataSlot)this.addDataSlot(this.entityId);
      this.textureColor = (MechWorkbenchMenu.ShortDataSlot)this.addDataSlot(this.textureColor);
      this.menuMode = (MechWorkbenchMenu.ShortDataSlot)this.addDataSlot(this.menuMode);
      this.openedFromDatapad = openedFromDataPad;
      this.addDataSlot(this.inventoryRevision);
      int offsetX = 136;
      int offsetY = 210;
      this.addSlotInternal(0, offsetX, offsetY, mechInventory, item -> item instanceof BasePartsItem.Head, mode, false);
      this.addSlotInternal(1, offsetX, offsetY, mechInventory, item -> item instanceof BasePartsItem.Body, mode, false);
      this.addSlotInternal(2, offsetX, offsetY, mechInventory, item -> item instanceof BasePartsItem.Arm, mode, false);
      this.addSlotInternal(3, offsetX, offsetY, mechInventory, item -> item instanceof BasePartsItem.Legs, mode, false);
      this.addSlotInternal(4, offsetX, offsetY, mechInventory, item -> item instanceof BasePartsItem.Generator, mode, false);
      this.addSlotInternal(5, offsetX, offsetY, mechInventory, item -> item instanceof BasePartsItem.Booster, mode, false);
      this.addSlotInternal(6, offsetX, offsetY, mechInventory, item -> item instanceof BasePartsItem.WeaponArm, mode, false);
      this.addSlotInternal(7, offsetX, offsetY, mechInventory, item -> item instanceof BasePartsItem.WeaponArm, mode, false);
      this.addSlotInternal(8, offsetX, offsetY, mechInventory, item -> item instanceof BasePartsItem.WeaponShoulder, mode, false);
      this.addSlotInternal(9, offsetX, offsetY, mechInventory, item -> item instanceof BasePartsItem.WeaponShoulder, mode, false);
      this.addSlotInternal(10, offsetX, offsetY, mechInventory, item -> item instanceof BasePartsItem.Extension, mode, false);
      this.addSlotInternal(11, offsetX, offsetY, mechInventory, item -> item instanceof BasePartsItem.Extension, mode, false);
      int offsetAmmoX = 9;
      int offsetAmmoY = 197;
      this.addSlotInternalAmmo(12, offsetAmmoX, offsetAmmoY, mechInventory, item -> item instanceof BasePartsItem.Magazine, mode, true);
      this.addSlotInternalAmmo(13, offsetAmmoX, offsetAmmoY, mechInventory, item -> item instanceof BasePartsItem.Magazine, mode, true);
      this.addSlotInternalAmmo(14, offsetAmmoX, offsetAmmoY, mechInventory, item -> item instanceof BasePartsItem.Magazine, mode, true);
      this.addSlotInternalAmmo(15, offsetAmmoX, offsetAmmoY, mechInventory, item -> item instanceof BasePartsItem.Magazine, mode, true);
      this.addSlotInternalAmmo(16, offsetAmmoX, offsetAmmoY, mechInventory, item -> item instanceof BasePartsItem.Fuel, mode, true);
      this.addSlotInternalAmmo(17, offsetAmmoX, offsetAmmoY, mechInventory, item -> item instanceof RepairKitItem, mode, false);
      int var15 = 312;
      int var17 = 53;

      for (int row = 0; row < 4; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(mechInventory, col + row * 9 + 27, var15 + col * 18, var17 + row * 18));
         }
      }

      var17 = 153;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col + row * 9 + 9, var15 + col * 18, var17 + row * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.addSlot(new Slot(playerInventory, i, var15 + i * 18, var17 + 54 + 4));
      }

      var15 = 228;
      int var19 = 53;

      for (int row = 0; row < 6; row++) {
         CircuitPrefix pref = switch (row) {
            case 1 -> CircuitPrefix.OFFENCE;
            case 2 -> CircuitPrefix.DEFENCE;
            case 3 -> CircuitPrefix.MOBILITY;
            case 4 -> CircuitPrefix.ENERGY;
            case 5 -> CircuitPrefix.UTILITY;
            default -> CircuitPrefix.BALANCED;
         };

         for (int col = 0; col < 3; col++) {
            this.addSlot(
               new MechWorkbenchMenu.MechCircuitSlot(
                  mechInventory, col + row * 3 + 63, var15 + col * 18, var19 + row * 19, mode, false, true, col == 2, pref, item -> true
               )
            );
         }
      }

      for (Slot slot : this.slots) {
         this.lastSlots.add(slot.getItem().copy());
      }

      this.mech = mech;
      this.accessor = consoleAccessor;
   }

   private void addSlotInternal(int index, int offsetX, int offsetY, Container mechInventory, Predicate<Item> pred, int mode, boolean isModifiableOnViewMode) {
      Vec2 pos = getItemPosition(index, offsetX, offsetY);
      this.addSlot(new MechWorkbenchMenu.ViewOnlySlot(mechInventory, index, (int)pos.x, (int)pos.y, this));
   }

   protected static Vec2 getItemPosition(int slotNum, int offsetX, int offsetY) {
      int col = slotNum % 6;
      int row = slotNum / 6;
      return new Vec2(offsetX + col * 18, offsetY + row * 18);
   }

   private void addSlotInternalAmmo(
      int index, int offsetX, int offsetY, Container mechInventory, Predicate<Item> pred, int mode, boolean isModifiableOnViewMode
   ) {
      Vec2 pos = getItemPositionAmmo(index, offsetX, offsetY);
      this.addSlot(
         new MechWorkbenchMenu.MechPartsCustomSlot(mechInventory, index, (int)pos.x, (int)pos.y, mode, isModifiableOnViewMode, true, pred)
      );
   }

   protected static Vec2 getItemPositionAmmo(int slotNum, int offsetX, int offsetY) {
      int col = slotNum % 6;
      int row = slotNum / 6;
      return new Vec2(offsetX + col * 18, offsetY + row * 18);
   }

   public ItemStack quickMoveStack(Player player, int index) {
      ItemStack result = ItemStack.EMPTY;
      Slot slot = (Slot)this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack stack = slot.getItem();
         result = stack.copy();
         if (index >= 18 && index < 54) {
            if (!this.quickMoveCircuitToMatchingSlot(stack) && !this.moveItemStackTo(stack, 54, 90, false)) {
               return ItemStack.EMPTY;
            }
         } else if (index >= 54 && index < 90) {
            if (!this.quickMoveCircuitToMatchingSlot(stack) && !this.moveItemStackTo(stack, 18, 54, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(stack, 54, 90, false)) {
            return ItemStack.EMPTY;
         }

         if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         return result;
      } else {
         return ItemStack.EMPTY;
      }
   }

   private boolean quickMoveCircuitToMatchingSlot(ItemStack stack) {
      if (!(stack.getItem() instanceof CircuitItem)) {
         return false;
      }

      CircuitPrefix prefix = CircuitItemStackHelper.getPrefixOrDefault(stack);

      for (Slot slot : this.slots) {
         if (slot instanceof MechWorkbenchMenu.CircuitPrefixSlot circuitSlot
            && circuitSlot.acceptsCircuitPrefix(prefix)
            && !slot.hasItem()
            && slot.mayPlace(stack)) {
            ItemStack moving = stack.split(1);
            slot.set(moving);
            slot.setChanged();
            return true;
         }
      }

      return false;
   }

   public boolean stillValid(Player player) {
      return true;
   }

   public void removed(Player player) {
      super.removed(player);
   }

   public short getEntityId() {
      return this.entityId.getShort();
   }

   public void setEntityId(int id) {
      this.setData(0, (short)(id & 65535));
   }

   public short getTextureColor() {
      return this.textureColor.getShort();
   }

   public void setTextureColor(int id) {
      this.setData(1, id);
   }

   public short getMode() {
      return this.menuMode.getShort();
   }

   public void setMode(short id) {
      this.setData(2, id);
   }

   public void startMechRepair(Pmvc01Entity mech) {
      if (this.accessor != null && mech != null) {
         this.accessor.startMechRepair(mech);
      }
   }

   public void setActivationForCircuitSlots(boolean flag) {
      for (int i = 90; i < 108; i++) {
         if (this.getSlot(i) instanceof MechWorkbenchMenu.MechPartsCustomSlot t) {
            t.setActive(flag);
         }
      }
   }

   public void setInventoryChangedListener(Runnable listener) {
      this.inventoryChangedListener = listener;
   }

   public void broadcastChanges() {
      super.broadcastChanges();
      boolean changed = false;

      for (int i = 0; i < this.slots.size(); i++) {
         ItemStack current = ((Slot)this.slots.get(i)).getItem();
         ItemStack last = (ItemStack)this.lastSlots.get(i);
         if (!ItemStack.matches(current, last)) {
            this.lastSlots.set(i, current.copy());
            changed = true;
         }
      }

      if (changed) {
         this.updateInventoryRevision();
      }
   }

   public void handlePartsChange(int command, int mechSlotIdx, int sourceSlotIdx) {
      if (command == 1) {
         this.equipParts(mechSlotIdx, sourceSlotIdx);
      } else {
         this.unequipPart(mechSlotIdx);
      }
   }

   public boolean equipParts(int mechSlotIdx, int sourceSlotIdx) {
      if (sourceSlotIdx >= this.slots.size()) {
         return false;
      }

      if (this.isVallidPartsIndex(mechSlotIdx) && mechSlotIdx < this.slots.size()) {
         Slot sourceSlot = this.getSlot(sourceSlotIdx);
         if (!sourceSlot.hasItem()) {
            return false;
         }

         ItemStack source = sourceSlot.getItem();
         Slot mechInvSlot = this.getSlot(mechSlotIdx);
         ItemStack equipped = mechInvSlot.getItem();
         mechInvSlot.set(source.copy());
         sourceSlot.set(equipped);
         sourceSlot.setChanged();
         mechInvSlot.setChanged();
         this.broadcastChanges();
         return true;
      } else {
         return false;
      }
   }

   public boolean unequipPart(int mechSlot) {
      Slot slot = this.getSlot(mechSlot);
      if (!slot.hasItem()) {
         return false;
      }

      ItemStack equipped = slot.getItem();
      boolean moved = this.moveItemStackTo(equipped, 27, this.slots.size(), false);
      if (!moved) {
         return false;
      }

      if (equipped.isEmpty()) {
         slot.set(ItemStack.EMPTY);
      }

      slot.setChanged();
      this.broadcastChanges();
      return true;
   }

   public void autoSupplyAll() {
      this.autoSupplyWeaponAmmo(6);
      this.autoSupplyWeaponAmmo(7);
      this.autoSupplyWeaponAmmo(8);
      this.autoSupplyWeaponAmmo(9);
      this.autoSupplySimpleItem(16, stack -> stack.getItem() instanceof BasePartsItem.Fuel);
      if (this.getMode() == MODE_ASSEMBLE) {
         this.autoSupplySimpleItem(17, stack -> stack.getItem() instanceof RepairKitItem);
      }

      this.broadcastChanges();
   }

   private void autoSupplyWeaponAmmo(int weaponSlotIndex) {
      Slot weaponSlot = this.getSlot(weaponSlotIndex);
      if (weaponSlot.hasItem()) {
         ItemStack weaponStack = weaponSlot.getItem();
         if (weaponStack.getItem() instanceof BasePartsItem.Weapon weapon) {
            int var6 = this.getAmmoSlotIdx(weaponSlotIndex);
            this.fillTargetSlot(var6, stack -> stack.getItem() instanceof BasePartsItem.Magazine mag ? weapon.isMatchAmmo(mag) : false);
         }
      }
   }

   private void autoSupplySimpleItem(int targetSlot, Predicate<ItemStack> matcher) {
      this.fillTargetSlot(targetSlot, matcher);
   }

   private void fillTargetSlot(int targetSlotIndex, Predicate<ItemStack> matcher) {
      Slot targetSlot = this.getSlot(targetSlotIndex);
      ItemStack target = targetSlot.getItem();
      boolean targetInvalid = !target.isEmpty() && !matcher.test(target);
      int firstValidSlot = -1;
      int maxStack = 64;

      for (int i = 0; i < this.slots.size(); i++) {
         if (i >= 27 && i != targetSlotIndex) {
            Slot slot = this.getSlot(i);
            if (slot.hasItem()) {
               ItemStack stack = slot.getItem();
               if (matcher.test(stack)) {
                  firstValidSlot = i;
                  maxStack = stack.getMaxStackSize();
                  break;
               }
            }
         }
      }

      if (targetInvalid) {
         if (firstValidSlot < 0) {
            return;
         }

         Slot sourceSlot = this.getSlot(firstValidSlot);
         ItemStack source = sourceSlot.getItem();
         sourceSlot.set(target);
         targetSlot.set(source);
         sourceSlot.setChanged();
         targetSlot.setChanged();
         target = source;
      }

      if (firstValidSlot >= 0 || !target.isEmpty()) {
         if (!target.isEmpty()) {
            maxStack = target.getMaxStackSize();
         }

         int currentCount = target.isEmpty() ? 0 : target.getCount();
         int needed = maxStack - currentCount;
         if (needed > 0) {
            for (int i = 0; i < this.slots.size(); i++) {
               if (i >= 27 && i != targetSlotIndex) {
                  Slot sourceSlot = this.getSlot(i);
                  if (sourceSlot.hasItem()) {
                     ItemStack source = sourceSlot.getItem();
                     if (matcher.test(source)) {
                        if (target.isEmpty()) {
                           int move = Math.min(needed, source.getCount());
                           ItemStack moved = source.copy();
                           moved.setCount(move);
                           targetSlot.set(moved);
                           source.shrink(move);
                           target = moved;
                           maxStack = moved.getMaxStackSize();
                           needed = maxStack - target.getCount();
                        } else {
                           if (!ItemStack.isSameItemSameTags(target, source)) {
                              continue;
                           }

                           int move = Math.min(needed, source.getCount());
                           target.grow(move);
                           source.shrink(move);
                           needed -= move;
                        }

                        if (source.isEmpty()) {
                           sourceSlot.set(ItemStack.EMPTY);
                        }

                        sourceSlot.setChanged();
                        targetSlot.setChanged();
                        if (needed <= 0) {
                           break;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void tryAutoLoadAmmo(int mechWeaponSlot) {
      Slot weaponSlot = this.getSlot(mechWeaponSlot);
      if (weaponSlot.hasItem()) {
         if (this.isVallidWeaponIndex(mechWeaponSlot)) {
            ItemStack weaponStack = weaponSlot.getItem();
            if (weaponStack.getItem() instanceof BasePartsItem.Weapon weapon) {
               int var12 = -1;
               int bestCount = -1;

               for (int ammoSlotIdx = 0; ammoSlotIdx < this.slots.size(); ammoSlotIdx++) {
                  if (ammoSlotIdx >= 27) {
                     Slot slot = this.getSlot(ammoSlotIdx);
                     if (slot.hasItem()) {
                        ItemStack stack = slot.getItem();
                        if (stack.getItem() instanceof BasePartsItem.Magazine mag && weapon.isMatchAmmo(mag) && stack.getCount() > bestCount) {
                           bestCount = stack.getCount();
                           var12 = ammoSlotIdx;
                        }
                     }
                  }
               }

               if (var12 >= 0) {
                  int ammoSlotIdx = this.getAmmoSlotIdx(mechWeaponSlot);
                  Slot sourceAmmoSlot = this.getSlot(var12);
                  Slot mechAmmoSlot = this.getSlot(ammoSlotIdx);
                  ItemStack sourceAmmo = sourceAmmoSlot.getItem();
                  ItemStack equippedAmmo = mechAmmoSlot.getItem();
                  mechAmmoSlot.set(sourceAmmo.copy());
                  sourceAmmoSlot.set(equippedAmmo);
                  sourceAmmoSlot.setChanged();
                  mechAmmoSlot.setChanged();
               }
            }
         }
      }
   }

   private boolean isVallidPartsIndex(int index) {
      return index >= 0 && index <= 11;
   }

   private boolean isVallidWeaponIndex(int index) {
      return index >= 6 && index <= 9;
   }

   private int getAmmoSlotIdx(int weaponInvIndex) {
      return weaponInvIndex + 6;
   }

   public void slotsChanged(Container container) {
      super.slotsChanged(container);
      this.updateInventoryRevision();
   }

   private void updateInventoryRevision() {
      this.inventoryRevision.set(this.inventoryRevision.get() + 1);
   }

   public int getInventoryRevision() {
      return this.inventoryRevision.get();
   }

   public interface CircuitPrefixSlot {
      CircuitPrefix acceptedPrefix();

      default boolean acceptsCircuitPrefix(CircuitPrefix prefix) {
         return this.acceptedPrefix().id().equals(prefix.id());
      }
   }

   public class MechCircuitSlot extends MechWorkbenchMenu.MechPartsCustomSlotSingle implements MechWorkbenchMenu.CircuitPrefixSlot {
      private final CircuitPrefix acceptedPrefix;
      private final boolean disablePlace;

      public MechCircuitSlot(
         Container container,
         int index,
         int x,
         int y,
         int mode,
         boolean isModifiableOnViewMode,
         boolean isActive,
         boolean disablePlace,
         CircuitPrefix acceptedPrefix,
         Predicate<Item> p
      ) {
         super(container, index, x, y, mode, isModifiableOnViewMode, isActive, p);
         this.acceptedPrefix = acceptedPrefix;
         this.disablePlace = disablePlace;
      }

      @Override
      public CircuitPrefix acceptedPrefix() {
         return this.acceptedPrefix;
      }

      public boolean isDisablePlace() {
         return this.disablePlace;
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
         if (!super.mayPlace(stack) || this.isDisablePlace()) {
            return false;
         }

         if (!(stack.getItem() instanceof CircuitItem)) {
            return false;
         }

         CircuitPrefix stackPrefix = CircuitItemStackHelper.getPrefixOrDefault(stack);
         return this.acceptedPrefix.id().equals(stackPrefix.id());
      }
   }

   private static class MechCustomSlot extends Slot {
      protected final int mode;

      public MechCustomSlot(Container container, int index, int x, int y, int mode) {
         super(container, index, x, y);
         this.mode = mode;
      }

      public boolean mayPlace(ItemStack stack) {
         return this.mode == 1 ? true : stack.getItem() instanceof BasePartsItem.Fuel || stack.getItem() instanceof BasePartsItem.Magazine;
      }
   }

   public static class MechPartsCustomSlot extends MechWorkbenchMenu.MechCustomSlot {
      private final Predicate<Item> p;
      private final boolean isModifiableOnViewMode;
      private boolean isActive;

      public MechPartsCustomSlot(Container container, int index, int x, int y, int mode, boolean isModifiableOnViewMode, boolean isActive, Predicate<Item> p) {
         super(container, index, x, y, mode);
         this.p = p;
         this.isModifiableOnViewMode = isModifiableOnViewMode;
         this.isActive = isActive;
      }

      public boolean isActive() {
         return this.isActive;
      }

      public void setActive(boolean flag) {
         this.isActive = flag;
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
         if (super.mayPlace(stack)) {
            Item item = stack.getItem();
            return this.p.test(item);
         } else {
            return false;
         }
      }

      public boolean mayPickup(Player player) {
         return this.isModifiableOnViewMode || this.mode == MechWorkbenchMenu.MODE_ASSEMBLE;
      }
   }

   public static class MechPartsCustomSlotSingle extends MechWorkbenchMenu.MechPartsCustomSlot {
      public MechPartsCustomSlotSingle(
         Container container, int index, int x, int y, int mode, boolean isModifiableOnViewMode, boolean isActive, Predicate<Item> p
      ) {
         super(container, index, x, y, mode, isModifiableOnViewMode, isActive, p);
      }

      public int getMaxStackSize() {
         return 1;
      }

      public int getMaxStackSize(ItemStack stack) {
         return 1;
      }
   }

   public static class ShortDataSlot extends DataSlot {
      private short value;

      public int get() {
         return this.value & 65535;
      }

      public void set(int value) {
         this.value = (short)value;
      }

      public short getShort() {
         return this.value;
      }

      public void setShort(short value) {
         this.value = value;
      }
   }

   private static class SimpleDataSlot extends DataSlot {
      private int val = 0;

      public int get() {
         return this.val;
      }

      public void set(int i) {
         this.val = i;
      }
   }

   public static class ViewOnlySlot extends Slot {
      MechWorkbenchMenu menu;

      public ViewOnlySlot(Container container, int i, int j, int k, MechWorkbenchMenu menu) {
         super(container, i, j, k);
         this.menu = menu;
      }

      public boolean isActive() {
         return false;
      }

      public boolean mayPlace(ItemStack stack) {
         return false;
      }

      public boolean mayPickup(Player player) {
         return false;
      }
   }
}
