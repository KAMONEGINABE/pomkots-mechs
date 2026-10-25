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
   public InteractionResult useOn(UseOnContext ctx) {
      Level level = ctx.getLevel();
      if (level.isClientSide) {
         return InteractionResult.SUCCESS;
      }

      BlockPos pos = ctx.getClickedPos();
      BlockState state = level.getBlockState(pos);
      Player player = ctx.getPlayer();
      Block block = state.getBlock();
      if (block instanceof PersonalPomkotsCubeBlock) {
         return this.retrievePersonalCube(ctx, level, pos, state, player);
      }

      if (!(block instanceof MechSalvagerBlock) && !(block instanceof MechWorkbenchBlock) && !(block instanceof PartsWorkbenchBlock)) {
         return InteractionResult.PASS;
      }

      if (level.getBlockEntity(pos) instanceof Container container) {
         Containers.dropContents(level, pos, container);
      }

      Block.popResource(level, pos, new ItemStack(state.getBlock()));
      level.removeBlock(pos, false);
      level.playSound(null, pos, SoundEvents.ANVIL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
      if (player != null && !player.getAbilities().instabuild) {
         ctx.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));
      }

      return InteractionResult.CONSUME;
   }

   private InteractionResult retrievePersonalCube(UseOnContext ctx, Level level, BlockPos pos, BlockState state, Player player) {
      if (level.getBlockEntity(pos) instanceof PersonalPomkotsCubeBlockEntity blockEntity) {
         if (player != null && blockEntity.isOwner(player)) {
            ItemStack retrievedCube = new ItemStack((ItemLike)PomkotsMechs.PERSONAL_POMKOTS_CUBE_BLOCK_ITEM.get());
            BlockItem.setBlockEntityData(retrievedCube, (BlockEntityType)PomkotsMechs.PERSONAL_POMKOTS_CUBE_BLOCK_ENTITY.get(), blockEntity.saveWithoutMetadata());
            blockEntity.clearContent();
            Block.popResource(level, pos, retrievedCube);
            level.removeBlock(pos, false);
            level.playSound(null, pos, SoundEvents.ANVIL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
               ctx.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));
            }

            return InteractionResult.CONSUME;
         } else {
            if (player != null) {
               player.sendSystemMessage(Component.translatable("text.pomkotsmechs.messages.personal_pomkots_cube.not_owner"));
            }

            return InteractionResult.CONSUME;
         }
      } else {
         return InteractionResult.FAIL;
      }
   }

   public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("tooltip.pomkotsmechs.pomkots_spanner").withStyle(ChatFormatting.GRAY));
      super.appendHoverText(stack, level, tooltip, flag);
   }
}
