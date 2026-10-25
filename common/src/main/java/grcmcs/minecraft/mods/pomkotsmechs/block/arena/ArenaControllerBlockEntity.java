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

   protected void m_183515_(CompoundTag tag) {
      super.m_183515_(tag);
      tag.m_128359_("ArenaId", this.arenaId);
      if (this.receptionistUuid != null) {
         tag.m_128362_("ReceptionistUuid", this.receptionistUuid);
      }
   }

   public void m_142466_(CompoundTag tag) {
      super.m_142466_(tag);
      this.arenaId = tag.m_128461_("ArenaId");
      if (tag.m_128403_("ReceptionistUuid")) {
         this.receptionistUuid = tag.m_128342_("ReceptionistUuid");
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, ArenaControllerBlockEntity be) {
      if (!level.m_5776_() && !Utils.isMapEditingMode(level)) {
         if (level.m_46467_() % 200L == 0L) {
            be.ensureReceptionist();
         }
      }
   }

   public void ensureReceptionist() {
      if (this.f_58857_ instanceof ServerLevel serverLevel) {
         ArenaManager.ensureArena(serverLevel.m_7654_(), this.arenaId, GlobalPos.m_122643_(serverLevel.m_46472_(), this.m_58899_()));
         Entity var3 = null;
         if (this.receptionistUuid != null) {
            var3 = serverLevel.m_8791_(this.receptionistUuid);
         }

         if (!(var3 instanceof ArenaReceptionistEntity)) {
            this.spawnReceptionist(serverLevel);
         }
      }
   }

   private void spawnReceptionist(ServerLevel level) {
      ArenaReceptionistEntity npc = (ArenaReceptionistEntity)((EntityType)PomkotsMechs.ARENA_RECEP.get()).m_20615_(level);
      if (npc != null) {
         npc.m_6027_(this.f_58858_.m_123341_() + 0.5, this.f_58858_.m_123342_() + 1, this.f_58858_.m_123343_() + 0.5);
         npc.setArenaId(this.arenaId);
         level.m_7967_(npc);
         this.receptionistUuid = npc.m_20148_();
         this.m_6596_();
      }
   }

   public ArenaLayout scanLayout() {
      ArenaLayout layout = new ArenaLayout();
      BlockPos center = this.m_58899_();
      int radius = 300;
      BlockPos.m_121990_(center.m_7918_(-radius, -16, -radius), center.m_7918_(radius, 40, radius)).forEach(pos -> {
         BlockState state = this.f_58857_.m_8055_(pos);
         if (state.m_60713_((Block)PomkotsMechs.ARENA_GATE_BLOCK.get())) {
            layout.getGates().add(pos.m_7949_());
         }

         if (state.m_60713_((Block)PomkotsMechs.ARENA_BATTLEFIELD_ANCHOR_BLOCK.get())) {
            layout.setBattleAnchor(pos.m_7949_());
         }

         if (state.m_60713_((Block)PomkotsMechs.ARENA_TELEPORT_BLOCK.get())) {
            layout.setTeleportPos(pos.m_7949_());
         }
      });
      return layout;
   }
}
