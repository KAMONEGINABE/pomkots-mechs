package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchRuntime;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaFighterData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaRank;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public class ArenaMatchResultData {
   public static void write(FriendlyByteBuf buf, ArenaMatchResultData.Entry entry) {
      writeFighterResult(buf, entry.getChallengerResult());
      writeFighterResult(buf, entry.getOpponentResult());
   }

   public static ArenaMatchResultData.Entry read(FriendlyByteBuf buf) {
      ArenaMatchResultData.FighterResult self = readFighterResult(buf);
      ArenaMatchResultData.FighterResult target = readFighterResult(buf);
      return new ArenaMatchResultData.Entry(self, target);
   }

   private static void writeFighterResult(FriendlyByteBuf buf, ArenaMatchResultData.FighterResult result) {
      buf.m_130077_(result.getFighterId());
      buf.m_130070_(result.getDisplayName());
      buf.writeInt(result.getRatingBefore());
      buf.writeInt(result.getRatingAfter());
      buf.writeInt(result.getWinsBefore());
      buf.writeInt(result.getWinsAfter());
      buf.writeInt(result.getLossesBefore());
      buf.writeInt(result.getLossesAfter());
      buf.writeInt(result.getDrawsBefore());
      buf.writeInt(result.getDrawsAfter());
      buf.m_130070_(result.getRankBefore());
      buf.m_130070_(result.getRankAfter());
      buf.writeInt(result.getResultType());
   }

   private static ArenaMatchResultData.FighterResult readFighterResult(FriendlyByteBuf buf) {
      ArenaMatchResultData.FighterResult result = new ArenaMatchResultData.FighterResult();
      result.setFighterId(buf.m_130259_());
      result.setDisplayName(buf.m_130277_());
      result.setRatingBefore(buf.readInt());
      result.setRatingAfter(buf.readInt());
      result.setWinsBefore(buf.readInt());
      result.setWinsAfter(buf.readInt());
      result.setLossesBefore(buf.readInt());
      result.setLossesAfter(buf.readInt());
      result.setDrawsBefore(buf.readInt());
      result.setDrawsAfter(buf.readInt());
      result.setRankBefore(buf.m_130277_());
      result.setRankAfter(buf.m_130277_());
      result.setResultType(buf.readInt());
      return result;
   }

   public static ArenaMatchResultData.Entry createBattleResult(ArenaFighterData challenger, ArenaFighterData opponent, ArenaMatchRuntime.BattleResult result) {
      int challengerRatingBefore = challenger.getRating();
      int opponentRatingBefore = opponent.getRating();
      int challengerWinsAfter = challenger.getWins();
      int challengerLossesAfter = challenger.getLosses();
      int challengerDrawsAfter = challenger.getDraws();
      int opponentWinsAfter = opponent.getWins();
      int opponentLossesAfter = opponent.getLosses();
      int opponentDrawsAfter = opponent.getDraws();
      double challengerScore;
      double opponentScore;
      switch (result.getResultType()) {
         case CHALLENGER_WIN:
            challengerScore = 1.0;
            opponentScore = 0.0;
            challengerWinsAfter++;
            opponentLossesAfter++;
            break;
         case OPPONENT_WIN:
            challengerScore = 0.0;
            opponentScore = 1.0;
            challengerLossesAfter++;
            opponentWinsAfter++;
            break;
         case DRAW:
            challengerScore = 0.5;
            opponentScore = 0.5;
            challengerDrawsAfter++;
            opponentDrawsAfter++;
            break;
         default:
            throw new IllegalStateException();
      }

      int challengerRatingAfter = calculateElo(challengerRatingBefore, opponentRatingBefore, challengerScore, getKFactor(challenger));
      int opponentRatingAfter = calculateElo(opponentRatingBefore, challengerRatingBefore, opponentScore, getKFactor(opponent));
      ArenaMatchResultData.FighterResult challengerResult = new ArenaMatchResultData.FighterResult();
      challengerResult.setFighterId(challenger.getFighterId());
      challengerResult.setDisplayName(challenger.getDisplayName());
      challengerResult.setRatingBefore(challengerRatingBefore);
      challengerResult.setRatingAfter(challengerRatingAfter);
      challengerResult.setWinsBefore(challenger.getWins());
      challengerResult.setWinsAfter(challengerWinsAfter);
      challengerResult.setLossesBefore(challenger.getLosses());
      challengerResult.setLossesAfter(challengerLossesAfter);
      challengerResult.setDrawsBefore(challenger.getDraws());
      challengerResult.setDrawsAfter(challengerDrawsAfter);
      challengerResult.setRankBefore(ArenaRank.getRank(challengerRatingBefore).name());
      challengerResult.setRankAfter(ArenaRank.getRank(challengerRatingAfter).name());
      challengerResult.setResultType(convertResult(result, challenger.getFighterId()));
      ArenaMatchResultData.FighterResult opponentResult = new ArenaMatchResultData.FighterResult();
      opponentResult.setFighterId(opponent.getFighterId());
      opponentResult.setDisplayName(opponent.getDisplayName());
      opponentResult.setRatingBefore(opponentRatingBefore);
      opponentResult.setRatingAfter(opponentRatingAfter);
      opponentResult.setWinsBefore(opponent.getWins());
      opponentResult.setWinsAfter(opponentWinsAfter);
      opponentResult.setLossesBefore(opponent.getLosses());
      opponentResult.setLossesAfter(opponentLossesAfter);
      opponentResult.setDrawsBefore(opponent.getDraws());
      opponentResult.setDrawsAfter(opponentDrawsAfter);
      opponentResult.setRankBefore(ArenaRank.getRank(opponentRatingBefore).name());
      opponentResult.setRankAfter(ArenaRank.getRank(opponentRatingAfter).name());
      opponentResult.setResultType(convertResult(result, opponent.getFighterId()));
      return new ArenaMatchResultData.Entry(challengerResult, opponentResult);
   }

   private static int calculateElo(int selfRating, int opponentRating, double score, int k) {
      double expected = 1.0 / (1.0 + Math.pow(10.0, (opponentRating - selfRating) / 400.0));
      int res = selfRating + (int)Math.round(k * (score - expected));
      return Math.max(res, 0);
   }

   private static int getKFactor(ArenaFighterData fighter) {
      int totalBattles = fighter.getWins() + fighter.getLosses() + fighter.getDraws();
      return totalBattles < 20 ? 64 : 32;
   }

   private static int convertResult(ArenaMatchRuntime.BattleResult result, UUID fighterId) {
      if (result.getResultType() == ArenaMatchRuntime.BattleResult.ResultType.DRAW) {
         return 0;
      } else {
         return fighterId.equals(result.getWinnerId()) ? 1 : 2;
      }
   }

   public static class Entry {
      private ArenaMatchResultData.FighterResult challengerResult;
      private ArenaMatchResultData.FighterResult opponentResult;

      public Entry(ArenaMatchResultData.FighterResult selfResult, ArenaMatchResultData.FighterResult targetResult) {
         this.challengerResult = selfResult;
         this.opponentResult = targetResult;
      }

      public ArenaMatchResultData.FighterResult getChallengerResult() {
         return this.challengerResult;
      }

      public void setChallengerResult(ArenaMatchResultData.FighterResult challengerResult) {
         this.challengerResult = challengerResult;
      }

      public ArenaMatchResultData.FighterResult getOpponentResult() {
         return this.opponentResult;
      }

      public void setOpponentResult(ArenaMatchResultData.FighterResult opponentResult) {
         this.opponentResult = opponentResult;
      }
   }

   public static class FighterResult {
      public static final int RESULT_DRAW = 0;
      public static final int RESULT_WIN = 1;
      public static final int RESULT_LOSE = 2;
      private UUID fighterId;
      private String displayName;
      private int resultType;
      private int ratingBefore;
      private int winsBefore;
      private int lossesBefore;
      private int drawsBefore;
      private String rankBefore;
      private int ratingAfter;
      private int winsAfter;
      private int lossesAfter;
      private int drawsAfter;
      private String rankAfter;

      public String getDisplayName() {
         return this.displayName;
      }

      public void setDisplayName(String displayName) {
         this.displayName = displayName;
      }

      public int getDrawsAfter() {
         return this.drawsAfter;
      }

      public void setDrawsAfter(int drawsAfter) {
         this.drawsAfter = drawsAfter;
      }

      public int getDrawsBefore() {
         return this.drawsBefore;
      }

      public void setDrawsBefore(int drawsBefore) {
         this.drawsBefore = drawsBefore;
      }

      public UUID getFighterId() {
         return this.fighterId;
      }

      public void setFighterId(UUID fighterId) {
         this.fighterId = fighterId;
      }

      public int getLossesAfter() {
         return this.lossesAfter;
      }

      public void setLossesAfter(int lossesAfter) {
         this.lossesAfter = lossesAfter;
      }

      public int getLossesBefore() {
         return this.lossesBefore;
      }

      public void setLossesBefore(int lossesBefore) {
         this.lossesBefore = lossesBefore;
      }

      public int getRatingAfter() {
         return this.ratingAfter;
      }

      public void setRatingAfter(int ratingAfter) {
         this.ratingAfter = ratingAfter;
      }

      public int getRatingBefore() {
         return this.ratingBefore;
      }

      public void setRatingBefore(int ratingBefore) {
         this.ratingBefore = ratingBefore;
      }

      public int getWinsAfter() {
         return this.winsAfter;
      }

      public void setWinsAfter(int winsAfter) {
         this.winsAfter = winsAfter;
      }

      public int getWinsBefore() {
         return this.winsBefore;
      }

      public void setWinsBefore(int winsBefore) {
         this.winsBefore = winsBefore;
      }

      public String getRankAfter() {
         return this.rankAfter;
      }

      public void setRankAfter(String rankAfter) {
         this.rankAfter = rankAfter;
      }

      public String getRankBefore() {
         return this.rankBefore;
      }

      public void setRankBefore(String rankBefore) {
         this.rankBefore = rankBefore;
      }

      public int getResultType() {
         return this.resultType;
      }

      public void setResultType(int resultType) {
         this.resultType = resultType;
      }
   }
}
