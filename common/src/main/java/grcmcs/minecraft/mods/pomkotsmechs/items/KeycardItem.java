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

   public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand interactionHand) {
      Level level = player.level();
      if (level instanceof ServerLevel serverLevel && target instanceof Pmvc01Entity mech && this.isTargetMech(mech, itemStack, serverLevel)) {
         if (mech.isLocked()) {
            player.sendSystemMessage(Utils.string2Component("{text.pomkotsmechs.messages.pmvc01.unlock}"));
         } else {
            player.sendSystemMessage(Utils.string2Component("{text.pomkotsmechs.messages.pmvc01.lock}"));
         }

         mech.setLocked(!mech.isLocked());
      }

      return InteractionResult.sidedSuccess(level.isClientSide());
   }

   public boolean isTargetMech(Pmvc01Entity mech, ItemStack stack, Level level) {
      if (stack.hasTag() && stack.getTag().contains(NBT_MECH_UUID) && level instanceof ServerLevel sl) {
         UUID uuid = stack.getTag().getUUID(NBT_MECH_UUID);
         return uuid.equals(mech.getUUID());
      } else {
         return false;
      }
   }

   public void setMech(ItemStack stack, Pmvc01Entity mech, Level level) {
      CompoundTag tag = stack.getOrCreateTag();
      tag.putUUID(NBT_MECH_UUID, mech.getUUID());
      if (mech.hasCustomName()) {
         tag.putString(NBT_MECH_NAME, mech.getCustomName().getString());
      }

      stack.setTag(tag);
   }

   public static UUID getMechUuid(ItemStack stack) {
      return stack.hasTag() && stack.getTag().contains(NBT_MECH_UUID) ? stack.getTag().getUUID(NBT_MECH_UUID) : null;
   }

   public static String getMechName(ItemStack stack) {
      if (stack.hasTag() && stack.getTag().contains(NBT_MECH_NAME)) {
         String name = stack.getTag().getString(NBT_MECH_NAME);
         if (!name.isEmpty()) {
            return name;
         }
      }

      return "NO_NAME";
   }

   public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltip, @NotNull TooltipFlag flag) {
      super.appendHoverText(stack, level, tooltip, flag);
      tooltip.add(Component.literal("Mech Name : " + getMechName(stack)));
   }
}
