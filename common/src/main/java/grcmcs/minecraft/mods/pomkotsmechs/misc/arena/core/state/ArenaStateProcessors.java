package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import java.util.EnumMap;
import java.util.Map;

public final class ArenaStateProcessors {
   private static final Map<ArenaMatchState, IArenaMatchStateProcessor> PROCESSORS = new EnumMap<>(ArenaMatchState.class);

   public static IArenaMatchStateProcessor get(ArenaMatchState state) {
      return PROCESSORS.get(state);
   }

   static {
      PROCESSORS.put(ArenaMatchState.WAIT_FOR_ENTRY, new WaitForEntryProcessor());
      PROCESSORS.put(ArenaMatchState.TRANSPORT_TO_ARENA, new TransportToArenaProcessor());
      PROCESSORS.put(ArenaMatchState.OPENING, new OpeningProcessor());
      PROCESSORS.put(ArenaMatchState.BATTLE, new BattleProcessor());
      PROCESSORS.put(ArenaMatchState.VICTORY_CEREMONY, new VictoryCeremonyProcessor());
      PROCESSORS.put(ArenaMatchState.RETURNING, new ReturningProcessor());
      PROCESSORS.put(ArenaMatchState.FINISHED, new FinishedProcessor());
      PROCESSORS.put(ArenaMatchState.CANCELLED, new CancelledProcessor());
   }
}
