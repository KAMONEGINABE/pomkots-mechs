package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

public enum ArenaRank {
   E("E", 8421504),
   D("D", 5635925),
   C("C", 5636095),
   B("B", 5592575),
   A("A", 16733695),
   S("S", 16755200),
   SS("SS", 16777045);

   private final String displayName;
   private final int color;

   ArenaRank(String displayName, int color) {
      this.displayName = displayName;
      this.color = color;
   }

   public String getName() {
      return this.displayName;
   }

   public int getColor() {
      return this.color;
   }

   public static final ArenaRank getRank(int rating) {
      if (rating >= 1400) {
         return SS;
      } else if (rating >= 1200) {
         return S;
      } else if (rating >= 900) {
         return A;
      } else if (rating >= 600) {
         return B;
      } else {
         return rating >= 300 ? C : D;
      }
   }

   public static ArenaRank getRank(String name) {
      if ("SS".equals(name)) {
         return SS;
      } else if ("S".equals(name)) {
         return S;
      } else if ("A".equals(name)) {
         return A;
      } else if ("B".equals(name)) {
         return B;
      } else {
         return "C".equals(name) ? C : D;
      }
   }
}
