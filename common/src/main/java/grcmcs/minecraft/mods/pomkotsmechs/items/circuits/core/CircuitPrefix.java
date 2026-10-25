package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.generator.WeightedSelector;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry.CircuitRegistries;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry.SkillRegistryEntry;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public final class CircuitPrefix implements SkillRegistryEntry {
   private final ResourceLocation id;
   private final Component name;
   @Nullable
   private final CircuitCategory mainCategory;
   private final EnumMap<CircuitCategory, Integer> categoryWeights;
   public static final CircuitPrefix BALANCED = register("balanced", "Balanced", null, weights(20, 20, 20, 20, 0));
   public static final CircuitPrefix OFFENCE = register("offence", "Offence", CircuitCategory.OFFENCE, weights(90, 0, 0, 10, 0));
   public static final CircuitPrefix DEFENCE = register("defence", "Defence", CircuitCategory.DEFENCE, weights(0, 90, 10, 0, 0));
   public static final CircuitPrefix ENERGY = register("energy", "Energy", CircuitCategory.ENERGY, weights(10, 0, 100, 0, 0));
   public static final CircuitPrefix MOBILITY = register("mobility", "Mobility", CircuitCategory.MOBILITY, weights(0, 10, 0, 100, 0));
   public static final CircuitPrefix UTILITY = register("utility", "Utility", CircuitCategory.UTILITY, weights(10, 10, 10, 10, 90));

   public CircuitPrefix(ResourceLocation id, Component name, @Nullable CircuitCategory mainCategory, Map<CircuitCategory, Integer> categoryWeights) {
      this.id = id;
      this.name = name;
      this.mainCategory = mainCategory;
      this.categoryWeights = new EnumMap<>(CircuitCategory.class);
      this.categoryWeights.putAll(categoryWeights);
   }

   @Override
   public ResourceLocation id() {
      return this.id;
   }

   public Component name() {
      return this.name;
   }

   @Nullable
   public CircuitCategory mainCategory() {
      return this.mainCategory;
   }

   public boolean isBalanced() {
      return this.mainCategory == null;
   }

   public boolean isMainCategory(CircuitCategory category) {
      return this.mainCategory != null && this.mainCategory == category;
   }

   public CircuitCategory rollCategory(RandomSource random) {
      CircuitCategory selected = WeightedSelector.select(this.categoryWeights.keySet(), category -> this.categoryWeights.getOrDefault(category, 0), random);
      if (selected != null) {
         return selected;
      } else {
         return this.mainCategory != null ? this.mainCategory : CircuitCategory.UTILITY;
      }
   }

   public static void init() {
   }

   private static CircuitPrefix register(String path, String displayName, CircuitCategory mainCategory, Map<CircuitCategory, Integer> categoryWeights) {
      ResourceLocation id = new ResourceLocation("pomkotsmechs", path);
      return CircuitRegistries.PREFIXES.register(new CircuitPrefix(id, Component.m_237113_(displayName), mainCategory, categoryWeights));
   }

   private static EnumMap<CircuitCategory, Integer> weights(int offence, int defence, int energy, int mobility, int utility) {
      EnumMap<CircuitCategory, Integer> weights = new EnumMap<>(CircuitCategory.class);
      weights.put(CircuitCategory.OFFENCE, offence);
      weights.put(CircuitCategory.DEFENCE, defence);
      weights.put(CircuitCategory.ENERGY, energy);
      weights.put(CircuitCategory.MOBILITY, mobility);
      weights.put(CircuitCategory.UTILITY, utility);
      return weights;
   }

   public static List<CircuitPrefix> creativeOrder() {
      return List.of(BALANCED, OFFENCE, DEFENCE, ENERGY, MOBILITY, UTILITY);
   }
}
