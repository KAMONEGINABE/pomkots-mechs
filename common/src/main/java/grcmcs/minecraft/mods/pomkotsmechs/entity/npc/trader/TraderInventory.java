package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader;

import net.minecraft.world.SimpleContainer;

public class TraderInventory extends SimpleContainer {
   public static final int CATEGORY_COUNT = 4;
   public static final int OFFERS_PER_CATEGORY = 8;
   public static final int SLOT_PER_ENTRY = 2;
   public static final int SLOT_SIZE = 64;

   public TraderInventory() {
      super(64);
   }

   public static int getOfferSlot(MechTraderEntity.TraderCategory category, int index) {
      return category.ordinal() * 8 * 2 + index * 2;
   }

   public static int getPriceSlot(MechTraderEntity.TraderCategory category, int index) {
      return getOfferSlot(category, index) + 1;
   }
}
