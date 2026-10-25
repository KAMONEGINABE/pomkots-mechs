package grcmcs.minecraft.mods.pomkotsmechs.items.coin;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.Nullable;

public class PomCoinGoldItem extends AbstractPomCoinItem {
   public PomCoinGoldItem(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   protected Item getLowerCoin() {
      return (Item)PomkotsMechs.POM_COIN_SILVER.get();
   }

   @Nullable
   @Override
   protected Item getHigherCoin() {
      return null;
   }
}
