package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class ArenaFighterData {
   private UUID fighterId = UUID.randomUUID();
   private ArenaFighterData.FighterType type = ArenaFighterData.FighterType.NPC;
   private String displayName = "";
   private int rating = 0;
   private int wins = 0;
   private int losses = 0;
   private int draws = 0;
   private long lastBattleTime = 0L;
   private String comment = "";
   private String aiLevel = "";
   private ResourceLocation texture = PomkotsMechs.id("textures/entity/pilot/mech_pilot_wide.png");
   private ResourceLocation model = PomkotsMechs.id("geo/mech_pilot_wide.geo.json");
   private ArenaRank rank = ArenaRank.E;
   private ArenaMechData mechData;

   public String getDisplayName() {
      return this.displayName;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public int getDraws() {
      return this.draws;
   }

   public void setDraws(int draws) {
      this.draws = draws;
   }

   public UUID getFighterId() {
      return this.fighterId;
   }

   public void setFighterId(UUID fighterId) {
      this.fighterId = fighterId;
   }

   public long getLastBattleTime() {
      return this.lastBattleTime;
   }

   public void setLastBattleTime(long lastBattleTime) {
      this.lastBattleTime = lastBattleTime;
   }

   public int getLosses() {
      return this.losses;
   }

   public void setLosses(int losses) {
      this.losses = losses;
   }

   public ArenaMechData getMechData() {
      return this.mechData;
   }

   public void setMechData(ArenaMechData mechData) {
      this.mechData = mechData;
   }

   public ArenaRank getRank() {
      return this.rank;
   }

   public void setRank(ArenaRank rank) {
      this.rank = rank;
   }

   public int getRating() {
      return this.rating;
   }

   public void setRating(int rating) {
      this.rating = rating;
   }

   public ArenaFighterData.FighterType getType() {
      return this.type;
   }

   public void setType(ArenaFighterData.FighterType type) {
      this.type = type;
   }

   public int getWins() {
      return this.wins;
   }

   public void setWins(int wins) {
      this.wins = wins;
   }

   public String getComment() {
      return this.comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public String getAiLevel() {
      return this.aiLevel;
   }

   public void setAiLevel(String aiLevel) {
      this.aiLevel = aiLevel;
   }

   public ResourceLocation getModel() {
      return this.model;
   }

   public void setModel(ResourceLocation model) {
      this.model = model;
   }

   public ResourceLocation getTexture() {
      return this.texture;
   }

   public void setTexture(ResourceLocation texture) {
      this.texture = texture;
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.m_128362_("FighterId", this.fighterId);
      tag.m_128359_("Name", this.displayName);
      tag.m_128359_("Comment", this.comment);
      tag.m_128405_("Rating", this.rating);
      if (this.type == ArenaFighterData.FighterType.PLAYER) {
         tag.m_128405_("Type", 0);
      } else {
         tag.m_128405_("Type", 1);
      }

      tag.m_128405_("Wins", this.wins);
      tag.m_128405_("Losses", this.losses);
      tag.m_128405_("Draws", this.draws);
      tag.m_128359_("AiLevel", this.aiLevel);
      tag.m_128359_("Texture", this.texture.toString());
      tag.m_128359_("Model", this.model.toString());
      tag.m_128365_("Mech", this.mechData.save());
      return tag;
   }

   public static ArenaFighterData load(CompoundTag tag) {
      ArenaFighterData data = new ArenaFighterData();
      data.fighterId = tag.m_128342_("FighterId");
      data.displayName = tag.m_128461_("Name");
      data.comment = tag.m_128461_("Comment");
      data.rating = tag.m_128451_("Rating");
      data.wins = tag.m_128451_("Wins");
      data.losses = tag.m_128451_("Losses");
      data.draws = tag.m_128451_("Draws");
      int type = tag.m_128451_("Type");
      if (type == 0) {
         data.type = ArenaFighterData.FighterType.PLAYER;
      } else {
         data.type = ArenaFighterData.FighterType.NPC;
      }

      data.aiLevel = tag.m_128461_("AiLevel");
      data.texture = new ResourceLocation(tag.m_128461_("Texture"));
      data.model = new ResourceLocation(tag.m_128461_("Model"));
      data.mechData = ArenaMechData.load(tag.m_128469_("Mech"));
      return data;
   }

   public enum FighterType {
      PLAYER,
      NPC;
   }
}
