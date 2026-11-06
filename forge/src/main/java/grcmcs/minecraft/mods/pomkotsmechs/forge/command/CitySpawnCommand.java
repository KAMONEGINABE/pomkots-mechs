package grcmcs.minecraft.mods.pomkotsmechs.forge.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import grcmcs.minecraft.mods.pomkotsmechs.forge.worldgen.LostCitiesEnemySpawner;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public class CitySpawnCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("pomkots_cityspawn")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> executeTest(ctx.getSource(), "large", true))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("large");
                                    builder.suggest("medium");
                                    builder.suggest("small");
                                    builder.suggest("inspect");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> executeTest(ctx.getSource(), StringArgumentType.getString(ctx, "type"), true))
                                .then(Commands.literal("force")
                                        .executes(ctx -> executeTest(ctx.getSource(), StringArgumentType.getString(ctx, "type"), true)))
                                .then(Commands.literal("normal")
                                        .executes(ctx -> executeTest(ctx.getSource(), StringArgumentType.getString(ctx, "type"), false)))
                        )
        );

        // pomkots:test_city_spawns エイリアス
        dispatcher.register(
                Commands.literal("pomkots:test_city_spawns")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> executeTest(ctx.getSource(), "large", true))
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("large");
                                    builder.suggest("medium");
                                    builder.suggest("small");
                                    builder.suggest("inspect");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> executeTest(ctx.getSource(), StringArgumentType.getString(ctx, "type"), true))
                                .then(Commands.literal("force")
                                        .executes(ctx -> executeTest(ctx.getSource(), StringArgumentType.getString(ctx, "type"), true)))
                                .then(Commands.literal("normal")
                                        .executes(ctx -> executeTest(ctx.getSource(), StringArgumentType.getString(ctx, "type"), false)))
                        )
        );
    }

    private static int executeTest(CommandSourceStack source, String type, boolean force) {
        ServerLevel level = source.getLevel();
        BlockPos playerPos = BlockPos.containing(source.getPosition());
        int chunkX = playerPos.getX() >> 4;
        int chunkZ = playerPos.getZ() >> 4;
        RandomSource random = level.getRandom();

        if ("inspect".equalsIgnoreCase(type)) {
            // 現在地チャンクの屋上と空間の診断
            BlockPos surface = LostCitiesEnemySpawner.findOutdoorSurface(level, playerPos.getX(), playerPos.getZ());
            if (surface == null) {
                source.sendFailure(Component.literal("[CitySpawn Inspect] No valid surface found at (" + playerPos.getX() + ", " + playerPos.getZ() + ")"));
                return 0;
            }

            LostCitiesEnemySpawner.SpaceCheckResult check14 = LostCitiesEnemySpawner.checkClearSpace(level, surface, 1, 14);
            LostCitiesEnemySpawner.SpaceCheckResult check4 = LostCitiesEnemySpawner.checkClearSpace(level, surface, 0, 4);

            source.sendSuccess(() -> Component.literal("=== [CitySpawn Inspection Report] ===").withStyle(ChatFormatting.GOLD), false);
            source.sendSuccess(() -> Component.literal("Chunk: [" + chunkX + ", " + chunkZ + "] | Surface Pos: " + surface).withStyle(ChatFormatting.AQUA), false);
            source.sendSuccess(() -> Component.literal("Large Mech Space (3x3 x 14h): " + (check14.isClear ? "CLEAR (OK)" : "BLOCKED by " + check14.blockName + " at " + check14.blockPos))
                    .withStyle(check14.isClear ? ChatFormatting.GREEN : ChatFormatting.RED), false);
            source.sendSuccess(() -> Component.literal("Medium Mech Space (1x1 x 4h): " + (check4.isClear ? "CLEAR (OK)" : "BLOCKED by " + check4.blockName + " at " + check4.blockPos))
                    .withStyle(check4.isClear ? ChatFormatting.GREEN : ChatFormatting.RED), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("[CitySpawn] Testing " + type.toUpperCase() + " mech spawn in chunk [" + chunkX + ", " + chunkZ + "] (force=" + force + ")...")
                .withStyle(ChatFormatting.YELLOW), false);

        boolean success = false;
        switch (type.toLowerCase()) {
            case "large":
                success = LostCitiesEnemySpawner.trySpawnLargeMech(level, chunkX, chunkZ, random, force);
                break;
            case "medium":
                success = LostCitiesEnemySpawner.trySpawnMediumMech(level, chunkX, chunkZ, random, force);
                break;
            case "small":
                success = LostCitiesEnemySpawner.trySpawnSmallMechSquad(level, chunkX, chunkZ, random, force);
                break;
            default:
                source.sendFailure(Component.literal("Unknown type: " + type + ". Available: large, medium, small, inspect"));
                return 0;
        }

        if (success) {
            source.sendSuccess(() -> Component.literal("[CitySpawn] SUCCESS: Spawned " + type.toUpperCase() + " in chunk [" + chunkX + ", " + chunkZ + "]! (Check console for exact coords)")
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("[CitySpawn] FAILED: Could not spawn " + type.toUpperCase() + " in chunk [" + chunkX + ", " + chunkZ + "]. Check latest.log for details."));
            return 0;
        }
    }
}
