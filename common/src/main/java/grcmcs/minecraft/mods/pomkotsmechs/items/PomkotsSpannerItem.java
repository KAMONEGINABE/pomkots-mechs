package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.MechSalvagerBlock;
import grcmcs.minecraft.mods.pomkotsmechs.block.MechWorkbenchBlock;
import grcmcs.minecraft.mods.pomkotsmechs.block.PartsWorkbenchBlock;
import grcmcs.minecraft.mods.pomkotsmechs.block.PersonalPomkotsCubeBlock;
import grcmcs.minecraft.mods.pomkotsmechs.block.PersonalPomkotsCubeBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PomkotsSpannerItem extends Item {
   public PomkotsSpannerItem(Properties properties) {
      super(properties);
   }

   @NotNull
   public InteractionResult m_6225_(UseOnContext ctx) {
      Level level = ctx.m_43725_();
      if (level.f_46443_) {
         return InteractionResult.SUCCESS;
      }

      BlockPos pos = ctx.m_8083_();
      BlockState state = level.m_8055_(pos);
      Player player = ctx.m_43723_();
      Block block = state.m_60734_();
      if (block instanceof PersonalPomkotsCubeBlock) {
         return this.retrievePersonalCube(ctx, level, pos, state, player);
      }

      if (!(block instanceof MechSalvagerBlock) && !(block instanceof MechWorkbenchBlock) && !(block instanceof PartsWorkbenchBlock)) {
         return InteractionResult.PASS;
      }

      if (level.m_7702_(pos) instanceof Container container) {
         Containers.m_19002_(level, pos, container);
      }

      Block.m_49840_(level, pos, new ItemStack(state.m_60734_()));
      level.m_7471_(pos, false);
      level.m_5594_(null, pos, SoundEvents.f_11664_, SoundSource.BLOCKS, 1.0F, 1.0F);
      if (player != null && !player.m_150110_().f_35937_) {
         ctx.m_43722_().m_41622_(1, player, p -> p.m_21190_(ctx.m_43724_()));
      }

      return InteractionResult.CONSUME;
   }

   private InteractionResult retrievePersonalCube(UseOnContext ctx, Level level, BlockPos pos, BlockState state, Player player) {
      if (level.m_7702_(pos) instanceof PersonalPomkotsCubeBlockEntity blockEntity) {
         if (player != null && blockEntity.isOwner(player)) {
            ItemStack retrievedCube = new ItemStack((ItemLike)PomkotsMechs.PERSONAL_POMKOTS_CUBE_BLOCK_ITEM.get());
            BlockItem.m_186338_(retrievedCube, (BlockEntityType)PomkotsMechs.PERSONAL_POMKOTS_CUBE_BLOCK_ENTITY.get(), blockEntity.m_187482_());
            blockEntity.m_6211_();
            Block.m_49840_(level, pos, retrievedCube);
            level.m_7471_(pos, false);
            level.m_5594_(null, pos, SoundEvents.f_11664_, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.m_150110_().f_35937_) {
               ctx.m_43722_().m_41622_(1, player, p -> p.m_21190_(ctx.m_43724_()));
            }

            return InteractionResult.CONSUME;
         } else {
            if (player != null) {
               player.m_213846_(Component.m_237115_("text.pomkotsmechs.messages.personal_pomkots_cube.not_owner"));
            }

            return InteractionResult.CONSUME;
         }
      } else {
         return InteractionResult.FAIL;
      }
   }

   public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.m_237115_("tooltip.pomkotsmechs.pomkots_spanner").m_130940_(ChatFormatting.GRAY));
      super.m_7373_(stack, level, tooltip, flag);
   }
}
