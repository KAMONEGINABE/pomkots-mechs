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

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
      super.inventoryTick(stack, level, entity, slotId, isSelected);
      if (!level.isClientSide) {
         CircuitItemStackHelper.ensureGenerated(stack, level.getRandom());
      }
   }

   public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
      boolean identified = CircuitItemStackHelper.isIdentified(stack);
      if (!identified) {
         tooltip.add(Component.translatable("tooltip.pomkotsmechs.circuit.unidentified").withStyle(ChatFormatting.GRAY));
      }

      CircuitPrefix prefix = CircuitItemStackHelper.getPrefixOrDefault(stack);
      CircuitRarity rarity = CircuitItemStackHelper.getRarityOrDefault(stack);
      appendCircuitHeader(tooltip, prefix, rarity);
      if (identified) {
         tooltip.add(Component.empty());

         for (CircuitSkillSnapshot snapshot : CircuitItemStackHelper.getSkillSnapshots(stack)) {
            CircuitTooltipBuilder.appendSkillSnapshot(tooltip, snapshot);
         }
      }
   }

   private static void appendCircuitHeader(List<Component> tooltip, CircuitPrefix prefix, CircuitRarity rarity) {
      tooltip.add(Component.translatable("tooltip.pomkotsmechs.circuit.type", new Object[]{prefix.name()}).withStyle(ChatFormatting.AQUA));
      tooltip.add(Component.translatable("tooltip.pomkotsmechs.circuit.rarity", new Object[]{rarity.displayName()}).withStyle(rarity.chatFormatting()));
   }
}
