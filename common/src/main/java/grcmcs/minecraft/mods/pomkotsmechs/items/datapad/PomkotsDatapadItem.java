package grcmcs.minecraft.mods.pomkotsmechs.items.datapad;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadMenu;
import grcmcs.minecraft.mods.pomkotsmechs.items.KeycardItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.RadarTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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

public class PomkotsDatapadItem extends Item {
   private static final String TAG_RADAR_TARGETS = PomkotsMechs.nbtName("RadarTargets");
   private static final String TAG_RADAR_SELECTED_INDEX = PomkotsMechs.nbtName("RadarSelectedIndex");
   private static final String TAG_ARENA = PomkotsMechs.nbtName("Arena");
   private static final String TAG_ARENA_REQUESTS = "Requests";
   private static final String TAG_RETURN_LOCATION = "ReturnLocation";
   private static final String TAG_REQUEST_UUID = "RequestUuid";
   private static final String TAG_CHALLENGER_UUID = "ChallengerUuid";
   private static final String TAG_ARENA_ID = "ArenaId";
   private static final String TAG_REGISTERED_ARENAS = "RegisteredArenas";
   private static final String TAG_RETURN_DIMENSION = "Dimension";
   private static final String TAG_RETURN_X = "X";
   private static final String TAG_RETURN_Y = "Y";
   private static final String TAG_RETURN_Z = "Z";
   private static final String TAG_KEY_CARDS = PomkotsMechs.nbtName("KeyCards");
   private static final String TAG_KEY_CARD_ITEMS = "Items";
   public static final int MAX_KEYCARD_SLOTS = 8;

   public PomkotsDatapadItem(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
      if (!level.m_5776_()) {
         player.m_5893_(new SimpleMenuProvider((id, inv, p) -> new DataPadMenu(id, inv, p, p.m_21120_(hand)), Component.m_237113_("Pomkots Datapad")));
      }

      return InteractionResultHolder.m_19090_(player.m_21120_(hand));
   }

   private static CompoundTag getKeyCardTag(ItemStack datapad) {
      CompoundTag root = datapad.m_41784_();
      if (!root.m_128425_(TAG_KEY_CARDS, 10)) {
         root.m_128365_(TAG_KEY_CARDS, new CompoundTag());
      }

      return root.m_128469_(TAG_KEY_CARDS);
   }

   public static int getKeyCardNum(ItemStack datapad) {
      CompoundTag root = datapad.m_41783_();
      if (root != null && root.m_128425_(TAG_KEY_CARDS, 10)) {
         CompoundTag keyCardTag = root.m_128469_(TAG_KEY_CARDS);
         ListTag list = keyCardTag.m_128437_("Items", 10);
         int num = 0;

         for (int i = 0; i < list.size() && i < 8; i++) {
            if (ItemStack.m_41712_(list.m_128728_(i)).m_41720_() instanceof KeycardItem) {
               num++;
            }
         }

         return num;
      } else {
         return 0;
      }
   }

   public static SimpleContainer createKeyCardContainer(ItemStack datapad) {
      SimpleContainer container = new SimpleContainer(8);
      CompoundTag root = datapad.m_41783_();
      if (root != null && root.m_128425_(TAG_KEY_CARDS, 10)) {
         CompoundTag keyCardTag = root.m_128469_(TAG_KEY_CARDS);
         ListTag list = keyCardTag.m_128437_("Items", 10);

         for (int i = 0; i < list.size() && i < 8; i++) {
            container.m_6836_(i, ItemStack.m_41712_(list.m_128728_(i)));
         }

         return container;
      } else {
         return container;
      }
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

      CompoundTag keyCardTag = getKeyCardTag(datapad);
      keyCardTag.m_128365_("Items", list);
   }

   public static void addTarget(ItemStack stack, RadarTarget target) {
      List<RadarTarget> targets = getTargets(stack);

      for (int i = 0; i < targets.size(); i++) {
         RadarTarget t = targets.get(i);
         if (t.label().equals(target.label())) {
            setSelectedIndex(stack, i);
            return;
         }
      }

      targets.add(target);
      setTargets(stack, targets);
      setSelectedIndex(stack, targets.size() - 1);
   }

