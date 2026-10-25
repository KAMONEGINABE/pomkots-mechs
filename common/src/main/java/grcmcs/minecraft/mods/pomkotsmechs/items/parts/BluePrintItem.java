package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BluePrintItem extends Item {
   public BluePrintItem(Properties properties) {
      super(properties);
   }

   public void m_7373_(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
      Item correspondingItem = Utils.getPartsClassFromBluePrint(itemStack);
      if (correspondingItem != null) {
         correspondingItem.m_7373_(new ItemStack(correspondingItem), level, list, tooltipFlag);
      }
   }
}
