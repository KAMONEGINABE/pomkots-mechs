package grcmcs.minecraft.mods.pomkotsmechs.items.coin;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import org.jetbrains.annotations.Nullable;

public class PomCoinItem extends AbstractPomCoinItem {
   public PomCoinItem(Properties properties) {
      super(properties);
   }

   @Nullable
   @Override
   protected Item getLowerCoin() {
      return null;
   }

   @Nullable
   @Override
   protected Item getHigherCoin() {
      return (Item)PomkotsMechs.POM_COIN_SILVER.get();
   }
}
