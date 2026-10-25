package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PersonalPomkotsCubeBlock extends ChestBlock implements PomkotsUnbreakableBlock {
   public PersonalPomkotsCubeBlock() {
      this(
         Properties.m_284310_().m_60913_(-1.0F, 3600000.0F).m_278166_(PushReaction.BLOCK).m_60918_(SoundType.f_56743_),
         PomkotsMechs.PERSONAL_POMKOTS_CUBE_BLOCK_ENTITY::get
      );
   }

   public PersonalPomkotsCubeBlock(Properties properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntityType) {
      super(properties, blockEntityType);
   }

   public BlockState m_5573_(BlockPlaceContext context) {
      return (BlockState)((BlockState)((BlockState)this.m_49966_().m_61124_(f_51478_, context.m_8125_().m_122424_())).m_61124_(f_51479_, ChestType.SINGLE))
         .m_61124_(f_51480_, context.m_43725_().m_6425_(context.m_8083_()).m_76152_() == Fluids.f_76193_);
   }

   public void m_6402_(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.m_6402_(level, pos, state, placer, stack);
      if (!level.f_46443_ && placer instanceof Player player && level.m_7702_(pos) instanceof PersonalPomkotsCubeBlockEntity blockEntity) {
         blockEntity.setOwnerIfAbsent(player.m_20148_());
      }
   }

   public InteractionResult m_6227_(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (player.m_21120_(hand).m_150930_((Item)PomkotsMechs.SPANNER_ITEM.get())) {
         return InteractionResult.PASS;
      }

      if (level.m_7702_(pos) instanceof PersonalPomkotsCubeBlockEntity blockEntity && !blockEntity.isOwner(player)) {
         if (!level.f_46443_) {
            player.m_213846_(Component.m_237115_("text.pomkotsmechs.messages.personal_pomkots_cube.not_owner"));
         }

         return InteractionResult.m_19078_(level.f_46443_);
      } else {
         return super.m_6227_(state, level, pos, player, hand, hit);
      }
   }

   public BlockEntity m_142194_(BlockPos pos, BlockState state) {
      return new PersonalPomkotsCubeBlockEntity(pos, state);
   }

   public <T extends BlockEntity> BlockEntityTicker<T> m_142354_(Level level, BlockState state, BlockEntityType<T> type) {
      return level.f_46443_ ? m_152132_(type, this.m_153066_(), ChestBlockEntity::m_155343_) : null;
   }

   public RenderShape m_7514_(BlockState state) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }
}
