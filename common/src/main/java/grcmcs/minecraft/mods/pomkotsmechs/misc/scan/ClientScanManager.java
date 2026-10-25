package grcmcs.minecraft.mods.pomkotsmechs.misc.scan;

import grcmcs.minecraft.mods.pomkotsmechs.block.ScanTargetBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.tags.ModEntityTags;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ClientScanManager {
   private static final Map<Integer, ClientScanPulseState> ACTIVE_PULSES = new HashMap<>();
   private static final Map<Integer, Long> ENTITY_HIGHLIGHT_END = new HashMap<>();
   private static final Map<BlockPos, Long> BLOCK_ENTITY_HIGHLIGHT_END = new HashMap<>();
   private static final int BLOCK_ENTITY_REFRESH_INTERVAL = 10;

   private ClientScanManager() {
   }

   public static void clientTick() {
      Minecraft minecraft = Minecraft.m_91087_();
      ClientLevel level = minecraft.f_91073_;
      Player localPlayer = minecraft.f_91074_;
      if (level != null && localPlayer != null) {
         long gameTime = level.m_46467_();
         ENTITY_HIGHLIGHT_END.entrySet().removeIf(entry -> entry.getValue() <= gameTime);
         BLOCK_ENTITY_HIGHLIGHT_END.entrySet().removeIf(entry -> entry.getValue() <= gameTime);
         Map<Integer, ScanPulseEntity> currentPulses = new HashMap<>();

         for (Entity entity : level.m_104735_()) {
            if (entity instanceof ScanPulseEntity pulse && pulse.isVisibleTo(localPlayer)) {
               currentPulses.put(pulse.m_19879_(), pulse);
               tickPulse(level, pulse);
            }
         }

         ACTIVE_PULSES.keySet().removeIf(id -> !currentPulses.containsKey(id));
      } else {
         clear();
      }
   }

   private static void tickPulse(ClientLevel level, ScanPulseEntity pulse) {
      ClientScanPulseState state = ACTIVE_PULSES.computeIfAbsent(pulse.m_19879_(), ignored -> new ClientScanPulseState());
      float currentRadius = pulse.getRadius(0.0F);
      float previousRadius = state.getLastRadius();
      if (state.getAge() % 10 == 0) {
         collectBlockEntityCandidates(level, pulse, state, currentRadius);
      }

      scanEntities(level, pulse, state, previousRadius, currentRadius);
      scanBlockEntities(level, pulse, state, previousRadius, currentRadius);
      state.setLastRadius(currentRadius);
      state.tickAge();
   }

   private static void scanEntities(ClientLevel level, ScanPulseEntity pulse, ClientScanPulseState state, float previousRadius, float currentRadius) {
      if (!(currentRadius <= 0.0F)) {
         Vec3 center = pulse.m_20182_();
         AABB searchBox = new AABB(
            center.f_82479_ - currentRadius,
            center.f_82480_ - currentRadius,
            center.f_82481_ - currentRadius,
            center.f_82479_ + currentRadius,
            center.f_82480_ + currentRadius,
            center.f_82481_ + currentRadius
         );

         for (Entity entity : level.m_6249_(pulse, searchBox, ClientScanManager::isEntityScanTarget)) {
            if (!state.getDetectedEntityIds().contains(entity.m_19879_())) {
               double distance = distanceToAabb(center, entity.m_20191_());
               if (didWaveReachTarget(previousRadius, currentRadius, distance)) {
                  state.getDetectedEntityIds().add(entity.m_19879_());
                  ENTITY_HIGHLIGHT_END.put(entity.m_19879_(), level.m_46467_() + pulse.getHighlightTicks());
               }
            }
         }
      }
   }

   private static boolean isEntityScanTarget(Entity entity) {
      return !entity.m_6084_() ? false : entity instanceof Pmvc01Entity || entity.m_6095_().m_204039_(ModEntityTags.SCAN_TARGETS);
   }

   private static double distanceToAabb(Vec3 center, AABB box) {
      double nearestX = Mth.m_14008_(center.f_82479_, box.f_82288_, box.f_82291_);
      double nearestY = Mth.m_14008_(center.f_82480_, box.f_82289_, box.f_82292_);
      double nearestZ = Mth.m_14008_(center.f_82481_, box.f_82290_, box.f_82293_);
      return center.m_82554_(new Vec3(nearestX, nearestY, nearestZ));
   }

   private static void collectBlockEntityCandidates(ClientLevel level, ScanPulseEntity pulse, ClientScanPulseState state, float currentRadius) {
      Vec3 center = pulse.m_20182_();
      float maxRadius = pulse.getMaxRadius();
      int minChunkX = SectionPos.m_123171_(Mth.m_14107_(center.f_82479_ - maxRadius));
      int maxChunkX = SectionPos.m_123171_(Mth.m_14107_(center.f_82479_ + maxRadius));
      int minChunkZ = SectionPos.m_123171_(Mth.m_14107_(center.f_82481_ - maxRadius));
      int maxChunkZ = SectionPos.m_123171_(Mth.m_14107_(center.f_82481_ + maxRadius));
      double maxRadiusSqr = maxRadius * maxRadius;

      for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
         for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
            LevelChunk chunk = level.m_7726_().m_7131_(chunkX, chunkZ);
            if (chunk != null) {
               for (BlockEntity blockEntity : chunk.m_62954_().values()) {
                  if (blockEntity instanceof ScanTargetBlockEntity) {
                     BlockPos pos = blockEntity.m_58899_().m_7949_();
                     double distanceSqr = Vec3.m_82512_(pos).m_82557_(center);
                     if (!(distanceSqr > maxRadiusSqr)) {
                        boolean newlyAdded = state.getBlockEntityCandidates().add(pos);
                        if (newlyAdded) {
                           detectLateLoadedBlockEntity(level, pulse, state, pos, currentRadius);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void detectLateLoadedBlockEntity(ClientLevel level, ScanPulseEntity pulse, ClientScanPulseState state, BlockPos pos, float currentRadius) {
      if (!state.getDetectedBlockEntities().contains(pos)) {
         double distance = pulse.m_20182_().m_82554_(Vec3.m_82512_(pos));
         if (!(distance > currentRadius)) {
            state.getDetectedBlockEntities().add(pos);
            BLOCK_ENTITY_HIGHLIGHT_END.put(pos, level.m_46467_() + pulse.getHighlightTicks());
         }
      }
   }

   private static void scanBlockEntities(ClientLevel level, ScanPulseEntity pulse, ClientScanPulseState state, float previousRadius, float currentRadius) {
      Vec3 center = pulse.m_20182_();
      Iterator<BlockPos> iterator = state.getBlockEntityCandidates().iterator();

      while (iterator.hasNext()) {
         BlockPos pos = iterator.next();
         BlockEntity blockEntity = level.m_7702_(pos);
         if (!(blockEntity instanceof ScanTargetBlockEntity)) {
            iterator.remove();
            state.getDetectedBlockEntities().remove(pos);
         } else if (!state.getDetectedBlockEntities().contains(pos)) {
            double distance = center.m_82554_(Vec3.m_82512_(pos));
            if (didWaveReachTarget(previousRadius, currentRadius, distance)) {
               state.getDetectedBlockEntities().add(pos);
               BLOCK_ENTITY_HIGHLIGHT_END.put(pos, level.m_46467_() + pulse.getHighlightTicks());
            }
         }
      }
   }

   private static boolean didWaveReachTarget(double previousRadius, double currentRadius, double targetDistance) {
      return previousRadius < targetDistance && targetDistance <= currentRadius;
   }

   public static boolean isEntityHighlighted(int entityId) {
      Minecraft minecraft = Minecraft.m_91087_();
      if (minecraft.f_91073_ == null) {
         return false;
      }

      Long endTime = ENTITY_HIGHLIGHT_END.get(entityId);
      return endTime != null && endTime > minecraft.f_91073_.m_46467_();
   }

   public static boolean isBlockEntityHighlighted(BlockPos pos) {
      Minecraft minecraft = Minecraft.m_91087_();
      if (minecraft.f_91073_ == null) {
         return false;
      }

      Long endTime = BLOCK_ENTITY_HIGHLIGHT_END.get(pos);
      return endTime != null && endTime > minecraft.f_91073_.m_46467_();
   }

   public static Iterable<BlockPos> getHighlightedBlockEntities() {
      return BLOCK_ENTITY_HIGHLIGHT_END.keySet();
   }

   public static boolean isBlockHighlightEmpty() {
      return BLOCK_ENTITY_HIGHLIGHT_END.isEmpty();
   }

   public static void clear() {
      ACTIVE_PULSES.clear();
      ENTITY_HIGHLIGHT_END.clear();
      BLOCK_ENTITY_HIGHLIGHT_END.clear();
   }
}
