package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.PartsWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PartsWorkbenchBlockEntity extends BlockEntity implements MenuProvider, Container {
   private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);

   public PartsWorkbenchBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.PARTS_WORKBENCH_BLOCK_ENTITY.get(), pos, state);
   }

   public Component getDisplayName() {
      return Component.literal("Parts Workbench");
   }

   public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
      return new PartsWorkbenchMenu(id, playerInv, this);
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      ContainerHelper.saveAllItems(tag, this.items);
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      ContainerHelper.loadAllItems(tag, this.items);
   }

   public int getContainerSize() {
      return this.items.size();
   }

   public boolean isEmpty() {
      return this.items.stream().allMatch(ItemStack::isEmpty);
   }

   public ItemStack getItem(int index) {
      return (ItemStack)this.items.get(index);
   }

   public ItemStack removeItem(int index, int count) {
      return ContainerHelper.removeItem(this.items, index, count);
   }

   public ItemStack removeItemNoUpdate(int index) {
      return ContainerHelper.takeItem(this.items, index);
   }

   public void setItem(int index, ItemStack stack) {
      this.items.set(index, stack);
      if (stack.getCount() > this.getMaxStackSize()) {
         stack.setCount(this.getMaxStackSize());
      }

      this.setChanged();
   }

   public boolean stillValid(Player player) {
      return player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) <= 64.0;
   }

   public void clearContent() {
      this.items.clear();
   }
}
