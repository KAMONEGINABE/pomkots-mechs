package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.cutscene.CutsceneDefinitionRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.cutscene.CutsceneService;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import net.minecraft.client.particle.Particle;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

public class PomkotsLeverBlockEntity extends BlockEntity implements GeoBlockEntity, PomkotsUnbreakableBlock {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected String command = "";
    private String cutsceneId = "";
    private Vec3 cutsceneTargetOffset = Vec3.ZERO;
    private int commandDelayTicks;
    private int pendingCommandTicks = -1;

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(PomkotsMechs.nbtName("Command"), command);
        CompoundTag cutscene = new CompoundTag();
        if (!cutsceneId.isEmpty()) cutscene.putString("id", cutsceneId);
        CompoundTag targetOffset = new CompoundTag();
        targetOffset.putDouble("x", cutsceneTargetOffset.x);
        targetOffset.putDouble("y", cutsceneTargetOffset.y);
        targetOffset.putDouble("z", cutsceneTargetOffset.z);
        cutscene.put("target_offset", targetOffset);
        cutscene.putInt("command_delay_ticks", commandDelayTicks);
        tag.put(PomkotsMechs.nbtName("Cutscene"), cutscene);
        if (pendingCommandTicks >= 0) {
            tag.putInt(PomkotsMechs.nbtName("PendingCommandTicks"), pendingCommandTicks);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.command = tag.getString(PomkotsMechs.nbtName("Command"));
        CompoundTag cutscene = tag.getCompound(PomkotsMechs.nbtName("Cutscene"));
        cutsceneId = cutscene.getString("id");
        CompoundTag targetOffset = cutscene.getCompound("target_offset");
        cutsceneTargetOffset = new Vec3(
                targetOffset.getDouble("x"),
                targetOffset.getDouble("y"),
                targetOffset.getDouble("z"));
        commandDelayTicks = Math.max(0, cutscene.getInt("command_delay_ticks"));
        pendingCommandTicks = tag.contains(PomkotsMechs.nbtName("PendingCommandTicks"))
                ? Math.max(0, tag.getInt(PomkotsMechs.nbtName("PendingCommandTicks")))
                : -1;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    // ====================== コンストラクタ ======================

    public PomkotsLeverBlockEntity(BlockPos pos, BlockState state) {
        this(PomkotsMechs.POMKOTS_LEVER_BLOCK_ENTITY.get(), pos, state);
    }

    protected PomkotsLeverBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void use(ServerLevel level, BlockPos pos, ServerPlayer viewer) {
        playConfiguredCutscene(pos, viewer);
        if (command == null || command.isEmpty()) {
            return;
        }

        if (commandDelayTicks > 0) {
            pendingCommandTicks = commandDelayTicks;
            setChanged();
            return;
        }

        // コマンド実行
        executeCommand(level, pos);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  PomkotsLeverBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel) || blockEntity.pendingCommandTicks < 0) return;
        if (blockEntity.pendingCommandTicks > 0) blockEntity.pendingCommandTicks--;
        if (blockEntity.pendingCommandTicks == 0) {
            blockEntity.pendingCommandTicks = -1;
            blockEntity.executeCommand(serverLevel, pos);
        }
        blockEntity.setChanged();
    }

    private void playConfiguredCutscene(BlockPos pos, ServerPlayer viewer) {
        if (cutsceneId.isEmpty()) return;
        ResourceLocation id = ResourceLocation.tryParse(cutsceneId);
        if (id == null) {
            PomkotsMechs.LOGGER.warn("Invalid cutscene ID on Pomkots Lever at {}: {}", pos, cutsceneId);
            return;
        }
        var definition = CutsceneDefinitionRegistry.get(id).orElse(null);
        if (definition == null) {
            PomkotsMechs.LOGGER.warn("Unknown cutscene on Pomkots Lever at {}: {}", pos, id);
            return;
        }
        CutsceneService.play(viewer, definition, Vec3.atCenterOf(pos).add(cutsceneTargetOffset), null);
    }

    private void executeCommand(ServerLevel level, BlockPos pos) {
        CommandSourceStack source = new CommandSourceStack(
                CommandSource.NULL,
                Vec3.atCenterOf(pos),
                Vec2.ZERO,
                level,
                2, // 権限レベル
                "command_block_once",
                Component.literal("command_block_once"),
                level.getServer(),
                null
        );

        try {
            level.getServer().getCommands().performPrefixedCommand(source, this.command);
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to execute command: {}", this.command, e);
        }
    }

    // ====================== アニメーション関係 ======================

    private boolean playAnimation = false;

    public void playAnimation() {
        if (this.level.isClientSide) {
            this.playAnimation = true;
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, event -> {
            if (playAnimation) {
                this.playAnimation = false;
                event.getController().forceAnimationReset();
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pomkotscube.use"));
            } else {
                return PlayState.CONTINUE;
            }
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            var sound = soundKeyframeEvent.getKeyframeData().getSound();

            if ("lever".equals(sound)) {
                var pos = this.getBlockPos();
                var v = new Vec3(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
                this.level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), PomkotsMechs.SE_PILEBUNKER_EVENT.get(), SoundSource.PLAYERS, 0.5F, 1.0F, false);
                ParticleUtil.addSparkParticles(v, this.level);
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object blockEntity) {
        return level != null ? level.getGameTime() : 0;
    }

}
