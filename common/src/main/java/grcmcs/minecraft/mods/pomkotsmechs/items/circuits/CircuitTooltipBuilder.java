package grcmcs.minecraft.mods.pomkotsmechs.items.circuits;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitOperation;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.SkillEffect;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.data.CircuitSkillSnapshot;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechFeatureType;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatType;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public final class CircuitTooltipBuilder {
   private static final DecimalFormat DECIMAL = new DecimalFormat("0.##");

   private CircuitTooltipBuilder() {
   }

   public static void appendSkillSnapshot(List<Component> tooltip, CircuitSkillSnapshot snapshot) {
      tooltip.add(Component.m_237113_(" - ").m_7220_(skillName(snapshot)).m_130940_(ChatFormatting.GRAY));

      for (SkillEffect effect : snapshot.effects()) {
         tooltip.add(Component.m_237113_("    ").m_7220_(formatEffect(effect)).m_130940_(getChatFormatting(effect)));
      }
   }

   @NotNull
   private static ChatFormatting getChatFormatting(SkillEffect effect) {
      ChatFormatting color;
      if (effect.isFeatureEffect()) {
         color = ChatFormatting.AQUA;
      } else if (MechStatType.ENERGY_COST.equals(effect.stat())) {
         color = effect.value() <= 0.0 ? ChatFormatting.DARK_GREEN : ChatFormatting.RED;
      } else {
         color = effect.value() >= 0.0 ? ChatFormatting.DARK_GREEN : ChatFormatting.RED;
      }

      return color;
   }

   private static Component skillName(CircuitSkillSnapshot snapshot) {
      return Component.m_237113_(snapshot.fallbackName());
   }

   private static Component formatEffect(SkillEffect effect) {
      return effect.isFeatureEffect() ? formatFeatureEffect(effect) : formatStatEffect(effect);
   }

   private static Component formatStatEffect(SkillEffect effect) {
      Component statName = statName(effect.stat());
      String valueText = formatValue(effect.operation(), effect.value());
      return Component.m_237113_(valueText + " ").m_7220_(statName);
   }

   private static Component formatFeatureEffect(SkillEffect effect) {
      Component featureName = featureName(effect.feature());
      return effect.enabled()
         ? Component.m_237110_("tooltip.pomkotsmechs.feature.enabled", new Object[]{featureName})
         : Component.m_237110_("tooltip.pomkotsmechs.feature.disabled", new Object[]{featureName});
   }

   private static Component featureName(MechFeatureType featureType) {
      return Component.m_237115_("mech_feature.pomkotsmechs." + featureType.name().toLowerCase());
   }

   private static Component statName(MechStatType statType) {
      return Component.m_237115_("mech_stat.pomkotsmechs." + statType.name().toLowerCase());
   }

   private static String formatValue(CircuitOperation operation, double value) {
      return switch (operation) {
         case ADD -> formatSignedNumber(value);
         case MULTIPLY -> formatSignedPercent(value);
         case OVERRIDE -> "= " + DECIMAL.format(value);
      };
   }

   private static String formatSignedNumber(double value) {
      return value > 0.0 ? "+" + DECIMAL.format(value) : DECIMAL.format(value);
   }

   private static String formatSignedPercent(double value) {
      double percent = value * 100.0;
      return percent > 0.0 ? "+" + DECIMAL.format(percent) + "%" : DECIMAL.format(percent) + "%";
   }
}
