package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ArenaMenuData {
   public static void write(FriendlyByteBuf buf, ArenaMenuData.Entry entry) {
      buf.m_130070_(entry.arenaId);
      buf.writeInt(entry.mode);
      buf.m_130055_(Objects.requireNonNullElse(entry.mechDataPadStack, ItemStack.f_41583_));
      writeRankings(buf, entry.rankings);
   }

   private static void writeRankings(FriendlyByteBuf buf, List<ArenaRankingEntry> rankings) {
      buf.writeInt(rankings.size());

      for (ArenaRankingEntry entry : rankings) {
         buf.m_130077_(entry.getFighterId());
         buf.m_130070_(entry.getDisplayName());
         buf.writeInt(entry.getRating());
         buf.m_130070_(entry.getComment());
         buf.m_130070_(entry.getMechName());
         buf.writeInt(entry.getWins());
         buf.writeInt(entry.getLosses());
         buf.writeInt(entry.getType());
         buf.writeBoolean(entry.isOnline());
      }
   }

   public static ArenaMenuData.Entry read(FriendlyByteBuf buf) {
      String arenaId = buf.m_130277_();
      int mode = buf.readInt();
      ItemStack mechDataPackStack = buf.m_130267_();
      int size = buf.readInt();
      List<ArenaRankingEntry> result = new ArrayList<>();

      for (int i = 0; i < size; i++) {
         ArenaRankingEntry entry = new ArenaRankingEntry();
         entry.setFighterId(buf.m_130259_());
         entry.setDisplayName(buf.m_130277_());
         entry.setRating(buf.readInt());
         entry.setComment(buf.m_130277_());
         entry.setMechName(buf.m_130277_());
         entry.setWins(buf.readInt());
         entry.setLosses(buf.readInt());
         entry.setType(buf.readInt());
         entry.setOnline(buf.readBoolean());
         result.add(entry);
      }

      return new ArenaMenuData.Entry(arenaId, mode, mechDataPackStack, result);
   }

   public static class Entry {
      private String arenaId;
      private List<ArenaRankingEntry> rankings;
      private int mode;
      private ItemStack mechDataPadStack;

      public Entry(String arenaId, int mode, ItemStack mechDataPadStack, List<ArenaRankingEntry> rankings) {
         this.arenaId = arenaId;
         this.mode = mode;
         this.mechDataPadStack = mechDataPadStack;
         this.rankings = rankings;
      }

      public String getArenaId() {
         return this.arenaId;
      }

      public ItemStack getMechDataPadStack() {
         return this.mechDataPadStack;
      }

      public int getMode() {
         return this.mode;
      }

      public List<ArenaRankingEntry> getRankings() {
         return this.rankings;
      }
   }
}
