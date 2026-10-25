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

   public void m_183515_(@NotNull CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128359_(PomkotsMechs.nbtName("Command"), this.command);
   }

   public void m_142466_(@NotNull CompoundTag tag) {
      super.m_142466_(tag);
      this.command = tag.m_128461_(PomkotsMechs.nbtName("Command"));
   }

   @NotNull
   public CompoundTag m_5995_() {
      return this.m_187482_();
   }

   public PomkotsLeverBlockEntity(BlockPos pos, BlockState state) {
      this((BlockEntityType<?>)PomkotsMechs.POMKOTS_LEVER_BLOCK_ENTITY.get(), pos, state);
   }

   protected PomkotsLeverBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
      super(blockEntityType, blockPos, blockState);
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.m_195640_(this);
   }

   public void use(ServerLevel level, BlockPos pos) {
      if (this.command != null && !this.command.isEmpty()) {
         CommandSourceStack source = new CommandSourceStack(
            CommandSource.f_80164_,
            Vec3.m_82512_(pos),
            Vec2.f_82462_,
            level,
            2,
            "command_block_once",
            Component.m_237113_("command_block_once"),
            level.m_7654_(),
            null
         );

         try {
            level.m_7654_().m_129892_().m_230957_(source, this.command);
         } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to execute command: {}", this.command, e);
         }
      }
   }

   public void playAnimation() {
      if (this.f_58857_.f_46443_) {
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
                        BlockPos pos = this.m_58899_();
                        Vec3 v = new Vec3(pos.m_123341_() + 0.5F, pos.m_123342_() + 0.5F, pos.m_123343_() + 0.5F);
                        this.f_58857_
                           .m_7785_(
                              pos.m_123341_(),
                              pos.m_123342_(),
                              pos.m_123343_(),
                              (SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get(),
                              SoundSource.PLAYERS,
                              0.5F,
                              1.0F,
                              false
                           );
                        ParticleUtil.addSparkParticles(v, this.f_58857_);
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
      return this.f_58857_ != null ? this.f_58857_.m_46467_() : 0.0;
   }
}
