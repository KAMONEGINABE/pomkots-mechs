package grcmcs.minecraft.mods.pomkotsmechs.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.CoreStoneBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.RaidDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.RadarTarget;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class PomkotsCommands {
   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)Commands.m_82127_("pomkots:raidstart")
            .then(
               Commands.m_82129_("raid_type", StringArgumentType.string())
                  .then(
                     Commands.m_82129_("command_success", StringArgumentType.string())
                        .then(Commands.m_82129_("command_fail", StringArgumentType.string()).executes(PomkotsCommands::executeRaid))
                  )
            )
      );
      dispatcher.register(
         (LiteralArgumentBuilder)Commands.m_82127_("pomkots:register_radartarget")
            .then(
               Commands.m_82129_("player", EntityArgument.m_91466_())
                  .then(
                     Commands.m_82129_("label", StringArgumentType.string())
                        .then(Commands.m_82127_("coord").then(Commands.m_82129_("pos", BlockPosArgument.m_118239_()).executes(ctx -> executeCoord(ctx))))
                  )
            )
      );
      dispatcher.register(
         (LiteralArgumentBuilder)Commands.m_82127_("pomkots:reset_boss_pos")
            .then(
               Commands.m_82129_("uuid", UuidArgument.m_113850_())
                  .then(Commands.m_82129_("pos", BlockPosArgument.m_118239_()).executes(ctx -> resetBossPosition(ctx)))
            )
      );
   }

   private static int resetBossPosition(CommandContext<CommandSourceStack> ctx) {
      try {
         CommandSourceStack source = (CommandSourceStack)ctx.getSource();
         UUID uuid = UuidArgument.m_113853_(ctx, "uuid");
         BlockPos pos = BlockPosArgument.m_118242_(ctx, "pos");
         ServerLevel level = source.m_81372_();
         if (level.m_8791_(uuid) instanceof BaseBossEntity boss) {
            boss.m_6021_(pos.m_123341_() + 0.5, pos.m_123342_(), pos.m_123343_() + 0.5);
            boss.setActivated(false);
            boss.setAiMode(-2);
            source.m_288197_(() -> Component.m_237113_("Boss moved!"), true);
            return 0;
         } else {
            source.m_81352_(Component.m_237113_("Entity not found"));
            return 0;
         }
      } catch (Exception e) {
         PomkotsMechs.LOGGER.error("Fail", e);
         return 0;
      }
   }

   private static int executeCoord(CommandContext<CommandSourceStack> ctx) {
      try {
         ServerPlayer player = EntityArgument.m_91474_(ctx, "player");
         String label = StringArgumentType.getString(ctx, "label");
         BlockPos pos = BlockPosArgument.m_264582_(ctx, "pos");
         ResourceLocation dim = player.m_9236_().m_46472_().m_135782_();
         ItemStack radarStack = findRadarItem(player);
         if (radarStack == null) {
            ((CommandSourceStack)ctx.getSource()).m_81352_(Component.m_237113_(player.m_7755_().getString() + " does not have a Pomkots Data Pad"));
            return 0;
         } else {
            PomkotsDatapadItem.addTarget(radarStack, new RadarTarget.CoordTarget(pos, label, dim));
            ((CommandSourceStack)ctx.getSource()).m_288197_(() -> Component.m_237113_("Registered radar target: " + label + " at " + pos), true);
            return 1;
         }
      } catch (Exception e) {
         PomkotsMechs.LOGGER.error("Fail", e);
         return 0;
      }
   }

   private static ItemStack findRadarItem(ServerPlayer player) {
      for (ItemStack stack : player.m_150109_().f_35974_) {
         if (stack.m_41720_() instanceof PomkotsDatapadItem) {
            return stack;
         }
      }

      return null;
   }

   private static int executeRaid(CommandContext<CommandSourceStack> ctx) {
      String raid_type = StringArgumentType.getString(ctx, "raid_type");
      String command_success = StringArgumentType.getString(ctx, "command_success");
      String command_fail = StringArgumentType.getString(ctx, "command_fail");
      CommandSourceStack source = (CommandSourceStack)ctx.getSource();
      Level level = source.m_81372_();
      RaidDefinition raidData = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(raid_type);
      if (raidData == null) {
         source.m_81352_(Component.m_237113_("Undefined raid: " + raid_type));
      } else if ("defense".equals(raidData.type)) {
         BlockPos origin = BlockPos.m_274446_(source.m_81371_());
         int radius = 100;
         double closestDist = Double.MAX_VALUE;
         BlockPos closest = null;
         Block targetBlock = (Block)PomkotsMechs.CORE_STONE_BLOCK.get();

         for (BlockPos pos : BlockPos.m_121940_(origin.m_7918_(-radius, -radius, -radius), origin.m_7918_(radius, radius, radius))) {
            if (level.m_8055_(pos).m_60713_(targetBlock)) {
               double dist = pos.m_203198_(origin.m_123341_(), origin.m_123342_(), origin.m_123343_());
               if (dist < closestDist) {
                  closestDist = dist;
                  closest = pos.m_7949_();
               }
            }
         }

         if (closest != null) {
            if (level.m_7702_(closest) instanceof CoreStoneBlockEntity csbe) {
               ServerPlayer p = source.m_230896_();
               if (!csbe.isRaidActive() && p != null) {
                  csbe.startRaid(raid_type, p.m_20148_(), command_success, command_fail);
                  csbe.m_6596_();
                  source.m_288197_(() -> Component.m_237113_("Raid Started"), true);
               } else {
                  source.m_81352_(Component.m_237113_("Another raid is active now."));
               }
            } else {
               source.m_81352_(Component.m_237113_("Block has no BlockEntity."));
            }
         } else {
            source.m_81352_(Component.m_237113_("No nearby block of type " + targetBlock + " found."));
         }
      } else if ("survive".equals(raidData.type)) {
         RaidControllerEntity rce = (RaidControllerEntity)((EntityType)PomkotsMechs.RAID_CONTROLLER.get()).m_20615_(level);
         if (rce != null) {
            BlockPos pos = BlockPos.m_274446_(source.m_81371_());
            rce.m_6034_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
            CompoundTag tag = RaidControllerEntity.buildCompoundTag(raid_type, source.m_230896_().m_20148_(), command_success, command_fail);
            rce.m_7378_(tag);
            level.m_7967_(rce);
            source.m_288197_(() -> Component.m_237113_("Raid Started"), true);
         } else {
            source.m_81352_(Component.m_237113_("Cant spawn raid controller"));
         }
      } else if ("sweep".equals(raidData.type)) {
         RaidControllerEntity rce = (RaidControllerEntity)((EntityType)PomkotsMechs.RAID_CONTROLLER.get()).m_20615_(level);
         if (rce != null) {
            BlockPos pos = BlockPos.m_274446_(source.m_81371_());
            rce.m_6034_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
            CompoundTag tag = RaidControllerEntity.buildCompoundTag(raid_type, source.m_230896_().m_20148_(), command_success, command_fail);
            rce.m_7378_(tag);
            level.m_7967_(rce);
            source.m_288197_(() -> Component.m_237113_("Raid Started"), true);
         } else {
            source.m_81352_(Component.m_237113_("Cant spawn raid controller"));
         }
      } else if ("activate".equals(raidData.type)) {
         RaidControllerEntity rce = (RaidControllerEntity)((EntityType)PomkotsMechs.RAID_CONTROLLER.get()).m_20615_(level);
         if (rce != null) {
            BlockPos pos = BlockPos.m_274446_(source.m_81371_());
            rce.m_6034_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
            CompoundTag tag = RaidControllerEntity.buildCompoundTag(raid_type, source.m_230896_().m_20148_(), command_success, command_fail);
            rce.m_7378_(tag);
            level.m_7967_(rce);
            source.m_288197_(() -> Component.m_237113_("Raid Started"), true);
         } else {
            source.m_81352_(Component.m_237113_("Cant spawn raid controller"));
         }
      } else {
         source.m_81352_(Component.m_237113_("Undefined raid type: " + raidData.type));
      }

      return 1;
   }
}
