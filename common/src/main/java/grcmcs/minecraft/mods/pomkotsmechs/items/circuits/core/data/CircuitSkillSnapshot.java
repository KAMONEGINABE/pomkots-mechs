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
      tag.m_128359_("Id", this.id.toString());
      tag.m_128359_("NameKey", this.nameKey);
      tag.m_128359_("FallbackName", this.fallbackName);
      ListTag effectsTag = new ListTag();

      for (SkillEffect effect : this.effects) {
         effectsTag.add(saveEffect(effect));
      }

      tag.m_128365_("Effects", effectsTag);
      return tag;
   }

   public static CircuitSkillSnapshot load(CompoundTag tag) {
      ResourceLocation id = ResourceLocation.m_135820_(tag.m_128461_("Id"));
      if (id == null) {
         return null;
      }

      String nameKey = tag.m_128461_("NameKey");
      String fallbackName = tag.m_128461_("FallbackName");
      List<SkillEffect> effects = new ArrayList<>();
      ListTag effectsTag = tag.m_128437_("Effects", 10);

      for (int i = 0; i < effectsTag.size(); i++) {
         SkillEffect effect = loadEffect(effectsTag.m_128728_(i));
         if (effect != null) {
            effects.add(effect);
         }
      }

      return new CircuitSkillSnapshot(id, nameKey, fallbackName, effects);
   }

   private static CompoundTag saveEffect(SkillEffect effect) {
      CompoundTag tag = new CompoundTag();
      tag.m_128359_("Kind", effect.kind().name().toLowerCase());
      if (effect.isStatEffect()) {
         tag.m_128359_("Stat", effect.stat().name().toLowerCase());
         tag.m_128359_("Operation", effect.operation().name().toLowerCase());
         tag.m_128347_("Value", effect.value());
         return tag;
      } else if (effect.isFeatureEffect()) {
         tag.m_128359_("Feature", effect.feature().name().toLowerCase());
         tag.m_128379_("Enabled", effect.enabled());
         return tag;
      } else {
         return tag;
      }
   }

   private static SkillEffect loadEffect(CompoundTag tag) {
      if (tag.m_128441_("Feature")) {
         MechFeatureType feature = MechFeatureType.byName(tag.m_128461_("Feature"), null);
         if (feature == null) {
            return null;
         }

         boolean enabled = !tag.m_128441_("Enabled") || tag.m_128471_("Enabled");
         return SkillEffect.feature(feature, enabled);
      } else if (tag.m_128441_("Stat")) {
         MechStatType stat = MechStatType.byName(tag.m_128461_("Stat"), null);
         if (stat == null) {
            return null;
         }

         CircuitOperation operation = CircuitOperation.byName(tag.m_128461_("Operation"), CircuitOperation.ADD);
         double value = tag.m_128459_("Value");
         return SkillEffect.stat(stat, operation, value);
      } else {
         return null;
      }
   }
}
