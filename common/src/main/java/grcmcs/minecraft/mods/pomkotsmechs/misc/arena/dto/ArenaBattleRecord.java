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
      tag.m_128362_("BattleId", this.battleId);
      tag.m_128362_("FighterA", this.fighterA);
      tag.m_128362_("FighterB", this.fighterB);
      tag.m_128362_("Winner", this.winner);
      tag.m_128356_("BattleTime", this.battleTime);
      return tag;
   }

   public static ArenaBattleRecord load(CompoundTag tag) {
      ArenaBattleRecord data = new ArenaBattleRecord();
      data.battleId = tag.m_128342_("BattleId");
      data.fighterA = tag.m_128342_("FighterA");
      data.fighterB = tag.m_128342_("FighterB");
      data.winner = tag.m_128342_("Winner");
      data.battleTime = tag.m_128454_("BattleTime");
      return data;
   }
}
