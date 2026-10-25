package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechSalvagerMenu;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.save.PomkotsMechsSaveData;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class MechSalvagerBlockEntity extends BlockEntity implements MenuProvider, Container {
   private static final int MAX_SUMMON_TICK = 60;
   private int summonTick = 0;
   private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
   private MechSalvagerBlockEntity.LoadTicket ticket = null;
   private ServerLevel sourceLevel = null;
   private ServerPlayer targetPlayer = null;
   private Vec3 targetPosition = null;
   private UUID targetUUID = null;

   public MechSalvagerBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.MECH_SALVAGER_BLOCK_ENTITY.get(), pos, state);
   }

   public Component getDisplayName() {
      return Component.literal("Mech Salvager");
   }

   public int getContainerSize() {
      return this.items.size();
   }

   public boolean isEmpty() {
      return this.items.stream().allMatch(ItemStack::isEmpty);
   }

   public ItemStack getItem(int index) {
      return (ItemStack)this.items.get(index);
   }

   public ItemStack removeItem(int index, int count) {
      return ContainerHelper.removeItem(this.items, index, count);
   }

   public ItemStack removeItemNoUpdate(int index) {
      return ContainerHelper.takeItem(this.items, index);
   }

   public void setItem(int index, ItemStack stack) {
      this.items.set(index, stack);
      if (stack.getCount() > this.getMaxStackSize()) {
         stack.setCount(this.getMaxStackSize());
      }

      this.setChanged();
   }

   public void clearContent() {
      this.items.clear();
   }

   public boolean stillValid(Player player) {
      return player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) <= 64.0;
   }

   public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
      return new MechSalvagerMenu(id, playerInv, this, ContainerLevelAccess.create(this.level, this.getBlockPos()));
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      ContainerHelper.saveAllItems(tag, this.items);
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      ContainerHelper.loadAllItems(tag, this.items);
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, MechSalvagerBlockEntity be) {
      be.tick();
   }

   public void tick() {
      if (this.level instanceof ServerLevel) {
         if (this.summonTick == 0) {
            return;
         }

         this.checkSummonMech();
         this.summonTick--;
      }
   }

   public void checkSummonMech() {
      Entity e = this.sourceLevel.getEntity(this.targetUUID);
      if (e != null) {
         this.sendMessage(this.targetPlayer, "{text.pomkotsmechs.messages.mechsalvager.01}");
         this.teleportMech(this.targetPlayer, e, (ServerLevel)this.level, this.targetPosition);
         this.summonTick = 1;
      }

      if (this.summonTick == 1) {
         this.sourceLevel.getChunkSource().removeRegionTicket(this.ticket.ticketType, this.ticket.chunkPos, this.ticket.i, this.ticket.chunkPos);
         this.clearContext();
      }
   }

   private void clearContext() {
      this.ticket = null;
      this.sourceLevel = null;
      this.targetPosition = null;
      this.targetUUID = null;
      this.targetPlayer = null;
   }

   public void summon(ServerPlayer player, UUID mechUuid) {
      if (this.summonTick <= 0) {
         ServerLevel level = player.serverLevel();
         MinecraftServer server = level.getServer();
         Vec3 targetPos = this.getTargetPos();
         this.summonMech(player, server, mechUuid, level, targetPos);
      }
   }

   public void summonMech(ServerPlayer player, MinecraftServer server, UUID mechId, ServerLevel targetLevel, Vec3 summonPos) {
      for (ServerLevel lvl : server.getAllLevels()) {
         Entity e = lvl.getEntity(mechId);
         if (e != null) {
            this.teleportMech(player, e, targetLevel, summonPos);
            return;
         }
      }

      PomkotsMechsSaveData data = PomkotsMechsSaveData.get(server.overworld());
      PomkotsMechsSaveData.MechLocation info = data.get(mechId);
      if (info == null) {
         this.sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.04}");
      } else {
         ServerLevel sourceLevel = server.getLevel(info.dimension());
         if (sourceLevel == null) {
            this.sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.05}");
         } else {
            this.ticket = this.createTicket(info.chunkPos());
            sourceLevel.getChunkSource().addRegionTicket(this.ticket.ticketType, this.ticket.chunkPos, this.ticket.i, this.ticket.chunkPos);
            this.sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.06}");
            this.targetPosition = summonPos;
            this.sourceLevel = sourceLevel;
            this.summonTick = 60;
            this.targetUUID = mechId;
            this.targetPlayer = player;
         }
      }
   }

   public Vec3 getTargetPos() {
      BlockPos pos = this.getBlockPos();
      Direction facing = (Direction)this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

      Vec3 offset = switch (facing) {
         case NORTH -> new Vec3(0.0, 0.0, -2.0);
         case SOUTH -> new Vec3(0.0, 0.0, 2.0);
         case WEST -> new Vec3(-2.0, 0.0, 0.0);
         case EAST -> new Vec3(2.0, 0.0, 0.0);
         default -> Vec3.ZERO;
      };
      return Vec3.atCenterOf(pos).add(offset);
   }

   private boolean teleportMech(ServerPlayer player, Entity mech, ServerLevel targetLevel, Vec3 pos) {
      if (mech.level() != targetLevel) {
         this.sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.07}");
         return false;
      }

      mech.teleportTo(pos.x() + 0.5, pos.y(), pos.z() + 0.5);
      mech.setDeltaMovement(Vec3.ZERO);
      if (mech instanceof Pmvc01Entity pmvc) {
         pmvc.warpEffect(targetLevel);
      }

      this.sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.08}");
      return true;
   }

   public void sendMessage(ServerPlayer player, String msg) {
      player.sendSystemMessage(Utils.string2Component(msg));
   }

   private MechSalvagerBlockEntity.LoadTicket createTicket(ChunkPos pos) {
      MechSalvagerBlockEntity.LoadTicket ticket = new MechSalvagerBlockEntity.LoadTicket();
      ticket.ticketType = TicketType.FORCED;
      ticket.chunkPos = pos;
      ticket.i = 3;
      return ticket;
   }

   private static class LoadTicket {
      TicketType<ChunkPos> ticketType;
      ChunkPos chunkPos;
      int i;
   }
}
