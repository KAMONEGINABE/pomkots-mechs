package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

public enum ArenaMatchState {
   WAIT_FOR_ENTRY("WAIT_FOR_ENTRY"),
   TRANSPORT_TO_ARENA("TRANSPORT_TO_ARENA"),
   OPENING("OPENING"),
   BATTLE("BATTLE"),
   VICTORY_CEREMONY("VICTORY_CEREMONY"),
   RETURNING("RETURNING"),
   FINISHED("FINISHED"),
   CANCELLED("CANCELLED");

   private final String id;

   ArenaMatchState(String id) {
      this.id = id;
   }

   public String getId() {
      return this.id;
   }

   public static ArenaMatchState byId(String id) {
      for (ArenaMatchState state : values()) {
         if (state.id.equals(id)) {
            return state;
         }
      }

      return CANCELLED;
   }
}
