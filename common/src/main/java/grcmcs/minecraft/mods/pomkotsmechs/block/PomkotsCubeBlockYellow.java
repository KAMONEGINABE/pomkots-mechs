package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class PomkotsCubeBlockYellow extends PomkotsCubeBlock {
   public PomkotsCubeBlockYellow() {
      super(Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.METAL), PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_YELLOW::get);
   }

   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
      return level.isClientSide
         ? createTickerHelper(blockEntityType, this.blockEntityType(), ChestBlockEntity::lidAnimateTick)
         : createTickerHelper(blockEntityType, (BlockEntityType)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_YELLOW.get(), PomkotsCubeBlockEntity::serverTick);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      PomkotsCubeBlockYellowEntity ent = new PomkotsCubeBlockYellowEntity(pos, state);
      ent.mode = 1;
      return ent;
   }
}
