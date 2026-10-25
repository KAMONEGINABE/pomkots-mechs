package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class PlaceHolderBlockEntity extends BlockEntity {
   private boolean dummyFlag = false;

   public PlaceHolderBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.PLACE_HOLDER_BLOCK_ENTITY.get(), pos, state);
   }

   public void m_183515_(@NotNull CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128379_(PomkotsMechs.nbtName("PlaceHolderDummy"), true);
   }

   public void m_142466_(@NotNull CompoundTag tag) {
      super.m_142466_(tag);
      this.dummyFlag = tag.m_128471_(PomkotsMechs.nbtName("PlaceHolderDummy"));
   }
}
