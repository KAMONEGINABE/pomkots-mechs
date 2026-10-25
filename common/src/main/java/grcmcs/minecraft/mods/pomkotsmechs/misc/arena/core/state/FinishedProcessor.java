package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaMatchResultData;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaFighterData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaRank;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaSavedData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util.ArenaUtil;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.server.level.ServerPlayer;

public class FinishedProcessor extends AbstractArenaMatchStateProcessor {
   @Override
   public void onEnter(ArenaMatchContext context) {
      if (context.runtime().getBossBar() != null) {
         context.runtime().getBossBar().m_7706_();
      }

      ArenaFighterData challengerData = ArenaUtil.getChallengerData(context);
      ArenaFighterData opponentData = ArenaUtil.getOpponentData(context);
      ArenaMatchResultData.Entry result = ArenaMatchResultData.createBattleResult(challengerData, opponentData, context.runtime().getResult());
      if (context.getChallengerEntity() instanceof ServerPlayer sp1) {
         ArenaUtil.openResultScreen(sp1, result);
         this.addAdvancement(ArenaRank.getRank(result.getChallengerResult().getRankAfter()), sp1);
         if (context.getChallengerMechEntity() instanceof Pmvc01Entity mech1) {
            ArenaUtil.supplyMechConsumables(mech1);
         }
      }

      if (context.getOpponentEntity() instanceof ServerPlayer sp2) {
         ArenaUtil.openResultScreen(sp2, result);
         this.addAdvancement(ArenaRank.getRank(result.getOpponentResult().getRankAfter()), sp2);
         if (context.getOpponentMechEntity() instanceof Pmvc01Entity mech2) {
            ArenaUtil.supplyMechConsumables(mech2);
         }
      }

      this.saveResultToFighter(challengerData, result.getChallengerResult());
      this.saveResultToFighter(opponentData, result.getOpponentResult());
      context.arena().setActiveMatch(null);
      ArenaSavedData.get(context.server()).m_77762_();
   }

   private void addAdvancement(ArenaRank rank, ServerPlayer sp) {
      switch (rank) {
         case D:
         case E:
            Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/arena_rank_d"), sp);
            break;
         case C:
            Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/arena_rank_c"), sp);
            break;
         case B:
            Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/arena_rank_b"), sp);
            break;
         case A:
            Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/arena_rank_a"), sp);
            break;
         case S:
            Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/arena_rank_s"), sp);
      }
   }

   private void saveResultToFighter(ArenaFighterData fighter, ArenaMatchResultData.FighterResult result) {
      fighter.setDraws(result.getDrawsAfter());
      fighter.setWins(result.getWinsAfter());
      fighter.setLosses(result.getLossesAfter());
      fighter.setRating(result.getRatingAfter());
      fighter.setRank(ArenaRank.getRank(result.getRankAfter()));
   }

   @Override
   public void tick(ArenaMatchContext context) {
   }

   @Override
   public void onExit(ArenaMatchContext context) {
   }

   @Override
   public void onCancel(ArenaMatchContext context) {
   }
}
