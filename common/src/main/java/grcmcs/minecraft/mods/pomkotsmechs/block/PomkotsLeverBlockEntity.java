package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PomkotsLeverBlockEntity extends BlockEntity implements GeoBlockEntity, PomkotsUnbreakableBlock {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   protected String command = "";
   private boolean playAnimation = false;

   public void saveAdditional(@NotNull CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putString(PomkotsMechs.nbtName("Command"), this.command);
   }

   public void load(@NotNull CompoundTag tag) {
      super.load(tag);
      this.command = tag.getString(PomkotsMechs.nbtName("Command"));
   }

   @NotNull
   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
   }

   public PomkotsLeverBlockEntity(BlockPos pos, BlockState state) {
      this((BlockEntityType<?>)PomkotsMechs.POMKOTS_LEVER_BLOCK_ENTITY.get(), pos, state);
   }

   protected PomkotsLeverBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
      super(blockEntityType, blockPos, blockState);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void use(ServerLevel level, BlockPos pos) {
      if (this.command != null && !this.command.isEmpty()) {
         CommandSourceStack source = new CommandSourceStack(
            CommandSource.NULL,
            Vec3.atCenterOf(pos),
            Vec2.ZERO,
            level,
            2,
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
   }

   public void playAnimation() {
      if (this.level.isClientSide) {
         this.playAnimation = true;
      }
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, event -> {
                  if (this.playAnimation) {
                     this.playAnimation = false;
                     event.getController().forceAnimationReset();
                     return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pomkotscube.use"));
                  } else {
                     return PlayState.CONTINUE;
                  }
               })
               .setSoundKeyframeHandler(
                  soundKeyframeEvent -> {
                     String sound = soundKeyframeEvent.getKeyframeData().getSound();
                     if ("lever".equals(sound)) {
                        BlockPos pos = this.getBlockPos();
                        Vec3 v = new Vec3(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
                        this.level
                           .playLocalSound(
                              pos.getX(),
                              pos.getY(),
                              pos.getZ(),
                              (SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get(),
                              SoundSource.PLAYERS,
                              0.5F,
                              1.0F,
                              false
                           );
                        ParticleUtil.addSparkParticles(v, this.level);
                     }
                  }
               )
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   public double getTick(Object blockEntity) {
      return this.level != null ? this.level.getGameTime() : 0.0;
   }
}
