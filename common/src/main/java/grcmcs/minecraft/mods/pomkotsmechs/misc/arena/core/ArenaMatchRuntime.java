package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.Entity;

public class ArenaMatchRuntime {
   private final UUID matchId;
   private ServerBossEvent bossBar;
   private String cancelReason;
   private Entity challengerEntity;
   private Entity challengerMech;
   private Entity opponentEntity;
   private Entity opponentMech;
   private Map<String, Object> generalObjects = new HashMap<>();
   private ArenaMatchRuntime.BattleResult result;
   private final Set<String> executedEvents = new HashSet<>();

   public ArenaMatchRuntime(UUID matchId) {
      this.matchId = matchId;
   }

   public UUID getMatchId() {
      return this.matchId;
   }

   public ServerBossEvent getBossBar() {
      return this.bossBar;
   }

   public void setBossBar(ServerBossEvent bossBar) {
      this.bossBar = bossBar;
   }

   public String getCancelReason() {
      return this.cancelReason;
   }

   public void setCancelReason(String cancelReason) {
      this.cancelReason = cancelReason;
   }

   public Entity getChallengerEntity() {
      return this.challengerEntity;
   }

   public void setChallengerEntity(Entity challengerEntity) {
      this.challengerEntity = challengerEntity;
   }

   public Entity getChallengerMech() {
      return this.challengerMech;
   }

   public void setChallengerMech(Entity challengerMech) {
      this.challengerMech = challengerMech;
   }

   public Entity getOpponentEntity() {
      return this.opponentEntity;
   }

   public void setOpponentEntity(Entity opponentEntity) {
      this.opponentEntity = opponentEntity;
   }

   public Entity getOpponentMech() {
      return this.opponentMech;
   }

   public void setOpponentMech(Entity opponentMech) {
      this.opponentMech = opponentMech;
   }

   public ArenaMatchRuntime.BattleResult getResult() {
      return this.result;
   }

   public void setResult(ArenaMatchRuntime.BattleResult result) {
      this.result = result;
   }

   public boolean runOnce(String key) {
      return this.executedEvents.add(key);
   }

   public void resetOnceFlag() {
      this.executedEvents.clear();
   }

   public void put(String key, Object value) {
      this.generalObjects.put(key, value);
   }

   public Object get(String key) {
      return this.generalObjects.get(key);
   }

   public Object remove(String key) {
      return this.generalObjects.remove(key);
   }

   public void clear() {
      this.generalObjects.clear();
   }

   public static class BattleResult {
      private ArenaMatchRuntime.BattleResult.ResultType resultType;
      private UUID winnerId;
      private UUID loserId;
      private String reason;

      public UUID getLoserId() {
         return this.loserId;
      }

      public void setLoserId(UUID loserId) {
         this.loserId = loserId;
      }

      public String getReason() {
         return this.reason;
      }

      public void setReason(String reason) {
         this.reason = reason;
      }

      public ArenaMatchRuntime.BattleResult.ResultType getResultType() {
         return this.resultType;
      }

      public void setResultType(ArenaMatchRuntime.BattleResult.ResultType resultType) {
         this.resultType = resultType;
      }

      public UUID getWinnerId() {
         return this.winnerId;
      }

      public void setWinnerId(UUID winnerId) {
         this.winnerId = winnerId;
      }

      public enum ResultType {
         CHALLENGER_WIN,
         OPPONENT_WIN,
         DRAW;
      }
   }
}
