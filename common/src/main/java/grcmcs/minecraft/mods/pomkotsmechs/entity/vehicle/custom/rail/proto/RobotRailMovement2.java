package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.rail.proto;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public class RobotRailMovement2 {
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
   private static final double BURIED_RAIL_HEIGHT_OFFSET = 1.0;
   private BlockPos prevBlockPos = null;
   private Vec3 prevPos = null;

   public void moveRobotOnRail(Entity targetEntity, Vec3 currentVelocity) {
      BlockPos entityBlockPos = targetEntity.m_20183_();
      Vec3 entityPos = targetEntity.m_20182_();
      Level level = targetEntity.m_9236_();
      RobotRailMovement2.RailDetectionResult railResult = this.findRailBlockBelow(level, entityBlockPos);
      if (railResult != null) {
         Vec3 horizontalVelocity = new Vec3(currentVelocity.f_82479_, currentVelocity.f_82480_, currentVelocity.f_82481_);
         if (horizontalVelocity.m_82553_() < 0.01) {
            targetEntity.m_20256_(Vec3.f_82478_);
         } else {
            RobotRailMovement2.RailPathMoveResult result = this.processRailPathMovement(level, railResult, entityPos, horizontalVelocity);
            targetEntity.m_6034_(result.finalPosition.f_82479_, result.finalPosition.f_82480_, result.finalPosition.f_82481_);
            targetEntity.m_20256_(result.finalVelocity);
         }
      }
   }

   private RobotRailMovement2.RailPathMoveResult processRailPathMovement(
      Level level, RobotRailMovement2.RailDetectionResult railResult, Vec3 startPos, Vec3 initialVelocity
   ) {
      List<RobotRailMovement2.RailPathSegment> railPath = this.buildRailPath(level, railResult.railPosition, railResult.isBuried, initialVelocity);
      Vec3 currentPos = startPos;
      Vec3 currentVelocity = initialVelocity;
      double remainingDistance = initialVelocity.m_82553_();

      for (RobotRailMovement2.RailPathSegment segment : railPath) {
         if (remainingDistance <= 0.001) {
            break;
         }

         RobotRailMovement2.RailSegmentResult segmentResult = this.processRailSegment(level, segment, currentPos, currentVelocity, remainingDistance);
         currentPos = segmentResult.exitPosition;
         currentVelocity = segmentResult.exitVelocity;
         remainingDistance = segmentResult.remainingDistance;
         if (currentVelocity.m_82553_() < 0.01) {
            currentVelocity = Vec3.f_82478_;
            break;
         }
      }

      return new RobotRailMovement2.RailPathMoveResult(currentPos, currentVelocity);
   }

   private List<RobotRailMovement2.RailPathSegment> buildRailPath(Level level, BlockPos startRailPos, boolean isBuried, Vec3 initialVelocity) {
      List<RobotRailMovement2.RailPathSegment> railPath = new ArrayList<>();
      BlockPos currentRailPos = startRailPos;
      boolean currentIsBuried = isBuried;
      Vec3 currentDirection = initialVelocity.m_82541_();
      int maxRailsToCheck = Math.min((int)Math.ceil(initialVelocity.m_82553_() * 2.0), 40);

      for (int i = 0; i < maxRailsToCheck && currentRailPos != null && this.isValidRailBlock(level, currentRailPos); i++) {
         BlockState railState = level.m_8055_(currentRailPos);
         RailShape railShape = this.getRailShape(railState);
         RobotRailMovement2.RailPathSegment segment = new RobotRailMovement2.RailPathSegment(currentRailPos, railState, railShape, currentIsBuried);
         railPath.add(segment);
         Vec3 exitDirection = this.getRailExitDirection(railShape, currentDirection);
         RobotRailMovement2.RailDetectionResult nextRailResult = this.findNextRailBlock(level, currentRailPos, railShape, exitDirection);
         if (nextRailResult == null) {
            break;
         }

         currentRailPos = nextRailResult.railPosition;
         currentIsBuried = nextRailResult.isBuried;
         currentDirection = exitDirection;
      }

      return railPath;
   }

   private RobotRailMovement2.RailSegmentResult processRailSegment(
      Level level, RobotRailMovement2.RailPathSegment segment, Vec3 entryPos, Vec3 entryVelocity, double remainingDistance
   ) {
      BlockPos railPos = segment.position;
      BlockState railState = segment.state;
      RailShape railShape = segment.shape;
      boolean isBuried = segment.isBuried;
      double minX = railPos.m_123341_();
      double maxX = railPos.m_123341_() + 1.0;
      double minZ = railPos.m_123343_();
      double maxZ = railPos.m_123343_() + 1.0;
      Vec3 adjustedEntryPos = this.adjustPositionToRailBounds(entryPos, minX, maxX, minZ, maxZ);
      Vec3 currentVelocity = entryVelocity;
      currentVelocity = currentVelocity.m_82490_(0.97);
      currentVelocity = this.applyRailTypeEffects(railState, currentVelocity);
      Vec3 railDirection;
      if (this.isSlopeRail(railShape)) {
         railDirection = this.getSlopeRailDirectionFixed(railShape, currentVelocity, adjustedEntryPos, railPos);
      } else {
         railDirection = this.getRailDirection(railShape, currentVelocity);
      }

      double speed = currentVelocity.m_82553_();
      Vec3 constrainedVelocity = railDirection.m_82490_(speed);
      constrainedVelocity = this.applySlopeEffectsFixed(railShape, constrainedVelocity, railDirection);
      if (constrainedVelocity.m_82553_() > 20.0) {
         constrainedVelocity = constrainedVelocity.m_82541_().m_82490_(20.0);
      }

      if (this.isSlopeRail(railShape)) {
         constrainedVelocity = this.handleSlopeMinimumSpeed(railShape, constrainedVelocity, railDirection);
      } else if (constrainedVelocity.m_82553_() < 0.01) {
         constrainedVelocity = Vec3.f_82478_;
      }

      double segmentDistance = this.calculateSegmentDistance(adjustedEntryPos, constrainedVelocity, minX, maxX, minZ, maxZ);
      double actualMoveDistance = Math.min(segmentDistance, remainingDistance);
      Vec3 exitPosition = adjustedEntryPos.m_82549_(constrainedVelocity.m_82541_().m_82490_(actualMoveDistance));
      exitPosition = this.adjustHeightForRail(exitPosition, railShape, railPos, isBuried);
      exitPosition = this.adjustPositionToRailBounds(exitPosition, minX, maxX, minZ, maxZ);
      return new RobotRailMovement2.RailSegmentResult(exitPosition, constrainedVelocity, remainingDistance - actualMoveDistance);
   }

   private Vec3 getSlopeRailDirectionFixed(RailShape railShape, Vec3 currentVelocity, Vec3 currentPos, BlockPos railPos) {
      double posX = currentPos.f_82479_ - railPos.m_123341_();
      double posZ = currentPos.f_82481_ - railPos.m_123343_();
      Vec3 horizontalVelocity = new Vec3(currentVelocity.f_82479_, 0.0, currentVelocity.f_82481_);
      switch (railShape) {
         case ASCENDING_EAST:
            return !(horizontalVelocity.f_82479_ > 0.1) && (!(Math.abs(horizontalVelocity.f_82479_) < 0.1) || !(posX < 0.5))
               ? new Vec3(-1.0, -1.0, 0.0).m_82541_()
               : new Vec3(1.0, 1.0, 0.0).m_82541_();
         case ASCENDING_WEST:
            if (!(horizontalVelocity.f_82479_ < -0.1) && (!(Math.abs(horizontalVelocity.f_82479_) < 0.1) || !(posX > 0.5))) {
               return new Vec3(1.0, -1.0, 0.0).m_82541_();
            }

            return new Vec3(-1.0, 1.0, 0.0).m_82541_();
         case ASCENDING_NORTH:
            if (!(horizontalVelocity.f_82481_ < -0.1) && (!(Math.abs(horizontalVelocity.f_82481_) < 0.1) || !(posZ > 0.5))) {
               return new Vec3(0.0, -1.0, 1.0).m_82541_();
            }

            return new Vec3(0.0, 1.0, -1.0).m_82541_();
         case ASCENDING_SOUTH:
            if (!(horizontalVelocity.f_82481_ > 0.1) && (!(Math.abs(horizontalVelocity.f_82481_) < 0.1) || !(posZ < 0.5))) {
               return new Vec3(0.0, -1.0, -1.0).m_82541_();
            }

            return new Vec3(0.0, 1.0, 1.0).m_82541_();
         default:
            return new Vec3(1.0, 0.0, 0.0);
      }
   }

   private Vec3 applySlopeEffectsFixed(RailShape railShape, Vec3 velocity, Vec3 railDirection) {
      if (!this.isSlopeRail(railShape)) {
         return velocity;
      } else {
         boolean isGoingUp = railDirection.f_82480_ > 0.0;
         if (isGoingUp) {
            double deceleration = 0.011;
            double newSpeed = velocity.m_82553_() * (1.0 - deceleration);
            newSpeed = Math.max(newSpeed, 0.05);
            return railDirection.m_82490_(newSpeed);
         } else {
            double acceleration = 0.02;
            double newSpeed = velocity.m_82553_() * (1.0 + acceleration);
            return railDirection.m_82490_(newSpeed);
         }
      }
   }

   private Vec3 handleSlopeMinimumSpeed(RailShape railShape, Vec3 velocity, Vec3 railDirection) {
      if (velocity.m_82553_() < 0.05) {
         if (railDirection.f_82480_ < 0.0) {
            return railDirection.m_82490_(0.1);
         }

         Vec3 downDirection = this.getOppositeDirection(railShape);
         return downDirection.m_82490_(0.025);
      } else {
         return velocity;
      }
   }

   private Vec3 getOppositeDirection(RailShape railShape) {
      switch (railShape) {
         case ASCENDING_EAST:
            return new Vec3(-1.0, -1.0, 0.0).m_82541_();
         case ASCENDING_WEST:
            return new Vec3(1.0, -1.0, 0.0).m_82541_();
         case ASCENDING_NORTH:
            return new Vec3(0.0, -1.0, 1.0).m_82541_();
         case ASCENDING_SOUTH:
            return new Vec3(0.0, -1.0, -1.0).m_82541_();
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
      if (velocity.m_82553_() < 0.001) {
         return 0.0;
      }

      Vec3 direction = velocity.m_82541_();
      double maxDistance = velocity.m_82553_();
      double distanceX = Double.MAX_VALUE;
      double distanceZ = Double.MAX_VALUE;
      if (Math.abs(direction.f_82479_) > 0.001) {
         double boundaryX = direction.f_82479_ > 0.0 ? maxX : minX;
         distanceX = Math.abs((boundaryX - startPos.f_82479_) / direction.f_82479_);
      }

      if (Math.abs(direction.f_82481_) > 0.001) {
         double boundaryZ = direction.f_82481_ > 0.0 ? maxZ : minZ;
         distanceZ = Math.abs((boundaryZ - startPos.f_82481_) / direction.f_82481_);
      }

      double minDistance = Math.min(distanceX, distanceZ);
      return !(minDistance <= 0.0) && minDistance != Double.MAX_VALUE ? Math.min(minDistance, maxDistance) : Math.min(1.0, maxDistance);
   }

   private Vec3 adjustHeightForRail(Vec3 position, RailShape railShape, BlockPos railPos, boolean isBuried) {
      double baseRailY = railPos.m_123342_() + 0.0625;
      double robotY = isBuried ? baseRailY + 1.0 : baseRailY;
      switch (railShape) {
         case ASCENDING_EAST: {
            double progressX = position.f_82479_ - railPos.m_123341_();
            progressX = Math.max(0.0, Math.min(1.0, progressX));
            return new Vec3(position.f_82479_, robotY + progressX, position.f_82481_);
         }
         case ASCENDING_WEST: {
            double progressX = 1.0 - (position.f_82479_ - railPos.m_123341_());
            progressX = Math.max(0.0, Math.min(1.0, progressX));
            return new Vec3(position.f_82479_, robotY + progressX, position.f_82481_);
         }
         case ASCENDING_NORTH: {
            double progressZ = 1.0 - (position.f_82481_ - railPos.m_123343_());
            progressZ = Math.max(0.0, Math.min(1.0, progressZ));
            return new Vec3(position.f_82479_, robotY + progressZ, position.f_82481_);
         }
         case ASCENDING_SOUTH: {
            double progressZ = position.f_82481_ - railPos.m_123343_();
            progressZ = Math.max(0.0, Math.min(1.0, progressZ));
            return new Vec3(position.f_82479_, robotY + progressZ, position.f_82481_);
         }
         default:
            return new Vec3(position.f_82479_, robotY, position.f_82481_);
      }
   }

   private Vec3 adjustPositionToRailBounds(Vec3 pos, double minX, double maxX, double minZ, double maxZ) {
      double clampedX = Math.max(minX, Math.min(maxX, pos.f_82479_));
      double clampedZ = Math.max(minZ, Math.min(maxZ, pos.f_82481_));
      return new Vec3(clampedX, pos.f_82480_, clampedZ);
   }

   private Vec3 applyRailTypeEffects(BlockState railState, Vec3 velocity) {
      if (railState.m_60734_() instanceof PoweredRailBlock) {
         boolean powered = (Boolean)railState.m_61143_(PoweredRailBlock.f_55215_);
         if (powered) {
            Vec3 direction = velocity.m_82553_() > 0.0 ? velocity.m_82541_() : new Vec3(1.0, 0.0, 0.0);
            return velocity.m_82549_(direction.m_82490_(4.0));
         } else {
            return velocity.m_82490_(0.95);
         }
      } else {
         return velocity.m_82490_(0.98);
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
            return currentVelocity.f_82481_ >= 0.0 ? new Vec3(0.0, 0.0, 1.0) : new Vec3(0.0, 0.0, -1.0);
         case EAST_WEST:
            return currentVelocity.f_82479_ >= 0.0 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(-1.0, 0.0, 0.0);
         case SOUTH_EAST:
            return this.chooseCurveDirection(currentVelocity, new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0));
         case SOUTH_WEST:
            return this.chooseCurveDirection(currentVelocity, new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0));
         case NORTH_WEST:
            return this.chooseCurveDirection(currentVelocity, new Vec3(-1.0, 0.0, 0.0), new Vec3(0.0, 0.0, -1.0));
         case NORTH_EAST:
            return this.chooseCurveDirection(currentVelocity, new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 0.0, -1.0));
         default:
            return currentVelocity.m_82553_() > 0.0 ? currentVelocity.m_82541_() : new Vec3(1.0, 0.0, 0.0);
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
            return entryDirection.f_82481_ >= 0.0 ? new Vec3(0.0, 0.0, 1.0) : new Vec3(0.0, 0.0, -1.0);
         case EAST_WEST:
            return entryDirection.f_82479_ >= 0.0 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(-1.0, 0.0, 0.0);
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
      if (velocity.m_82553_() < 0.01) {
         return dir1;
      }

      double speed1 = Math.abs(velocity.m_82526_(dir1));
      double speed2 = Math.abs(velocity.m_82526_(dir2));
      return speed1 > speed2 ? dir2.m_82490_(velocity.m_82553_()) : dir1.m_82490_(velocity.m_82553_());
   }

   private RobotRailMovement2.RailDetectionResult findNextRailBlock(Level level, BlockPos currentRail, RailShape railShape, Vec3 direction) {
      for (BlockPos pos : this.getPossibleNextRailPositions(currentRail, railShape, direction)) {
         if (this.isValidRailBlock(level, pos)) {
            Vec3 dir = new Vec3(pos.m_123341_() - currentRail.m_123341_(), pos.m_123342_() - currentRail.m_123342_(), pos.m_123343_() - currentRail.m_123343_());
            if (direction.m_82526_(dir) > 0.0) {
               return new RobotRailMovement2.RailDetectionResult(pos, false);
            }
         }

         BlockPos buriedPos = pos.m_7495_();
         if (this.isValidRailBlock(level, buriedPos)) {
            Vec3 dir = new Vec3(pos.m_123341_() - currentRail.m_123341_(), pos.m_123342_() - currentRail.m_123342_(), pos.m_123343_() - currentRail.m_123343_());
            if (direction.m_82526_(dir) > 0.0) {
               return new RobotRailMovement2.RailDetectionResult(buriedPos, true);
            }
         }
      }

      return null;
   }

   private List<BlockPos> getPossibleNextRailPositions(BlockPos currentRail, RailShape railShape, Vec3 direction) {
      List<BlockPos> positions = new ArrayList<>();
      switch (railShape) {
         case ASCENDING_EAST:
            positions.add(currentRail.m_122029_().m_7494_());
            positions.add(currentRail.m_122024_().m_7495_());
            break;
         case ASCENDING_WEST:
            positions.add(currentRail.m_122024_().m_7494_());
            positions.add(currentRail.m_122029_().m_7495_());
            break;
         case ASCENDING_NORTH:
            positions.add(currentRail.m_122012_().m_7494_());
            positions.add(currentRail.m_122019_().m_7495_());
            break;
         case ASCENDING_SOUTH:
            positions.add(currentRail.m_122019_().m_7494_());
            positions.add(currentRail.m_122012_().m_7495_());
            break;
         case NORTH_SOUTH:
            positions.add(currentRail.m_122012_());
            positions.add(currentRail.m_122019_());
            break;
         case EAST_WEST:
            positions.add(currentRail.m_122029_());
            positions.add(currentRail.m_122024_());
            break;
         case SOUTH_EAST:
            positions.add(currentRail.m_122019_());
            positions.add(currentRail.m_122029_());
            break;
         case SOUTH_WEST:
            positions.add(currentRail.m_122019_());
            positions.add(currentRail.m_122024_());
            break;
         case NORTH_WEST:
            positions.add(currentRail.m_122012_());
            positions.add(currentRail.m_122024_());
            break;
         case NORTH_EAST:
            positions.add(currentRail.m_122012_());
            positions.add(currentRail.m_122029_());
      }

      return positions;
   }

   public RobotRailMovement2.RailDetectionResult findRailBlockBelow(Level level, BlockPos pos) {
      for (int y = pos.m_123342_() + 1; y >= pos.m_123342_(); y--) {
         BlockPos checkPos = new BlockPos(pos.m_123341_(), y, pos.m_123343_());
         if (this.isValidRailBlock(level, checkPos)) {
            return new RobotRailMovement2.RailDetectionResult(checkPos, false);
         }
      }

      for (int y = pos.m_123342_(); y >= pos.m_123342_() - 2; y--) {
         BlockPos checkPos = new BlockPos(pos.m_123341_(), y, pos.m_123343_());
         if (this.isValidRailBlock(level, checkPos)) {
            return new RobotRailMovement2.RailDetectionResult(checkPos, true);
         }
      }

      return null;
   }

   private boolean isValidRailBlock(Level level, BlockPos pos) {
      BlockState state = level.m_8055_(pos);
      return state.m_60734_() instanceof BaseRailBlock;
   }

   private RailShape getRailShape(BlockState railState) {
      return railState.m_60734_() instanceof BaseRailBlock
         ? (RailShape)railState.m_61143_(((BaseRailBlock)railState.m_60734_()).m_7978_())
         : RailShape.NORTH_SOUTH;
   }

   private static class RailDetectionResult {
      final BlockPos railPosition;
      final boolean isBuried;

      RailDetectionResult(BlockPos railPosition, boolean isBuried) {
         this.railPosition = railPosition;
         this.isBuried = isBuried;
      }
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
      final boolean isBuried;

      RailPathSegment(BlockPos position, BlockState state, RailShape shape, boolean isBuried) {
         this.position = position;
         this.state = state;
         this.shape = shape;
         this.isBuried = isBuried;
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
