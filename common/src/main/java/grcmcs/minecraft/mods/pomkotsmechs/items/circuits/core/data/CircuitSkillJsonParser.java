package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitCategory;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitOperation;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitSkill;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.SkillEffect;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechFeatureType;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public final class CircuitSkillJsonParser {
   private CircuitSkillJsonParser() {
   }

   public static CircuitSkill parse(JsonObject json) {
      ResourceLocation id = ResourceLocation.m_135820_(GsonHelper.m_13906_(json, "id"));
      if (id == null) {
         throw new IllegalArgumentException("Invalid or missing circuit skill id.");
      }

      String defaultTranslationKey = "circuit_skill." + id.m_135827_() + "." + id.m_135815_().replace('/', '.');
      String translationKey = GsonHelper.m_13851_(json, "translation_key", defaultTranslationKey);
      String fallbackName = GsonHelper.m_13851_(json, "display_name", id.m_135815_());
      Component displayName = parseDisplayName(json, translationKey, fallbackName);
      CircuitCategory category = CircuitCategory.byName(GsonHelper.m_13851_(json, "category", "utility"), CircuitCategory.UTILITY);
      int tier = GsonHelper.m_13824_(json, "tier", 1);
      CircuitRarity minimumRarity = CircuitRarity.byName(GsonHelper.m_13851_(json, "minimum_rarity", "common"), CircuitRarity.COMMON);
      int weight = GsonHelper.m_13824_(json, "weight", 10);
      List<SkillEffect> effects = parseEffects(id, json);
      return new CircuitSkill(id, displayName, translationKey, fallbackName, category, tier, minimumRarity, weight, effects);
   }

   private static Component parseDisplayName(JsonObject json, String translationKey, String fallbackName) {
      if (json.has("name")) {
         Component component = Serializer.m_130691_(json.get("name"));
         if (component != null) {
            return component;
         }
      }

      if (json.has("translation_key")) {
         return Component.m_237115_(translationKey);
      } else {
         return json.has("display_name") ? Component.m_237113_(fallbackName) : Component.m_237115_(translationKey);
      }
   }

   private static List<SkillEffect> parseEffects(ResourceLocation skillId, JsonObject json) {
      JsonArray array = GsonHelper.m_13933_(json, "effects");
      List<SkillEffect> effects = new ArrayList<>();

      for (JsonElement element : array) {
         if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Effect entry must be object in skill: " + skillId);
         }

         JsonObject effectJson = element.getAsJsonObject();
         if (effectJson.has("stat")) {
            effects.add(parseStatEffect(skillId, effectJson));
         } else {
            if (!effectJson.has("feature")) {
               throw new IllegalArgumentException("Effect must have either 'stat' or 'feature' in skill: " + skillId);
            }

            effects.add(parseFeatureEffect(skillId, effectJson));
         }
      }

      if (effects.isEmpty()) {
         throw new IllegalArgumentException("Skill has no effects: " + skillId);
      } else {
         return effects;
      }
   }

   private static SkillEffect parseStatEffect(ResourceLocation skillId, JsonObject effectJson) {
      MechStatType stat = MechStatType.byName(GsonHelper.m_13906_(effectJson, "stat"), null);
      if (stat == null) {
         throw new IllegalArgumentException("Unknown or missing stat in skill: " + skillId);
      }

      CircuitOperation operation = CircuitOperation.byName(GsonHelper.m_13851_(effectJson, "operation", "add"), CircuitOperation.ADD);
      double value = GsonHelper.m_144784_(effectJson, "value");
      return SkillEffect.stat(stat, operation, value);
   }

   private static SkillEffect parseFeatureEffect(ResourceLocation skillId, JsonObject effectJson) {
      MechFeatureType feature = MechFeatureType.byName(GsonHelper.m_13906_(effectJson, "feature"), null);
      if (feature == null) {
         throw new IllegalArgumentException("Unknown or missing feature in skill: " + skillId);
      }

      boolean enabled = GsonHelper.m_13855_(effectJson, "enabled", true);
      return SkillEffect.feature(feature, enabled);
   }
}