   public static List<RadarTarget> getTargets(ItemStack stack) {
      ListTag list = stack.m_41784_().m_128437_(TAG_RADAR_TARGETS, 10);
      List<RadarTarget> result = new ArrayList<>();

      for (Tag tag : list) {
         result.add(RadarTarget.fromNbt((CompoundTag)tag));
      }

      return result;
   }

   public static void setTargets(ItemStack stack, List<RadarTarget> targets) {
      ListTag list = new ListTag();

      for (RadarTarget t : targets) {
         list.add(RadarTarget.toNbt(t));
      }

      stack.m_41784_().m_128365_(TAG_RADAR_TARGETS, list);
   }

   public static int getSelectedIndex(ItemStack stack) {
      return stack.m_41784_().m_128451_(TAG_RADAR_SELECTED_INDEX);
   }

   public static void setSelectedIndex(ItemStack stack, int index) {
      stack.m_41784_().m_128405_(TAG_RADAR_SELECTED_INDEX, index);
   }

   public static Optional<RadarTarget> getActiveTarget(ItemStack stack) {
      List<RadarTarget> targets = getTargets(stack);
      if (targets.isEmpty()) {
         return Optional.empty();
      }

      int idx = Mth.m_14045_(getSelectedIndex(stack), 0, targets.size() - 1);
      return Optional.of(targets.get(idx));
   }

   private static CompoundTag getArenaTag(ItemStack datapad) {
      CompoundTag root = datapad.m_41784_();
      if (!root.m_128425_(TAG_ARENA, 10)) {
         root.m_128365_(TAG_ARENA, new CompoundTag());
      }

      return root.m_128469_(TAG_ARENA);
   }

   public static void addArenaRequest(ItemStack datapad, UUID challengerUuid, UUID requestUuid, String arenaId) {
      CompoundTag arenaTag = getArenaTag(datapad);
      ListTag requests = arenaTag.m_128437_("Requests", 10);
      CompoundTag request = new CompoundTag();
      request.m_128362_("ChallengerUuid", challengerUuid);
      request.m_128362_("RequestUuid", requestUuid);
      request.m_128359_("ArenaId", arenaId);
      requests.add(request);
      arenaTag.m_128365_("Requests", requests);
   }

   public static boolean hasArenaRequests(ItemStack datapad) {
      return !getArenaRequests(datapad).isEmpty();
   }

   public static List<PomkotsDatapadItem.ArenaRequestInfo> getArenaRequests(ItemStack datapad) {
      List<PomkotsDatapadItem.ArenaRequestInfo> result = new ArrayList<>();
      CompoundTag root = datapad.m_41783_();
      if (root != null && root.m_128425_(TAG_ARENA, 10)) {
         CompoundTag arenaTag = root.m_128469_(TAG_ARENA);
         ListTag requests = arenaTag.m_128437_("Requests", 10);

         for (int i = 0; i < requests.size(); i++) {
            CompoundTag request = requests.m_128728_(i);
            result.add(
               new PomkotsDatapadItem.ArenaRequestInfo(request.m_128342_("RequestUuid"), request.m_128342_("ChallengerUuid"), request.m_128461_("ArenaId"))
            );
         }

         return result;
      } else {
         return result;
      }
   }

   public static void removeArenaRequest(ItemStack datapad, UUID requestUuid) {
      CompoundTag arenaTag = getArenaTag(datapad);
      ListTag requests = arenaTag.m_128437_("Requests", 10);

      for (int i = requests.size() - 1; i >= 0; i--) {
         CompoundTag request = requests.m_128728_(i);
         if (requestUuid.equals(request.m_128342_("RequestUuid"))) {
            requests.remove(i);
         }
      }

      arenaTag.m_128365_("Requests", requests);
   }

   public static void clearArenaRequests(ItemStack datapad) {
      CompoundTag root = datapad.m_41783_();
      if (root != null && root.m_128441_(TAG_ARENA)) {
         root.m_128469_(TAG_ARENA).m_128473_("Requests");
      }
   }

