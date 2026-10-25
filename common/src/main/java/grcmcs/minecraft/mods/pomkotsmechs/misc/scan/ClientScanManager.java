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
      Minecraft minecraft = Minecraft.getInstance();
      ClientLevel level = minecraft.level;
      Player localPlayer = minecraft.player;
      if (level != null && localPlayer != null) {
         long gameTime = level.getGameTime();
         ENTITY_HIGHLIGHT_END.entrySet().removeIf(entry -> entry.getValue() <= gameTime);
         BLOCK_ENTITY_HIGHLIGHT_END.entrySet().removeIf(entry -> entry.getValue() <= gameTime);
         Map<Integer, ScanPulseEntity> currentPulses = new HashMap<>();

         for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof ScanPulseEntity pulse && pulse.isVisibleTo(localPlayer)) {
               currentPulses.put(pulse.getId(), pulse);
               tickPulse(level, pulse);
            }
         }

         ACTIVE_PULSES.keySet().removeIf(id -> !currentPulses.containsKey(id));
      } else {
         clear();
      }
   }

   private static void tickPulse(ClientLevel level, ScanPulseEntity pulse) {
      ClientScanPulseState state = ACTIVE_PULSES.computeIfAbsent(pulse.getId(), ignored -> new ClientScanPulseState());
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
         Vec3 center = pulse.position();
         AABB searchBox = new AABB(
            center.x - currentRadius,
            center.y - currentRadius,
            center.z - currentRadius,
            center.x + currentRadius,
            center.y + currentRadius,
            center.z + currentRadius
         );

         for (Entity entity : level.getEntities(pulse, searchBox, ClientScanManager::isEntityScanTarget)) {
            if (!state.getDetectedEntityIds().contains(entity.getId())) {
               double distance = distanceToAabb(center, entity.getBoundingBox());
               if (didWaveReachTarget(previousRadius, currentRadius, distance)) {
                  state.getDetectedEntityIds().add(entity.getId());
                  ENTITY_HIGHLIGHT_END.put(entity.getId(), level.getGameTime() + pulse.getHighlightTicks());
               }
            }
         }
      }
   }

   private static boolean isEntityScanTarget(Entity entity) {
      return !entity.isAlive() ? false : entity instanceof Pmvc01Entity || entity.getType().is(ModEntityTags.SCAN_TARGETS);
   }

   private static double distanceToAabb(Vec3 center, AABB box) {
      double nearestX = Mth.clamp(center.x, box.minX, box.maxX);
      double nearestY = Mth.clamp(center.y, box.minY, box.maxY);
      double nearestZ = Mth.clamp(center.z, box.minZ, box.maxZ);
      return center.distanceTo(new Vec3(nearestX, nearestY, nearestZ));
   }

   private static void collectBlockEntityCandidates(ClientLevel level, ScanPulseEntity pulse, ClientScanPulseState state, float currentRadius) {
      Vec3 center = pulse.position();
      float maxRadius = pulse.getMaxRadius();
      int minChunkX = SectionPos.blockToSectionCoord(Mth.floor(center.x - maxRadius));
      int maxChunkX = SectionPos.blockToSectionCoord(Mth.floor(center.x + maxRadius));
      int minChunkZ = SectionPos.blockToSectionCoord(Mth.floor(center.z - maxRadius));
      int maxChunkZ = SectionPos.blockToSectionCoord(Mth.floor(center.z + maxRadius));
      double maxRadiusSqr = maxRadius * maxRadius;

      for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
         for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
            if (chunk != null) {
               for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                  if (blockEntity instanceof ScanTargetBlockEntity) {
                     BlockPos pos = blockEntity.getBlockPos().immutable();
                     double distanceSqr = Vec3.atCenterOf(pos).distanceToSqr(center);
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
         double distance = pulse.position().distanceTo(Vec3.atCenterOf(pos));
         if (!(distance > currentRadius)) {
            state.getDetectedBlockEntities().add(pos);
            BLOCK_ENTITY_HIGHLIGHT_END.put(pos, level.getGameTime() + pulse.getHighlightTicks());
         }
      }
   }

   private static void scanBlockEntities(ClientLevel level, ScanPulseEntity pulse, ClientScanPulseState state, float previousRadius, float currentRadius) {
      Vec3 center = pulse.position();
      Iterator<BlockPos> iterator = state.getBlockEntityCandidates().iterator();

      while (iterator.hasNext()) {
         BlockPos pos = iterator.next();
         BlockEntity blockEntity = level.getBlockEntity(pos);
         if (!(blockEntity instanceof ScanTargetBlockEntity)) {
            iterator.remove();
            state.getDetectedBlockEntities().remove(pos);
         } else if (!state.getDetectedBlockEntities().contains(pos)) {
            double distance = center.distanceTo(Vec3.atCenterOf(pos));
            if (didWaveReachTarget(previousRadius, currentRadius, distance)) {
               state.getDetectedBlockEntities().add(pos);
               BLOCK_ENTITY_HIGHLIGHT_END.put(pos, level.getGameTime() + pulse.getHighlightTicks());
            }
         }
      }
   }

   private static boolean didWaveReachTarget(double previousRadius, double currentRadius, double targetDistance) {
      return previousRadius < targetDistance && targetDistance <= currentRadius;
   }

   public static boolean isEntityHighlighted(int entityId) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.level == null) {
         return false;
      }

      Long endTime = ENTITY_HIGHLIGHT_END.get(entityId);
      return endTime != null && endTime > minecraft.level.getGameTime();
   }

   public static boolean isBlockEntityHighlighted(BlockPos pos) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.level == null) {
         return false;
      }

      Long endTime = BLOCK_ENTITY_HIGHLIGHT_END.get(pos);
      return endTime != null && endTime > minecraft.level.getGameTime();
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
