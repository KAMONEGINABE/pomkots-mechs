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
      super(Properties.m_284310_().m_60978_(2.5F).m_60918_(SoundType.f_56743_));
   }

   public RenderShape m_7514_(BlockState state) {
      return RenderShape.MODEL;
   }

   public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level, BlockState state, BlockEntityType<T> type) {
      return level.m_5776_() ? null : (lvl, pos, blockState, be) -> {
         if (!lvl.m_46469_().m_46207_(PomkotsMechs.RULE_MAP_EDIT)) {
            lvl.m_7471_(pos, false);
         }
      };
   }

   public BlockEntity m_142194_(BlockPos pos, BlockState state) {
      return new PlaceHolderBlockEntity(pos, state);
   }
}
