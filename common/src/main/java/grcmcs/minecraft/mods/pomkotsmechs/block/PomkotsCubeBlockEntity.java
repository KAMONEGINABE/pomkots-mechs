package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.RaidDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.AlertEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PomkotsCubeBlockEntity extends ChestBlockEntity implements GeoBlockEntity, ScanTargetBlockEntity {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private static final long REFILL_INTERVAL = 24000L;
   protected int mode = this.getDefaultMode();
   private int summonActionTickCount = 0;
   private Player opener = null;
   private boolean openedOnce = false;
   private long lastRefillGameTime = -1L;
   private ResourceLocation refillLootTable = null;
   private UUID raidControllerUUID;
   private RaidControllerEntity raidControllerEntity;
   public static final int MODE_BLUE = 0;
   public static final int MODE_YELLOW = 1;
   public static final int MODE_RED = 2;
   public static final int MODE_PURPLE = 3;
   private boolean playLockedAnimation = false;
   private float prevOpeness = 0.0F;

   public void m_183515_(@NotNull CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128405_(PomkotsMechs.nbtName("CubeMode"), this.mode);
      tag.m_128379_(PomkotsMechs.nbtName("OpenedOnce"), this.openedOnce);
      tag.m_128356_(PomkotsMechs.nbtName("LastRefillTime"), this.lastRefillGameTime);
      if (this.refillLootTable != null) {
         tag.m_128359_(PomkotsMechs.nbtName("RefillLootTable"), this.refillLootTable.toString());
      }

      if (this.raidControllerUUID != null) {
         tag.m_128362_(PomkotsMechs.nbtName("RaidControllerUUID"), this.raidControllerUUID);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("RaidControllerUUID"));
      }
   }

   public void m_142466_(@NotNull CompoundTag tag) {
      super.m_142466_(tag);
      this.mode = tag.m_128451_(PomkotsMechs.nbtName("CubeMode"));
      this.openedOnce = tag.m_128471_(PomkotsMechs.nbtName("OpenedOnce"));
      this.lastRefillGameTime = tag.m_128454_(PomkotsMechs.nbtName("LastRefillTime"));
      if (tag.m_128441_(PomkotsMechs.nbtName("RefillLootTable"))) {
         this.refillLootTable = new ResourceLocation(tag.m_128461_(PomkotsMechs.nbtName("RefillLootTable")));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("RaidControllerUUID"))) {
         this.raidControllerUUID = tag.m_128342_(PomkotsMechs.nbtName("RaidControllerUUID"));
         if (this.f_58857_ instanceof ServerLevel sl) {
            if (sl.m_8791_(this.raidControllerUUID) instanceof RaidControllerEntity raid) {
               this.raidControllerEntity = raid;
            } else {
               this.raidControllerUUID = null;
            }
         }
      }

      this.tryRefill();
   }

   @NotNull
   public CompoundTag m_5995_() {
      return this.m_187482_();
   }

   public PomkotsCubeBlockEntity(BlockPos pos, BlockState state) {
      this((BlockEntityType<?>)PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY.get(), pos, state);
   }

   protected PomkotsCubeBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
      super(blockEntityType, blockPos, blockState);
   }

   public int getMode() {
      return this.mode;
   }

   public void incrementMode() {
      this.mode++;
      if (this.mode > 3) {
         this.mode = 0;
      }

      this.updateMode(this.mode);
   }

   public void updateMode(int mode) {
      this.updateMode(mode, this.f_58857_);
   }

   public void updateMode(int mode, Level level) {
      this.mode = mode;
      this.m_6596_();
      BlockState state = this.m_58900_();
      level.m_7260_(this.f_58858_, state, state, 3);
   }

   public int getDefaultMode() {
      return 0;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.m_195640_(this);
   }

   public void openBox(Player p) {
      this.opener = p;
      switch (this.getMode()) {
         case 0:
            if (!this.f_58857_.f_46443_ && !this.openedOnce) {
               this.openedOnce = true;
               this.lastRefillGameTime = this.f_58857_.m_8044_();
               this.m_6596_();
            }
            break;
         case 1:
            if (!this.f_58857_.f_46443_) {
               if (this.raidControllerUUID == null && this.summonActionTickCount == 0) {
                  this.summonActionTickCount = 1;
               }
            } else {
               this.playOpenFailAnimation();
            }
            break;
         case 2:
            if (!this.f_58857_.f_46443_) {
               if (p.m_21205_().m_150930_((Item)PomkotsMechs.CUBEKEY_ITEM.get())) {
                  if (this.raidControllerUUID == null && this.summonActionTickCount == 0 && this.f_59605_ != null) {
                     PomkotsDataPack.ChestData chestData = PomkotsDataPackManager.getInstance().getDataPack().getChestData(this.f_59605_.toString());
                     RaidDefinition raidData = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(chestData.raid_id);
                     if (raidData != null) {
                        if ("activate".equals(raidData.type)) {
                           List<BaseBossEntity> bosses = RaidControllerEntity.getInactiveBossesAroundPos(this.f_58857_, this.m_58899_());
                           if (bosses.isEmpty()) {
                              this.sendMessageForOpener("{text.pomkotsmechs.messages.pomkotscube.01}");
                              break;
                           }
                        }

                        this.summonActionTickCount = 1;
                        if (!PomkotsMechs.CONFIG.debugModeEnabled) {
                           p.m_21205_().m_41774_(1);
                        }
                     }
                  }
               } else {
                  this.sendMessageForOpener("{text.pomkotsmechs.messages.pomkotscube.02}");
               }
            } else {
               this.playOpenFailAnimation();
            }
            break;
         case 3:
            if (!this.f_58857_.f_46443_) {
               if (p.m_21205_().m_150930_((Item)PomkotsMechs.CUBEKEY_ITEM_PURPLE.get())) {
                  if (this.raidControllerUUID == null && this.summonActionTickCount == 0 && this.f_59605_ != null) {
                     PomkotsDataPack.ChestData chestData = PomkotsDataPackManager.getInstance().getDataPack().getChestData(this.f_59605_.toString());
                     RaidDefinition raidData = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(chestData.raid_id);
                     if (raidData != null) {
                        if ("activate".equals(raidData.type)) {
                           List<BaseBossEntity> bosses = RaidControllerEntity.getInactiveBossesAroundPos(this.f_58857_, this.m_58899_());
                           if (bosses.isEmpty()) {
                              this.sendMessageForOpener("{text.pomkotsmechs.messages.pomkotscube.01}");
                              break;
                           }
                        }

                        this.summonActionTickCount = 1;
                        p.m_21205_().m_41774_(1);
                     }
                  }
               } else {
                  this.sendMessageForOpener("{text.pomkotsmechs.messages.pomkotscube.03}");
               }
            } else {
               this.playOpenFailAnimation();
            }
            break;
         default:
            this.playOpenFailAnimation();
      }
   }

   public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, PomkotsCubeBlockEntity entity) {
      entity.tick();
   }

   public void tick() {
      if (this.f_58857_ != null && !this.f_58857_.f_46443_) {
         if (this.mode == 0) {
            this.tryRefill();
         } else if (this.summonActionTickCount > 0) {
            if (this.opener == null || !this.opener.m_6084_()) {
               this.summonActionTickCount = 0;
               return;
            }

            this.summonActionTickCount++;
            if (this.summonActionTickCount == 10) {
               this.spawnAlertEffect();
            } else if (this.summonActionTickCount > 50) {
               this.startRaid();
               this.summonActionTickCount = 0;
            }
         } else if (this.raidControllerEntity == null || !this.raidControllerEntity.m_6084_()) {
            this.raidControllerUUID = null;
            this.raidControllerEntity = null;
            this.m_6596_();
         }
      }
   }

   private void spawnAlertEffect() {
      AlertEntity e;
      if (this.getMode() == 1) {
         e = new AlertEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.ALERT.get(), this.f_58857_);
      } else {
         e = new AlertEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.ALERTRED.get(), this.f_58857_);
      }

      BlockPos bp = this.m_58899_();
      e.m_6034_(bp.m_123341_() + 0.5, bp.m_123342_(), bp.m_123343_() + 0.5);
      this.f_58857_.m_7967_(e);
   }

   private void startRaid() {
      if (this.opener != null
         && this.f_59605_ != null
         && this.f_59605_.m_135827_().equals("pomkotsmechs")
         && PomkotsDataPackManager.getInstance().getDataPack().getChestData(this.f_59605_.toString()) != null) {
         PomkotsDataPack.ChestData chestData = PomkotsDataPackManager.getInstance().getDataPack().getChestData(this.f_59605_.toString());
         RaidDefinition raidData = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(chestData.raid_id);
         if (raidData != null) {
            RaidControllerEntity rce = (RaidControllerEntity)((EntityType)PomkotsMechs.RAID_CONTROLLER.get()).m_20615_(this.f_58857_);
            if (rce != null) {
               BlockPos raidEntityPos = this.getRaidEntitySpawnPos(raidData);
               rce.m_6034_(raidEntityPos.m_123341_(), raidEntityPos.m_123342_(), raidEntityPos.m_123343_());
               BlockPos cubePos = this.m_58899_();
               String failCommand;
               if (raidData.type.equals(RaidControllerEntity.RaidType.ACTIVATE.getId())) {
                  List<BaseBossEntity> bosses = RaidControllerEntity.getInactiveBossesAroundPos(this.f_58857_, this.m_58899_());
                  if (bosses != null && !bosses.isEmpty()) {
                     BaseBossEntity boss = bosses.get(0);
                     UUID uuid = boss.m_20148_();
                     BlockPos pos = boss.m_20183_();
                     failCommand = String.format("pomkots:reset_boss_pos %s %d %d %d", uuid, pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
                  } else {
                     failCommand = "say mission failed...";
                  }
               } else {
                  failCommand = "say mission failed...";
               }

               CompoundTag tag = RaidControllerEntity.buildCompoundTag(
                  chestData.raid_id,
                  this.opener.m_20148_(),
                  "data modify block " + cubePos.m_123341_() + " " + cubePos.m_123342_() + " " + cubePos.m_123343_() + " pomkotsmechsCubeMode set value 0",
                  failCommand
               );
               rce.m_7378_(tag);
               this.f_58857_.m_7967_(rce);
               this.raidControllerUUID = rce.m_20148_();
               this.raidControllerEntity = rce;
               this.m_6596_();
            }
         } else {
            PomkotsMechs.LOGGER.error("Specified raid does not exist:" + chestData.raid_id);
         }
      }
   }

   private BlockPos getRaidEntitySpawnPos(RaidDefinition raidData) {
      if (RaidControllerEntity.RaidType.SWEEP_BOSS_BOX.getId().equals(raidData.type)) {
         Direction facing = (Direction)this.m_58900_().m_61143_(BlockStateProperties.f_61374_);
         Direction behind = facing.m_122424_();
         return this.m_58899_().m_5484_(behind, 15);
      } else {
         return this.m_58899_();
      }
   }

   public void m_59640_(Player player) {
      if (!this.f_58857_.f_46443_ && this.refillLootTable == null) {
         this.refillLootTable = this.f_59605_;
         super.m_59640_(player);
         this.compactStacksRespectNBT();
         this.m_6596_();
      }
   }

   private void compactStacksRespectNBT() {
      NonNullList<ItemStack> items = this.m_7086_();
      List<ItemStack> merged = new ArrayList<>();

      for (ItemStack stack : items) {
         if (!stack.m_41619_()) {
            for (ItemStack existing : merged) {
               if (ItemStack.m_150942_(existing, stack) && existing.m_41613_() < existing.m_41741_()) {
                  int move = Math.min(stack.m_41613_(), existing.m_41741_() - existing.m_41613_());
                  existing.m_41769_(move);
                  stack.m_41774_(move);
                  if (stack.m_41619_()) {
                     break;
                  }
               }
            }

            if (!stack.m_41619_()) {
               merged.add(stack.m_41777_());
            }
         }
      }

      List<Integer> slots = new ArrayList<>();

      for (int i = 0; i < items.size(); i++) {
         slots.add(i);
      }

      Collections.shuffle(slots, new Random());
      items.clear();

      for (int i = 0; i < merged.size() && i < slots.size(); i++) {
         items.set(slots.get(i), merged.get(i));
      }
   }

   private void compactStacksRespectNBT2() {
      List<ItemStack> merged = new ArrayList<>();

      for (int i = 0; i < this.m_6643_(); i++) {
         ItemStack stack = this.m_8020_(i);
         if (!stack.m_41619_()) {
            boolean mergedFlag = false;

            for (ItemStack existing : merged) {
               if (ItemStack.m_150942_(existing, stack) && existing.m_41613_() < existing.m_41741_()) {
                  int move = Math.min(stack.m_41613_(), existing.m_41741_() - existing.m_41613_());
                  existing.m_41769_(move);
                  stack.m_41774_(move);
                  if (stack.m_41619_()) {
                     mergedFlag = true;
                     break;
                  }
               }
            }

            if (!stack.m_41619_()) {
               merged.add(stack.m_41777_());
            }
         }
      }

      for (int i = 0; i < this.m_6643_(); i++) {
         this.m_6836_(i, ItemStack.f_41583_);
      }

      int slot = 0;

      for (ItemStack stack : merged) {
         while (!stack.m_41619_() && slot < this.m_6643_()) {
            ItemStack split = stack.m_41620_(stack.m_41741_());
            this.m_6836_(slot++, split);
         }
      }
   }

   private void tryRefill() {
      if (this.openedOnce) {
         if (this.f_58857_ != null && !this.f_58857_.f_46443_) {
            long now = this.f_58857_.m_8044_();
            if (now - this.lastRefillGameTime >= 24000L) {
               this.refillLoot();
               this.lastRefillGameTime = now;
               this.openedOnce = false;
               this.updateMode(this.getDefaultMode());
               this.m_6596_();
            }
         }
      }
   }

   private void refillLoot() {
      if (this.f_58857_ instanceof ServerLevel serverLevel) {
         if (this.refillLootTable != null) {
            this.m_6211_();
            this.m_59626_(this.refillLootTable, new Random().nextLong());
            this.refillLootTable = null;
         }
      }
   }

   public void playOpenFailAnimation() {
      if (this.f_58857_.f_46443_) {
         this.playLockedAnimation = true;
      }
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               event -> {
                  float curOpenness = ((PomkotsCubeBlockEntity)event.getAnimatable()).m_6683_(event.getPartialTick());
                  float prevOpenness = this.prevOpeness;
                  this.prevOpeness = curOpenness;
                  if (this.playLockedAnimation) {
                     this.playLockedAnimation = false;
                     event.getController().forceAnimationReset();
                     return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pomkotscube.openfail"));
                  } else if (prevOpenness == 0.0F && curOpenness > 0.0F) {
                     return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pomkotscube.open"));
                  } else {
                     return prevOpenness == 1.0F && curOpenness < 1.0F
                        ? event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pomkotscube.close"))
                        : PlayState.CONTINUE;
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

   private void sendMessageForOpener(String message) {
      if (this.opener instanceof ServerPlayer sp) {
         ArrayList<ServerPlayer> l = new ArrayList<>();
         l.add(sp);
         this.sendMessage(message, l);
      }
   }

   private void sendMessage(String message, Collection<ServerPlayer> players) {
      for (ServerPlayer player : players) {
         player.m_213846_(Utils.string2Component(message));
      }
   }
}
