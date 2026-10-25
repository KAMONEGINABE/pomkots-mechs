package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import java.util.UUID;

public class ArenaRankingEntry {
   private UUID fighterId;
   private String displayName;
   private int rating;
   private int wins;
   private int losses;
   private int type;
   private String comment;
   private String mechName;
   private boolean isOnline;

   public String getDisplayName() {
      return this.displayName;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public UUID getFighterId() {
      return this.fighterId;
   }

   public void setFighterId(UUID fighterId) {
      this.fighterId = fighterId;
   }

   public int getLosses() {
      return this.losses;
   }

   public void setLosses(int losses) {
      this.losses = losses;
   }

   public int getRating() {
      return this.rating;
   }

   public void setRating(int rating) {
      this.rating = rating;
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

   public String getMechName() {
      return this.mechName;
   }

   public void setMechName(String mechName) {
      this.mechName = mechName;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public boolean isOnline() {
      return this.isOnline;
   }

   public void setOnline(boolean online) {
      this.isOnline = online;
   }
}
