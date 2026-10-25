package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CoreStoneBlockEntity extends BlockEntity {
   private String startTargetRaidName = null;
   private UUID raidControllerEntityUUID = null;
   private UUID raidOwnerUUID = null;
   private String raidFinalizeCommandSuccess = null;
   private String raidFinalizeCommandFail = null;
   private RaidControllerEntity raidControllerEntity = null;

   public CoreStoneBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.CORE_STONE_BLOCK_ENTITY.get(), pos, state);
   }

   public void m_142466_(CompoundTag tag) {
      super.m_142466_(tag);
      if (tag.m_128441_(PomkotsMechs.nbtName("StartTargetRaidName"))) {
         this.startTargetRaidName = tag.m_128461_(PomkotsMechs.nbtName("StartTargetRaidName"));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("RaidControllerEntityUUID"))) {
         this.raidControllerEntityUUID = tag.m_128342_(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("RaidOwnerUUID"))) {
         this.raidOwnerUUID = tag.m_128342_(PomkotsMechs.nbtName("RaidOwnerUUID"));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"))) {
         this.raidFinalizeCommandSuccess = tag.m_128461_(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"));
      }

      if (tag.m_128441_(PomkotsMechs.nbtName("RaidFinalizeCommandFail"))) {
         this.raidFinalizeCommandFail = tag.m_128461_(PomkotsMechs.nbtName("RaidFinalizeCommandFail"));
      }
   }

   protected void m_183515_(CompoundTag tag) {
      super.m_183515_(tag);
      if (this.startTargetRaidName != null) {
         tag.m_128359_(PomkotsMechs.nbtName("StartTargetRaidName"), this.startTargetRaidName);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("StartTargetRaidName"));
      }

      if (this.raidControllerEntityUUID != null) {
         tag.m_128362_(PomkotsMechs.nbtName("RaidControllerEntityUUID"), this.raidControllerEntityUUID);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
      }

      if (this.raidOwnerUUID != null) {
         tag.m_128362_(PomkotsMechs.nbtName("RaidOwnerUUID"), this.raidOwnerUUID);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("RaidOwnerUUID"));
      }

      if (this.raidFinalizeCommandSuccess != null) {
         tag.m_128359_(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"), this.raidFinalizeCommandSuccess);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"));
      }

      if (this.raidFinalizeCommandFail != null) {
         tag.m_128359_(PomkotsMechs.nbtName("RaidFinalizeCommandFail"), this.raidFinalizeCommandFail);
      } else {
         tag.m_128473_(PomkotsMechs.nbtName("RaidFinalizeCommandFail"));
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, CoreStoneBlockEntity be) {
      if (be.f_58857_ != null && !be.f_58857_.f_46443_) {
         ServerLevel serverLevel = (ServerLevel)be.f_58857_;
         if (be.startTargetRaidName != null) {
            if (be.raidControllerEntityUUID == null) {
               RaidControllerEntity rce = (RaidControllerEntity)((EntityType)PomkotsMechs.RAID_CONTROLLER.get()).m_20615_(serverLevel);
               if (rce != null) {
                  rce.m_6034_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
                  CompoundTag tag = RaidControllerEntity.buildCompoundTag(
                     be.startTargetRaidName, be.raidOwnerUUID, be.raidFinalizeCommandSuccess, be.raidFinalizeCommandFail
                  );
                  rce.m_7378_(tag);
                  be.f_58857_.m_7967_(rce);
                  be.raidControllerEntity = rce;
                  be.raidControllerEntityUUID = rce.m_20148_();
                  be.m_6596_();
               }
            } else if (be.raidControllerEntity == null) {
               be.raidControllerEntity = (RaidControllerEntity)serverLevel.m_8791_(be.raidControllerEntityUUID);
               if (be.raidControllerEntity == null) {
                  be.resetInternalStatus();
                  be.m_6596_();
               }
            }

            if (be.raidControllerEntity == null || !be.raidControllerEntity.m_6084_()) {
               be.resetInternalStatus();
               be.m_6596_();
            }
         }
      }
   }

   protected void resetInternalStatus() {
      this.startTargetRaidName = null;
      this.raidControllerEntity = null;
      this.raidControllerEntityUUID = null;
      this.raidOwnerUUID = null;
      this.raidFinalizeCommandSuccess = null;
      this.raidFinalizeCommandFail = null;
   }

   public void startRaid(String raidName, UUID owner, String successCommand, String failCommand) {
      this.startTargetRaidName = raidName;
      this.raidOwnerUUID = owner;
      this.raidFinalizeCommandSuccess = successCommand;
      this.raidFinalizeCommandFail = failCommand;
   }

   public boolean isRaidActive() {
      return this.startTargetRaidName != null;
   }
}
