package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.data;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitOperation;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitSkill;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.SkillEffect;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechFeatureType;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

public final class CircuitSkillSnapshot {
   private static final String TAG_ID = "Id";
   private static final String TAG_NAME_KEY = "NameKey";
   private static final String TAG_FALLBACK_NAME = "FallbackName";
   private static final String TAG_EFFECTS = "Effects";
   private final ResourceLocation id;
   private final String nameKey;
   private final String fallbackName;
   private final List<SkillEffect> effects;

   public CircuitSkillSnapshot(ResourceLocation id, String nameKey, String fallbackName, List<SkillEffect> effects) {
      this.id = id;
      this.nameKey = nameKey;
      this.fallbackName = fallbackName;
      this.effects = List.copyOf(effects);
   }

   public static CircuitSkillSnapshot fromSkill(CircuitSkill skill) {
      return new CircuitSkillSnapshot(skill.id(), skill.translationKey(), skill.fallbackName(), skill.effects());
   }

   public ResourceLocation id() {
      return this.id;
   }

   public String nameKey() {
      return this.nameKey;
   }

   public String fallbackName() {
      return this.fallbackName;
   }

   public List<SkillEffect> effects() {
      return this.effects;
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putString("Id", this.id.toString());
      tag.putString("NameKey", this.nameKey);
      tag.putString("FallbackName", this.fallbackName);
      ListTag effectsTag = new ListTag();

      for (SkillEffect effect : this.effects) {
         effectsTag.add(saveEffect(effect));
      }

      tag.put("Effects", effectsTag);
      return tag;
   }

   public static CircuitSkillSnapshot load(CompoundTag tag) {
      ResourceLocation id = ResourceLocation.tryParse(tag.getString("Id"));
      if (id == null) {
         return null;
      }

      String nameKey = tag.getString("NameKey");
      String fallbackName = tag.getString("FallbackName");
      List<SkillEffect> effects = new ArrayList<>();
      ListTag effectsTag = tag.getList("Effects", 10);

      for (int i = 0; i < effectsTag.size(); i++) {
         SkillEffect effect = loadEffect(effectsTag.getCompound(i));
         if (effect != null) {
            effects.add(effect);
         }
      }

      return new CircuitSkillSnapshot(id, nameKey, fallbackName, effects);
   }

   private static CompoundTag saveEffect(SkillEffect effect) {
      CompoundTag tag = new CompoundTag();
      tag.putString("Kind", effect.kind().name().toLowerCase());
      if (effect.isStatEffect()) {
         tag.putString("Stat", effect.stat().name().toLowerCase());
         tag.putString("Operation", effect.operation().name().toLowerCase());
         tag.putDouble("Value", effect.value());
         return tag;
      } else if (effect.isFeatureEffect()) {
         tag.putString("Feature", effect.feature().name().toLowerCase());
         tag.putBoolean("Enabled", effect.enabled());
         return tag;
      } else {
         return tag;
      }
   }

   private static SkillEffect loadEffect(CompoundTag tag) {
      if (tag.contains("Feature")) {
         MechFeatureType feature = MechFeatureType.byName(tag.getString("Feature"), null);
         if (feature == null) {
            return null;
         }

         boolean enabled = !tag.contains("Enabled") || tag.getBoolean("Enabled");
         return SkillEffect.feature(feature, enabled);
      } else if (tag.contains("Stat")) {
         MechStatType stat = MechStatType.byName(tag.getString("Stat"), null);
         if (stat == null) {
            return null;
         }

         CircuitOperation operation = CircuitOperation.byName(tag.getString("Operation"), CircuitOperation.ADD);
         double value = tag.getDouble("Value");
         return SkillEffect.stat(stat, operation, value);
      } else {
         return null;
      }
   }
}
