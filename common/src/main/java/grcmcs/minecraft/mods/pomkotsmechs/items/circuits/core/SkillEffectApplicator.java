package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechFeatures;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatType;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStats;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatus;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class SkillEffectApplicator {
   private SkillEffectApplicator() {
   }

   public static SkillEffectApplicator.Accumulator createAccumulator() {
      return new SkillEffectApplicator.Accumulator();
   }

   public static void applyFeature(MechFeatures features, SkillEffect effect) {
      features.setEnabled(effect.feature(), effect.enabled());
   }

   private static final class AccumulatedStatEffect {
      private double addValue = 0.0;
      private double multiplyValue = 0.0;
      private Double overrideValue = null;

      public void add(SkillEffect effect) {
         switch (effect.operation()) {
            case ADD:
               this.addValue = this.addValue + effect.value();
               break;
            case MULTIPLY:
               this.multiplyValue = this.multiplyValue + effect.value();
               break;
            case OVERRIDE:
               this.overrideValue = effect.value();
         }
      }

      public double apply(double base) {
         if (this.overrideValue != null) {
            return this.overrideValue;
         }

         double result = base + this.addValue;
         return result * (1.0 + this.multiplyValue);
      }
   }

   public static final class Accumulator {
      private final Map<MechStatType, SkillEffectApplicator.AccumulatedStatEffect> statEffects = new EnumMap<>(MechStatType.class);
      private final List<SkillEffect> featureEffects = new ArrayList<>();

      public void add(CircuitInstance circuit) {
         for (CircuitModifier modifier : circuit.modifiers()) {
            this.add(modifier);
         }
      }

      public void add(CircuitModifier modifier) {
         this.add(modifier.skill());
      }

      public void add(CircuitSkill skill) {
         for (SkillEffect effect : skill.effects()) {
            this.add(effect);
         }
      }

      public void add(SkillEffect effect) {
         if (effect.isStatEffect()) {
            this.statEffects.computeIfAbsent(effect.stat(), ignored -> new SkillEffectApplicator.AccumulatedStatEffect()).add(effect);
         } else {
            if (effect.isFeatureEffect()) {
               this.featureEffects.add(effect);
            }
         }
      }

      public void applyTo(MechStatus status) {
         this.applyStats(status.stats());
         this.applyFeatures(status.features());
      }

      private void applyStats(MechStats stats) {
         for (Entry<MechStatType, SkillEffectApplicator.AccumulatedStatEffect> entry : this.statEffects.entrySet()) {
            MechStatType statType = entry.getKey();
            double base = stats.get(statType);
            double result = entry.getValue().apply(base);
            stats.set(statType, result);
         }
      }

      private void applyFeatures(MechFeatures features) {
         for (SkillEffect effect : this.featureEffects) {
            SkillEffectApplicator.applyFeature(features, effect);
         }
      }
   }
}
