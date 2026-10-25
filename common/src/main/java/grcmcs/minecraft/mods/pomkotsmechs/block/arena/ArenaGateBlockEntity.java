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
   private BlockPos relativeDstBlockPos = BlockPos.ZERO;

   public ArenaGateBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.ARENA_GATE_BLOCK_ENTITY.get(), pos, state);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, ArenaGateBlockEntity be) {
      if (!level.isClientSide() && !Utils.isMapEditingMode(level)) {
         if (level.getGameTime() % 200L == 0L) {
            be.ensureElevator();
         }
      }
   }

   public void ensureElevator() {
      if (this.level instanceof ServerLevel serverLevel) {
         Entity var3 = null;
         if (this.elevatorUuid != null) {
            var3 = serverLevel.getEntity(this.elevatorUuid);
         }

         if (!(var3 instanceof ElevatorEntity)) {
            this.spawnElevator(serverLevel);
         }
      }
   }

   public ElevatorEntity getElevator() {
      if (this.level instanceof ServerLevel serverLevel) {
         Entity var4 = null;
         if (this.elevatorUuid != null) {
            var4 = serverLevel.getEntity(this.elevatorUuid);
         }

         return var4 instanceof ElevatorEntity e ? e : null;
      } else {
         return null;
      }
   }

   public ElevatorEntity refreshElevator() {
      if (this.level instanceof ServerLevel serverLevel) {
         if (this.elevatorUuid != null) {
            Entity entity = serverLevel.getEntity(this.elevatorUuid);
            if (entity instanceof ElevatorEntity) {
               entity.discard();
               this.elevatorUuid = null;
            }
         }

         return this.spawnElevator(serverLevel);
      } else {
         return null;
      }
   }

   private ElevatorEntity spawnElevator(ServerLevel level) {
      ElevatorEntity elevator = (ElevatorEntity)((EntityType)PomkotsMechs.ELEVATOR.get()).create(level);
      if (elevator == null) {
         return null;
      }

      elevator.moveTo(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.2, this.worldPosition.getZ() + 0.5);
      BlockPos src = this.getBlockPos();
      BlockPos dstR = this.relativeDstBlockPos;
      elevator.setSrcBlockPos(src.above(1));
      elevator.setDstBlockPos(new BlockPos(src.getX() + dstR.getX(), src.getY() + dstR.getY(), src.getZ() + dstR.getZ()));
      elevator.setMode(ElevatorEntity.Mode.IDLE);
      elevator.setFixedYaw(this.getBlockYaw());
      level.addFreshEntity(elevator);
      this.elevatorUuid = elevator.getUUID();
      this.setChanged();
      return elevator;
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      if (this.elevatorUuid != null) {
         tag.putUUID(PomkotsMechs.nbtName("ElevatorUuid"), this.elevatorUuid);
      }

      Utils.saveBlockPos(tag, "Dst", this.relativeDstBlockPos);
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      String nbtName = PomkotsMechs.nbtName("ElevatorUuid");
      if (tag.hasUUID(nbtName)) {
         this.elevatorUuid = tag.getUUID(nbtName);
      }

      this.relativeDstBlockPos = Utils.loadBlockPos("Dst", tag);
   }

   private float getBlockYaw() {
      BlockState state = this.getBlockState();
      Direction facing = (Direction)state.getValue(ArenaGateBlock.FACING);

      return switch (facing) {
         case SOUTH -> 0.0F;
         case WEST -> 90.0F;
         case NORTH -> 180.0F;
         case EAST -> -90.0F;
         default -> 0.0F;
      };
   }

   private void rotateEntity(Entity ent, float yaw) {
      ent.setYRot(yaw);
      ent.yRotO = yaw;
      ent.setYHeadRot(yaw);
      ent.setYBodyRot(yaw);
   }
}
