package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class PlaceHolderBlock extends BaseEntityBlock {
   public PlaceHolderBlock() {
      super(Properties.of().strength(2.5F).sound(SoundType.METAL));
   }

   public RenderShape getRenderShape(BlockState state) {
      return RenderShape.MODEL;
   }

   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
      return level.isClientSide() ? null : (lvl, pos, blockState, be) -> {
         if (!lvl.getGameRules().getBoolean(PomkotsMechs.RULE_MAP_EDIT)) {
            lvl.removeBlock(pos, false);
         }
      };
   }

   public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
      return new PlaceHolderBlockEntity(pos, state);
   }
}
