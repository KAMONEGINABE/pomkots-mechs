package grcmcs.minecraft.mods.pomkotsmechs.items.datapad;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadMenu;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class PomkotsDatapadItem3 extends Item {
   private static final String TAG_KEY_CARDS = PomkotsMechs.nbtName("KeyCards");
   private static final String TAG_ARENA_REQUEST_UUID = PomkotsMechs.nbtName("ArenaRequestUuid");
   private static final String TAG_ARENA_ID = PomkotsMechs.nbtName("ArenaId");
   private static final String TAG_RETURN_DIMENSION = PomkotsMechs.nbtName("ReturnDimension");
   private static final String TAG_RETURN_X = PomkotsMechs.nbtName("ReturnPosX");
   private static final String TAG_RETURN_Y = PomkotsMechs.nbtName("ReturnPosY");
   private static final String TAG_RETURN_Z = PomkotsMechs.nbtName("ReturnPosZ");
   public static final int MAX_KEYCARD_SLOTS = 8;

   public PomkotsDatapadItem3(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      if (!level.isClientSide()) {
         player.openMenu(new SimpleMenuProvider((id, inv, p) -> new DataPadMenu(id, inv, p, p.getItemInHand(hand)), Component.literal("Pomkots Datapad")));
      }

      return InteractionResultHolder.success(player.getItemInHand(hand));
   }

   public static SimpleContainer createKeyCardContainer(ItemStack datapad) {
      SimpleContainer container = new SimpleContainer(8);
      CompoundTag tag = datapad.getTag();
      if (tag == null) {
         return container;
      }

      ListTag list = tag.getList(TAG_KEY_CARDS, 10);

      for (int i = 0; i < list.size() && i < 8; i++) {
         container.setItem(i, ItemStack.of(list.getCompound(i)));
      }

      return container;
   }

   public static void saveKeyCardContainer(ItemStack datapad, Container container) {
      ListTag list = new ListTag();

      for (int i = 0; i < container.getContainerSize(); i++) {
         ItemStack stack = container.getItem(i);
         if (!stack.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            stack.save(itemTag);
            list.add(itemTag);
         }
      }

      datapad.getOrCreateTag().put(TAG_KEY_CARDS, list);
   }

   public static boolean isMatchApplied(ItemStack stack) {
      return true;
   }

   public static boolean isReturnPosEnabled(ItemStack stack) {
      return true;
   }

   public static void setArenaRequest(ItemStack datapad, UUID requestUuid, String arenaId) {
      CompoundTag tag = datapad.getOrCreateTag();
      tag.putUUID(TAG_ARENA_REQUEST_UUID, requestUuid);
      tag.putString(TAG_ARENA_ID, arenaId);
   }

   @Nullable
   public static PomkotsDatapadItem3.ArenaRequestInfo getArenaRequest(ItemStack datapad) {
      CompoundTag tag = datapad.getTag();
      return tag != null && tag.hasUUID(TAG_ARENA_REQUEST_UUID) && tag.contains(TAG_ARENA_ID)
         ? new PomkotsDatapadItem3.ArenaRequestInfo(tag.getUUID(TAG_ARENA_REQUEST_UUID), tag.getString(TAG_ARENA_ID))
         : null;
   }

   public static boolean hasArenaRequest(ItemStack datapad) {
      return getArenaRequest(datapad) != null;
   }

   public static void clearArenaRequest(ItemStack datapad) {
      CompoundTag tag = datapad.getTag();
      if (tag != null) {
         tag.remove(TAG_ARENA_REQUEST_UUID);
         tag.remove(TAG_ARENA_ID);
      }
   }

   public static void setReturnLocation(ItemStack datapad, ResourceKey<Level> dimension, BlockPos pos) {
      CompoundTag tag = datapad.getOrCreateTag();
      tag.putString(TAG_RETURN_DIMENSION, dimension.location().toString());
      tag.putInt(TAG_RETURN_X, pos.getX());
      tag.putInt(TAG_RETURN_Y, pos.getY());
      tag.putInt(TAG_RETURN_Z, pos.getZ());
   }

   @Nullable
   public static PomkotsDatapadItem3.ReturnLocation getReturnLocation(ItemStack datapad) {
      CompoundTag tag = datapad.getTag();
      if (tag != null && tag.contains(TAG_RETURN_DIMENSION)) {
         ResourceLocation dimensionId = new ResourceLocation(tag.getString(TAG_RETURN_DIMENSION));
         ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
         BlockPos pos = new BlockPos(tag.getInt(TAG_RETURN_X), tag.getInt(TAG_RETURN_Y), tag.getInt(TAG_RETURN_Z));
         return new PomkotsDatapadItem3.ReturnLocation(dimension, pos);
      } else {
         return null;
      }
   }

   public static boolean hasReturnLocation(ItemStack datapad) {
      CompoundTag tag = datapad.getTag();
      return tag != null && tag.contains(TAG_RETURN_DIMENSION);
   }

   public static void clearReturnLocation(ItemStack datapad) {
      CompoundTag tag = datapad.getTag();
      if (tag != null) {
         tag.remove(TAG_RETURN_DIMENSION);
         tag.remove(TAG_RETURN_X);
         tag.remove(TAG_RETURN_Y);
         tag.remove(TAG_RETURN_Z);
      }
   }

   public record ArenaRequestInfo(UUID requestUuid, String arenaId) {
   }

   public record ReturnLocation(ResourceKey<Level> dimension, BlockPos pos) {
   }
}
