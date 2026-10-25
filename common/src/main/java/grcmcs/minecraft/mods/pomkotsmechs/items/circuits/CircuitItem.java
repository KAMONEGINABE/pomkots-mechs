package grcmcs.minecraft.mods.pomkotsmechs.items.circuits;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitPrefix;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.data.CircuitSkillSnapshot;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CircuitItem extends Item {
   public CircuitItem(Properties properties) {
      super(properties);
   }

   public void m_6883_(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
      super.m_6883_(stack, level, entity, slotId, isSelected);
      if (!level.f_46443_) {
         CircuitItemStackHelper.ensureGenerated(stack, level.m_213780_());
      }
   }

   public void m_7373_(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
      boolean identified = CircuitItemStackHelper.isIdentified(stack);
      if (!identified) {
         tooltip.add(Component.m_237115_("tooltip.pomkotsmechs.circuit.unidentified").m_130940_(ChatFormatting.GRAY));
      }

      CircuitPrefix prefix = CircuitItemStackHelper.getPrefixOrDefault(stack);
      CircuitRarity rarity = CircuitItemStackHelper.getRarityOrDefault(stack);
      appendCircuitHeader(tooltip, prefix, rarity);
      if (identified) {
         tooltip.add(Component.m_237119_());

         for (CircuitSkillSnapshot snapshot : CircuitItemStackHelper.getSkillSnapshots(stack)) {
            CircuitTooltipBuilder.appendSkillSnapshot(tooltip, snapshot);
         }
      }
   }

   private static void appendCircuitHeader(List<Component> tooltip, CircuitPrefix prefix, CircuitRarity rarity) {
      tooltip.add(Component.m_237110_("tooltip.pomkotsmechs.circuit.type", new Object[]{prefix.name()}).m_130940_(ChatFormatting.AQUA));
      tooltip.add(Component.m_237110_("tooltip.pomkotsmechs.circuit.rarity", new Object[]{rarity.displayName()}).m_130940_(rarity.chatFormatting()));
   }
}
