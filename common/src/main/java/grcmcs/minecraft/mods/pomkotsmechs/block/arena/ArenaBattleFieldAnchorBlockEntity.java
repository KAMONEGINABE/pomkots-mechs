package grcmcs.minecraft.mods.pomkotsmechs.block.arena;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ArenaBattleFieldAnchorBlockEntity extends BlockEntity {
   public ArenaBattleFieldAnchorBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.ARENA_BATTLEFIELD_ANCHOR_BLOCK_ENTITY.get(), pos, state);
   }
}
