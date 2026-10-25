package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KeycardItem extends Item {
   public static final String NBT_MECH_UUID = PomkotsMechs.nbtName("MechUUID");
   public static final String NBT_MECH_NAME = PomkotsMechs.nbtName("MechNAME");

   public KeycardItem(Properties properties) {
      super(properties);
   }

   public InteractionResult m_6880_(ItemStack itemStack, Player player, LivingEntity target, InteractionHand interactionHand) {
      Level level = player.m_9236_();
      if (level instanceof ServerLevel serverLevel && target instanceof Pmvc01Entity mech && this.isTargetMech(mech, itemStack, serverLevel)) {
         if (mech.isLocked()) {
            player.m_213846_(Utils.string2Component("{text.pomkotsmechs.messages.pmvc01.unlock}"));
         } else {
            player.m_213846_(Utils.string2Component("{text.pomkotsmechs.messages.pmvc01.lock}"));
         }

         mech.setLocked(!mech.isLocked());
      }

      return InteractionResult.m_19078_(level.m_5776_());
   }

   public boolean isTargetMech(Pmvc01Entity mech, ItemStack stack, Level level) {
      if (stack.m_41782_() && stack.m_41783_().m_128441_(NBT_MECH_UUID) && level instanceof ServerLevel sl) {
         UUID uuid = stack.m_41783_().m_128342_(NBT_MECH_UUID);
         return uuid.equals(mech.m_20148_());
      } else {
         return false;
      }
   }

   public void setMech(ItemStack stack, Pmvc01Entity mech, Level level) {
      CompoundTag tag = stack.m_41784_();
      tag.m_128362_(NBT_MECH_UUID, mech.m_20148_());
      if (mech.m_8077_()) {
         tag.m_128359_(NBT_MECH_NAME, mech.m_7770_().getString());
      }

      stack.m_41751_(tag);
   }

   public static UUID getMechUuid(ItemStack stack) {
      return stack.m_41782_() && stack.m_41783_().m_128441_(NBT_MECH_UUID) ? stack.m_41783_().m_128342_(NBT_MECH_UUID) : null;
   }

   public static String getMechName(ItemStack stack) {
      if (stack.m_41782_() && stack.m_41783_().m_128441_(NBT_MECH_NAME)) {
         String name = stack.m_41783_().m_128461_(NBT_MECH_NAME);
         if (!name.isEmpty()) {
            return name;
         }
      }

      return "NO_NAME";
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltip, @NotNull TooltipFlag flag) {
      super.m_7373_(stack, level, tooltip, flag);
      tooltip.add(Component.m_237113_("Mech Name : " + getMechName(stack)));
   }
}
