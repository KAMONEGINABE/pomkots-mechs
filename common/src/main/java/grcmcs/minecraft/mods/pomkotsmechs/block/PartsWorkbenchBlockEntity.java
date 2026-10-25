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
   private final NonNullList<ItemStack> items = NonNullList.m_122780_(2, ItemStack.f_41583_);

   public PartsWorkbenchBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.PARTS_WORKBENCH_BLOCK_ENTITY.get(), pos, state);
   }

   public Component m_5446_() {
      return Component.m_237113_("Parts Workbench");
   }

   public AbstractContainerMenu m_7208_(int id, Inventory playerInv, Player player) {
      return new PartsWorkbenchMenu(id, playerInv, this);
   }

   protected void m_183515_(CompoundTag tag) {
      super.m_183515_(tag);
      ContainerHelper.m_18973_(tag, this.items);
   }

   public void m_142466_(CompoundTag tag) {
      super.m_142466_(tag);
      ContainerHelper.m_18980_(tag, this.items);
   }

   public int m_6643_() {
      return this.items.size();
   }

   public boolean m_7983_() {
      return this.items.stream().allMatch(ItemStack::m_41619_);
   }

   public ItemStack m_8020_(int index) {
      return (ItemStack)this.items.get(index);
   }

   public ItemStack m_7407_(int index, int count) {
      return ContainerHelper.m_18969_(this.items, index, count);
   }

   public ItemStack m_8016_(int index) {
      return ContainerHelper.m_18966_(this.items, index);
   }

   public void m_6836_(int index, ItemStack stack) {
      this.items.set(index, stack);
      if (stack.m_41613_() > this.m_6893_()) {
         stack.m_41764_(this.m_6893_());
      }

      this.m_6596_();
   }

   public boolean m_6542_(Player player) {
      return player.m_20238_(Vec3.m_82512_(this.f_58858_)) <= 64.0;
   }

   public void m_6211_() {
      this.items.clear();
   }
}
