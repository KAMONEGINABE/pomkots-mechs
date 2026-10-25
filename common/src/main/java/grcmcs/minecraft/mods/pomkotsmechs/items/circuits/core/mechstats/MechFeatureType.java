package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats;

import java.util.Locale;

public enum MechFeatureType {
   TARGET_ASSIST_SOFT,
   TARGET_ASSIST_HARD,
   WALL_KICK;

   public static MechFeatureType byName(String name, MechFeatureType fallback) {
      if (name == null) {
         return fallback;
      }

      String normalized = name.toUpperCase(Locale.ROOT);

      for (MechFeatureType type : values()) {
         if (type.name().equals(normalized)) {
            return type;
         }
      }

      return fallback;
   }
}
