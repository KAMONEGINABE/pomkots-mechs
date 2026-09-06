package grcmcs.minecraft.mods.pomkotsmechs.block.elevator;

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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public final class HorizontalGateControllerBlock extends BaseEntityBlock implements PomkotsUnbreakableBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public HorizontalGateControllerBlock() {
        super(Properties.of().strength(-1.0F, 3_600_000.0F).sound(SoundType.METAL));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) { return 0.0F; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HorizontalGateControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (currentLevel, pos, currentState, blockEntity) -> {
            if (blockEntity instanceof HorizontalGateControllerBlockEntity controller) {
                HorizontalGateControllerBlockEntity.serverTick(currentLevel, pos, controller);
            }
        };
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block sourceBlock, BlockPos sourcePos, boolean moving) {
        super.neighborChanged(state, level, pos, sourceBlock, sourcePos, moving);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof HorizontalGateControllerBlockEntity controller) {
            controller.updateRedstoneSignal();
        }
    }
}
