package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.google.common.collect.ImmutableList;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
   @Shadow
   public abstract Inventory m_150109_();

   protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
      super(entityType, world);
   }

   @Inject(method = "dropEquipment", cancellable = true, at = @At("HEAD"))
   private void onDropEquipment(CallbackInfo ci) {
      super.m_5907_();
      Player entity = (Player)this;
      if (!entity.m_9236_().m_46469_().m_46207_(GameRules.f_46133_)) {
         Inventory inv = this.m_150109_();
         this.destroyVanishingCursedItems(inv);

         for (List<ItemStack> invItem : ImmutableList.of(inv.f_35974_, inv.f_35975_, inv.f_35976_)) {
            for (int i = 0; i < invItem.size(); i++) {
               ItemStack itemStack = invItem.get(i);
               if (!itemStack.m_41619_() && this.isDropTarget(itemStack)) {
                  inv.f_35978_.m_7197_(itemStack, true, false);
                  invItem.set(i, ItemStack.f_41583_);
               }
            }
         }
      }

      ci.cancel();
   }

   protected boolean isDropTarget(ItemStack stack) {
      return !stack.m_150930_((Item)PomkotsMechs.KEYCARD_ITEM.get()) && !stack.m_150930_((Item)PomkotsMechs.POMKOTS_RADAR_ITEM.get());
   }

   protected void destroyVanishingCursedItems(Inventory inventory) {
      for (int i = 0; i < inventory.m_6643_(); i++) {
         ItemStack itemStack = inventory.m_8020_(i);
         if (!itemStack.m_41619_() && EnchantmentHelper.m_44924_(itemStack)) {
            inventory.m_8016_(i);
         }
      }
   }
}
