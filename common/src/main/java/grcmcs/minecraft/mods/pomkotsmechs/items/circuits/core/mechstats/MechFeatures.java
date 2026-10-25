package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats;

import java.util.EnumSet;
import java.util.Set;

public final class MechFeatures {
   private final EnumSet<MechFeatureType> enabledFeatures = EnumSet.noneOf(MechFeatureType.class);

   public boolean isEnabled(MechFeatureType feature) {
      return this.enabledFeatures.contains(feature);
   }

   public void setEnabled(MechFeatureType feature, boolean enabled) {
      if (enabled) {
         this.enabledFeatures.add(feature);
      } else {
         this.enabledFeatures.remove(feature);
      }
   }

   public void enable(MechFeatureType feature) {
      this.enabledFeatures.add(feature);
   }

   public void disable(MechFeatureType feature) {
      this.enabledFeatures.remove(feature);
   }

   public Set<MechFeatureType> values() {
      return Set.copyOf(this.enabledFeatures);
   }

   public MechFeatures copy() {
      MechFeatures copy = new MechFeatures();
      copy.enabledFeatures.addAll(this.enabledFeatures);
      return copy;
   }
}
