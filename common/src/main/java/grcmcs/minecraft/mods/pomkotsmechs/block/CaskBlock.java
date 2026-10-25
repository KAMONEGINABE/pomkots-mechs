package grcmcs.minecraft.mods.pomkotsmechs.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CaskBlock extends HorizontalDirectionalBlock {
   protected static final VoxelShape SHAPE = Block.m_49796_(0.1, 0.1, 0.1, 15.9, 15.9, 15.9);

   public CaskBlock() {
      super(Properties.m_284310_().m_60978_(2.5F).m_60918_(SoundType.f_56743_));
      this.m_49959_((BlockState)((BlockState)this.f_49792_.m_61090_()).m_61124_(BlockStateProperties.f_61374_, Direction.NORTH));
   }

   public BlockState m_5573_(BlockPlaceContext context) {
      return (BlockState)this.m_49966_().m_61124_(BlockStateProperties.f_61374_, context.m_8125_().m_122424_());
   }

   public RenderShape m_7514_(BlockState state) {
      return RenderShape.MODEL;
   }

   public VoxelShape m_5940_(BlockState bs, BlockGetter bg, BlockPos bp, CollisionContext cc) {
      return SHAPE;
   }

   protected void m_7926_(Builder<Block, BlockState> builder) {
      builder.m_61104_(new Property[]{BlockStateProperties.f_61374_});
   }
}
