package grcmcs.minecraft.mods.pomkotsmechs.block.arena;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.ArenaReceptionistEntity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaLayout;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ArenaControllerBlockEntity extends BlockEntity {
   private String arenaId = ArenaManager.DEFAULT_ARENA_ID;
   private UUID receptionistUuid;

   public ArenaControllerBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.ARENA_CONTROLLER_BLOCK_ENTITY.get(), pos, state);
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putString("ArenaId", this.arenaId);
      if (this.receptionistUuid != null) {
         tag.putUUID("ReceptionistUuid", this.receptionistUuid);
      }
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      this.arenaId = tag.getString("ArenaId");
      if (tag.hasUUID("ReceptionistUuid")) {
         this.receptionistUuid = tag.getUUID("ReceptionistUuid");
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, ArenaControllerBlockEntity be) {
      if (!level.isClientSide() && !Utils.isMapEditingMode(level)) {
         if (level.getGameTime() % 200L == 0L) {
            be.ensureReceptionist();
         }
      }
   }

   public void ensureReceptionist() {
      if (this.level instanceof ServerLevel serverLevel) {
         ArenaManager.ensureArena(serverLevel.getServer(), this.arenaId, GlobalPos.of(serverLevel.dimension(), this.getBlockPos()));
         Entity var3 = null;
         if (this.receptionistUuid != null) {
            var3 = serverLevel.getEntity(this.receptionistUuid);
         }

         if (!(var3 instanceof ArenaReceptionistEntity)) {
            this.spawnReceptionist(serverLevel);
         }
      }
   }

   private void spawnReceptionist(ServerLevel level) {
      ArenaReceptionistEntity npc = (ArenaReceptionistEntity)((EntityType)PomkotsMechs.ARENA_RECEP.get()).create(level);
      if (npc != null) {
         npc.moveTo(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1, this.worldPosition.getZ() + 0.5);
         npc.setArenaId(this.arenaId);
         level.addFreshEntity(npc);
         this.receptionistUuid = npc.getUUID();
         this.setChanged();
      }
   }

   public ArenaLayout scanLayout() {
      ArenaLayout layout = new ArenaLayout();
      BlockPos center = this.getBlockPos();
      int radius = 300;
      BlockPos.betweenClosedStream(center.offset(-radius, -16, -radius), center.offset(radius, 40, radius)).forEach(pos -> {
         BlockState state = this.level.getBlockState(pos);
         if (state.is((Block)PomkotsMechs.ARENA_GATE_BLOCK.get())) {
            layout.getGates().add(pos.immutable());
         }

         if (state.is((Block)PomkotsMechs.ARENA_BATTLEFIELD_ANCHOR_BLOCK.get())) {
            layout.setBattleAnchor(pos.immutable());
         }

         if (state.is((Block)PomkotsMechs.ARENA_TELEPORT_BLOCK.get())) {
            layout.setTeleportPos(pos.immutable());
         }
      });
      return layout;
   }
}
