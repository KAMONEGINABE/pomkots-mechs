package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.PomkotsSpannerItem;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MechWorkbenchBlock extends HorizontalDirectionalBlock implements EntityBlock, PomkotsUnbreakableBlock {
   protected static final VoxelShape SHAPE = Block.m_49796_(0.1, 0.1, 0.1, 15.9, 15.9, 15.9);

   public MechWorkbenchBlock() {
      super(Properties.m_284310_().m_60913_(100.0F, 3600000.0F).m_60918_(SoundType.f_56743_));
      this.m_49959_((BlockState)((BlockState)this.f_49792_.m_61090_()).m_61124_(BlockStateProperties.f_61374_, Direction.NORTH));
   }

   public float m_5880_(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      return 0.0F;
   }

   public BlockState m_5573_(BlockPlaceContext context) {
      return (BlockState)this.m_49966_().m_61124_(BlockStateProperties.f_61374_, context.m_8125_().m_122424_());
   }

   public BlockEntity m_142194_(BlockPos pos, BlockState state) {
      return new MechWorkbenchBlockEntity(pos, state);
   }

   public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
      return level.f_46443_ ? null : (lvl, pos, st, be) -> {
         if (be instanceof MechWorkbenchBlockEntity station) {
            MechWorkbenchBlockEntity.serverTick(lvl, pos, st, station);
         }
      };
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

   public InteractionResult m_6227_(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      ItemStack held = player.m_21120_(hand);
      if (held.m_41720_() instanceof PomkotsSpannerItem) {
         return InteractionResult.PASS;
      }

      if (!level.f_46443_) {
         List<Pmvc01Entity> mechs = this.getMechsBehindWorkbench(level, pos, state);
         if (!mechs.isEmpty()) {
            if (level.m_7702_(pos) instanceof MechWorkbenchBlockEntity mwbe) {
               Pmvc01Entity targetMech = mechs.get(0);
               if (targetMech.isLocked(player)) {
                  return InteractionResult.FAIL;
               }

               targetMech.setConsoleAccessor(mwbe);
               targetMech.m_213583_(player);
            }

            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.SUCCESS;
   }

   public List<Pmvc01Entity> getMechsBehindWorkbench(Level level, BlockPos pos, BlockState state) {
      Direction facing = (Direction)state.m_61143_(BlockStateProperties.f_61374_);
      AABB searchBox = getBackArea(pos, facing);
      return level.m_45976_(Pmvc01Entity.class, searchBox);
   }

   private static AABB getBackArea(BlockPos pos, Direction facing) {
      double x = pos.m_123341_();
      double y = pos.m_123342_();
      double z = pos.m_123343_();
      switch (facing) {
         case NORTH:
            return new AABB(x - 5.0, y - 3.0, z, x + 5.0, y + 3.0, z + 10.0);
         case SOUTH:
            return new AABB(x - 5.0, y - 3.0, z - 10.0, x + 5.0, y + 3.0, z);
         case WEST:
            return new AABB(x, y - 3.0, z - 5.0, x + 10.0, y + 3.0, z + 5.0);
         case EAST:
            return new AABB(x - 10.0, y - 3.0, z - 5.0, x, y + 3.0, z + 5.0);
         default:
            return new AABB(pos);
      }
   }
}
