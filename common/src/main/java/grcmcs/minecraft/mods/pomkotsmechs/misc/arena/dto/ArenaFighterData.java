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
      tag.putUUID("FighterId", this.fighterId);
      tag.putString("Name", this.displayName);
      tag.putString("Comment", this.comment);
      tag.putInt("Rating", this.rating);
      if (this.type == ArenaFighterData.FighterType.PLAYER) {
         tag.putInt("Type", 0);
      } else {
         tag.putInt("Type", 1);
      }

      tag.putInt("Wins", this.wins);
      tag.putInt("Losses", this.losses);
      tag.putInt("Draws", this.draws);
      tag.putString("AiLevel", this.aiLevel);
      tag.putString("Texture", this.texture.toString());
      tag.putString("Model", this.model.toString());
      tag.put("Mech", this.mechData.save());
      return tag;
   }

   public static ArenaFighterData load(CompoundTag tag) {
      ArenaFighterData data = new ArenaFighterData();
      data.fighterId = tag.getUUID("FighterId");
      data.displayName = tag.getString("Name");
      data.comment = tag.getString("Comment");
      data.rating = tag.getInt("Rating");
      data.wins = tag.getInt("Wins");
      data.losses = tag.getInt("Losses");
      data.draws = tag.getInt("Draws");
      int type = tag.getInt("Type");
      if (type == 0) {
         data.type = ArenaFighterData.FighterType.PLAYER;
      } else {
         data.type = ArenaFighterData.FighterType.NPC;
      }

      data.aiLevel = tag.getString("AiLevel");
      data.texture = new ResourceLocation(tag.getString("Texture"));
      data.model = new ResourceLocation(tag.getString("Model"));
      data.mechData = ArenaMechData.load(tag.getCompound("Mech"));
      return data;
   }

   public enum FighterType {
      PLAYER,
      NPC;
   }
}
