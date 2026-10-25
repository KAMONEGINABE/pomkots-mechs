package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitRewardRoller;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaRank;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ArenaBattleResultMenu extends AbstractContainerMenu {
   private final SimpleContainer rewardsContainer = new SimpleContainer(3);
   private ArenaMatchResultData.FighterResult challengerResult;
   private ArenaMatchResultData.FighterResult opponentResult;

   public ArenaBattleResultMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
      this(containerId, inventory, null, ArenaMatchResultData.read(buf));
   }

   public ArenaBattleResultMenu(int containerId, Inventory inventory, Player player, ArenaMatchResultData.Entry entry) {
      super((MenuType)PomkotsMechs.ARENA_RESULT_MENU.get(), containerId);
      if (entry != null) {
         this.challengerResult = entry.getChallengerResult();
         this.opponentResult = entry.getOpponentResult();
      }

      this.addSlots(inventory);
      if (player instanceof ServerPlayer) {
         ArenaMatchResultData.FighterResult selfResult = null;
         ArenaMatchResultData.FighterResult targetResult = null;
         UUID cId = this.challengerResult.getFighterId();
         UUID oId = this.opponentResult.getFighterId();
         if (cId != null && player.m_20148_().equals(cId)) {
            selfResult = this.challengerResult;
            targetResult = this.opponentResult;
         } else if (oId != null && player.m_20148_().equals(oId)) {
            selfResult = this.opponentResult;
            targetResult = this.challengerResult;
         }

         if (selfResult == null || targetResult == null) {
            return;
         }

         int baseReward = selfResult.getRatingAfter() - selfResult.getRatingBefore();
         baseReward = Mth.m_14045_(baseReward, 1, 64);
         int rankReward = this.getRankReward(ArenaRank.getRank(selfResult.getRankAfter()));
         ((Slot)this.f_38839_.get(0)).m_5852_(new ItemStack((ItemLike)PomkotsMechs.POM_COIN.get(), baseReward));
         ((Slot)this.f_38839_.get(1)).m_5852_(new ItemStack((ItemLike)PomkotsMechs.POM_COIN.get(), rankReward));

         CircuitRewardRoller.WeightedEntry<CircuitRarity>[] cRarity = switch (ArenaRank.getRank(targetResult.getRankBefore())) {
            case E, D, C -> CircuitRewardRoller.RARITY_WEIGHTS_ARENA_C_D;
            case B, A -> CircuitRewardRoller.RARITY_WEIGHTS_ARENA_A_B;
            case S, SS -> CircuitRewardRoller.RARITY_WEIGHTS_ARENA_SS_S;
         };
         if (selfResult.getWinsAfter() > selfResult.getWinsBefore()) {
            ItemStack circuit = CircuitRewardRoller.roll(CircuitRewardRoller.PREFIX_WEIGHTS_ARENA, cRarity, 1.0F, player.m_217043_());
            if (!circuit.m_41619_()) {
               ((Slot)this.f_38839_.get(2)).m_5852_(circuit);
            }
         }
      }
   }

   private int getRankReward(ArenaRank rank) {
      return switch (rank) {
         case E -> 0;
         case D -> 0;
         case C -> 4;
         case B -> 8;
         case A -> 16;
         case S -> 24;
         case SS -> 32;
      };
   }

   private void addSlots(Inventory playerInventory) {
      this.addRewardSlots();
      this.addPlayerInventory(playerInventory);
   }

   private void addRewardSlots() {
      int offsetX = 73;
      int offsetY = 180;

      for (int i = 0; i < this.rewardsContainer.f_19147_.size(); i++) {
         this.m_38897_(new Slot(this.rewardsContainer, i, offsetX, offsetY + i * 18));
      }
   }

   private void addPlayerInventory(Inventory inventory) {
      int offsetX = 270;
      int offsetY = 176;

      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 9; col++) {
            this.m_38897_(new Slot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18));
         }
      }

      for (int i = 0; i < 9; i++) {
         this.m_38897_(new Slot(inventory, i, offsetX + i * 18, offsetY + 54 + 4));
      }
   }

   public ItemStack m_7648_(Player player, int i) {
      return ItemStack.f_41583_;
   }

   public boolean m_6875_(Player player) {
      return true;
   }

   public ArenaMatchResultData.FighterResult getChallengerResult() {
      return this.challengerResult;
   }

   public ArenaMatchResultData.FighterResult getOpponentResult() {
      return this.opponentResult;
   }
}
