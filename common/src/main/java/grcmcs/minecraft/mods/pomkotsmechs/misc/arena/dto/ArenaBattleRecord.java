package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public class ArenaBattleRecord {
   private UUID battleId;
   private UUID fighterA;
   private UUID fighterB;
   private UUID winner;
   private long battleTime;

   public UUID getBattleId() {
      return this.battleId;
   }

   public void setBattleId(UUID battleId) {
      this.battleId = battleId;
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.putUUID("BattleId", this.battleId);
      tag.putUUID("FighterA", this.fighterA);
      tag.putUUID("FighterB", this.fighterB);
      tag.putUUID("Winner", this.winner);
      tag.putLong("BattleTime", this.battleTime);
      return tag;
   }

   public static ArenaBattleRecord load(CompoundTag tag) {
      ArenaBattleRecord data = new ArenaBattleRecord();
      data.battleId = tag.getUUID("BattleId");
      data.fighterA = tag.getUUID("FighterA");
      data.fighterB = tag.getUUID("FighterB");
      data.winner = tag.getUUID("Winner");
      data.battleTime = tag.getLong("BattleTime");
      return data;
   }
}
