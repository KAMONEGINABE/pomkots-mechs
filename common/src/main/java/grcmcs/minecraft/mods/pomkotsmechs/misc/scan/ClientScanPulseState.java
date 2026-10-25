package grcmcs.minecraft.mods.pomkotsmechs.misc.scan;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;

public final class ClientScanPulseState {
   private int age;
   private float lastRadius;
   private final Set<BlockPos> blockEntityCandidates = new HashSet<>();
   private final Set<BlockPos> detectedBlockEntities = new HashSet<>();
   private final Set<Integer> detectedEntityIds = new HashSet<>();

   public void tickAge() {
      this.age++;
   }

   public int getAge() {
      return this.age;
   }

   public float getLastRadius() {
      return this.lastRadius;
   }

   public void setLastRadius(float lastRadius) {
      this.lastRadius = lastRadius;
   }

   public Set<BlockPos> getBlockEntityCandidates() {
      return this.blockEntityCandidates;
   }

   public Set<BlockPos> getDetectedBlockEntities() {
      return this.detectedBlockEntities;
   }

   public Set<Integer> getDetectedEntityIds() {
      return this.detectedEntityIds;
   }
}
