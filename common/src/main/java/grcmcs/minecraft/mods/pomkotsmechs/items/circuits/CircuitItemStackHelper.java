package grcmcs.minecraft.mods.pomkotsmechs.items.circuits;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitInstance;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitModifier;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitPrefix;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitSkill;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.data.CircuitSkillSnapshot;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.generator.CircuitGenerator;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry.CircuitRegistries;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class CircuitItemStackHelper {
   private static final String TAG_CIRCUIT = "Circuit";
   private static final String TAG_IDENTIFIED = "Identified";
   private static final String TAG_PREFIX = "Prefix";
   private static final String TAG_RARITY = "Rarity";
   private static final String TAG_SKILLS = "Skills";

   private CircuitItemStackHelper() {
   }

   public static boolean hasCircuitTag(ItemStack stack) {
      return stack.m_41782_() && stack.m_41783_() != null && stack.m_41783_().m_128425_("Circuit", 10);
   }

   public static boolean isIdentified(ItemStack stack) {
      CompoundTag circuitTag = getCircuitTag(stack, false);
      return circuitTag == null ? false : circuitTag.m_128471_("Identified") && circuitTag.m_128425_("Skills", 9);
   }

   public static void setUnidentified(ItemStack stack, CircuitPrefix prefix, CircuitRarity rarity) {
      CompoundTag circuitTag = getCircuitTag(stack, true);
      circuitTag.m_128379_("Identified", false);
      circuitTag.m_128359_("Prefix", prefix.id().toString());
      circuitTag.m_128359_("Rarity", rarity.serializedName());
      circuitTag.m_128473_("Skills");
   }

   public static void setIdentified(ItemStack stack, CircuitInstance instance) {
      CompoundTag circuitTag = getCircuitTag(stack, true);
      circuitTag.m_128379_("Identified", true);
      circuitTag.m_128359_("Prefix", instance.prefix().id().toString());
      circuitTag.m_128359_("Rarity", instance.rarity().serializedName());
      ListTag skillsTag = new ListTag();

      for (CircuitModifier modifier : instance.modifiers()) {
         CircuitSkillSnapshot snapshot = CircuitSkillSnapshot.fromSkill(modifier.skill());
         skillsTag.add(snapshot.save());
      }

      circuitTag.m_128365_("Skills", skillsTag);
   }

   public static void ensureGenerated(ItemStack stack, RandomSource random) {
      if (!isIdentified(stack)) {
         CompoundTag circuitTag = getCircuitTag(stack, true);
         CircuitPrefix prefix = readPrefix(circuitTag);
         CircuitRarity rarity = readRarity(circuitTag);
         CircuitInstance generated = CircuitGenerator.generate(random, prefix, rarity);
         setIdentified(stack, generated);
      }
   }

   public static CircuitInstance getCircuit(ItemStack stack) {
      if (!isIdentified(stack)) {
         return null;
      }

      CompoundTag circuitTag = getCircuitTag(stack, false);
      if (circuitTag == null) {
         return null;
      }

      CircuitPrefix prefix = readPrefix(circuitTag);
      CircuitRarity rarity = readRarity(circuitTag);
      List<CircuitModifier> modifiers = new ArrayList<>();
      ListTag skillsTag = circuitTag.m_128437_("Skills", 10);

      for (int i = 0; i < skillsTag.size(); i++) {
         CompoundTag skillTag = skillsTag.m_128728_(i);
         ResourceLocation skillId = ResourceLocation.m_135820_(skillTag.m_128461_("Id"));
         if (skillId != null) {
            CircuitSkill skill = CircuitRegistries.SKILLS.get(skillId);
            if (skill != null) {
               modifiers.add(new CircuitModifier(skill));
            }
         }
      }

      return new CircuitInstance(prefix, rarity, List.copyOf(modifiers));
   }

   public static List<CircuitSkillSnapshot> getSkillSnapshots(ItemStack stack) {
      if (!isIdentified(stack)) {
         return List.of();
      }

      CompoundTag circuitTag = getCircuitTag(stack, false);
      if (circuitTag == null) {
         return List.of();
      }

      ListTag skillsTag = circuitTag.m_128437_("Skills", 10);
      List<CircuitSkillSnapshot> snapshots = new ArrayList<>();

      for (int i = 0; i < skillsTag.size(); i++) {
         CircuitSkillSnapshot snapshot = CircuitSkillSnapshot.load(skillsTag.m_128728_(i));
         if (snapshot != null) {
            snapshots.add(snapshot);
         }
      }

      return List.copyOf(snapshots);
   }

   public static CircuitPrefix getPrefixOrDefault(ItemStack stack) {
      CompoundTag circuitTag = getCircuitTag(stack, false);
      return circuitTag == null ? CircuitPrefix.BALANCED : readPrefix(circuitTag);
   }

   public static CircuitRarity getRarityOrDefault(ItemStack stack) {
      CompoundTag circuitTag = getCircuitTag(stack, false);
      return circuitTag == null ? CircuitRarity.COMMON : readRarity(circuitTag);
   }

   private static CompoundTag getCircuitTag(ItemStack stack, boolean create) {
      if (!create) {
         if (stack.m_41782_() && stack.m_41783_() != null) {
            CompoundTag root = stack.m_41783_();
            return !root.m_128425_("Circuit", 10) ? null : root.m_128469_("Circuit");
         } else {
            return null;
         }
      } else {
         CompoundTag root = stack.m_41784_();
         if (!root.m_128425_("Circuit", 10)) {
            root.m_128365_("Circuit", new CompoundTag());
         }

         return root.m_128469_("Circuit");
      }
   }

   private static CircuitPrefix readPrefix(CompoundTag circuitTag) {
      if (circuitTag.m_128425_("Prefix", 8)) {
         ResourceLocation id = ResourceLocation.m_135820_(circuitTag.m_128461_("Prefix"));
         if (id != null) {
            CircuitPrefix prefix = CircuitRegistries.PREFIXES.get(id);
            if (prefix != null) {
               return prefix;
            }
         }
      }

      return CircuitPrefix.BALANCED;
   }

   private static CircuitRarity readRarity(CompoundTag circuitTag) {
      return circuitTag.m_128425_("Rarity", 8) ? CircuitRarity.byName(circuitTag.m_128461_("Rarity"), CircuitRarity.COMMON) : CircuitRarity.COMMON;
   }
}
