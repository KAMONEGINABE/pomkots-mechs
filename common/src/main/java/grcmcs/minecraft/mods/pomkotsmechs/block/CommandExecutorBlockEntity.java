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
            CommandSource.NULL,
            Vec3.atCenterOf(pos),
            Vec2.ZERO,
            (ServerLevel)level,
            2,
            "command_block_once",
            Component.literal("command_block_once"),
            level.getServer(),
            null
         );

         try {
            level.getServer().getCommands().performPrefixedCommand(source, be.command);
         } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to execute command: {}", be.command, e);
         }

         be.executed = true;
         be.setChanged();
         level.removeBlock(pos, false);
      }
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putBoolean(PomkotsMechs.nbtName("CommandExecutorExecuted"), this.executed);
      if (!this.command.isEmpty()) {
         tag.putString(PomkotsMechs.nbtName("CommandExecutorCommand"), this.command);
      }
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      this.executed = tag.getBoolean(PomkotsMechs.nbtName("CommandExecutorExecuted"));
      if (tag.contains(PomkotsMechs.nbtName("CommandExecutorCommand"))) {
         this.command = tag.getString(PomkotsMechs.nbtName("CommandExecutorCommand"));
      }
   }
}
