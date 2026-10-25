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
      super(Properties.m_284310_().m_60913_(-1.0F, 3600000.0F).m_60918_(SoundType.f_56743_), PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_RED::get);
   }

   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
      return level.f_46443_
         ? m_152132_(blockEntityType, this.m_153066_(), ChestBlockEntity::m_155343_)
         : m_152132_(blockEntityType, (BlockEntityType)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_RED.get(), PomkotsCubeBlockEntity::serverTick);
   }

   @Override
   public BlockEntity m_142194_(BlockPos pos, BlockState state) {
      PomkotsCubeBlockRedEntity ent = new PomkotsCubeBlockRedEntity(pos, state);
      ent.mode = 2;
      return ent;
   }
}
