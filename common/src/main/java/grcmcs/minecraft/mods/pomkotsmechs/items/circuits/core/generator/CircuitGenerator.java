package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.generator;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitCategory;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitInstance;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitModifier;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitPrefix;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitSkill;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry.CircuitRegistries;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public final class CircuitGenerator {
   private CircuitGenerator() {
   }

   public static CircuitInstance generate(RandomSource random, CircuitPrefix prefix, CircuitRarity rarity) {
      int targetModifierCount = rarity.rollModifierCount(random);
      List<CircuitModifier> modifiers = new ArrayList<>();
      Set<ResourceLocation> selectedSkillIds = new HashSet<>();
      int attempts = 0;
      int maxAttempts = Math.max(16, targetModifierCount * 10);

      while (modifiers.size() < targetModifierCount && attempts++ < maxAttempts) {
         int modifierIndex = modifiers.size();
         CircuitCategory category = rollCategoryForModifier(random, prefix, modifierIndex);
         CircuitRarity effectiveRarity = resolveEffectiveRarity(prefix, rarity, category, modifierIndex);
         List<CircuitSkill> candidates = findCandidates(category, effectiveRarity, selectedSkillIds);
         if (candidates.isEmpty()) {
            candidates = findFallbackCandidates(prefix, rarity, selectedSkillIds);
         }

         if (candidates.isEmpty()) {
            break;
         }

         CircuitSkill selected = WeightedSelector.select(candidates, CircuitSkill::weight, random);
         if (selected == null) {
            break;
         }

         selectedSkillIds.add(selected.id());
         modifiers.add(new CircuitModifier(selected));
      }

      return new CircuitInstance(prefix, rarity, List.copyOf(modifiers));
   }

   private static CircuitCategory rollCategoryForModifier(RandomSource random, CircuitPrefix prefix, int modifierIndex) {
      return modifierIndex == 0 && !prefix.isBalanced() ? prefix.mainCategory() : prefix.rollCategory(random);
   }

   private static CircuitRarity resolveEffectiveRarity(CircuitPrefix prefix, CircuitRarity baseRarity, CircuitCategory rolledCategory, int modifierIndex) {
      if (prefix.isBalanced()) {
         return baseRarity.lower();
      } else if (modifierIndex == 0) {
         return baseRarity;
      } else {
         return prefix.isMainCategory(rolledCategory) ? baseRarity : baseRarity.lower();
      }
   }

   private static List<CircuitSkill> findCandidates(CircuitCategory category, CircuitRarity rarity, Set<ResourceLocation> selectedSkillIds) {
      return CircuitRegistries.SKILLS
         .find(skill -> skill.category() == category && isAvailableForRarity(skill, rarity) && !selectedSkillIds.contains(skill.id()));
   }

   private static List<CircuitSkill> findFallbackCandidates(CircuitPrefix prefix, CircuitRarity rarity, Set<ResourceLocation> selectedSkillIds) {
      return prefix.isBalanced() ? findFallbackAnyCategory(rarity, selectedSkillIds) : findCandidates(prefix.mainCategory(), rarity, selectedSkillIds);
   }

   private static List<CircuitSkill> findFallbackAnyCategory(CircuitRarity rarity, Set<ResourceLocation> selectedSkillIds) {
      return CircuitRegistries.SKILLS.find(skill -> isAvailableForRarity(skill, rarity) && !selectedSkillIds.contains(skill.id()));
   }

   private static boolean isAvailableForRarity(CircuitSkill skill, CircuitRarity rarity) {
      return skill.minimumRarity().ordinal() <= rarity.ordinal() && skill.tier() <= rarity.maxSkillTier();
   }
}
