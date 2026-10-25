package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.KeycardItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaFighterData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaMatchData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaMechData;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ArenaReceptionistMenu extends AbstractContainerMenu {
   public static final int MODE_RECEPTION = 0;
   public static final int MODE_DATAPAD = 1;
   public static final int BTN_ID_ACCEPT_MATCH = 0;
   public static final int BTN_ID_DECLINE_MATCH = 1;
   public static final int BTN_ID_RETURN_FROM_MATCH = 2;
   private final SimpleContainer dataPadContainerForRegistration = new SimpleContainer(1);
   private String arenaId = "";
   private int mode = 0;
   private ItemStack dataPadStack = ItemStack.EMPTY;
   private List<ArenaRankingEntry> rankings = List.of();

   public ArenaReceptionistMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
      this(containerId, playerInventory, getData(buf));
   }

   private static ArenaMenuData.Entry getData(FriendlyByteBuf buf) {
      ArenaMenuData.Entry data = null;
      if (buf != null) {
         data = ArenaMenuData.read(buf);
      }

      return data;
   }

   public ArenaReceptionistMenu(int containerId, Inventory playerInventory, ArenaMenuData.Entry extendedData) {
      super((MenuType)PomkotsMechs.ARENA_RECEPTIONIST.get(), containerId);
      this.addSlots(playerInventory);
      if (extendedData != null) {
         this.arenaId = extendedData.getArenaId();
         this.mode = extendedData.getMode();
         this.dataPadStack = extendedData.getMechDataPadStack();
         this.rankings = extendedData.getRankings();
      }
   }

   private void addSlots(Inventory playerInventory) {
      this.addSlot(new ArenaReceptionistMenu.ToggleableSlot(this.dataPadContainerForRegistration, 0, 40, 164) {
         public boolean mayPlace(ItemStack stack) {
            return stack.is((Item)PomkotsMechs.POMKOTS_DATAPAD_ITEM.get());
         }
      });
      this.addPlayerInventory(playerInventory);
   }

   private void addPlayerInventory(Inventory inventory) {
      int offsetX = 312;
      int offsetY = 53;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.addSlot(new ArenaReceptionistMenu.ToggleableSlot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.addSlot(new ArenaReceptionistMenu.ToggleableSlot(inventory, i, offsetX + i * 18, offsetY + 54 + 4));
      }
   }

   public String getArenaId() {
      return this.arenaId;
   }

   public List<ArenaRankingEntry> getRankings() {
      return this.rankings;
   }

   public ItemStack getDataPadStack() {
      return this.dataPadStack;
   }

   public int getMode() {
      return this.mode;
   }

   public boolean stillValid(Player player) {
      return true;
   }

   public ItemStack quickMoveStack(Player player, int slot) {
      return ItemStack.EMPTY;
   }

   public void savePlayerProfile(Player player, String name, String comment, String arenaId) {
      if (player instanceof ServerPlayer serverPlayer) {
         ItemStack dataPadItemStack = this.dataPadContainerForRegistration.getItem(0);
         ArenaFighterData registeredInfo = ArenaManager.getFighter(serverPlayer.server, arenaId, serverPlayer.getUUID());
         if (registeredInfo == null) {
            ArenaFighterData data = new ArenaFighterData();
            if (name.isEmpty()) {
               name = player.getName().getString();
            }

            if (comment.isEmpty()) {
               comment = "NO COMMENT";
            }

            if (!(dataPadItemStack.getItem() instanceof PomkotsDatapadItem)) {
               this.sendMessage("Set your Pomkots Data Pad to the slot", player);
               return;
            }

            data.setType(ArenaFighterData.FighterType.PLAYER);
            data.setRating(0);
            data.setFighterId(player.getUUID());
            data.setDisplayName(name);
            data.setComment(comment);
            ArenaMechData mech = this.getMechDataFromDataPad(dataPadItemStack, player);
            if (mech == null) {
               this.sendMessage("No mech data is registered in your Data Pad", player);
               return;
            }

            data.setMechData(mech);
            System.out.println(mech.getMechName());
            PomkotsDatapadItem.addArenaRegistration(dataPadItemStack, arenaId);
            this.backDataPad(player);
            ArenaManager.registerFighter(serverPlayer.server, arenaId, data);
            Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/arena_register"), serverPlayer);
         } else {
            boolean changed = false;
            if (!name.isEmpty()) {
               registeredInfo.setDisplayName(name);
               changed = true;
            }

            if (!comment.isEmpty()) {
               registeredInfo.setComment(comment);
               changed = true;
            }

            if (dataPadItemStack.getItem() instanceof PomkotsDatapadItem) {
               ArenaMechData mech = this.getMechDataFromDataPad(dataPadItemStack, player);
               if (mech != null) {
                  registeredInfo.setMechData(mech);
                  changed = true;
               }

               this.backDataPad(player);
            }

            if (changed) {
               ArenaManager.update(serverPlayer.server, arenaId);
            }
         }
      }
   }

   private void backDataPad(Player player) {
      ItemStack card = this.dataPadContainerForRegistration.getItem(0);
      this.dataPadContainerForRegistration.setItem(0, ItemStack.EMPTY);
      if (!card.isEmpty() && !player.getInventory().add(card)) {
         player.drop(card, false);
      }
   }

   private ArenaMechData getMechDataFromDataPad(ItemStack dataPadItemStack, Player player) {
      ArenaMechData res = null;

      for (ItemStack card : PomkotsDatapadItem.createKeyCardContainer(dataPadItemStack).items) {
         if (card.getItem() instanceof KeycardItem) {
            res = this.getMechDataFromKeyCard(card, player);
            break;
         }
      }

      return res;
   }

   private ArenaMechData getMechDataFromKeyCard(ItemStack cardItemStack, Player player) {
      CompoundTag tag = cardItemStack.getTag();
      if (tag != null && tag.hasUUID(KeycardItem.NBT_MECH_UUID)) {
         UUID mechUuid = tag.getUUID(KeycardItem.NBT_MECH_UUID);
         if (((ServerLevel)player.level()).getEntity(mechUuid) instanceof Pmvc01Entity mech) {
            return ArenaMechData.fromMechInstance(mech);
         } else {
            this.sendMessage("Bring mech near by", player);
            return null;
         }
      } else {
         this.sendMessage("Invalid Key Card", player);
         return null;
      }
   }

   public void removePlayerProfile(Player player, String arenaId) {
      if (player instanceof ServerPlayer serverPlayer) {
         ItemStack dataPadItemStack = this.dataPadContainerForRegistration.getItem(0);
         if (dataPadItemStack.getItem() instanceof PomkotsDatapadItem) {
            PomkotsDatapadItem.removeArenaRegistration(dataPadItemStack, arenaId);
            this.backDataPad(player);
            ArenaManager.removeFighter(serverPlayer.server, arenaId, serverPlayer.getUUID());
         } else {
            this.sendMessage("Set your Data Pad to remove", player);
         }
      }
   }

   private void sendMessage(String msg, Player player) {
      player.sendSystemMessage(Component.literal(msg));
   }

   public void setRankings(List<ArenaRankingEntry> rankings) {
      this.rankings = rankings;
   }

   public void removed(Player player) {
      this.backDataPad(player);
      super.removed(player);
   }

   public boolean clickMenuButton(Player player, int id) {
      if (player instanceof ServerPlayer serverPlayer) {
         switch (id) {
            case 0:
               if (this.dataPadStack == null) {
                  return true;
               }

               if (serverPlayer.getVehicle() instanceof Pmvc01Entity mech) {
                  for (PomkotsDatapadItem.ArenaRequestInfo req : PomkotsDatapadItem.getArenaRequests(this.dataPadStack)) {
                     ArenaMatchData match = ArenaManager.getActiveMatch(this.arenaId, req.requestUuid(), serverPlayer.getServer());
                     if (match != null && match.getTeleportPointPos() != null) {
                        PomkotsDatapadItem.setReturnLocation(this.dataPadStack, serverPlayer.level().dimension(), mech.blockPosition());
                        PomkotsDatapadItem.clearArenaRequests(this.dataPadStack);
                        BlockPos pos = match.getTeleportPointPos();
                        mech.teleportTo(pos.getX(), pos.getY(), pos.getZ());
                        return true;
                     }
                  }
               } else {
                  serverPlayer.sendSystemMessage(Component.literal("Ride a mech before teleport"));
               }
               break;
            case 1:
               if (this.dataPadStack == null) {
                  return true;
               }

               for (PomkotsDatapadItem.ArenaRequestInfo req : PomkotsDatapadItem.getArenaRequests(this.dataPadStack)) {
                  ArenaMatchData match = ArenaManager.getActiveMatch(this.arenaId, req.requestUuid(), serverPlayer.getServer());
                  if (match != null) {
                     ArenaManager.cancelMatch("Declined from opponent", this.arenaId, match.getMatchId(), serverPlayer.server);
                  }
               }
               break;
            case 2:
               if (serverPlayer.getVehicle() instanceof Pmvc01Entity mech) {
                  PomkotsDatapadItem.ReturnLocation location = PomkotsDatapadItem.getReturnLocation(this.dataPadStack);
                  if (location != null) {
                     ServerLevel targetLevel = serverPlayer.server.getLevel(location.dimension());
                     if (targetLevel != null) {
                        PomkotsDatapadItem.clearReturnLocation(this.dataPadStack);
                        mech.teleportTo(
                           targetLevel,
                           location.pos().getX() + 0.5,
                           location.pos().getY(),
                           location.pos().getZ() + 0.5,
                           Set.of(),
                           player.getYRot(),
                           player.getXRot()
                        );
                     }
                  }
               } else {
                  serverPlayer.sendSystemMessage(Component.literal("Ride a mech before teleport"));
               }
         }
      }

      return true;
   }

   public static class ToggleableSlot extends Slot {
      private boolean active = true;

      public ToggleableSlot(Container container, int slot, int x, int y) {
         super(container, slot, x, y);
      }

      public boolean isActive() {
         return this.active;
      }

      public void setActive(boolean active) {
         this.active = active;
      }
   }
}
