package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandExecutorBlockEntity extends BlockEntity {
   private boolean executed = false;
   private String command = "";

   public CommandExecutorBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.ENTITY_COMMAND_EXECUTOR_BLOCK.get(), pos, state);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, CommandExecutorBlockEntity be) {
      if (!be.executed && be.command != null && !be.command.isEmpty() && !Utils.isMapEditingMode(level)) {
         CommandSourceStack source = new CommandSourceStack(
            CommandSource.f_80164_,
            Vec3.m_82512_(pos),
            Vec2.f_82462_,
            (ServerLevel)level,
            2,
            "command_block_once",
            Component.m_237113_("command_block_once"),
            level.m_7654_(),
            null
         );

         try {
            level.m_7654_().m_129892_().m_230957_(source, be.command);
         } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to execute command: {}", be.command, e);
         }

         be.executed = true;
         be.m_6596_();
         level.m_7471_(pos, false);
      }
   }

   protected void m_183515_(CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128379_(PomkotsMechs.nbtName("CommandExecutorExecuted"), this.executed);
      if (!this.command.isEmpty()) {
         tag.m_128359_(PomkotsMechs.nbtName("CommandExecutorCommand"), this.command);
      }
   }

   public void m_142466_(CompoundTag tag) {
      super.m_142466_(tag);
      this.executed = tag.m_128471_(PomkotsMechs.nbtName("CommandExecutorExecuted"));
      if (tag.m_128441_(PomkotsMechs.nbtName("CommandExecutorCommand"))) {
         this.command = tag.m_128461_(PomkotsMechs.nbtName("CommandExecutorCommand"));
      }
   }
}
