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

   public void saveAdditional(@NotNull CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putBoolean(PomkotsMechs.nbtName("PlaceHolderDummy"), true);
   }

   public void load(@NotNull CompoundTag tag) {
      super.load(tag);
      this.dummyFlag = tag.getBoolean(PomkotsMechs.nbtName("PlaceHolderDummy"));
   }
}
