package grcmcs.minecraft.mods.pomkotsmechs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.RandomSource;

public class WeightedList<T> {
   private final List<WeightedList.Entry<T>> entries = new ArrayList<>();
   private int totalWeight = 0;

   public void addObject(int weight, T instance) {
      this.entries.add(new WeightedList.Entry<>(weight, instance));
      this.totalWeight += weight;
   }

   public T getRandomObject(RandomSource random) {
      int roll = random.m_188503_(this.totalWeight);
      int current = 0;

      for (WeightedList.Entry<T> entry : this.entries) {
         current += entry.weight;
         if (roll < current) {
            return entry.instance;
         }
      }

      return this.entries.get(this.entries.size() - 1).instance;
   }

   public boolean isEmpty() {
      return this.entries.isEmpty();
   }

   public List<WeightedList.Entry<T>> getEntries() {
      return Collections.unmodifiableList(this.entries);
   }

   public record Entry<T>(int weight, T instance) {
   }
}
