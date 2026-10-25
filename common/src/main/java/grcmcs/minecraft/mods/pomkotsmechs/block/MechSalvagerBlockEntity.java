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
   private final NonNullList<ItemStack> items = NonNullList.m_122780_(2, ItemStack.f_41583_);
   private MechSalvagerBlockEntity.LoadTicket ticket = null;
   private ServerLevel sourceLevel = null;
   private ServerPlayer targetPlayer = null;
   private Vec3 targetPosition = null;
   private UUID targetUUID = null;

   public MechSalvagerBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.MECH_SALVAGER_BLOCK_ENTITY.get(), pos, state);
   }

   public Component m_5446_() {
      return Component.m_237113_("Mech Salvager");
   }

   public int m_6643_() {
      return this.items.size();
   }

   public boolean m_7983_() {
      return this.items.stream().allMatch(ItemStack::m_41619_);
   }

   public ItemStack m_8020_(int index) {
      return (ItemStack)this.items.get(index);
   }

   public ItemStack m_7407_(int index, int count) {
      return ContainerHelper.m_18969_(this.items, index, count);
   }

   public ItemStack m_8016_(int index) {
      return ContainerHelper.m_18966_(this.items, index);
   }

   public void m_6836_(int index, ItemStack stack) {
      this.items.set(index, stack);
      if (stack.m_41613_() > this.m_6893_()) {
         stack.m_41764_(this.m_6893_());
      }

      this.m_6596_();
   }

   public void m_6211_() {
      this.items.clear();
   }

   public boolean m_6542_(Player player) {
      return player.m_20238_(Vec3.m_82512_(this.f_58858_)) <= 64.0;
   }

   public AbstractContainerMenu m_7208_(int id, Inventory playerInv, Player player) {
      return new MechSalvagerMenu(id, playerInv, this, ContainerLevelAccess.m_39289_(this.f_58857_, this.m_58899_()));
   }

   protected void m_183515_(CompoundTag tag) {
      super.m_183515_(tag);
      ContainerHelper.m_18973_(tag, this.items);
   }

   public void m_142466_(CompoundTag tag) {
      super.m_142466_(tag);
      ContainerHelper.m_18980_(tag, this.items);
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, MechSalvagerBlockEntity be) {
      be.tick();
   }

   public void tick() {
      if (this.f_58857_ instanceof ServerLevel) {
         if (this.summonTick == 0) {
            return;
         }

         this.checkSummonMech();
         this.summonTick--;
      }
   }

   public void checkSummonMech() {
      Entity e = this.sourceLevel.m_8791_(this.targetUUID);
      if (e != null) {
         this.sendMessage(this.targetPlayer, "{text.pomkotsmechs.messages.mechsalvager.01}");
         this.teleportMech(this.targetPlayer, e, (ServerLevel)this.f_58857_, this.targetPosition);
         this.summonTick = 1;
      }

      if (this.summonTick == 1) {
         this.sourceLevel.m_7726_().m_8438_(this.ticket.ticketType, this.ticket.chunkPos, this.ticket.i, this.ticket.chunkPos);
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
         ServerLevel level = player.m_284548_();
         MinecraftServer server = level.m_7654_();
         Vec3 targetPos = this.getTargetPos();
         this.summonMech(player, server, mechUuid, level, targetPos);
      }
   }

   public void summonMech(ServerPlayer player, MinecraftServer server, UUID mechId, ServerLevel targetLevel, Vec3 summonPos) {
      for (ServerLevel lvl : server.m_129785_()) {
         Entity e = lvl.m_8791_(mechId);
         if (e != null) {
            this.teleportMech(player, e, targetLevel, summonPos);
            return;
         }
      }

      PomkotsMechsSaveData data = PomkotsMechsSaveData.get(server.m_129783_());
      PomkotsMechsSaveData.MechLocation info = data.get(mechId);
      if (info == null) {
         this.sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.04}");
      } else {
         ServerLevel sourceLevel = server.m_129880_(info.dimension());
         if (sourceLevel == null) {
            this.sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.05}");
         } else {
            this.ticket = this.createTicket(info.chunkPos());
            sourceLevel.m_7726_().m_8387_(this.ticket.ticketType, this.ticket.chunkPos, this.ticket.i, this.ticket.chunkPos);
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
      BlockPos pos = this.m_58899_();
      Direction facing = (Direction)this.m_58900_().m_61143_(BlockStateProperties.f_61374_);

      Vec3 offset = switch (facing) {
         case NORTH -> new Vec3(0.0, 0.0, -2.0);
         case SOUTH -> new Vec3(0.0, 0.0, 2.0);
         case WEST -> new Vec3(-2.0, 0.0, 0.0);
         case EAST -> new Vec3(2.0, 0.0, 0.0);
         default -> Vec3.f_82478_;
      };
      return Vec3.m_82512_(pos).m_82549_(offset);
   }

   private boolean teleportMech(ServerPlayer player, Entity mech, ServerLevel targetLevel, Vec3 pos) {
      if (mech.m_9236_() != targetLevel) {
         this.sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.07}");
         return false;
      }

      mech.m_6021_(pos.m_7096_() + 0.5, pos.m_7098_(), pos.m_7094_() + 0.5);
      mech.m_20256_(Vec3.f_82478_);
      if (mech instanceof Pmvc01Entity pmvc) {
         pmvc.warpEffect(targetLevel);
      }

      this.sendMessage(player, "{text.pomkotsmechs.messages.mechsalvager.08}");
      return true;
   }

   public void sendMessage(ServerPlayer player, String msg) {
      player.m_213846_(Utils.string2Component(msg));
   }

   private MechSalvagerBlockEntity.LoadTicket createTicket(ChunkPos pos) {
      MechSalvagerBlockEntity.LoadTicket ticket = new MechSalvagerBlockEntity.LoadTicket();
      ticket.ticketType = TicketType.f_9445_;
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
