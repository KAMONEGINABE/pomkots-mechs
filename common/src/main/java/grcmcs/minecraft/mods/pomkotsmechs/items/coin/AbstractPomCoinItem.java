package grcmcs.minecraft.mods.pomkotsmechs.items.coin;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPomCoinItem extends Item {
   protected AbstractPomCoinItem(Properties properties) {
      super(properties);
   }

   @Nullable
   protected abstract Item getLowerCoin();

   @Nullable
   protected abstract Item getHigherCoin();

   @NotNull
   public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, @NotNull InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!level.m_5776_()) {
         if (player.m_6144_()) {
            this.exchangeUp((ServerPlayer)player, stack);
         } else {
            this.exchangeDown((ServerPlayer)player, stack);
         }
      }

      return InteractionResultHolder.m_19092_(stack, level.m_5776_());
   }

   private void exchangeUp(ServerPlayer player, ItemStack stack) {
      Item higherCoin = this.getHigherCoin();
      if (higherCoin != null) {
         if (stack.m_41613_() >= 64) {
            ItemStack result = new ItemStack(higherCoin, 1);
            if (!player.m_150109_().m_36054_(result.m_41777_())) {
               player.m_5661_(Component.m_237115_("text.pomkotsmechs.messages.inventory_full"), true);
            } else {
               stack.m_41774_(64);
               this.playExchangeSound(player);
            }
         }
      }
   }

   private void exchangeDown(ServerPlayer player, ItemStack stack) {
      Item lowerCoin = this.getLowerCoin();
      if (lowerCoin != null) {
         ItemStack result = new ItemStack(lowerCoin, 64);
         if (!player.m_150109_().m_36054_(result.m_41777_())) {
            player.m_5661_(Component.m_237115_("text.pomkotsmechs.messages.inventory_full"), true);
         } else {
            stack.m_41774_(1);
            this.playExchangeSound(player);
         }
      }
   }

   protected void playExchangeSound(ServerPlayer player) {
      player.m_9236_().m_5594_(null, player.m_20183_(), (SoundEvent)PomkotsMechs.SE_COIN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltip, @NotNull TooltipFlag flag) {
      tooltip.add(Component.m_237115_("tooltip.pomkotsmechs.pom_coin_1"));
      tooltip.add(Component.m_237115_("tooltip.pomkotsmechs.pom_coin_2"));
   }
}
