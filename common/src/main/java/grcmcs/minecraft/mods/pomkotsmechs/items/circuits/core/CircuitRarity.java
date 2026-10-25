package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public enum CircuitRarity implements StringRepresentable {
   COMMON("common", 1, 1, ChatFormatting.WHITE, 16777215),
   RARE("rare", 2, 2, ChatFormatting.BLUE, 4886015),
   EPIC("epic", 3, 3, ChatFormatting.LIGHT_PURPLE, 12606719);

   private final String name;
   private final int maxModifierCount;
   private final int maxSkillTier;
   private final ChatFormatting chatFormatting;
   private final int tintColor;

   CircuitRarity(String name, int maxModifierCount, int maxSkillTier, ChatFormatting chatFormatting, int tintColor) {
      this.name = name;
      this.maxModifierCount = maxModifierCount;
      this.maxSkillTier = maxSkillTier;
      this.chatFormatting = chatFormatting;
      this.tintColor = tintColor;
   }

   public String m_7912_() {
      return this.name;
   }

   public String serializedName() {
      return this.name;
   }

   public int maxModifierCount() {
      return this.maxModifierCount;
   }

   public int maxSkillTier() {
      return this.maxSkillTier;
   }

   public ChatFormatting chatFormatting() {
      return this.chatFormatting;
   }

   public Component displayName() {
      return Component.m_237115_("tooltip.pomkotsmechs.circuit.rarity." + this.name().toLowerCase());
   }

   public int rollModifierCount(RandomSource random) {
      return this.maxModifierCount <= 1 ? 1 : 1 + random.m_188503_(this.maxModifierCount);
   }

   public static CircuitRarity byName(String name, CircuitRarity fallback) {
      String normalized = name.toLowerCase(Locale.ROOT);

      for (CircuitRarity rarity : values()) {
         if (rarity.name.equals(normalized)) {
            return rarity;
         }
      }

      return fallback;
   }

   public static List<CircuitRarity> creativeOrder() {
      return List.of(COMMON, RARE, EPIC);
   }

   public int tintColor() {
      return this.tintColor;
   }

   public CircuitRarity lower() {
      int index = this.ordinal();
      return index <= 0 ? this : values()[index - 1];
   }
}
