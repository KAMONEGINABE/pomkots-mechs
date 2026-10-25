package grcmcs.minecraft.mods.pomkotsmechs.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.Nullable;

public class CommandExecutorBlock extends BaseEntityBlock implements PomkotsUnbreakableBlock {
   public CommandExecutorBlock() {
      super(Properties.m_284310_().m_60913_(-1.0F, 3600000.0F).m_60918_(SoundType.f_56743_));
   }

   public float m_5880_(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      return 0.0F;
   }

   public RenderShape m_7514_(BlockState state) {
      return RenderShape.MODEL;
   }

   @Nullable
   public BlockEntity m_142194_(BlockPos pos, BlockState state) {
      return new CommandExecutorBlockEntity(pos, state);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level, BlockState state, BlockEntityType<T> type) {
      return level.f_46443_ ? null : (lvl, pos, st, be) -> {
         if (be instanceof CommandExecutorBlockEntity spawner) {
            CommandExecutorBlockEntity.tick(lvl, pos, st, spawner);
         }
      };
   }
}
