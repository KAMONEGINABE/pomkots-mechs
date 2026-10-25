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

public class PomkotsCubeBlockRed extends PomkotsCubeBlock {
   public PomkotsCubeBlockRed() {
      super(Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.METAL), PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_RED::get);
   }

   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
      return level.isClientSide
         ? createTickerHelper(blockEntityType, this.blockEntityType(), ChestBlockEntity::lidAnimateTick)
         : createTickerHelper(blockEntityType, (BlockEntityType)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_RED.get(), PomkotsCubeBlockEntity::serverTick);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      PomkotsCubeBlockRedEntity ent = new PomkotsCubeBlockRedEntity(pos, state);
      ent.mode = 2;
      return ent;
   }
}
