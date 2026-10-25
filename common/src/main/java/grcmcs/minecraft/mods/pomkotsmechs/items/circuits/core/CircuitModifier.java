package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatType;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public final class CircuitModifier {
   private final CircuitSkill skill;

   public CircuitModifier(CircuitSkill skill) {
      this.skill = Objects.requireNonNull(skill, "skill");
   }

   public CircuitSkill skill() {
      return this.skill;
   }

   public ResourceLocation skillId() {
      return this.skill.id();
   }

   public CircuitCategory category() {
      return this.skill.category();
   }

   public int tier() {
      return this.skill.tier();
   }

   public CircuitRarity minimumRarity() {
      return this.skill.minimumRarity();
   }

   public int weight() {
      return this.skill.weight();
   }

   public List<SkillEffect> effects() {
      return this.skill.effects();
   }

   public boolean hasEffect(MechStatType statType) {
      for (SkillEffect effect : this.skill.effects()) {
         if (effect.effect() == statType) {
            return true;
         }
      }

      return false;
   }

   @Override
   public String toString() {
      return "CircuitModifier[skill=" + this.skill.id() + "]";
   }
}
