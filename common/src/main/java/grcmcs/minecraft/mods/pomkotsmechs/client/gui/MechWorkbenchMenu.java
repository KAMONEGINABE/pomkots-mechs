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
   private final DataSlot inventoryRevision = DataSlot.m_39401_();
   private MechWorkbenchBlockEntity accessor = null;
   private boolean openedFromDatapad = false;
   private Pmvc01Entity mech;
   private Runnable inventoryChangedListener;
   private NonNullList<ItemStack> lastSlots = NonNullList.m_122779_();
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
      this.entityId = (MechWorkbenchMenu.ShortDataSlot)this.m_38895_(this.entityId);
      this.textureColor = (MechWorkbenchMenu.ShortDataSlot)this.m_38895_(this.textureColor);
      this.menuMode = (MechWorkbenchMenu.ShortDataSlot)this.m_38895_(this.menuMode);
      this.openedFromDatapad = openedFromDataPad;
      this.m_38895_(this.inventoryRevision);
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
            this.m_38897_(new Slot(mechInventory, col + row * 9 + 27, var15 + col * 18, var17 + row * 18));
         }
      }

      var17 = 153;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.m_38897_(new Slot(playerInventory, col + row * 9 + 9, var15 + col * 18, var17 + row * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.m_38897_(new Slot(playerInventory, i, var15 + i * 18, var17 + 54 + 4));
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
            this.m_38897_(
               new MechWorkbenchMenu.MechCircuitSlot(
                  mechInventory, col + row * 3 + 63, var15 + col * 18, var19 + row * 19, mode, false, true, col == 2, pref, item -> true
               )
            );
         }
      }

      for (Slot slot : this.f_38839_) {
         this.lastSlots.add(slot.m_7993_().m_41777_());
      }

      this.mech = mech;
      this.accessor = consoleAccessor;
   }

   private void addSlotInternal(int index, int offsetX, int offsetY, Container mechInventory, Predicate<Item> pred, int mode, boolean isModifiableOnViewMode) {
      Vec2 pos = getItemPosition(index, offsetX, offsetY);
      this.m_38897_(new MechWorkbenchMenu.ViewOnlySlot(mechInventory, index, (int)pos.f_82470_, (int)pos.f_82471_, this));
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
      this.m_38897_(
         new MechWorkbenchMenu.MechPartsCustomSlot(mechInventory, index, (int)pos.f_82470_, (int)pos.f_82471_, mode, isModifiableOnViewMode, true, pred)
      );
   }

   protected static Vec2 getItemPositionAmmo(int slotNum, int offsetX, int offsetY) {
      int col = slotNum % 6;
      int row = slotNum / 6;
      return new Vec2(offsetX + col * 18, offsetY + row * 18);
   }

   public ItemStack m_7648_(Player player, int index) {
      ItemStack result = ItemStack.f_41583_;
      Slot slot = (Slot)this.f_38839_.get(index);
      if (slot != null && slot.m_6657_()) {
         ItemStack stack = slot.m_7993_();
         result = stack.m_41777_();
         if (index >= 18 && index < 54) {
            if (!this.quickMoveCircuitToMatchingSlot(stack) && !this.m_38903_(stack, 54, 90, false)) {
               return ItemStack.f_41583_;
            }
         } else if (index >= 54 && index < 90) {
            if (!this.quickMoveCircuitToMatchingSlot(stack) && !this.m_38903_(stack, 18, 54, false)) {
               return ItemStack.f_41583_;
            }
         } else if (!this.m_38903_(stack, 54, 90, false)) {
            return ItemStack.f_41583_;
         }

         if (stack.m_41619_()) {
            slot.m_5852_(ItemStack.f_41583_);
         } else {
            slot.m_6654_();
         }

         return result;
      } else {
         return ItemStack.f_41583_;
      }
   }

   private boolean quickMoveCircuitToMatchingSlot(ItemStack stack) {
      if (!(stack.m_41720_() instanceof CircuitItem)) {
         return false;
      }

      CircuitPrefix prefix = CircuitItemStackHelper.getPrefixOrDefault(stack);

      for (Slot slot : this.f_38839_) {
         if (slot instanceof MechWorkbenchMenu.CircuitPrefixSlot circuitSlot
            && circuitSlot.acceptsCircuitPrefix(prefix)
            && !slot.m_6657_()
            && slot.m_5857_(stack)) {
            ItemStack moving = stack.m_41620_(1);
            slot.m_5852_(moving);
            slot.m_6654_();
            return true;
         }
      }

      return false;
   }

   public boolean m_6875_(Player player) {
      return true;
   }

   public void m_6877_(Player player) {
      super.m_6877_(player);
   }

   public short getEntityId() {
      return this.entityId.getShort();
   }

   public void setEntityId(int id) {
      this.m_7511_(0, (short)(id & 65535));
   }

   public short getTextureColor() {
      return this.textureColor.getShort();
   }

   public void setTextureColor(int id) {
      this.m_7511_(1, id);
   }

   public short getMode() {
      return this.menuMode.getShort();
   }

   public void setMode(short id) {
      this.m_7511_(2, id);
   }

   public void startMechRepair(Pmvc01Entity mech) {
      if (this.accessor != null && mech != null) {
         this.accessor.startMechRepair(mech);
      }
   }

   public void setActivationForCircuitSlots(boolean flag) {
      for (int i = 90; i < 108; i++) {
         if (this.m_38853_(i) instanceof MechWorkbenchMenu.MechPartsCustomSlot t) {
            t.setActive(flag);
         }
      }
   }

   public void setInventoryChangedListener(Runnable listener) {
      this.inventoryChangedListener = listener;
   }

   public void m_38946_() {
      super.m_38946_();
      boolean changed = false;

      for (int i = 0; i < this.f_38839_.size(); i++) {
         ItemStack current = ((Slot)this.f_38839_.get(i)).m_7993_();
         ItemStack last = (ItemStack)this.lastSlots.get(i);
         if (!ItemStack.m_41728_(current, last)) {
            this.lastSlots.set(i, current.m_41777_());
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
      if (sourceSlotIdx >= this.f_38839_.size()) {
         return false;
      }

      if (this.isVallidPartsIndex(mechSlotIdx) && mechSlotIdx < this.f_38839_.size()) {
         Slot sourceSlot = this.m_38853_(sourceSlotIdx);
         if (!sourceSlot.m_6657_()) {
            return false;
         }

         ItemStack source = sourceSlot.m_7993_();
         Slot mechInvSlot = this.m_38853_(mechSlotIdx);
         ItemStack equipped = mechInvSlot.m_7993_();
         mechInvSlot.m_5852_(source.m_41777_());
         sourceSlot.m_5852_(equipped);
         sourceSlot.m_6654_();
         mechInvSlot.m_6654_();
         this.m_38946_();
         return true;
      } else {
         return false;
      }
   }

   public boolean unequipPart(int mechSlot) {
      Slot slot = this.m_38853_(mechSlot);
      if (!slot.m_6657_()) {
         return false;
      }

      ItemStack equipped = slot.m_7993_();
      boolean moved = this.m_38903_(equipped, 27, this.f_38839_.size(), false);
      if (!moved) {
         return false;
      }

      if (equipped.m_41619_()) {
         slot.m_5852_(ItemStack.f_41583_);
      }

      slot.m_6654_();
      this.m_38946_();
      return true;
   }

   public void autoSupplyAll() {
      this.autoSupplyWeaponAmmo(6);
      this.autoSupplyWeaponAmmo(7);
      this.autoSupplyWeaponAmmo(8);
      this.autoSupplyWeaponAmmo(9);
      this.autoSupplySimpleItem(16, stack -> stack.m_41720_() instanceof BasePartsItem.Fuel);
      if (this.getMode() == MODE_ASSEMBLE) {
         this.autoSupplySimpleItem(17, stack -> stack.m_41720_() instanceof RepairKitItem);
      }

      this.m_38946_();
   }

   private void autoSupplyWeaponAmmo(int weaponSlotIndex) {
      Slot weaponSlot = this.m_38853_(weaponSlotIndex);
      if (weaponSlot.m_6657_()) {
         ItemStack weaponStack = weaponSlot.m_7993_();
         if (weaponStack.m_41720_() instanceof BasePartsItem.Weapon weapon) {
            int var6 = this.getAmmoSlotIdx(weaponSlotIndex);
            this.fillTargetSlot(var6, stack -> stack.m_41720_() instanceof BasePartsItem.Magazine mag ? weapon.isMatchAmmo(mag) : false);
         }
      }
   }

   private void autoSupplySimpleItem(int targetSlot, Predicate<ItemStack> matcher) {
      this.fillTargetSlot(targetSlot, matcher);
   }

   private void fillTargetSlot(int targetSlotIndex, Predicate<ItemStack> matcher) {
      Slot targetSlot = this.m_38853_(targetSlotIndex);
      ItemStack target = targetSlot.m_7993_();
      boolean targetInvalid = !target.m_41619_() && !matcher.test(target);
      int firstValidSlot = -1;
      int maxStack = 64;

      for (int i = 0; i < this.f_38839_.size(); i++) {
         if (i >= 27 && i != targetSlotIndex) {
            Slot slot = this.m_38853_(i);
            if (slot.m_6657_()) {
               ItemStack stack = slot.m_7993_();
               if (matcher.test(stack)) {
                  firstValidSlot = i;
                  maxStack = stack.m_41741_();
                  break;
               }
            }
         }
      }

      if (targetInvalid) {
         if (firstValidSlot < 0) {
            return;
         }

         Slot sourceSlot = this.m_38853_(firstValidSlot);
         ItemStack source = sourceSlot.m_7993_();
         sourceSlot.m_5852_(target);
         targetSlot.m_5852_(source);
         sourceSlot.m_6654_();
         targetSlot.m_6654_();
         target = source;
      }

      if (firstValidSlot >= 0 || !target.m_41619_()) {
         if (!target.m_41619_()) {
            maxStack = target.m_41741_();
         }

         int currentCount = target.m_41619_() ? 0 : target.m_41613_();
         int needed = maxStack - currentCount;
         if (needed > 0) {
            for (int i = 0; i < this.f_38839_.size(); i++) {
               if (i >= 27 && i != targetSlotIndex) {
                  Slot sourceSlot = this.m_38853_(i);
                  if (sourceSlot.m_6657_()) {
                     ItemStack source = sourceSlot.m_7993_();
                     if (matcher.test(source)) {
                        if (target.m_41619_()) {
                           int move = Math.min(needed, source.m_41613_());
                           ItemStack moved = source.m_41777_();
                           moved.m_41764_(move);
                           targetSlot.m_5852_(moved);
                           source.m_41774_(move);
                           target = moved;
                           maxStack = moved.m_41741_();
                           needed = maxStack - target.m_41613_();
                        } else {
                           if (!ItemStack.m_150942_(target, source)) {
                              continue;
                           }

                           int move = Math.min(needed, source.m_41613_());
                           target.m_41769_(move);
                           source.m_41774_(move);
                           needed -= move;
                        }

                        if (source.m_41619_()) {
                           sourceSlot.m_5852_(ItemStack.f_41583_);
                        }

                        sourceSlot.m_6654_();
                        targetSlot.m_6654_();
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
      Slot weaponSlot = this.m_38853_(mechWeaponSlot);
      if (weaponSlot.m_6657_()) {
         if (this.isVallidWeaponIndex(mechWeaponSlot)) {
            ItemStack weaponStack = weaponSlot.m_7993_();
            if (weaponStack.m_41720_() instanceof BasePartsItem.Weapon weapon) {
               int var12 = -1;
               int bestCount = -1;

               for (int ammoSlotIdx = 0; ammoSlotIdx < this.f_38839_.size(); ammoSlotIdx++) {
                  if (ammoSlotIdx >= 27) {
                     Slot slot = this.m_38853_(ammoSlotIdx);
                     if (slot.m_6657_()) {
                        ItemStack stack = slot.m_7993_();
                        if (stack.m_41720_() instanceof BasePartsItem.Magazine mag && weapon.isMatchAmmo(mag) && stack.m_41613_() > bestCount) {
                           bestCount = stack.m_41613_();
                           var12 = ammoSlotIdx;
                        }
                     }
                  }
               }

               if (var12 >= 0) {
                  int ammoSlotIdx = this.getAmmoSlotIdx(mechWeaponSlot);
                  Slot sourceAmmoSlot = this.m_38853_(var12);
                  Slot mechAmmoSlot = this.m_38853_(ammoSlotIdx);
                  ItemStack sourceAmmo = sourceAmmoSlot.m_7993_();
                  ItemStack equippedAmmo = mechAmmoSlot.m_7993_();
                  mechAmmoSlot.m_5852_(sourceAmmo.m_41777_());
                  sourceAmmoSlot.m_5852_(equippedAmmo);
                  sourceAmmoSlot.m_6654_();
                  mechAmmoSlot.m_6654_();
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

   public void m_6199_(Container container) {
      super.m_6199_(container);
      this.updateInventoryRevision();
   }

   private void updateInventoryRevision() {
      this.inventoryRevision.m_6422_(this.inventoryRevision.m_6501_() + 1);
   }

   public int getInventoryRevision() {
      return this.inventoryRevision.m_6501_();
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
      public boolean m_5857_(ItemStack stack) {
         if (!super.m_5857_(stack) || this.isDisablePlace()) {
            return false;
         }

         if (!(stack.m_41720_() instanceof CircuitItem)) {
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

      public boolean m_5857_(ItemStack stack) {
         return this.mode == 1 ? true : stack.m_41720_() instanceof BasePartsItem.Fuel || stack.m_41720_() instanceof BasePartsItem.Magazine;
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

      public boolean m_6659_() {
         return this.isActive;
      }

      public void setActive(boolean flag) {
         this.isActive = flag;
      }

      @Override
      public boolean m_5857_(ItemStack stack) {
         if (super.m_5857_(stack)) {
            Item item = stack.m_41720_();
            return this.p.test(item);
         } else {
            return false;
         }
      }

      public boolean m_8010_(Player player) {
         return this.isModifiableOnViewMode || this.mode == MechWorkbenchMenu.MODE_ASSEMBLE;
      }
   }

   public static class MechPartsCustomSlotSingle extends MechWorkbenchMenu.MechPartsCustomSlot {
      public MechPartsCustomSlotSingle(
         Container container, int index, int x, int y, int mode, boolean isModifiableOnViewMode, boolean isActive, Predicate<Item> p
      ) {
         super(container, index, x, y, mode, isModifiableOnViewMode, isActive, p);
      }

      public int m_6641_() {
         return 1;
      }

      public int m_5866_(ItemStack stack) {
         return 1;
      }
   }

   public static class ShortDataSlot extends DataSlot {
      private short value;

      public int m_6501_() {
         return this.value & 65535;
      }

      public void m_6422_(int value) {
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

      public int m_6501_() {
         return this.val;
      }

      public void m_6422_(int i) {
         this.val = i;
      }
   }

   public static class ViewOnlySlot extends Slot {
      MechWorkbenchMenu menu;

      public ViewOnlySlot(Container container, int i, int j, int k, MechWorkbenchMenu menu) {
         super(container, i, j, k);
         this.menu = menu;
      }

      public boolean m_6659_() {
         return false;
      }

      public boolean m_5857_(ItemStack stack) {
         return false;
      }

      public boolean m_8010_(Player player) {
         return false;
      }
   }
}
