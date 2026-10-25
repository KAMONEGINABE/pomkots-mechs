package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class TraderOffer {
   private ItemStack stack;
   private int price;
   private int rarity;
   private boolean sold;

   public TraderOffer(ItemStack stack, int price, int rarity) {
      this.stack = stack;
      this.price = price;
      this.rarity = rarity;
   }

   public ItemStack getStack() {
      return this.stack;
   }

   public int getPrice() {
      return this.price;
   }

   public int getRarity() {
      return this.rarity;
   }

   public boolean isSold() {
      return this.sold;
   }

   public void setSold(boolean sold) {
      this.sold = sold;
   }

   public CompoundTag save() {
      return null;
   }

   public static TraderOffer load() {
      return null;
   }
}
