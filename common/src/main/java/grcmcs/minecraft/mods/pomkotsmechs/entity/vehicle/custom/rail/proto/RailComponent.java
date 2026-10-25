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
      if (!this.ent.m_9236_().f_46443_) {
         Level level = this.ent.m_9236_();
         if (!this.railBound) {
            this.findInitialRail(level, this.ent.m_20183_()).ifPresent(pos -> {
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

         Vec3 velocity = this.ent.m_20184_();
         double remainingDistance = velocity.m_82553_();
         Vec3 position = this.ent.m_20182_();
         Vec3 accumulatedMovement = Vec3.f_82478_;

         while (remainingDistance > 0.001 && this.currentRailPos != null) {
            List<BlockPos> neighbors = this.getConnectedRails(level, this.currentRailPos);
            BlockPos bestNext = null;
            double bestDot = -Double.MAX_VALUE;
            Vec3 moveDir = velocity.m_82541_();

            for (BlockPos neighbor : neighbors) {
               if (!neighbor.equals(this.previousRailPos) && this.isRail(level, neighbor)) {
                  Vec3 dir = Vec3.m_82512_(neighbor).m_82546_(Vec3.m_82512_(this.currentRailPos)).m_82541_();
                  double dot = moveDir.m_82526_(dir);
                  if (dot > bestDot) {
                     bestDot = dot;
                     bestNext = neighbor;
                  }
               }
            }

            if (bestNext == null) {
               break;
            }

            Vec3 nextCenter = Vec3.m_82512_(bestNext);
            Vec3 currentCenter = Vec3.m_82512_(this.currentRailPos);
            Vec3 segmentVec = nextCenter.m_82546_(currentCenter);
            double segmentLength = segmentVec.m_82553_();
            if (segmentLength <= remainingDistance) {
               accumulatedMovement = accumulatedMovement.m_82549_(segmentVec);
               remainingDistance -= segmentLength;
               this.previousRailPos = this.currentRailPos;
               this.currentRailPos = bestNext;
            } else {
               Vec3 step = segmentVec.m_82541_().m_82490_(remainingDistance);
               accumulatedMovement = accumulatedMovement.m_82549_(step);
               remainingDistance = 0.0;
            }
         }

         if (accumulatedMovement.m_82556_() > 0.0) {
            this.ent.m_20256_(accumulatedMovement);
            if (this.passedPoweredRail(level, Arrays.asList(this.currentRailPos, this.previousRailPos))) {
               Vec3 boosted = this.ent.m_20184_().m_82490_(1.2);
               this.ent.m_20256_(boosted);
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
      BlockState state = level.m_8055_(pos);
      return state.m_60734_() instanceof BaseRailBlock;
   }

   public Optional<BlockPos> findInitialRail(Level level, BlockPos pos) {
      if (this.isRail(level, pos)) {
         return Optional.of(pos);
      }

      BlockPos below = pos.m_7495_();
      return this.isRail(level, below) ? Optional.of(below) : Optional.empty();
   }

   public RailShape getRailShape(Level level, BlockPos pos) {
      BlockState state = level.m_8055_(pos);
      return state.m_60734_() instanceof BaseRailBlock ? (RailShape)state.m_61143_(((BaseRailBlock)state.m_60734_()).m_7978_()) : RailShape.NORTH_SOUTH;
   }

   public List<BlockPos> getConnectedRails(Level level, BlockPos pos) {
      RailShape shape = this.getRailShape(level, pos);
      List<BlockPos> neighbors = new ArrayList<>();
      switch (shape) {
         case NORTH_SOUTH:
            neighbors.add(pos.m_122012_());
            neighbors.add(pos.m_122019_());
            break;
         case EAST_WEST:
            neighbors.add(pos.m_122029_());
            neighbors.add(pos.m_122024_());
            break;
         case ASCENDING_EAST:
            neighbors.add(pos.m_122029_().m_7494_());
            neighbors.add(pos.m_122024_());
            break;
         case ASCENDING_WEST:
            neighbors.add(pos.m_122024_().m_7494_());
            neighbors.add(pos.m_122029_());
            break;
         case ASCENDING_NORTH:
            neighbors.add(pos.m_122012_().m_7494_());
            neighbors.add(pos.m_122019_());
            break;
         case ASCENDING_SOUTH:
            neighbors.add(pos.m_122019_().m_7494_());
            neighbors.add(pos.m_122012_());
            break;
         case NORTH_EAST:
            neighbors.add(pos.m_122012_());
            neighbors.add(pos.m_122029_());
            break;
         case NORTH_WEST:
            neighbors.add(pos.m_122012_());
            neighbors.add(pos.m_122024_());
            break;
         case SOUTH_EAST:
            neighbors.add(pos.m_122019_());
            neighbors.add(pos.m_122029_());
            break;
         case SOUTH_WEST:
            neighbors.add(pos.m_122019_());
            neighbors.add(pos.m_122024_());
      }

      return neighbors;
   }

   public boolean passedPoweredRail(Level level, List<BlockPos> path) {
      for (BlockPos pos : path) {
         if (pos != null && level.m_8055_(pos).m_60713_(Blocks.f_50030_) && (Boolean)level.m_8055_(pos).m_61143_(PoweredRailBlock.f_55215_)) {
            return true;
         }
      }

      return false;
   }
}
