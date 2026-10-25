package grcmcs.minecraft.mods.pomkotsmechs.block.arena;

import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsUnbreakableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class ArenaControllerBlock extends BaseEntityBlock implements PomkotsUnbreakableBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.f_54117_;

   public ArenaControllerBlock() {
      super(Properties.m_284310_().m_60913_(-1.0F, 3600000.0F).m_60918_(SoundType.f_56743_));
      this.m_49959_((BlockState)((BlockState)this.f_49792_.m_61090_()).m_61124_(FACING, Direction.NORTH));
   }

   public float m_5880_(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      return 0.0F;
   }

   protected void m_7926_(Builder<Block, BlockState> builder) {
      builder.m_61104_(new Property[]{FACING});
   }

   public BlockState m_5573_(BlockPlaceContext context) {
      return (BlockState)this.m_49966_().m_61124_(FACING, context.m_8125_().m_122424_());
   }

   public RenderShape m_7514_(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level, BlockState state, BlockEntityType<T> type) {
      return level.f_46443_ ? null : (lvl, pos, st, be) -> {
         if (be instanceof ArenaControllerBlockEntity spawner) {
            ArenaControllerBlockEntity.tick(lvl, pos, st, spawner);
         }
      };
   }

   public BlockEntity m_142194_(BlockPos pos, BlockState state) {
      return new ArenaControllerBlockEntity(pos, state);
   }
}
