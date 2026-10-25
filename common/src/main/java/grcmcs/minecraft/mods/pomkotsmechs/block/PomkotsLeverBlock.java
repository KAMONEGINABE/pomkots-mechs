package grcmcs.minecraft.mods.pomkotsmechs.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PomkotsLeverBlock extends BaseEntityBlock {
   protected static final VoxelShape SHAPE = Block.m_49796_(0.1, 0.1, 0.1, 15.9, 15.9, 15.9);

   public PomkotsLeverBlock() {
      super(Properties.m_284310_().m_60913_(-1.0F, 3600000.0F).m_60918_(SoundType.f_56743_));
      this.m_49959_((BlockState)((BlockState)this.f_49792_.m_61090_()).m_61124_(BlockStateProperties.f_61374_, Direction.NORTH));
   }

   public float m_5880_(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      return 0.0F;
   }

   protected void m_7926_(Builder<Block, BlockState> builder) {
      builder.m_61104_(new Property[]{BlockStateProperties.f_61374_});
   }

   public BlockState m_5573_(BlockPlaceContext context) {
      return (BlockState)this.m_49966_().m_61124_(BlockStateProperties.f_61374_, context.m_8125_().m_122424_());
   }

   public BlockEntity m_142194_(BlockPos pos, BlockState state) {
      return new PomkotsLeverBlockEntity(pos, state);
   }

   public InteractionResult m_6227_(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (level.m_7702_(pos) instanceof PomkotsLeverBlockEntity le) {
         if (level.f_46443_) {
            le.playAnimation();
            return InteractionResult.CONSUME;
         } else {
            le.use((ServerLevel)level, pos);
            return InteractionResult.CONSUME;
         }
      } else {
         return InteractionResult.FAIL;
      }
   }

   public VoxelShape m_5940_(BlockState bs, BlockGetter bg, BlockPos bp, CollisionContext cc) {
      return SHAPE;
   }

   public RenderShape m_7514_(BlockState state) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }
}
