package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.rail.proto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public class RailComponent {
   private boolean railBound = false;
   private LivingEntity ent;
   private BlockPos currentRailPos = null;
   private BlockPos previousRailPos = null;

   public RailComponent(LivingEntity ent) {
      this.ent = ent;
   }

   public void tick() {
      if (!this.ent.level().isClientSide) {
         Level level = this.ent.level();
         if (!this.railBound) {
            this.findInitialRail(level, this.ent.blockPosition()).ifPresent(pos -> {
               this.railBound = true;
               this.currentRailPos = pos;
               this.previousRailPos = null;
            });
            return;
         }

         if (this.currentRailPos == null || !this.isRail(level, this.currentRailPos)) {
            this.unbindRail();
            return;
         }

         Vec3 velocity = this.ent.getDeltaMovement();
         double remainingDistance = velocity.length();
         Vec3 position = this.ent.position();
         Vec3 accumulatedMovement = Vec3.ZERO;

         while (remainingDistance > 0.001 && this.currentRailPos != null) {
            List<BlockPos> neighbors = this.getConnectedRails(level, this.currentRailPos);
            BlockPos bestNext = null;
            double bestDot = -Double.MAX_VALUE;
            Vec3 moveDir = velocity.normalize();

            for (BlockPos neighbor : neighbors) {
               if (!neighbor.equals(this.previousRailPos) && this.isRail(level, neighbor)) {
                  Vec3 dir = Vec3.atCenterOf(neighbor).subtract(Vec3.atCenterOf(this.currentRailPos)).normalize();
                  double dot = moveDir.dot(dir);
                  if (dot > bestDot) {
                     bestDot = dot;
                     bestNext = neighbor;
                  }
               }
            }

            if (bestNext == null) {
               break;
            }

            Vec3 nextCenter = Vec3.atCenterOf(bestNext);
            Vec3 currentCenter = Vec3.atCenterOf(this.currentRailPos);
            Vec3 segmentVec = nextCenter.subtract(currentCenter);
            double segmentLength = segmentVec.length();
            if (segmentLength <= remainingDistance) {
               accumulatedMovement = accumulatedMovement.add(segmentVec);
               remainingDistance -= segmentLength;
               this.previousRailPos = this.currentRailPos;
               this.currentRailPos = bestNext;
            } else {
               Vec3 step = segmentVec.normalize().scale(remainingDistance);
               accumulatedMovement = accumulatedMovement.add(step);
               remainingDistance = 0.0;
            }
         }

         if (accumulatedMovement.lengthSqr() > 0.0) {
            this.ent.setDeltaMovement(accumulatedMovement);
            if (this.passedPoweredRail(level, Arrays.asList(this.currentRailPos, this.previousRailPos))) {
               Vec3 boosted = this.ent.getDeltaMovement().scale(1.2);
               this.ent.setDeltaMovement(boosted);
            }
         } else {
            this.unbindRail();
         }
      }
   }

   public void unbindRail() {
      this.railBound = false;
      this.currentRailPos = null;
      this.previousRailPos = null;
   }

   public boolean isRailBound() {
      return this.railBound;
   }

   public boolean isRail(Level level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      return state.getBlock() instanceof BaseRailBlock;
   }

   public Optional<BlockPos> findInitialRail(Level level, BlockPos pos) {
      if (this.isRail(level, pos)) {
         return Optional.of(pos);
      }

      BlockPos below = pos.below();
      return this.isRail(level, below) ? Optional.of(below) : Optional.empty();
   }

   public RailShape getRailShape(Level level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      return state.getBlock() instanceof BaseRailBlock ? (RailShape)state.getValue(((BaseRailBlock)state.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
   }

   public List<BlockPos> getConnectedRails(Level level, BlockPos pos) {
      RailShape shape = this.getRailShape(level, pos);
      List<BlockPos> neighbors = new ArrayList<>();
      switch (shape) {
         case NORTH_SOUTH:
            neighbors.add(pos.north());
            neighbors.add(pos.south());
            break;
         case EAST_WEST:
            neighbors.add(pos.east());
            neighbors.add(pos.west());
            break;
         case ASCENDING_EAST:
            neighbors.add(pos.east().above());
            neighbors.add(pos.west());
            break;
         case ASCENDING_WEST:
            neighbors.add(pos.west().above());
            neighbors.add(pos.east());
            break;
         case ASCENDING_NORTH:
            neighbors.add(pos.north().above());
            neighbors.add(pos.south());
            break;
         case ASCENDING_SOUTH:
            neighbors.add(pos.south().above());
            neighbors.add(pos.north());
            break;
         case NORTH_EAST:
            neighbors.add(pos.north());
            neighbors.add(pos.east());
            break;
         case NORTH_WEST:
            neighbors.add(pos.north());
            neighbors.add(pos.west());
            break;
         case SOUTH_EAST:
            neighbors.add(pos.south());
            neighbors.add(pos.east());
            break;
         case SOUTH_WEST:
            neighbors.add(pos.south());
            neighbors.add(pos.west());
      }

      return neighbors;
   }

   public boolean passedPoweredRail(Level level, List<BlockPos> path) {
      for (BlockPos pos : path) {
         if (pos != null && level.getBlockState(pos).is(Blocks.POWERED_RAIL) && (Boolean)level.getBlockState(pos).getValue(PoweredRailBlock.POWERED)) {
            return true;
         }
      }

      return false;
   }
}
