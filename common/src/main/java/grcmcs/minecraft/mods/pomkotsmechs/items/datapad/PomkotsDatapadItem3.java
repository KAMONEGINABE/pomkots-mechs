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

   public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
      if (!level.m_5776_()) {
         player.m_5893_(new SimpleMenuProvider((id, inv, p) -> new DataPadMenu(id, inv, p, p.m_21120_(hand)), Component.m_237113_("Pomkots Datapad")));
      }

      return InteractionResultHolder.m_19090_(player.m_21120_(hand));
   }

   public static SimpleContainer createKeyCardContainer(ItemStack datapad) {
      SimpleContainer container = new SimpleContainer(8);
      CompoundTag tag = datapad.m_41783_();
      if (tag == null) {
         return container;
      }

      ListTag list = tag.m_128437_(TAG_KEY_CARDS, 10);

      for (int i = 0; i < list.size() && i < 8; i++) {
         container.m_6836_(i, ItemStack.m_41712_(list.m_128728_(i)));
      }

      return container;
   }

   public static void saveKeyCardContainer(ItemStack datapad, Container container) {
      ListTag list = new ListTag();

      for (int i = 0; i < container.m_6643_(); i++) {
         ItemStack stack = container.m_8020_(i);
         if (!stack.m_41619_()) {
            CompoundTag itemTag = new CompoundTag();
            stack.m_41739_(itemTag);
            list.add(itemTag);
         }
      }

      datapad.m_41784_().m_128365_(TAG_KEY_CARDS, list);
   }

   public static boolean isMatchApplied(ItemStack stack) {
      return true;
   }

   public static boolean isReturnPosEnabled(ItemStack stack) {
      return true;
   }

   public static void setArenaRequest(ItemStack datapad, UUID requestUuid, String arenaId) {
      CompoundTag tag = datapad.m_41784_();
      tag.m_128362_(TAG_ARENA_REQUEST_UUID, requestUuid);
      tag.m_128359_(TAG_ARENA_ID, arenaId);
   }

   @Nullable
   public static PomkotsDatapadItem3.ArenaRequestInfo getArenaRequest(ItemStack datapad) {
      CompoundTag tag = datapad.m_41783_();
      return tag != null && tag.m_128403_(TAG_ARENA_REQUEST_UUID) && tag.m_128441_(TAG_ARENA_ID)
         ? new PomkotsDatapadItem3.ArenaRequestInfo(tag.m_128342_(TAG_ARENA_REQUEST_UUID), tag.m_128461_(TAG_ARENA_ID))
         : null;
   }

   public static boolean hasArenaRequest(ItemStack datapad) {
      return getArenaRequest(datapad) != null;
   }

   public static void clearArenaRequest(ItemStack datapad) {
      CompoundTag tag = datapad.m_41783_();
      if (tag != null) {
         tag.m_128473_(TAG_ARENA_REQUEST_UUID);
         tag.m_128473_(TAG_ARENA_ID);
      }
   }

   public static void setReturnLocation(ItemStack datapad, ResourceKey<Level> dimension, BlockPos pos) {
      CompoundTag tag = datapad.m_41784_();
      tag.m_128359_(TAG_RETURN_DIMENSION, dimension.m_135782_().toString());
      tag.m_128405_(TAG_RETURN_X, pos.m_123341_());
      tag.m_128405_(TAG_RETURN_Y, pos.m_123342_());
      tag.m_128405_(TAG_RETURN_Z, pos.m_123343_());
   }

   @Nullable
   public static PomkotsDatapadItem3.ReturnLocation getReturnLocation(ItemStack datapad) {
      CompoundTag tag = datapad.m_41783_();
      if (tag != null && tag.m_128441_(TAG_RETURN_DIMENSION)) {
         ResourceLocation dimensionId = new ResourceLocation(tag.m_128461_(TAG_RETURN_DIMENSION));
         ResourceKey<Level> dimension = ResourceKey.m_135785_(Registries.f_256858_, dimensionId);
         BlockPos pos = new BlockPos(tag.m_128451_(TAG_RETURN_X), tag.m_128451_(TAG_RETURN_Y), tag.m_128451_(TAG_RETURN_Z));
         return new PomkotsDatapadItem3.ReturnLocation(dimension, pos);
      } else {
         return null;
      }
   }

   public static boolean hasReturnLocation(ItemStack datapad) {
      CompoundTag tag = datapad.m_41783_();
      return tag != null && tag.m_128441_(TAG_RETURN_DIMENSION);
   }

   public static void clearReturnLocation(ItemStack datapad) {
      CompoundTag tag = datapad.m_41783_();
      if (tag != null) {
         tag.m_128473_(TAG_RETURN_DIMENSION);
         tag.m_128473_(TAG_RETURN_X);
         tag.m_128473_(TAG_RETURN_Y);
         tag.m_128473_(TAG_RETURN_Z);
      }
   }

   public record ArenaRequestInfo(UUID requestUuid, String arenaId) {
   }

   public record ReturnLocation(ResourceKey<Level> dimension, BlockPos pos) {
   }
}
