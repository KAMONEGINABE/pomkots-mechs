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
   private ItemStack dataPadStack = ItemStack.f_41583_;
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
      this.m_38897_(new ArenaReceptionistMenu.ToggleableSlot(this.dataPadContainerForRegistration, 0, 40, 164) {
         public boolean m_5857_(ItemStack stack) {
            return stack.m_150930_((Item)PomkotsMechs.POMKOTS_DATAPAD_ITEM.get());
         }
      });
      this.addPlayerInventory(playerInventory);
   }

   private void addPlayerInventory(Inventory inventory) {
      int offsetX = 312;
      int offsetY = 53;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.m_38897_(new ArenaReceptionistMenu.ToggleableSlot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.m_38897_(new ArenaReceptionistMenu.ToggleableSlot(inventory, i, offsetX + i * 18, offsetY + 54 + 4));
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

   public boolean m_6875_(Player player) {
      return true;
   }

   public ItemStack m_7648_(Player player, int slot) {
      return ItemStack.f_41583_;
   }

   public void savePlayerProfile(Player player, String name, String comment, String arenaId) {
      if (player instanceof ServerPlayer serverPlayer) {
         ItemStack dataPadItemStack = this.dataPadContainerForRegistration.m_8020_(0);
         ArenaFighterData registeredInfo = ArenaManager.getFighter(serverPlayer.f_8924_, arenaId, serverPlayer.m_20148_());
         if (registeredInfo == null) {
            ArenaFighterData data = new ArenaFighterData();
            if (name.isEmpty()) {
               name = player.m_7755_().getString();
            }

            if (comment.isEmpty()) {
               comment = "NO COMMENT";
            }

            if (!(dataPadItemStack.m_41720_() instanceof PomkotsDatapadItem)) {
               this.sendMessage("Set your Pomkots Data Pad to the slot", player);
               return;
            }

            data.setType(ArenaFighterData.FighterType.PLAYER);
            data.setRating(0);
            data.setFighterId(player.m_20148_());
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
            ArenaManager.registerFighter(serverPlayer.f_8924_, arenaId, data);
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

            if (dataPadItemStack.m_41720_() instanceof PomkotsDatapadItem) {
               ArenaMechData mech = this.getMechDataFromDataPad(dataPadItemStack, player);
               if (mech != null) {
                  registeredInfo.setMechData(mech);
                  changed = true;
               }

               this.backDataPad(player);
            }

            if (changed) {
               ArenaManager.update(serverPlayer.f_8924_, arenaId);
            }
         }
      }
   }

   private void backDataPad(Player player) {
      ItemStack card = this.dataPadContainerForRegistration.m_8020_(0);
      this.dataPadContainerForRegistration.m_6836_(0, ItemStack.f_41583_);
      if (!card.m_41619_() && !player.m_150109_().m_36054_(card)) {
         player.m_36176_(card, false);
      }
   }

   private ArenaMechData getMechDataFromDataPad(ItemStack dataPadItemStack, Player player) {
      ArenaMechData res = null;

      for (ItemStack card : PomkotsDatapadItem.createKeyCardContainer(dataPadItemStack).f_19147_) {
         if (card.m_41720_() instanceof KeycardItem) {
            res = this.getMechDataFromKeyCard(card, player);
            break;
         }
      }

      return res;
   }

   private ArenaMechData getMechDataFromKeyCard(ItemStack cardItemStack, Player player) {
      CompoundTag tag = cardItemStack.m_41783_();
      if (tag != null && tag.m_128403_(KeycardItem.NBT_MECH_UUID)) {
         UUID mechUuid = tag.m_128342_(KeycardItem.NBT_MECH_UUID);
         if (((ServerLevel)player.m_9236_()).m_8791_(mechUuid) instanceof Pmvc01Entity mech) {
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
         ItemStack dataPadItemStack = this.dataPadContainerForRegistration.m_8020_(0);
         if (dataPadItemStack.m_41720_() instanceof PomkotsDatapadItem) {
            PomkotsDatapadItem.removeArenaRegistration(dataPadItemStack, arenaId);
            this.backDataPad(player);
            ArenaManager.removeFighter(serverPlayer.f_8924_, arenaId, serverPlayer.m_20148_());
         } else {
            this.sendMessage("Set your Data Pad to remove", player);
         }
      }
   }

   private void sendMessage(String msg, Player player) {
      player.m_213846_(Component.m_237113_(msg));
   }

   public void setRankings(List<ArenaRankingEntry> rankings) {
      this.rankings = rankings;
   }

   public void m_6877_(Player player) {
      this.backDataPad(player);
      super.m_6877_(player);
   }

   public boolean m_6366_(Player player, int id) {
      if (player instanceof ServerPlayer serverPlayer) {
         switch (id) {
            case 0:
               if (this.dataPadStack == null) {
                  return true;
               }

               if (serverPlayer.m_20202_() instanceof Pmvc01Entity mech) {
                  for (PomkotsDatapadItem.ArenaRequestInfo req : PomkotsDatapadItem.getArenaRequests(this.dataPadStack)) {
                     ArenaMatchData match = ArenaManager.getActiveMatch(this.arenaId, req.requestUuid(), serverPlayer.m_20194_());
                     if (match != null && match.getTeleportPointPos() != null) {
                        PomkotsDatapadItem.setReturnLocation(this.dataPadStack, serverPlayer.m_9236_().m_46472_(), mech.m_20183_());
                        PomkotsDatapadItem.clearArenaRequests(this.dataPadStack);
                        BlockPos pos = match.getTeleportPointPos();
                        mech.m_6021_(pos.m_123341_(), pos.m_123342_(), pos.m_123343_());
                        return true;
                     }
                  }
               } else {
                  serverPlayer.m_213846_(Component.m_237113_("Ride a mech before teleport"));
               }
               break;
            case 1:
               if (this.dataPadStack == null) {
                  return true;
               }

               for (PomkotsDatapadItem.ArenaRequestInfo req : PomkotsDatapadItem.getArenaRequests(this.dataPadStack)) {
                  ArenaMatchData match = ArenaManager.getActiveMatch(this.arenaId, req.requestUuid(), serverPlayer.m_20194_());
                  if (match != null) {
                     ArenaManager.cancelMatch("Declined from opponent", this.arenaId, match.getMatchId(), serverPlayer.f_8924_);
                  }
               }
               break;
            case 2:
               if (serverPlayer.m_20202_() instanceof Pmvc01Entity mech) {
                  PomkotsDatapadItem.ReturnLocation location = PomkotsDatapadItem.getReturnLocation(this.dataPadStack);
                  if (location != null) {
                     ServerLevel targetLevel = serverPlayer.f_8924_.m_129880_(location.dimension());
                     if (targetLevel != null) {
                        PomkotsDatapadItem.clearReturnLocation(this.dataPadStack);
                        mech.m_264318_(
                           targetLevel,
                           location.pos().m_123341_() + 0.5,
                           location.pos().m_123342_(),
                           location.pos().m_123343_() + 0.5,
                           Set.of(),
                           player.m_146908_(),
                           player.m_146909_()
                        );
                     }
                  }
               } else {
                  serverPlayer.m_213846_(Component.m_237113_("Ride a mech before teleport"));
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

      public boolean m_6659_() {
         return this.active;
      }

      public void setActive(boolean active) {
         this.active = active;
      }
   }
}
