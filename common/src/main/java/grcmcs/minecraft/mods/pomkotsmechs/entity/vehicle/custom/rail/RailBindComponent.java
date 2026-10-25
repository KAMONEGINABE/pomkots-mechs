package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.rail;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public class RailBindComponent {
   private static final double RAIL_FRICTION = 0.02;
   private static final double POWERED_RAIL_ACCELERATION = 4.0;
   private static final double POWERED_RAIL_DECELERATION = 0.05;
   private static final double MAX_SPEED = 20.0;
   private static final double MIN_SPEED = 0.01;
   private static final double NATURAL_DECAY = 0.97;
   private static final double SLOPE_ACCELERATION = 0.01;
   private static final double SLOPE_DECELERATION = 0.001;
   private static final double MAX_RAILS_PER_TICK = 40.0;
   private static final double SLOPE_ENTRY_THRESHOLD = 0.1;
   private static final double SLOPE_MIN_SPEED = 0.05;

   public void moveRobotOnRail(Pmvc01Entity targetEntity, Vec3 currentVelocity) {
      BlockPos entityBlockPos = targetEntity.blockPosition();
      Vec3 entityPos = targetEntity.position();
      Level level = targetEntity.level();
      BlockPos railBlockPos = this.findRailBlockBelow(level, entityBlockPos);
      if (railBlockPos == null) {
         targetEntity.unbindFromRail();
      } else {
         Vec3 horizontalVelocity = new Vec3(currentVelocity.x, currentVelocity.y, currentVelocity.z);
         if (horizontalVelocity.length() < 0.01) {
            targetEntity.setDeltaMovement(Vec3.ZERO);
         } else {
            RailBindComponent.RailPathMoveResult result = this.processRailPathMovement(level, railBlockPos, entityPos, horizontalVelocity);
            targetEntity.setPos(result.finalPosition.x, result.finalPosition.y, result.finalPosition.z);
            targetEntity.setDeltaMovement(result.finalVelocity);
         }
      }
   }

   private RailBindComponent.RailPathMoveResult processRailPathMovement(Level level, BlockPos startRailPos, Vec3 startPos, Vec3 initialVelocity) {
      List<RailBindComponent.RailPathSegment> railPath = this.buildRailPath(level, startRailPos, initialVelocity);
      Vec3 currentPos = startPos;
      Vec3 currentVelocity = initialVelocity;
      double remainingDistance = initialVelocity.length();

      for (RailBindComponent.RailPathSegment segment : railPath) {
         if (remainingDistance <= 0.001) {
            break;
         }

         RailBindComponent.RailSegmentResult segmentResult = this.processRailSegment(level, segment, currentPos, currentVelocity, remainingDistance);
         currentPos = segmentResult.exitPosition;
         currentVelocity = segmentResult.exitVelocity;
         remainingDistance = segmentResult.remainingDistance;
         if (currentVelocity.length() < 0.01) {
            currentVelocity = Vec3.ZERO;
            break;
         }
      }

      return new RailBindComponent.RailPathMoveResult(currentPos, currentVelocity);
   }

   private List<RailBindComponent.RailPathSegment> buildRailPath(Level level, BlockPos startRailPos, Vec3 initialVelocity) {
      List<RailBindComponent.RailPathSegment> railPath = new ArrayList<>();
      BlockPos currentRailPos = startRailPos;
      Vec3 currentDirection = initialVelocity.normalize();
      int maxRailsToCheck = Math.min((int)Math.ceil(initialVelocity.length() * 2.0), 40);

      for (int i = 0; i < maxRailsToCheck && currentRailPos != null && this.isValidRailBlock(level, currentRailPos); i++) {
         BlockState railState = level.getBlockState(currentRailPos);
         RailShape railShape = this.getRailShape(railState);
         RailBindComponent.RailPathSegment segment = new RailBindComponent.RailPathSegment(currentRailPos, railState, railShape);
         railPath.add(segment);
         Vec3 exitDirection = this.getRailExitDirection(railShape, currentDirection);
         BlockPos nextRailPos = this.findNextRailBlock(level, currentRailPos, railShape, exitDirection);
         currentRailPos = nextRailPos;
         currentDirection = exitDirection;
      }

      return railPath;
   }

   private RailBindComponent.RailSegmentResult processRailSegment(
      Level level, RailBindComponent.RailPathSegment segment, Vec3 entryPos, Vec3 entryVelocity, double remainingDistance
   ) {
      BlockPos railPos = segment.position;
      BlockState railState = segment.state;
      RailShape railShape = segment.shape;
      double minX = railPos.getX();
      double maxX = railPos.getX() + 1.0;
      double minZ = railPos.getZ();
      double maxZ = railPos.getZ() + 1.0;
      Vec3 adjustedEntryPos = this.adjustPositionToRailBounds(entryPos, minX, maxX, minZ, maxZ);
      Vec3 currentVelocity = entryVelocity;
      currentVelocity = currentVelocity.scale(0.97);
      currentVelocity = this.applyRailTypeEffects(railState, currentVelocity);
      Vec3 railDirection;
      if (this.isSlopeRail(railShape)) {
         railDirection = this.getSlopeRailDirectionFixed(railShape, currentVelocity, adjustedEntryPos, railPos);
      } else {
         railDirection = this.getRailDirection(railShape, currentVelocity);
      }

      double speed = currentVelocity.length();
      Vec3 constrainedVelocity = railDirection.scale(speed);
      constrainedVelocity = this.applySlopeEffectsFixed(railShape, constrainedVelocity, railDirection);
      if (constrainedVelocity.length() > 20.0) {
         constrainedVelocity = constrainedVelocity.normalize().scale(20.0);
      }

      if (this.isSlopeRail(railShape)) {
         constrainedVelocity = this.handleSlopeMinimumSpeed(railShape, constrainedVelocity, railDirection);
      } else if (constrainedVelocity.length() < 0.01) {
         constrainedVelocity = Vec3.ZERO;
      }

      double segmentDistance = this.calculateSegmentDistance(adjustedEntryPos, constrainedVelocity, minX, maxX, minZ, maxZ);
      double actualMoveDistance = Math.min(segmentDistance, remainingDistance);
      Vec3 exitPosition = adjustedEntryPos.add(constrainedVelocity.normalize().scale(actualMoveDistance));
      exitPosition = this.adjustHeightForRail(exitPosition, railShape, railPos);
      exitPosition = this.adjustPositionToRailBounds(exitPosition, minX, maxX, minZ, maxZ);
      return new RailBindComponent.RailSegmentResult(exitPosition, constrainedVelocity, remainingDistance - actualMoveDistance);
   }

   private Vec3 getSlopeRailDirectionFixed(RailShape railShape, Vec3 currentVelocity, Vec3 currentPos, BlockPos railPos) {
      double posX = currentPos.x - railPos.getX();
      double posZ = currentPos.z - railPos.getZ();
      Vec3 horizontalVelocity = new Vec3(currentVelocity.x, 0.0, currentVelocity.z);
      switch (railShape) {
         case ASCENDING_EAST:
            return !(horizontalVelocity.x > 0.1) && (!(Math.abs(horizontalVelocity.x) < 0.1) || !(posX < 0.5))
               ? new Vec3(-1.0, -1.0, 0.0).normalize()
               : new Vec3(1.0, 1.0, 0.0).normalize();
         case ASCENDING_WEST:
            if (!(horizontalVelocity.x < -0.1) && (!(Math.abs(horizontalVelocity.x) < 0.1) || !(posX > 0.5))) {
               return new Vec3(1.0, -1.0, 0.0).normalize();
            }

            return new Vec3(-1.0, 1.0, 0.0).normalize();
         case ASCENDING_NORTH:
            if (!(horizontalVelocity.z < -0.1) && (!(Math.abs(horizontalVelocity.z) < 0.1) || !(posZ > 0.5))) {
               return new Vec3(0.0, -1.0, 1.0).normalize();
            }

            return new Vec3(0.0, 1.0, -1.0).normalize();
         case ASCENDING_SOUTH:
            if (!(horizontalVelocity.z > 0.1) && (!(Math.abs(horizontalVelocity.z) < 0.1) || !(posZ < 0.5))) {
               return new Vec3(0.0, -1.0, -1.0).normalize();
            }

            return new Vec3(0.0, 1.0, 1.0).normalize();
         default:
            return new Vec3(1.0, 0.0, 0.0);
      }
   }

   private Vec3 applySlopeEffectsFixed(RailShape railShape, Vec3 velocity, Vec3 railDirection) {
      if (!this.isSlopeRail(railShape)) {
         return velocity;
      } else {
         boolean isGoingUp = railDirection.y > 0.0;
         if (isGoingUp) {
            double deceleration = 0.011;
            double newSpeed = velocity.length() * (1.0 - deceleration);
            newSpeed = Math.max(newSpeed, 0.05);
            return railDirection.scale(newSpeed);
         } else {
            double acceleration = 0.02;
            double newSpeed = velocity.length() * (1.0 + acceleration);
            return railDirection.scale(newSpeed);
         }
      }
   }

   private Vec3 handleSlopeMinimumSpeed(RailShape railShape, Vec3 velocity, Vec3 railDirection) {
      if (velocity.length() < 0.05) {
         if (railDirection.y < 0.0) {
            return railDirection.scale(0.1);
         }

         Vec3 downDirection = this.getOppositeDirection(railShape);
         return downDirection.scale(0.025);
      } else {
         return velocity;
      }
   }

   private Vec3 getOppositeDirection(RailShape railShape) {
      switch (railShape) {
         case ASCENDING_EAST:
            return new Vec3(-1.0, -1.0, 0.0).normalize();
         case ASCENDING_WEST:
            return new Vec3(1.0, -1.0, 0.0).normalize();
         case ASCENDING_NORTH:
            return new Vec3(0.0, -1.0, 1.0).normalize();
         case ASCENDING_SOUTH:
            return new Vec3(0.0, -1.0, -1.0).normalize();
         default:
            return new Vec3(-1.0, 0.0, 0.0);
      }
   }

   private boolean isSlopeRail(RailShape railShape) {
      return railShape == RailShape.ASCENDING_EAST
         || railShape == RailShape.ASCENDING_WEST
         || railShape == RailShape.ASCENDING_NORTH
         || railShape == RailShape.ASCENDING_SOUTH;
   }

   private double calculateSegmentDistance(Vec3 startPos, Vec3 velocity, double minX, double maxX, double minZ, double maxZ) {
      if (velocity.length() < 0.001) {
         return 0.0;
      }

      Vec3 direction = velocity.normalize();
      double maxDistance = velocity.length();
      double distanceX = Double.MAX_VALUE;
      double distanceZ = Double.MAX_VALUE;
      if (Math.abs(direction.x) > 0.001) {
         double boundaryX = direction.x > 0.0 ? maxX : minX;
         distanceX = Math.abs((boundaryX - startPos.x) / direction.x);
      }

      if (Math.abs(direction.z) > 0.001) {
         double boundaryZ = direction.z > 0.0 ? maxZ : minZ;
         distanceZ = Math.abs((boundaryZ - startPos.z) / direction.z);
      }

      double minDistance = Math.min(distanceX, distanceZ);
      return !(minDistance <= 0.0) && minDistance != Double.MAX_VALUE ? Math.min(minDistance, maxDistance) : Math.min(1.0, maxDistance);
   }

   private Vec3 adjustHeightForRail(Vec3 position, RailShape railShape, BlockPos railPos) {
      double railY = railPos.getY() + 0.0625;
      switch (railShape) {
         case ASCENDING_EAST: {
            double progressX = position.x - railPos.getX();
            progressX = Math.max(0.0, Math.min(1.0, progressX));
            return new Vec3(position.x, railY + progressX, position.z);
         }
         case ASCENDING_WEST: {
            double progressX = 1.0 - (position.x - railPos.getX());
            progressX = Math.max(0.0, Math.min(1.0, progressX));
            return new Vec3(position.x, railY + progressX, position.z);
         }
         case ASCENDING_NORTH: {
            double progressZ = 1.0 - (position.z - railPos.getZ());
            progressZ = Math.max(0.0, Math.min(1.0, progressZ));
            return new Vec3(position.x, railY + progressZ, position.z);
         }
         case ASCENDING_SOUTH: {
            double progressZ = position.z - railPos.getZ();
            progressZ = Math.max(0.0, Math.min(1.0, progressZ));
            return new Vec3(position.x, railY + progressZ, position.z);
         }
         default:
            return new Vec3(position.x, railY, position.z);
      }
   }

   private Vec3 adjustPositionToRailBounds(Vec3 pos, double minX, double maxX, double minZ, double maxZ) {
      double clampedX = Math.max(minX, Math.min(maxX, pos.x));
      double clampedZ = Math.max(minZ, Math.min(maxZ, pos.z));
      return new Vec3(clampedX, pos.y, clampedZ);
   }

   private Vec3 applyRailTypeEffects(BlockState railState, Vec3 velocity) {
      if (railState.getBlock() instanceof PoweredRailBlock) {
         boolean powered = (Boolean)railState.getValue(PoweredRailBlock.POWERED);
         if (powered) {
            Vec3 direction = velocity.length() > 0.0 ? velocity.normalize() : new Vec3(1.0, 0.0, 0.0);
            return velocity.add(direction.scale(4.0));
         } else {
            return velocity.scale(0.95);
         }
      } else {
         return velocity.scale(0.98);
      }
   }

   private Vec3 getRailDirection(RailShape railShape, Vec3 currentVelocity) {
      switch (railShape) {
         case ASCENDING_EAST:
            return new Vec3(1.0, 0.0, 0.0);
         case ASCENDING_WEST:
            return new Vec3(-1.0, 0.0, 0.0);
         case ASCENDING_NORTH:
            return new Vec3(0.0, 0.0, -1.0);
         case ASCENDING_SOUTH:
            return new Vec3(0.0, 0.0, 1.0);
         case NORTH_SOUTH:
            return currentVelocity.z >= 0.0 ? new Vec3(0.0, 0.0, 1.0) : new Vec3(0.0, 0.0, -1.0);
         case EAST_WEST:
            return currentVelocity.x >= 0.0 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(-1.0, 0.0, 0.0);
         case SOUTH_EAST:
            return this.chooseCurveDirection(currentVelocity, new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0));
         case SOUTH_WEST:
            return this.chooseCurveDirection(currentVelocity, new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0));
         case NORTH_WEST:
            return this.chooseCurveDirection(currentVelocity, new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, -1.0));
         case NORTH_EAST:
            return this.chooseCurveDirection(currentVelocity, new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, -1.0));
         default:
            return currentVelocity.length() > 0.0 ? currentVelocity.normalize() : new Vec3(1.0, 0.0, 0.0);
      }
   }

   private Vec3 getRailExitDirection(RailShape railShape, Vec3 entryDirection) {
      switch (railShape) {
         case ASCENDING_EAST:
            return new Vec3(1.0, 0.0, 0.0);
         case ASCENDING_WEST:
            return new Vec3(-1.0, 0.0, 0.0);
         case ASCENDING_NORTH:
            return new Vec3(0.0, 0.0, -1.0);
         case ASCENDING_SOUTH:
            return new Vec3(0.0, 0.0, 1.0);
         case NORTH_SOUTH:
            return entryDirection.z >= 0.0 ? new Vec3(0.0, 0.0, 1.0) : new Vec3(0.0, 0.0, -1.0);
         case EAST_WEST:
            return entryDirection.x >= 0.0 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(-1.0, 0.0, 0.0);
         case SOUTH_EAST:
            return this.chooseCurveDirection(entryDirection, new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0));
         case SOUTH_WEST:
            return this.chooseCurveDirection(entryDirection, new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0));
         case NORTH_WEST:
            return this.chooseCurveDirection(entryDirection, new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, -1.0));
         case NORTH_EAST:
            return this.chooseCurveDirection(entryDirection, new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, -1.0));
         default:
            return entryDirection;
      }
   }

   private Vec3 chooseCurveDirection(Vec3 velocity, Vec3 dir1, Vec3 dir2) {
      if (velocity.length() < 0.01) {
         return dir1;
      }

      double speed1 = Math.abs(velocity.dot(dir1));
      double speed2 = Math.abs(velocity.dot(dir2));
      return speed1 > speed2 ? dir2.scale(velocity.length()) : dir1.scale(velocity.length());
   }

   private BlockPos findNextRailBlock(Level level, BlockPos currentRail, RailShape railShape, Vec3 direction) {
      List<BlockPos> possiblePositions = this.getPossibleNextRailPositions(currentRail, railShape, direction);
      return possiblePositions.stream()
         .filter(pos -> this.isValidRailBlock(level, pos))
         .min(
            (pos1, pos2) -> {
               Vec3 dir1 = new Vec3(
                  pos1.getX() - currentRail.getX(), pos1.getY() - currentRail.getY(), pos1.getZ() - currentRail.getZ()
               );
               Vec3 dir2 = new Vec3(
                  pos2.getX() - currentRail.getX(), pos2.getY() - currentRail.getY(), pos2.getZ() - currentRail.getZ()
               );
               double dot1 = direction.dot(dir1);
               double dot2 = direction.dot(dir2);
               return Double.compare(dot2, dot1);
            }
         )
         .orElse(null);
   }

   private List<BlockPos> getPossibleNextRailPositions(BlockPos currentRail, RailShape railShape, Vec3 direction) {
      List<BlockPos> positions = new ArrayList<>();
      switch (railShape) {
         case ASCENDING_EAST:
            positions.add(currentRail.east().above());
            positions.add(currentRail.west().below());
            break;
         case ASCENDING_WEST:
            positions.add(currentRail.west().above());
            positions.add(currentRail.east().below());
            break;
         case ASCENDING_NORTH:
            positions.add(currentRail.north().above());
            positions.add(currentRail.south().below());
            break;
         case ASCENDING_SOUTH:
            positions.add(currentRail.south().above());
            positions.add(currentRail.north().below());
            break;
         case NORTH_SOUTH:
            positions.add(currentRail.north());
            positions.add(currentRail.south());
            break;
         case EAST_WEST:
            positions.add(currentRail.east());
            positions.add(currentRail.west());
            break;
         case SOUTH_EAST:
            positions.add(currentRail.south());
            positions.add(currentRail.east());
            break;
         case SOUTH_WEST:
            positions.add(currentRail.south());
            positions.add(currentRail.west());
            break;
         case NORTH_WEST:
            positions.add(currentRail.north());
            positions.add(currentRail.west());
            break;
         case NORTH_EAST:
            positions.add(currentRail.north());
            positions.add(currentRail.east());
      }

      return positions;
   }

   public BlockPos findRailBlockBelow(Level level, BlockPos pos) {
      for (int y = pos.getY() + 1; y >= pos.getY() - 2; y--) {
         BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
         if (this.isValidRailBlock(level, checkPos)) {
            return checkPos;
         }
      }

      return null;
   }

   private boolean isValidRailBlock(Level level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      return state.getBlock() instanceof BaseRailBlock;
   }

   private RailShape getRailShape(BlockState railState) {
      return railState.getBlock() instanceof BaseRailBlock
         ? (RailShape)railState.getValue(((BaseRailBlock)railState.getBlock()).getShapeProperty())
         : RailShape.NORTH_SOUTH;
   }

   private static class RailPathMoveResult {
      final Vec3 finalPosition;
      final Vec3 finalVelocity;

      RailPathMoveResult(Vec3 finalPosition, Vec3 finalVelocity) {
         this.finalPosition = finalPosition;
         this.finalVelocity = finalVelocity;
      }
   }

   private static class RailPathSegment {
      final BlockPos position;
      final BlockState state;
      final RailShape shape;

      RailPathSegment(BlockPos position, BlockState state, RailShape shape) {
         this.position = position;
         this.state = state;
         this.shape = shape;
      }
   }

   private static class RailSegmentResult {
      final Vec3 exitPosition;
      final Vec3 exitVelocity;
      final double remainingDistance;

      RailSegmentResult(Vec3 exitPosition, Vec3 exitVelocity, double remainingDistance) {
         this.exitPosition = exitPosition;
         this.exitVelocity = exitVelocity;
         this.remainingDistance = remainingDistance;
      }
   }
}
