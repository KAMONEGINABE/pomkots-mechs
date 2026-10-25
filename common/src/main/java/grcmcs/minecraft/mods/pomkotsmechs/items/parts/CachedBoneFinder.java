package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public class CachedBoneFinder {
   private BakedGeoModel model;
   private Map<String, GeoBone> cache = new WeakHashMap<>();

   public void setModel(BakedGeoModel model) {
      if (this.model != model) {
         this.cache.clear();
         this.model = model;
      }
   }

   public GeoBone getBone(String name) {
      GeoBone bone = this.cache.get(name);
      if (bone == null) {
         Optional<GeoBone> rawBone = this.model.getBone(name);
         if (rawBone != null) {
            bone = rawBone.get();
            this.cache.put(name, bone);
         }
      }

      return bone;
   }
}
