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
      tag.m_128362_("MatchId", this.matchId);
      tag.m_128362_("ChallengerId", this.challengerId);
      tag.m_128362_("OpponentId", this.opponentId);
      tag.m_128359_("State", this.state.name());
      tag.m_128356_("CreatedTime", this.createdTime);
      tag.m_128356_("StateChangedTime", this.stateChangedTime);
      tag.m_128405_("GateAX", this.gatePosA.m_123341_());
      tag.m_128405_("GateAY", this.gatePosA.m_123342_());
      tag.m_128405_("GateAZ", this.gatePosA.m_123343_());
      tag.m_128405_("GateBX", this.gatePosB.m_123341_());
      tag.m_128405_("GateBY", this.gatePosB.m_123342_());
      tag.m_128405_("GateBZ", this.gatePosB.m_123343_());
      tag.m_128405_("BFieldX", this.battleFieldAnchorPos.m_123341_());
      tag.m_128405_("BFieldY", this.battleFieldAnchorPos.m_123342_());
      tag.m_128405_("BFieldZ", this.battleFieldAnchorPos.m_123343_());
      tag.m_128405_("TelepoX", this.teleportPointPos.m_123341_());
      tag.m_128405_("TelepoY", this.teleportPointPos.m_123342_());
      tag.m_128405_("TelepoZ", this.teleportPointPos.m_123343_());
      saveEntityUUID("ChallengerEntityId", this.challengerEntityId, tag);
      saveEntityUUID("ChallengerMechId", this.challengerMechId, tag);
      saveEntityUUID("OpponentEntityId", this.opponentEntityId, tag);
      saveEntityUUID("OpponentMechId", this.opponentMechId, tag);
      return tag;
   }

   private static void saveEntityUUID(String name, UUID id, CompoundTag tag) {
      if (id != null) {
         tag.m_128362_(name, id);
      }
   }

   public static ArenaMatchData load(CompoundTag tag) {
      ArenaMatchData data = new ArenaMatchData();
      data.matchId = tag.m_128342_("MatchId");
      data.challengerId = tag.m_128342_("ChallengerId");
      data.opponentId = tag.m_128342_("OpponentId");
      data.state = ArenaMatchState.byId(tag.m_128461_("State"));
      data.createdTime = tag.m_128454_("CreatedTime");
      data.stateChangedTime = tag.m_128454_("StateChangedTime");
      data.gatePosA = new BlockPos(tag.m_128451_("GateAX"), tag.m_128451_("GateAY"), tag.m_128451_("GateAZ"));
      data.gatePosB = new BlockPos(tag.m_128451_("GateBX"), tag.m_128451_("GateBY"), tag.m_128451_("GateBZ"));
      data.battleFieldAnchorPos = new BlockPos(tag.m_128451_("BFieldX"), tag.m_128451_("BFieldY"), tag.m_128451_("BFieldZ"));
      data.teleportPointPos = new BlockPos(tag.m_128451_("TelepoX"), tag.m_128451_("TelepoY"), tag.m_128451_("TelepoZ"));
      if (tag.m_128403_("ChallengerEntityId")) {
         data.challengerEntityId = tag.m_128342_("ChallengerEntityId");
      }

      if (tag.m_128403_("ChallengerMechId")) {
         data.challengerEntityId = tag.m_128342_("ChallengerMechId");
      }

      if (tag.m_128403_("OpponentEntityId")) {
         data.challengerEntityId = tag.m_128342_("OpponentEntityId");
      }

      if (tag.m_128403_("OpponentMechId")) {
         data.challengerEntityId = tag.m_128342_("OpponentMechId");
      }

      return data;
   }
}
