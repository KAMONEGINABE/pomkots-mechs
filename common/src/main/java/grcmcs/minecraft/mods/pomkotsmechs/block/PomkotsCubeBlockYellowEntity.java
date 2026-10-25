package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PomkotsCubeBlockYellowEntity extends PomkotsCubeBlockEntity {
   public PomkotsCubeBlockYellowEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_YELLOW.get(), pos, state);
   }

   @Override
   public int getDefaultMode() {
      return 1;
   }
}