   public static void addArenaRegistration(ItemStack datapad, String arenaId) {
      CompoundTag arenaTag = getArenaTag(datapad);
      ListTag list = arenaTag.m_128437_("RegisteredArenas", 8);

      for (int i = 0; i < list.size(); i++) {
         if (arenaId.equals(list.m_128778_(i))) {
            return;
         }
      }

      list.add(StringTag.m_129297_(arenaId));
      arenaTag.m_128365_("RegisteredArenas", list);
   }

   public static void removeArenaRegistration(ItemStack datapad, String arenaId) {
      CompoundTag arenaTag = getArenaTag(datapad);
      ListTag list = arenaTag.m_128437_("RegisteredArenas", 8);

      for (int i = list.size() - 1; i >= 0; i--) {
         if (arenaId.equals(list.m_128778_(i))) {
            list.remove(i);
         }
      }

      arenaTag.m_128365_("RegisteredArenas", list);
   }

   public static boolean isArenaRegistered(ItemStack datapad, String arenaId) {
      CompoundTag root = datapad.m_41783_();
      if (root != null && root.m_128425_(TAG_ARENA, 10)) {
         ListTag list = root.m_128469_(TAG_ARENA).m_128437_("RegisteredArenas", 8);

         for (int i = 0; i < list.size(); i++) {
            if (arenaId.equals(list.m_128778_(i))) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static List<String> getRegisteredArenaIds(ItemStack datapad) {
      List<String> result = new ArrayList<>();
      CompoundTag root = datapad.m_41783_();
      if (root != null && root.m_128425_(TAG_ARENA, 10)) {
         ListTag list = root.m_128469_(TAG_ARENA).m_128437_("RegisteredArenas", 8);

         for (int i = 0; i < list.size(); i++) {
            result.add(list.m_128778_(i));
         }

         return result;
      } else {
         return result;
      }
   }

   public static void setReturnLocation(ItemStack datapad, ResourceKey<Level> dimension, BlockPos pos) {
      CompoundTag arenaTag = getArenaTag(datapad);
      CompoundTag returnTag = new CompoundTag();
      returnTag.m_128359_("Dimension", dimension.m_135782_().toString());
      returnTag.m_128405_("X", pos.m_123341_());
      returnTag.m_128405_("Y", pos.m_123342_());
      returnTag.m_128405_("Z", pos.m_123343_());
      arenaTag.m_128365_("ReturnLocation", returnTag);
   }

   @Nullable
   public static PomkotsDatapadItem.ReturnLocation getReturnLocation(ItemStack datapad) {
      CompoundTag root = datapad.m_41783_();
      if (root != null && root.m_128425_(TAG_ARENA, 10)) {
         CompoundTag arenaTag = root.m_128469_(TAG_ARENA);
         if (!arenaTag.m_128425_("ReturnLocation", 10)) {
            return null;
         }

         CompoundTag returnTag = arenaTag.m_128469_("ReturnLocation");
         ResourceKey<Level> dimension = ResourceKey.m_135785_(Registries.f_256858_, new ResourceLocation(returnTag.m_128461_("Dimension")));
         return new PomkotsDatapadItem.ReturnLocation(dimension, new BlockPos(returnTag.m_128451_("X"), returnTag.m_128451_("Y"), returnTag.m_128451_("Z")));
      } else {
         return null;
      }
   }

   public static void clearReturnLocation(ItemStack datapad) {
      CompoundTag root = datapad.m_41783_();
      if (root != null && root.m_128441_(TAG_ARENA)) {
         root.m_128469_(TAG_ARENA).m_128473_("ReturnLocation");
      }
   }

   public static boolean hasReturnLocation(ItemStack datapad) {
      return getReturnLocation(datapad) != null;
   }

   public static List<ItemStack> getPlayersDataPads(Player player) {
      List<ItemStack> res = new ArrayList<>();

      for (ItemStack ent : player.m_150109_().f_35974_) {
         if (ent.m_41720_() instanceof PomkotsDatapadItem) {
            res.add(ent);
         }
      }

      return res;
   }

   public record ArenaRequestInfo(UUID requestUuid, UUID challengerUuid, String arenaId) {
   }

   public record ReturnLocation(ResourceKey<Level> dimension, BlockPos pos) {
   }
}
