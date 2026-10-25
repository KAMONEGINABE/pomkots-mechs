package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

public class PomkotsCubeBlock extends ChestBlock implements PomkotsUnbreakableBlock {
   public PomkotsCubeBlock() {
      this(Properties.m_284310_().m_60913_(-1.0F, 3600000.0F).m_60918_(SoundType.f_56743_), PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY::get);
   }

   public PomkotsCubeBlock(Properties properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier) {
      super(properties, supplier);
   }

   public float m_5880_(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      return 0.0F;
   }

   public BlockState m_5573_(BlockPlaceContext arg) {
      ChestType chesttype = ChestType.SINGLE;
      Direction direction = arg.m_8125_().m_122424_();
      FluidState fluidstate = arg.m_43725_().m_6425_(arg.m_8083_());
      boolean flag = arg.m_7078_();
      Direction direction1 = arg.m_43719_();
      if (direction1.m_122434_().m_122479_() && flag) {
         Direction direction2 = this.candidatePartnerFacing(arg, direction1.m_122424_());
         if (direction2 != null && direction2.m_122434_() != direction1.m_122434_()) {
            direction = direction2;
            chesttype = direction2.m_122428_() == direction1.m_122424_() ? ChestType.RIGHT : ChestType.LEFT;
         }
      }

      if (chesttype == ChestType.SINGLE && !flag) {
         if (direction == this.candidatePartnerFacing(arg, direction.m_122427_())) {
            chesttype = ChestType.LEFT;
         } else if (direction == this.candidatePartnerFacing(arg, direction.m_122428_())) {
            chesttype = ChestType.RIGHT;
         }
      }

      return (BlockState)((BlockState)((BlockState)this.m_49966_().m_61124_(f_51478_, direction)).m_61124_(f_51479_, chesttype))
         .m_61124_(f_51480_, fluidstate.m_76152_() == Fluids.f_76193_);
   }

   private Direction candidatePartnerFacing(BlockPlaceContext arg, Direction arg2) {
      return null;
   }

   public BlockEntity m_142194_(BlockPos pos, BlockState state) {
      return new PomkotsCubeBlockEntity(pos, state);
   }

   public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
      return level.f_46443_
         ? m_152132_(blockEntityType, this.m_153066_(), ChestBlockEntity::m_155343_)
         : m_152132_(blockEntityType, (BlockEntityType)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY.get(), PomkotsCubeBlockEntity::serverTick);
   }

   public InteractionResult m_6227_(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (level.m_7702_(pos) instanceof PomkotsCubeBlockEntity cubeBe) {
         if (player.m_6144_()) {
            if (!level.f_46443_) {
               cubeBe.incrementMode();
            }

            return InteractionResult.SUCCESS;
         } else {
            switch (cubeBe.getMode()) {
               case 0:
                  cubeBe.openBox(player);
                  return super.m_6227_(state, level, pos, player, hand, hit);
               default:
                  cubeBe.openBox(player);
                  return InteractionResult.CONSUME;
            }
         }
      } else {
         return InteractionResult.FAIL;
      }
   }

   public RenderShape m_7514_(BlockState state) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }
}
