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

   public void load(CompoundTag tag) {
      super.load(tag);
      if (tag.contains(PomkotsMechs.nbtName("StartTargetRaidName"))) {
         this.startTargetRaidName = tag.getString(PomkotsMechs.nbtName("StartTargetRaidName"));
      }

      if (tag.contains(PomkotsMechs.nbtName("RaidControllerEntityUUID"))) {
         this.raidControllerEntityUUID = tag.getUUID(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
      }

      if (tag.contains(PomkotsMechs.nbtName("RaidOwnerUUID"))) {
         this.raidOwnerUUID = tag.getUUID(PomkotsMechs.nbtName("RaidOwnerUUID"));
      }

      if (tag.contains(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"))) {
         this.raidFinalizeCommandSuccess = tag.getString(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"));
      }

      if (tag.contains(PomkotsMechs.nbtName("RaidFinalizeCommandFail"))) {
         this.raidFinalizeCommandFail = tag.getString(PomkotsMechs.nbtName("RaidFinalizeCommandFail"));
      }
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      if (this.startTargetRaidName != null) {
         tag.putString(PomkotsMechs.nbtName("StartTargetRaidName"), this.startTargetRaidName);
      } else {
         tag.remove(PomkotsMechs.nbtName("StartTargetRaidName"));
      }

      if (this.raidControllerEntityUUID != null) {
         tag.putUUID(PomkotsMechs.nbtName("RaidControllerEntityUUID"), this.raidControllerEntityUUID);
      } else {
         tag.remove(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
      }

      if (this.raidOwnerUUID != null) {
         tag.putUUID(PomkotsMechs.nbtName("RaidOwnerUUID"), this.raidOwnerUUID);
      } else {
         tag.remove(PomkotsMechs.nbtName("RaidOwnerUUID"));
      }

      if (this.raidFinalizeCommandSuccess != null) {
         tag.putString(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"), this.raidFinalizeCommandSuccess);
      } else {
         tag.remove(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"));
      }

      if (this.raidFinalizeCommandFail != null) {
         tag.putString(PomkotsMechs.nbtName("RaidFinalizeCommandFail"), this.raidFinalizeCommandFail);
      } else {
         tag.remove(PomkotsMechs.nbtName("RaidFinalizeCommandFail"));
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, CoreStoneBlockEntity be) {
      if (be.level != null && !be.level.isClientSide) {
         ServerLevel serverLevel = (ServerLevel)be.level;
         if (be.startTargetRaidName != null) {
            if (be.raidControllerEntityUUID == null) {
               RaidControllerEntity rce = (RaidControllerEntity)((EntityType)PomkotsMechs.RAID_CONTROLLER.get()).create(serverLevel);
               if (rce != null) {
                  rce.setPos(pos.getX(), pos.getY(), pos.getZ());
                  CompoundTag tag = RaidControllerEntity.buildCompoundTag(
                     be.startTargetRaidName, be.raidOwnerUUID, be.raidFinalizeCommandSuccess, be.raidFinalizeCommandFail
                  );
                  rce.readAdditionalSaveData(tag);
                  be.level.addFreshEntity(rce);
                  be.raidControllerEntity = rce;
                  be.raidControllerEntityUUID = rce.getUUID();
                  be.setChanged();
               }
            } else if (be.raidControllerEntity == null) {
               be.raidControllerEntity = (RaidControllerEntity)serverLevel.getEntity(be.raidControllerEntityUUID);
               if (be.raidControllerEntity == null) {
                  be.resetInternalStatus();
                  be.setChanged();
               }
            }

            if (be.raidControllerEntity == null || !be.raidControllerEntity.isAlive()) {
               be.resetInternalStatus();
               be.setChanged();
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
