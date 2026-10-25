package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state.ArenaMatchState;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public class ArenaMatchData {
   private UUID matchId;
   private UUID challengerId;
   private UUID opponentId;
   private ArenaMatchState state;
   private long createdTime;
   private long stateChangedTime;
   private BlockPos gatePosA;
   private BlockPos gatePosB;
   private BlockPos battleFieldAnchorPos;
   private BlockPos teleportPointPos;
   private UUID challengerEntityId;
   private UUID challengerMechId;
   private UUID opponentEntityId;
   private UUID opponentMechId;

   public UUID getMatchId() {
      return this.matchId;
   }

   public void setMatchId(UUID matchId) {
      this.matchId = matchId;
   }

   public UUID getChallengerId() {
      return this.challengerId;
   }

   public void setChallengerId(UUID challengerId) {
      this.challengerId = challengerId;
   }

   public UUID getOpponentId() {
      return this.opponentId;
   }

   public void setOpponentId(UUID opponentId) {
      this.opponentId = opponentId;
   }

   public ArenaMatchState getState() {
      return this.state;
   }

   public void setState(ArenaMatchState state) {
      this.state = state;
   }

   public long getCreatedTime() {
      return this.createdTime;
   }

   public void setCreatedTime(long createdTime) {
      this.createdTime = createdTime;
   }

   public long getStateChangedTime() {
      return this.stateChangedTime;
   }

   public void setStateChangedTime(long stateChangedTime) {
      this.stateChangedTime = stateChangedTime;
   }

   public BlockPos getGatePosA() {
      return this.gatePosA;
   }

   public void setGatePosA(BlockPos gatePosA) {
      this.gatePosA = gatePosA;
   }

   public BlockPos getGatePosB() {
      return this.gatePosB;
   }

   public void setGatePosB(BlockPos gatePosB) {
      this.gatePosB = gatePosB;
   }

   public BlockPos getBattleFieldAnchorPos() {
      return this.battleFieldAnchorPos;
   }

   public void setBattleFieldAnchorPos(BlockPos battleFieldAnchorPos) {
      this.battleFieldAnchorPos = battleFieldAnchorPos;
   }

   public BlockPos getTeleportPointPos() {
      return this.teleportPointPos;
   }

   public void setTeleportPointPos(BlockPos teleportPointPos) {
      this.teleportPointPos = teleportPointPos;
   }

   public UUID getChallengerEntityId() {
      return this.challengerEntityId;
   }

   public void setChallengerEntityId(UUID challengerEntityId) {
      this.challengerEntityId = challengerEntityId;
   }

   public UUID getChallengerMechId() {
      return this.challengerMechId;
   }

   public void setChallengerMechId(UUID challengerMechId) {
      this.challengerMechId = challengerMechId;
   }

   public UUID getOpponentEntityId() {
      return this.opponentEntityId;
   }

   public void setOpponentEntityId(UUID opponentEntityId) {
      this.opponentEntityId = opponentEntityId;
   }

   public UUID getOpponentMechId() {
      return this.opponentMechId;
   }

   public void setOpponentMechId(UUID opponentMechId) {
      this.opponentMechId = opponentMechId;
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("MatchId", this.matchId);
      tag.putUUID("ChallengerId", this.challengerId);
      tag.putUUID("OpponentId", this.opponentId);
      tag.putString("State", this.state.name());
      tag.putLong("CreatedTime", this.createdTime);
      tag.putLong("StateChangedTime", this.stateChangedTime);
      tag.putInt("GateAX", this.gatePosA.getX());
      tag.putInt("GateAY", this.gatePosA.getY());
      tag.putInt("GateAZ", this.gatePosA.getZ());
      tag.putInt("GateBX", this.gatePosB.getX());
      tag.putInt("GateBY", this.gatePosB.getY());
      tag.putInt("GateBZ", this.gatePosB.getZ());
      tag.putInt("BFieldX", this.battleFieldAnchorPos.getX());
      tag.putInt("BFieldY", this.battleFieldAnchorPos.getY());
      tag.putInt("BFieldZ", this.battleFieldAnchorPos.getZ());
      tag.putInt("TelepoX", this.teleportPointPos.getX());
      tag.putInt("TelepoY", this.teleportPointPos.getY());
      tag.putInt("TelepoZ", this.teleportPointPos.getZ());
      saveEntityUUID("ChallengerEntityId", this.challengerEntityId, tag);
      saveEntityUUID("ChallengerMechId", this.challengerMechId, tag);
      saveEntityUUID("OpponentEntityId", this.opponentEntityId, tag);
      saveEntityUUID("OpponentMechId", this.opponentMechId, tag);
      return tag;
   }

   private static void saveEntityUUID(String name, UUID id, CompoundTag tag) {
      if (id != null) {
         tag.putUUID(name, id);
      }
   }

   public static ArenaMatchData load(CompoundTag tag) {
      ArenaMatchData data = new ArenaMatchData();
      data.matchId = tag.getUUID("MatchId");
      data.challengerId = tag.getUUID("ChallengerId");
      data.opponentId = tag.getUUID("OpponentId");
      data.state = ArenaMatchState.byId(tag.getString("State"));
      data.createdTime = tag.getLong("CreatedTime");
      data.stateChangedTime = tag.getLong("StateChangedTime");
      data.gatePosA = new BlockPos(tag.getInt("GateAX"), tag.getInt("GateAY"), tag.getInt("GateAZ"));
      data.gatePosB = new BlockPos(tag.getInt("GateBX"), tag.getInt("GateBY"), tag.getInt("GateBZ"));
      data.battleFieldAnchorPos = new BlockPos(tag.getInt("BFieldX"), tag.getInt("BFieldY"), tag.getInt("BFieldZ"));
      data.teleportPointPos = new BlockPos(tag.getInt("TelepoX"), tag.getInt("TelepoY"), tag.getInt("TelepoZ"));
      if (tag.hasUUID("ChallengerEntityId")) {
         data.challengerEntityId = tag.getUUID("ChallengerEntityId");
      }

      if (tag.hasUUID("ChallengerMechId")) {
         data.challengerEntityId = tag.getUUID("ChallengerMechId");
      }

      if (tag.hasUUID("OpponentEntityId")) {
         data.challengerEntityId = tag.getUUID("OpponentEntityId");
      }

      if (tag.hasUUID("OpponentMechId")) {
         data.challengerEntityId = tag.getUUID("OpponentMechId");
      }

      return data;
   }
}
