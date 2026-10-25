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

   public void saveAdditional(@NotNull CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putInt(PomkotsMechs.nbtName("CubeMode"), this.mode);
      tag.putBoolean(PomkotsMechs.nbtName("OpenedOnce"), this.openedOnce);
      tag.putLong(PomkotsMechs.nbtName("LastRefillTime"), this.lastRefillGameTime);
      if (this.refillLootTable != null) {
         tag.putString(PomkotsMechs.nbtName("RefillLootTable"), this.refillLootTable.toString());
      }

      if (this.raidControllerUUID != null) {
         tag.putUUID(PomkotsMechs.nbtName("RaidControllerUUID"), this.raidControllerUUID);
      } else {
         tag.remove(PomkotsMechs.nbtName("RaidControllerUUID"));
      }
   }

   public void load(@NotNull CompoundTag tag) {
      super.load(tag);
      this.mode = tag.getInt(PomkotsMechs.nbtName("CubeMode"));
      this.openedOnce = tag.getBoolean(PomkotsMechs.nbtName("OpenedOnce"));
      this.lastRefillGameTime = tag.getLong(PomkotsMechs.nbtName("LastRefillTime"));
      if (tag.contains(PomkotsMechs.nbtName("RefillLootTable"))) {
         this.refillLootTable = new ResourceLocation(tag.getString(PomkotsMechs.nbtName("RefillLootTable")));
      }

      if (tag.contains(PomkotsMechs.nbtName("RaidControllerUUID"))) {
         this.raidControllerUUID = tag.getUUID(PomkotsMechs.nbtName("RaidControllerUUID"));
         if (this.level instanceof ServerLevel sl) {
            if (sl.getEntity(this.raidControllerUUID) instanceof RaidControllerEntity raid) {
               this.raidControllerEntity = raid;
            } else {
               this.raidControllerUUID = null;
            }
         }
      }

      this.tryRefill();
   }

   @NotNull
   public CompoundTag getUpdateTag() {
      return this.saveWithoutMetadata();
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
      this.updateMode(mode, this.level);
   }

   public void updateMode(int mode, Level level) {
      this.mode = mode;
      this.setChanged();
      BlockState state = this.getBlockState();
      level.sendBlockUpdated(this.worldPosition, state, state, 3);
   }

   public int getDefaultMode() {
      return 0;
   }

   public ClientboundBlockEntityDataPacket getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   public void openBox(Player p) {
      this.opener = p;
      switch (this.getMode()) {
         case 0:
            if (!this.level.isClientSide && !this.openedOnce) {
               this.openedOnce = true;
               this.lastRefillGameTime = this.level.dayTime();
               this.setChanged();
            }
            break;
         case 1:
            if (!this.level.isClientSide) {
               if (this.raidControllerUUID == null && this.summonActionTickCount == 0) {
                  this.summonActionTickCount = 1;
               }
            } else {
               this.playOpenFailAnimation();
            }
            break;
         case 2:
            if (!this.level.isClientSide) {
               if (p.getMainHandItem().is((Item)PomkotsMechs.CUBEKEY_ITEM.get())) {
                  if (this.raidControllerUUID == null && this.summonActionTickCount == 0 && this.lootTable != null) {
                     PomkotsDataPack.ChestData chestData = PomkotsDataPackManager.getInstance().getDataPack().getChestData(this.lootTable.toString());
                     RaidDefinition raidData = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(chestData.raid_id);
                     if (raidData != null) {
                        if ("activate".equals(raidData.type)) {
                           List<BaseBossEntity> bosses = RaidControllerEntity.getInactiveBossesAroundPos(this.level, this.getBlockPos());
                           if (bosses.isEmpty()) {
                              this.sendMessageForOpener("{text.pomkotsmechs.messages.pomkotscube.01}");
                              break;
                           }
                        }

                        this.summonActionTickCount = 1;
                        if (!PomkotsMechs.CONFIG.debugModeEnabled) {
                           p.getMainHandItem().shrink(1);
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
            if (!this.level.isClientSide) {
               if (p.getMainHandItem().is((Item)PomkotsMechs.CUBEKEY_ITEM_PURPLE.get())) {
                  if (this.raidControllerUUID == null && this.summonActionTickCount == 0 && this.lootTable != null) {
                     PomkotsDataPack.ChestData chestData = PomkotsDataPackManager.getInstance().getDataPack().getChestData(this.lootTable.toString());
                     RaidDefinition raidData = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(chestData.raid_id);
                     if (raidData != null) {
                        if ("activate".equals(raidData.type)) {
                           List<BaseBossEntity> bosses = RaidControllerEntity.getInactiveBossesAroundPos(this.level, this.getBlockPos());
                           if (bosses.isEmpty()) {
                              this.sendMessageForOpener("{text.pomkotsmechs.messages.pomkotscube.01}");
                              break;
                           }
                        }

                        this.summonActionTickCount = 1;
                        p.getMainHandItem().shrink(1);
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
      if (this.level != null && !this.level.isClientSide) {
         if (this.mode == 0) {
            this.tryRefill();
         } else if (this.summonActionTickCount > 0) {
            if (this.opener == null || !this.opener.isAlive()) {
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
         } else if (this.raidControllerEntity == null || !this.raidControllerEntity.isAlive()) {
            this.raidControllerUUID = null;
            this.raidControllerEntity = null;
            this.setChanged();
         }
      }
   }

   private void spawnAlertEffect() {
      AlertEntity e;
      if (this.getMode() == 1) {
         e = new AlertEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.ALERT.get(), this.level);
      } else {
         e = new AlertEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.ALERTRED.get(), this.level);
      }

      BlockPos bp = this.getBlockPos();
      e.setPos(bp.getX() + 0.5, bp.getY(), bp.getZ() + 0.5);
      this.level.addFreshEntity(e);
   }

   private void startRaid() {
      if (this.opener != null
         && this.lootTable != null
         && this.lootTable.getNamespace().equals("pomkotsmechs")
         && PomkotsDataPackManager.getInstance().getDataPack().getChestData(this.lootTable.toString()) != null) {
         PomkotsDataPack.ChestData chestData = PomkotsDataPackManager.getInstance().getDataPack().getChestData(this.lootTable.toString());
         RaidDefinition raidData = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(chestData.raid_id);
         if (raidData != null) {
            RaidControllerEntity rce = (RaidControllerEntity)((EntityType)PomkotsMechs.RAID_CONTROLLER.get()).create(this.level);
            if (rce != null) {
               BlockPos raidEntityPos = this.getRaidEntitySpawnPos(raidData);
               rce.setPos(raidEntityPos.getX(), raidEntityPos.getY(), raidEntityPos.getZ());
               BlockPos cubePos = this.getBlockPos();
               String failCommand;
               if (raidData.type.equals(RaidControllerEntity.RaidType.ACTIVATE.getId())) {
                  List<BaseBossEntity> bosses = RaidControllerEntity.getInactiveBossesAroundPos(this.level, this.getBlockPos());
                  if (bosses != null && !bosses.isEmpty()) {
                     BaseBossEntity boss = bosses.get(0);
                     UUID uuid = boss.getUUID();
                     BlockPos pos = boss.blockPosition();
                     failCommand = String.format("pomkots:reset_boss_pos %s %d %d %d", uuid, pos.getX(), pos.getY(), pos.getZ());
                  } else {
                     failCommand = "say mission failed...";
                  }
               } else {
                  failCommand = "say mission failed...";
               }

               CompoundTag tag = RaidControllerEntity.buildCompoundTag(
                  chestData.raid_id,
                  this.opener.getUUID(),
                  "data modify block " + cubePos.getX() + " " + cubePos.getY() + " " + cubePos.getZ() + " pomkotsmechsCubeMode set value 0",
                  failCommand
               );
               rce.readAdditionalSaveData(tag);
               this.level.addFreshEntity(rce);
               this.raidControllerUUID = rce.getUUID();
               this.raidControllerEntity = rce;
               this.setChanged();
            }
         } else {
            PomkotsMechs.LOGGER.error("Specified raid does not exist:" + chestData.raid_id);
         }
      }
   }

   private BlockPos getRaidEntitySpawnPos(RaidDefinition raidData) {
      if (RaidControllerEntity.RaidType.SWEEP_BOSS_BOX.getId().equals(raidData.type)) {
         Direction facing = (Direction)this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
         Direction behind = facing.getOpposite();
         return this.getBlockPos().relative(behind, 15);
      } else {
         return this.getBlockPos();
      }
   }

   public void unpackLootTable(Player player) {
      if (!this.level.isClientSide && this.refillLootTable == null) {
         this.refillLootTable = this.lootTable;
         super.unpackLootTable(player);
         this.compactStacksRespectNBT();
         this.setChanged();
      }
   }

   private void compactStacksRespectNBT() {
      NonNullList<ItemStack> items = this.getItems();
      List<ItemStack> merged = new ArrayList<>();

      for (ItemStack stack : items) {
         if (!stack.isEmpty()) {
            for (ItemStack existing : merged) {
               if (ItemStack.isSameItemSameTags(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
                  int move = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                  existing.grow(move);
                  stack.shrink(move);
                  if (stack.isEmpty()) {
                     break;
                  }
               }
            }

            if (!stack.isEmpty()) {
               merged.add(stack.copy());
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

      for (int i = 0; i < this.getContainerSize(); i++) {
         ItemStack stack = this.getItem(i);
         if (!stack.isEmpty()) {
            boolean mergedFlag = false;

            for (ItemStack existing : merged) {
               if (ItemStack.isSameItemSameTags(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
                  int move = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                  existing.grow(move);
                  stack.shrink(move);
                  if (stack.isEmpty()) {
                     mergedFlag = true;
                     break;
                  }
               }
            }

            if (!stack.isEmpty()) {
               merged.add(stack.copy());
            }
         }
      }

      for (int i = 0; i < this.getContainerSize(); i++) {
         this.setItem(i, ItemStack.EMPTY);
      }

      int slot = 0;

      for (ItemStack stack : merged) {
         while (!stack.isEmpty() && slot < this.getContainerSize()) {
            ItemStack split = stack.split(stack.getMaxStackSize());
            this.setItem(slot++, split);
         }
      }
   }

   private void tryRefill() {
      if (this.openedOnce) {
         if (this.level != null && !this.level.isClientSide) {
            long now = this.level.dayTime();
            if (now - this.lastRefillGameTime >= 24000L) {
               this.refillLoot();
               this.lastRefillGameTime = now;
               this.openedOnce = false;
               this.updateMode(this.getDefaultMode());
               this.setChanged();
            }
         }
      }
   }

   private void refillLoot() {
      if (this.level instanceof ServerLevel serverLevel) {
         if (this.refillLootTable != null) {
            this.clearContent();
            this.setLootTable(this.refillLootTable, new Random().nextLong());
            this.refillLootTable = null;
         }
      }
   }

   public void playOpenFailAnimation() {
      if (this.level.isClientSide) {
         this.playLockedAnimation = true;
      }
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               event -> {
                  float curOpenness = ((PomkotsCubeBlockEntity)event.getAnimatable()).getOpenNess(event.getPartialTick());
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
      return this.level != null ? this.level.getGameTime() : 0.0;
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
         player.sendSystemMessage(Utils.string2Component(message));
      }
   }
}
