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

   public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
      if (!level.isClientSide()) {
         player.openMenu(new SimpleMenuProvider((id, inv, p) -> new DataPadMenu(id, inv, p, p.getItemInHand(hand)), Component.literal("Pomkots Datapad")));
      }

      return InteractionResultHolder.success(player.getItemInHand(hand));
   }

   private static CompoundTag getKeyCardTag(ItemStack datapad) {
      CompoundTag root = datapad.getOrCreateTag();
      if (!root.contains(TAG_KEY_CARDS, 10)) {
         root.put(TAG_KEY_CARDS, new CompoundTag());
      }

      return root.getCompound(TAG_KEY_CARDS);
   }

   public static int getKeyCardNum(ItemStack datapad) {
      CompoundTag root = datapad.getTag();
      if (root != null && root.contains(TAG_KEY_CARDS, 10)) {
         CompoundTag keyCardTag = root.getCompound(TAG_KEY_CARDS);
         ListTag list = keyCardTag.getList("Items", 10);
         int num = 0;

         for (int i = 0; i < list.size() && i < 8; i++) {
            if (ItemStack.of(list.getCompound(i)).getItem() instanceof KeycardItem) {
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
      CompoundTag root = datapad.getTag();
      if (root != null && root.contains(TAG_KEY_CARDS, 10)) {
         CompoundTag keyCardTag = root.getCompound(TAG_KEY_CARDS);
         ListTag list = keyCardTag.getList("Items", 10);

         for (int i = 0; i < list.size() && i < 8; i++) {
            container.setItem(i, ItemStack.of(list.getCompound(i)));
         }

         return container;
      } else {
         return container;
      }
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

      CompoundTag keyCardTag = getKeyCardTag(datapad);
      keyCardTag.put("Items", list);
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
      ListTag list = stack.getOrCreateTag().getList(TAG_RADAR_TARGETS, 10);
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

      stack.getOrCreateTag().put(TAG_RADAR_TARGETS, list);
   }

   public static int getSelectedIndex(ItemStack stack) {
      return stack.getOrCreateTag().getInt(TAG_RADAR_SELECTED_INDEX);
   }

   public static void setSelectedIndex(ItemStack stack, int index) {
      stack.getOrCreateTag().putInt(TAG_RADAR_SELECTED_INDEX, index);
   }

   public static Optional<RadarTarget> getActiveTarget(ItemStack stack) {
      List<RadarTarget> targets = getTargets(stack);
      if (targets.isEmpty()) {
         return Optional.empty();
      }

      int idx = Mth.clamp(getSelectedIndex(stack), 0, targets.size() - 1);
      return Optional.of(targets.get(idx));
   }

   private static CompoundTag getArenaTag(ItemStack datapad) {
      CompoundTag root = datapad.getOrCreateTag();
      if (!root.contains(TAG_ARENA, 10)) {
         root.put(TAG_ARENA, new CompoundTag());
      }

      return root.getCompound(TAG_ARENA);
   }

   public static void addArenaRequest(ItemStack datapad, UUID challengerUuid, UUID requestUuid, String arenaId) {
      CompoundTag arenaTag = getArenaTag(datapad);
      ListTag requests = arenaTag.getList("Requests", 10);
      CompoundTag request = new CompoundTag();
      request.putUUID("ChallengerUuid", challengerUuid);
      request.putUUID("RequestUuid", requestUuid);
      request.putString("ArenaId", arenaId);
      requests.add(request);
      arenaTag.put("Requests", requests);
   }

   public static boolean hasArenaRequests(ItemStack datapad) {
      return !getArenaRequests(datapad).isEmpty();
   }

   public static List<PomkotsDatapadItem.ArenaRequestInfo> getArenaRequests(ItemStack datapad) {
      List<PomkotsDatapadItem.ArenaRequestInfo> result = new ArrayList<>();
      CompoundTag root = datapad.getTag();
      if (root != null && root.contains(TAG_ARENA, 10)) {
         CompoundTag arenaTag = root.getCompound(TAG_ARENA);
         ListTag requests = arenaTag.getList("Requests", 10);

         for (int i = 0; i < requests.size(); i++) {
            CompoundTag request = requests.getCompound(i);
            result.add(
               new PomkotsDatapadItem.ArenaRequestInfo(request.getUUID("RequestUuid"), request.getUUID("ChallengerUuid"), request.getString("ArenaId"))
            );
         }

         return result;
      } else {
         return result;
      }
   }

   public static void removeArenaRequest(ItemStack datapad, UUID requestUuid) {
      CompoundTag arenaTag = getArenaTag(datapad);
      ListTag requests = arenaTag.getList("Requests", 10);

      for (int i = requests.size() - 1; i >= 0; i--) {
         CompoundTag request = requests.getCompound(i);
         if (requestUuid.equals(request.getUUID("RequestUuid"))) {
            requests.remove(i);
         }
      }

      arenaTag.put("Requests", requests);
   }

   public static void clearArenaRequests(ItemStack datapad) {
      CompoundTag root = datapad.getTag();
      if (root != null && root.contains(TAG_ARENA)) {
         root.getCompound(TAG_ARENA).remove("Requests");
      }
   }

   public static void addArenaRegistration(ItemStack datapad, String arenaId) {
      CompoundTag arenaTag = getArenaTag(datapad);
      ListTag list = arenaTag.getList("RegisteredArenas", 8);

      for (int i = 0; i < list.size(); i++) {
         if (arenaId.equals(list.getString(i))) {
            return;
         }
      }

      list.add(StringTag.valueOf(arenaId));
      arenaTag.put("RegisteredArenas", list);
   }

   public static void removeArenaRegistration(ItemStack datapad, String arenaId) {
      CompoundTag arenaTag = getArenaTag(datapad);
      ListTag list = arenaTag.getList("RegisteredArenas", 8);

      for (int i = list.size() - 1; i >= 0; i--) {
         if (arenaId.equals(list.getString(i))) {
            list.remove(i);
         }
      }

      arenaTag.put("RegisteredArenas", list);
   }

   public static boolean isArenaRegistered(ItemStack datapad, String arenaId) {
      CompoundTag root = datapad.getTag();
      if (root != null && root.contains(TAG_ARENA, 10)) {
         ListTag list = root.getCompound(TAG_ARENA).getList("RegisteredArenas", 8);

         for (int i = 0; i < list.size(); i++) {
            if (arenaId.equals(list.getString(i))) {
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
      CompoundTag root = datapad.getTag();
      if (root != null && root.contains(TAG_ARENA, 10)) {
         ListTag list = root.getCompound(TAG_ARENA).getList("RegisteredArenas", 8);

         for (int i = 0; i < list.size(); i++) {
            result.add(list.getString(i));
         }

         return result;
      } else {
         return result;
      }
   }

   public static void setReturnLocation(ItemStack datapad, ResourceKey<Level> dimension, BlockPos pos) {
      CompoundTag arenaTag = getArenaTag(datapad);
      CompoundTag returnTag = new CompoundTag();
      returnTag.putString("Dimension", dimension.location().toString());
      returnTag.putInt("X", pos.getX());
      returnTag.putInt("Y", pos.getY());
      returnTag.putInt("Z", pos.getZ());
      arenaTag.put("ReturnLocation", returnTag);
   }

   @Nullable
   public static PomkotsDatapadItem.ReturnLocation getReturnLocation(ItemStack datapad) {
      CompoundTag root = datapad.getTag();
      if (root != null && root.contains(TAG_ARENA, 10)) {
         CompoundTag arenaTag = root.getCompound(TAG_ARENA);
         if (!arenaTag.contains("ReturnLocation", 10)) {
            return null;
         }

         CompoundTag returnTag = arenaTag.getCompound("ReturnLocation");
         ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(returnTag.getString("Dimension")));
         return new PomkotsDatapadItem.ReturnLocation(dimension, new BlockPos(returnTag.getInt("X"), returnTag.getInt("Y"), returnTag.getInt("Z")));
      } else {
         return null;
      }
   }

   public static void clearReturnLocation(ItemStack datapad) {
      CompoundTag root = datapad.getTag();
      if (root != null && root.contains(TAG_ARENA)) {
         root.getCompound(TAG_ARENA).remove("ReturnLocation");
      }
   }

   public static boolean hasReturnLocation(ItemStack datapad) {
      return getReturnLocation(datapad) != null;
   }

   public static List<ItemStack> getPlayersDataPads(Player player) {
      List<ItemStack> res = new ArrayList<>();

      for (ItemStack ent : player.getInventory().items) {
         if (ent.getItem() instanceof PomkotsDatapadItem) {
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
