package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class FlavorTextItem extends Item {
   public FlavorTextItem(Properties properties) {
      super(properties);
   }

   public Component m_7626_(ItemStack stack) {
      Component base = super.m_7626_(stack);
      CompoundTag tag = stack.m_41783_();
      if (tag != null && tag.m_128441_(PomkotsMechs.nbtName("FlavorTextNo"))) {
         String no = tag.m_128461_(PomkotsMechs.nbtName("FlavorTextNo"));
         return base.m_6881_().m_130946_(": " + no);
      } else {
         return base;
      }
   }
}
