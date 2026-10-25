package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechFeatureType;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatType;
import java.util.Objects;

public final class SkillEffect {
   private final SkillEffectKind kind;
   private final MechStatType stat;
   private final MechFeatureType feature;
   private final CircuitOperation operation;
   private final double value;

   private SkillEffect(SkillEffectKind kind, MechStatType stat, MechFeatureType feature, CircuitOperation operation, double value) {
      this.kind = Objects.requireNonNull(kind, "kind");
      this.stat = stat;
      this.feature = feature;
      this.operation = operation;
      this.value = value;
   }

   public static SkillEffect stat(MechStatType stat, CircuitOperation operation, double value) {
      return new SkillEffect(SkillEffectKind.STAT, Objects.requireNonNull(stat, "stat"), null, Objects.requireNonNull(operation, "operation"), value);
   }

   public static SkillEffect feature(MechFeatureType feature, boolean enabled) {
      return new SkillEffect(SkillEffectKind.FEATURE, null, Objects.requireNonNull(feature, "feature"), null, enabled ? 1.0 : 0.0);
   }

   public SkillEffectKind kind() {
      return this.kind;
   }

   public MechStatType effect() {
      return this.stat;
   }

   public MechStatType stat() {
      return this.stat;
   }

   public MechFeatureType feature() {
      return this.feature;
   }

   public CircuitOperation operation() {
      return this.operation;
   }

   public double value() {
      return this.value;
   }

   public boolean enabled() {
      return this.value > 0.0;
   }

   public boolean isStatEffect() {
      return this.kind == SkillEffectKind.STAT;
   }

   public boolean isFeatureEffect() {
      return this.kind == SkillEffectKind.FEATURE;
   }
}
