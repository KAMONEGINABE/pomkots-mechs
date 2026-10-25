package grcmcs.minecraft.mods.pomkotsmechs.block.arena;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.ElevatorEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ArenaGateBlockEntity extends BlockEntity {
   private UUID elevatorUuid = null;
   private BlockPos relativeDstBlockPos = BlockPos.f_121853_;

   public ArenaGateBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.ARENA_GATE_BLOCK_ENTITY.get(), pos, state);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, ArenaGateBlockEntity be) {
      if (!level.m_5776_() && !Utils.isMapEditingMode(level)) {
         if (level.m_46467_() % 200L == 0L) {
            be.ensureElevator();
         }
      }
   }

   public void ensureElevator() {
      if (this.f_58857_ instanceof ServerLevel serverLevel) {
         Entity var3 = null;
         if (this.elevatorUuid != null) {
            var3 = serverLevel.m_8791_(this.elevatorUuid);
         }

         if (!(var3 instanceof ElevatorEntity)) {
            this.spawnElevator(serverLevel);
         }
      }
   }

   public ElevatorEntity getElevator() {
      if (this.f_58857_ instanceof ServerLevel serverLevel) {
         Entity var4 = null;
         if (this.elevatorUuid != null) {
            var4 = serverLevel.m_8791_(this.elevatorUuid);
         }

         return var4 instanceof ElevatorEntity e ? e : null;
      } else {
         return null;
      }
   }

   public ElevatorEntity refreshElevator() {
      if (this.f_58857_ instanceof ServerLevel serverLevel) {
         if (this.elevatorUuid != null) {
            Entity entity = serverLevel.m_8791_(this.elevatorUuid);
            if (entity instanceof ElevatorEntity) {
               entity.m_146870_();
               this.elevatorUuid = null;
            }
         }

         return this.spawnElevator(serverLevel);
      } else {
         return null;
      }
   }

   private ElevatorEntity spawnElevator(ServerLevel level) {
      ElevatorEntity elevator = (ElevatorEntity)((EntityType)PomkotsMechs.ELEVATOR.get()).m_20615_(level);
      if (elevator == null) {
         return null;
      }

      elevator.m_6027_(this.f_58858_.m_123341_() + 0.5, this.f_58858_.m_123342_() + 1.2, this.f_58858_.m_123343_() + 0.5);
      BlockPos src = this.m_58899_();
      BlockPos dstR = this.relativeDstBlockPos;
      elevator.setSrcBlockPos(src.m_6630_(1));
      elevator.setDstBlockPos(new BlockPos(src.m_123341_() + dstR.m_123341_(), src.m_123342_() + dstR.m_123342_(), src.m_123343_() + dstR.m_123343_()));
      elevator.setMode(ElevatorEntity.Mode.IDLE);
      elevator.setFixedYaw(this.getBlockYaw());
      level.m_7967_(elevator);
      this.elevatorUuid = elevator.m_20148_();
      this.m_6596_();
      return elevator;
   }

   protected void m_183515_(CompoundTag tag) {
      super.m_183515_(tag);
      if (this.elevatorUuid != null) {
         tag.m_128362_(PomkotsMechs.nbtName("ElevatorUuid"), this.elevatorUuid);
      }

      Utils.saveBlockPos(tag, "Dst", this.relativeDstBlockPos);
   }

   public void m_142466_(CompoundTag tag) {
      super.m_142466_(tag);
      String nbtName = PomkotsMechs.nbtName("ElevatorUuid");
      if (tag.m_128403_(nbtName)) {
         this.elevatorUuid = tag.m_128342_(nbtName);
      }

      this.relativeDstBlockPos = Utils.loadBlockPos("Dst", tag);
   }

   private float getBlockYaw() {
      BlockState state = this.m_58900_();
      Direction facing = (Direction)state.m_61143_(ArenaGateBlock.FACING);

      return switch (facing) {
         case SOUTH -> 0.0F;
         case WEST -> 90.0F;
         case NORTH -> 180.0F;
         case EAST -> -90.0F;
         default -> 0.0F;
      };
   }

   private void rotateEntity(Entity ent, float yaw) {
      ent.m_146922_(yaw);
      ent.f_19859_ = yaw;
      ent.m_5616_(yaw);
      ent.m_5618_(yaw);
   }
}
